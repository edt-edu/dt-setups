package dts.gateway.implementations.mqttGateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dts.dtengine.eventsystem.gateway.observer.AbstractGatewayObserver
import dts.dtsystem.datastructures.DataProbe
import dts.dtsystem.datastructures.Functionality
import dts.events.neweventsystem.gateway.events.GatewayNewDatapointEvent
import dts.gateway.AbstractGateway
import dts.gateway.implementations.mqttGateway.commands.MachineCommand
import dts.gateway.implementations.mqttGateway.model.Machine
import dts.gateway.implementations.mqttGateway.status.MachineStatus
import dts.gateway.influx.InfluxConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttException
import java.time.LocalDateTime
import java.util.*

class MqttGatewayAdapter(
    id: String,
    observers: MutableList<AbstractGatewayObserver> = mutableListOf(),
    properties: MutableSet<String> = mutableSetOf(),
    values: MutableMap<String, Any?> = mutableMapOf(),
    functions: MutableSet<Functionality> = mutableSetOf(),
    thread: Thread? = null,
    private val mqttConfigs: List<MqttConfig> = emptyList(),
    private val influxConnection: InfluxConnection? = null,
) : AbstractGateway(id, observers, properties, values, functions, thread) {

    private val mqttClients: MutableMap<String, MqttClientImplementation> = mutableMapOf()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}
    private val machines: MutableMap<String, Machine> = mutableMapOf()


    init {
        logger.info { "MqttGatewayAdapter initialised." }
        fillValuesField()
        setupMachines()
        setupMqttClients()
    }

    private fun setupMachines() {
        mqttConfigs.forEach { config ->
            config.machineNames.forEach { name ->
                machines.putIfAbsent(name, Machine.fromName(name))
            }
        }
    }

    private fun setupMqttClients() {
        mqttConfigs.forEach { config ->
            val client = MqttClientImplementation(config)
            mqttClients[config.configId] = client
            try {
                client.connect(config.toConnectOptions())
                subscribeToDefaultTopics(client, config)
            } catch (e: MqttException) {
                logger.error(e) { "Error setting up MQTT client '${config.clientId}' for broker '${config.brokerUrl}'" }
            }
        }
    }

    private fun subscribeToDefaultTopics(client: MqttClientImplementation, config: MqttConfig) {
        // Custom subscriptions from config
        config.topicsToSubscribe.forEach { (topic, qos) ->
            client.subscribe(topic, qos) { receivedTopic, message ->
                processMqttMessage(config.configId, receivedTopic, message)
            }
        }

        // Machine status topics
        config.machineNames.forEach { machineName ->
            val topic = MqttTopics.machineStatus(machineName)
            client.subscribe(topic, config.qosPublish) { t, m ->
                processMqttMessage(config.configId, t, m)
            }
        }


        // Island related topics
        config.islandName?.let { islandName ->
            client.subscribe(MqttTopics.islandStatus(islandName), config.qosPublish) { t, m ->
                processMqttMessage(config.configId, t, m)
            }
            client.subscribe(MqttTopics.islandErrors(islandName), config.qosPublish) { t, m ->
                processMqttMessage(config.configId, t, m)
            }
            config.machineNames.forEach { machineName ->
                val topic = MqttTopics.islandMachineLifecycle(islandName, machineName)
                client.subscribe(topic, config.qosPublish) { t, m ->
                    processMqttMessage(config.configId, t, m)
                }
            }
        }
    }

    private fun processMqttMessage(configId: String, topic: String, message: ByteArray) {
        val messagePayload = String(message)
        logger.info { "MQTT message received (config=$configId, topic=$topic): $messagePayload" }

        val parsedPayload: Any = try {
            objectMapper.readValue(messagePayload, MachineStatus::class.java)
        } catch (e: Exception) {
            messagePayload
        }

        values[topic] = parsedPayload

        if (parsedPayload is MachineStatus && topic.endsWith("/status")) {
            val machineName = topic.substringBefore("/status")
            val machine = machines.getOrPut(machineName) { Machine.fromName(machineName) }
            machine.status = parsedPayload
            logger.debug { "Machine '$machineName' status updated: $parsedPayload" }
        } else if (topic.contains("/machines/") && parsedPayload is String) {
            val machineName = topic.substringAfter("/machines/")
            val machine = machines.getOrPut(machineName) { Machine.fromName(machineName) }
            machine.status = MachineStatus(status = parsedPayload)
            logger.debug { "Machine '$machineName' lifecycle status: $parsedPayload" }
        }

        val event = GatewayNewDatapointEvent().apply {
            sourceID = configId
            timestamp = LocalDateTime.now()
            content = DataProbe(
                UUID.randomUUID().toString(),
                topic,
                configId,
                if (parsedPayload is MachineStatus) "JSON" else "String",
                "MQTT",
                LocalDateTime.now(),
            )
        }
        observers.forEach { it.handleEvent(event) }

        influxConnection?.let { influx ->
            val fields: Map<String, Any> = if (parsedPayload is MachineStatus) {
                @Suppress("UNCHECKED_CAST")
                val asMap = objectMapper.convertValue(parsedPayload, Map::class.java) as Map<String, Any?>
                asMap.filterValues { it != null }.mapValues { it.value!! }
            } else {
                mapOf("value" to messagePayload)
            }

            if (fields.isNotEmpty()) {
                influx.writeMeasurement(
                    measurement = "mqtt",
                    fields = fields,
                    tags = mapOf("topic" to topic, "gateway" to configId),
                )
            }
        }
    }

    fun sendMachineCommand(configId: String, machineName: String, command: MachineCommand) {
        val client = mqttClients[configId]
        val config = mqttConfigs.find { it.configId == configId }
        if (client == null || config == null) {
            logger.warn { "No MQTT client/config found for id '$configId'." }
            return
        }
        try {
            val topic = MqttTopics.machineControl(machineName)
            val payload = objectMapper.writeValueAsBytes(command)
            client.publish(topic, payload, config.qosPublish, config.isRetainedPublish)
        } catch (e: MqttException) {
            logger.error(e) { "Error sending command to machine '$machineName' (config=$configId)" }
        } catch (e: Exception) {
            logger.error(e) { "Error serialising command for machine '$machineName' (config=$configId)" }
        }
    }

    private fun publishLifecycle(configId: String, topic: String, payload: ByteArray = ByteArray(0)) {
        val client = mqttClients[configId]
        val config = mqttConfigs.find { it.configId == configId }
        if (client == null || config == null) {
            logger.warn { "No MQTT client/config found for id '$configId'." }
            return
        }
        try {
            client.publish(topic, payload, config.qosPublish, config.isRetainedPublish)
        } catch (e: MqttException) {
            logger.error(e) { "Error publishing lifecycle command to '$topic' (config=$configId)" }
        }
    }

    fun stopIsland(configId: String) {
        val island = mqttConfigs.find { it.configId == configId }?.islandName
        if (island == null) {
            logger.warn { "No island configured for id '$configId'." }
            return
        }
        publishLifecycle(configId, MqttTopics.islandStop(island))
    }

    fun queryAllMachines(configId: String) {
        val island = mqttConfigs.find { it.configId == configId }?.islandName
        if (island == null) {
            logger.warn { "No island configured for id '$configId'." }
            return
        }
        publishLifecycle(configId, MqttTopics.islandQueryAll(island))
    }

    fun createMachine(configId: String, machineName: String, machineConfigJson: String) {
        val island = mqttConfigs.find { it.configId == configId }?.islandName
        if (island == null) {
            logger.warn { "No island configured for id '$configId'." }
            return
        }
        publishLifecycle(configId, MqttTopics.islandCreate(island, machineName), machineConfigJson.toByteArray())
    }

    fun destroyMachine(configId: String, machineName: String) {
        val island = mqttConfigs.find { it.configId == configId }?.islandName
        if (island == null) {
            logger.warn { "No island configured for id '$configId'." }
            return
        }
        publishLifecycle(configId, MqttTopics.islandDestroy(island, machineName))
    }

    fun getMachine(name: String): Machine? = machines[name]
    fun getMachines(): Collection<Machine> = machines.values


    override fun findByTimestamp() {
        TODO("Not yet implemented")
    }

    override fun updateToGW() {
        TODO("Not yet implemented")
    }

    override fun readFromGW() {
        TODO("Not yet implemented")
    }

    override fun start() {
        thread?.start()
    }

    fun stop() {
        mqttClients.forEach { (_, client) -> client.disconnect() }
        thread?.interrupt()
    }
}
