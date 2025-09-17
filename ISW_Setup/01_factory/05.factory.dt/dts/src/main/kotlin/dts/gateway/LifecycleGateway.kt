package dts.gateway

import dts.connection.SynchronizationDirection
import java.time.LocalDateTime
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.events.observer.IGatewayObserver
import dts.mqtt.getWildcardTopics
import dts.mqtt.setWildcardTopics
import org.eclipse.paho.mqttv5.client.*
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import org.eclipse.paho.mqttv5.common.util.MqttTopicValidator


/**
 * Gateway for controlling the lifecycle of one island.
 *
 * It provides the island status and the capability do shut the island down.
 * The machine lifecycle status and methods to create or destroy machines on this island
 * are also provided.
 * The property IDs used to access are the same as the MQTT topics.
 *
 * Properties send by the island are read only, while all controlling topics are write only.
 *
 * The default value for the island status is `shutting down` and the default value
 * for the machine lifecycle is unknown (The empty string).
 *
 * @param gatewayID The unique ID of this gateway
 * @param island The name of the controlled island
 * @param client The MQTT client used to connect to the island
 * @param qos The QoS used for sending and receiving messages
 * @param machines The set of machines controlled by this gateway
 * @param autoAddMachines Control if unknown machines of this island should automatically be added to this gateway
 */
class LifecycleGateway(
    gatewayID: String,
    private val island: String,
    private val client: IMqttAsyncClient,
    private val qos: Int = 2,
    machines: Set<String> = HashSet(),
    private val autoAddMachines: Boolean = true,
) : AbstractGateway(setOf(), gatewayID) {

    /**
     * The list of all machines known to this gateway.
     *
     * Machines can be added or removed during this gateway's lifetime.
     */
    private val machines: MutableSet<String> = HashSet()
    override val propertyIDs: MutableSet<String> = mutableSetOf("${island}/status", "${island}/stop")
    private val propertyMap: MutableMap<String, Any> = HashMap()

    private val islandStatusTopic = "${island}/status"
    private val islandShutdownTopic = "${island}/stop"

    private val machineStatusTopic = "${island}/machines/+"
    private val machineCreateTopic = "${island}/create/+"
    private val machineDestroyTopic = "${island}/destroy/+"

    /**
     * The MQTT listener to handle incoming lifecycle messages.
     */
    private val mqttCallback = IMqttMessageListener { topic, message ->
        if (MqttTopicValidator.isMatched(islandStatusTopic, topic)) {
            // island status
            propertyMap[topic] = message.toString()
        } else if (MqttTopicValidator.isMatched(machineStatusTopic, topic)) {
            // machine status
            val machine = getWildcardTopics(machineStatusTopic, topic)[0]
            if (machine !in this.machines) {
                if (autoAddMachines) {
                    // TODO Extra event?
                    logger.info { "Received status report from unknown machine '${machine}', machine is automatically added" }
                    addMachine(machine)
                } else {
                    logger.info { "Received status report from unknown machine '${machine}', machine is ignored" }
                    return@IMqttMessageListener
                }
            }
            propertyMap[topic] = message.toString()
            logger.debug { "$topic: $message" }
        } else {
            logger.warn { "unknown topic '${topic}'" }
        }
        createNewDataPointEvent(topic)
    }

    override fun listen() {
        val topics = arrayOf(
            MqttSubscription(islandStatusTopic, qos),
            MqttSubscription(machineStatusTopic, qos)
        )
        val mqttProperties = MqttProperties()
        mqttProperties.setSubscriptionIdentifiers(listOf(0))

        try {

            client.subscribe(topics, null, null, mqttCallback, mqttProperties)

        } catch (me: MqttException) {
            logger.error("Listen failed for ${gatewayID}", me)
        }
    }

    /**
     * Not needed since messages are send on the fly using the MQTT protocol.
     *
     * Also, all lifecycle status messages should have the retain flag set.
     */
    override fun getDataFromSource() {}

    override fun sendError(error: ErrorEvent) {
        for (gatewayListener: IGatewayObserver in observer) {
            val event = ErrorEvent(this)
            event.sourceID = error.source.toString()
            event.timestamp = LocalDateTime.now()
            event.message = error.message
            gatewayListener.handleError(event)
        }
    }

    /**
     * Signal the reception of a new message to all listeners via a `NewDataPointEvent`.
     *
     * @param message An optional message to pass to the event
     */
    private fun createNewDataPointEvent(property: String, message: String = "") {
        for(gatewayListener: IGatewayObserver in observer) {
            // Set up the event
            val dataPointEvent = NewDataPointEvent(this, property)
            dataPointEvent.sourceID= gatewayID
            dataPointEvent.timestamp = LocalDateTime.now()
            dataPointEvent.synchronizationDirection = SynchronizationDirection.GATEWAY_TO_DT
            dataPointEvent.message = message

            // let the observer handle it
            gatewayListener.handleDatapointEvent(dataPointEvent)
        }
    }

    override fun getValue(id: String): Any {
        if (MqttTopicValidator.isMatched(islandStatusTopic, id)) {
            return propertyMap.getValue(id)
        } else if (MqttTopicValidator.isMatched(machineStatusTopic, id)) {
            return propertyMap.getValue(id)
        }
        throw IllegalArgumentException("Can not read from write only property $id")
    }

    override fun getAllValues(): Map<String, Any?> {
        return propertyMap.toMap()
    }

    override fun setValue(id: String, value: Any?) {
        if (MqttTopicValidator.isMatched(islandShutdownTopic, id)) {
            client.publish(id, "destroy".toByteArray(), qos, false)
        } else if (MqttTopicValidator.isMatched(machineCreateTopic, id)) {
            client.publish(id, (value as String).toByteArray(), qos, false)
        } else if (MqttTopicValidator.isMatched(machineDestroyTopic, id)) {
            client.publish(id, "destroy".toByteArray(), qos, false)
        } else {
            throw IllegalArgumentException("Can not write to read only property $id")
        }
    }

    /**
     * Add a machine to this gateway.
     *
     * The machines status can be retrieved, and it can be created or destroyed.
     *
     * @param machine The machines name
     */
    fun addMachine(machine: String) {
        if (machine in this.machines) {
            throw Exception("Machine is already added")
        }
        machines.add(machine)

        val statusProp = setWildcardTopics(machineStatusTopic, listOf(machine))
        this.propertyMap.set(statusProp, "")
        this.propertyIDs.add(statusProp)

        val createProp = setWildcardTopics(machineCreateTopic, listOf(machine))
        this.propertyIDs.add(createProp)

        val destroyProp = setWildcardTopics(machineDestroyTopic, listOf(machine))
        this.propertyIDs.add(destroyProp)
    }

    /**
     * Remove a machine from this gateway.
     *
     * The machines must be known to this gateway.
     *
     * @param machine The machines name
     */
    fun removeMachine(machine: String) {
        if (machine !in this.machines) {
            throw Exception("Machine is not known")
        }
        machines.remove(machine)

        val statusProp = setWildcardTopics(machineStatusTopic, listOf(machine))
        this.propertyMap.remove(statusProp)
        this.propertyIDs.remove(statusProp)

        val createProp = setWildcardTopics(machineCreateTopic, listOf(machine))
        this.propertyIDs.remove(createProp)

        val destroyProp = setWildcardTopics(machineDestroyTopic, listOf(machine))
        this.propertyIDs.remove(destroyProp)
    }

    /**
     * Test if a specific machine is known to this gateway.
     *
     * @param machine The machines name
     */
    fun hasMachine(machine: String): Boolean {
        return machine in machines
    }

    init {
        machines.forEach { machine ->
            addMachine(machine)
        }
        propertyMap[islandStatusTopic] = "shutting down"
        listen()
    }
}
