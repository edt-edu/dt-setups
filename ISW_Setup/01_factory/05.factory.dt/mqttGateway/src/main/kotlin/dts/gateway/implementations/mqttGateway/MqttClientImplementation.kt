package dts.gateway.implementations.mqttGateway

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * Thin wrapper around the eclipse paho MQTT client used by the
 * gateway.  It exposes a minimal set of operations while providing a
 * common logging behaviour.
 */
class MqttClientImplementation(
    private val config: MqttConfig,
    private val persistence: MemoryPersistence = MemoryPersistence(),
) {

    private var mqttClient: MqttClient? = null
    var isConnected: Boolean = false
        private set

    private val logger = KotlinLogging.logger {}

    @Throws(MqttException::class)
    fun connect(options: MqttConnectOptions = MqttConnectOptions()) {
        if (isConnected) return
        try {
            mqttClient = MqttClient(config.brokerUrl, config.clientId, persistence)
            config.username?.let { options.userName = it }
            config.password?.let { options.setPassword(it.toCharArray()) }
            mqttClient?.connect(options)
            isConnected = true
            logger.info { "MQTT client '${config.clientId}' connected to broker '${config.brokerUrl}'." }
        } catch (e: MqttException) {
            logger.error(e) { "Failed to connect MQTT client '${config.clientId}' to broker '${config.brokerUrl}'" }
            isConnected = false
            throw e
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            isConnected = false
            logger.info { "MQTT client '${config.clientId}' disconnected." }
        } catch (e: MqttException) {
            logger.error(e) { "Error while disconnecting MQTT client '${config.clientId}'" }
        } finally {
            mqttClient?.close()
            mqttClient = null
        }
    }

    @Throws(MqttException::class)
    fun subscribe(topic: String, qos: Int = 0, callback: (topic: String, message: ByteArray) -> Unit) {
        if (!isConnected || mqttClient == null) {
            logger.warn { "MQTT client is not connected. Cannot subscribe to '$topic'." }
            return
        }
        mqttClient?.subscribe(topic, qos) { receivedTopic, message ->
            callback(receivedTopic, message.payload)
        }
        logger.info { "MQTT client '${config.clientId}' subscribed to '$topic' with QoS '$qos'." }
    }

    @Throws(MqttException::class)
    fun publish(topic: String, payload: ByteArray, qos: Int = 0, retained: Boolean = false) {
        if (!isConnected || mqttClient == null) {
            logger.warn { "MQTT client is not connected. Cannot publish on '$topic'." }
            return
        }
        val message = org.eclipse.paho.client.mqttv3.MqttMessage(payload)
        message.qos = qos
        message.isRetained = retained
        mqttClient?.publish(topic, message)
        logger.info { "MQTT client '${config.clientId}' published message on '$topic' (QoS: $qos, Retained: $retained)." }
    }
}
