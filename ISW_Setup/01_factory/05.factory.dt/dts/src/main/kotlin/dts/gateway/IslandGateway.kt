package dts.gateway

import dts.connection.SynchronizationDirection
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.events.observer.IGatewayObserver
import dts.gateway.KeyValuePairBytearrayParser.Companion.utf8
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.LocalDateTime

class IslandGateway(
    gatewayID: String,
    private val client: IMqttAsyncClient,
    private val machines: Set<String> = setOf(
        "1-1-conveyor",
        "1-2-clawGripper",
        "1-3-conveyor",
        "1-4-sortingLine",
        "1-5-warehouse",
        "1-6-vacuumGripper",
        "1-7-indexedLine",
        "1-8-clawGripper",
    )
) : AbstractGateway(
    propertyIDs=setOf(
        *machines.map { m -> "${m}/status" }.toTypedArray(),
        *machines.map { m -> "${m}/control" }.toTypedArray()
    ),
    gatewayID=gatewayID
) {

    private val propertyMap = HashMap<String, Any?>()

    private val qos = 2

    override fun listen() {
        val topics = machines.map { MqttSubscription("${it}/status", qos) }.toTypedArray()
        val mqttProperties = MqttProperties()
        mqttProperties.setSubscriptionIdentifiers(listOf(0))

        try {
            client.subscribe(topics, null, null, object : IMqttMessageListener {
                override fun messageArrived(topicName: String?, message: MqttMessage?) {
                    val propertyIDValuePairs: Map<String, Any?>
                    try {
                        propertyIDValuePairs = KeyValuePairBytearrayParser.decode(message!!.payload)
                        logger.debug { "$topicName: $propertyIDValuePairs" }
                    } catch (ex: Exception) {
                        logger.warn(ex) { "Could not decode message" }
                        return
                    }
                    propertyMap.put(topicName!!, propertyIDValuePairs)
                    logger.info { "received updated $topicName" }

                    createNewDataPointEvent(topicName)
                }
            }, mqttProperties)

        } catch (me: MqttException) {
            logger.error(me) { "Listen failed for ${gatewayID}" }
        }
    }

    init {
        propertyIDs.forEach { propertyMap[it] = null }
        listen()
    }

    override fun getDataFromSource() {
        client.publish("queryAll", "query".toByteArray(utf8), qos, false)
    }

    override fun sendError(error: ErrorEvent) {
        for (gatewayListener: IGatewayObserver in observer) {
            val event = ErrorEvent(this)
            event.sourceID = error.source.toString()
            event.timestamp = LocalDateTime.now()
            event.message = error.message
            gatewayListener.handleError(event)
        }
    }

    override fun getValue(id: String): Any? {
        if (propertyMap.getValue(id) == null) {
            getDataFromSource()
        }
        return propertyMap.getValue(id)
    }

    override fun getAllValues(): Map<String, Any?> {
        return propertyMap
    }

    override fun setValue(id: String, value: Any?) {
        if (id.substringAfter("/") == "status") {
            throw IllegalArgumentException("Can not write to read only `status` property $id")
        }
        if (propertyMap.containsKey(id) && value is Map<*, *>) {
            propertyMap.set(id, value)
            try {
                val message = KeyValuePairBytearrayParser.encode(value as Map<String, Any?>)
                // NOTE Probably multiple requests for the same id in quick succession
                client.publish(id, message, qos, false)
            } catch (e: Exception) {
                logger.error(e) { "Something went wrong while encoding and publishing the property map" }
            }
        }
    }

    private fun createNewDataPointEvent(property: String, message: String = "") {
        for(gatewayListener: IGatewayObserver in observer){
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
}

/**
 * This helper class provides functions for encoding/decoding a list of key-value pairs as a bytearray that can be
 * transferred via MQTT.
 */
private class KeyValuePairBytearrayParser {
    companion object {

        val utf8: Charset = Charset.forName("UTF-8")

        private enum class ValueType {
            BOOL, STRING, INT, FLOAT, NONE
        }

        /**
         * Encodes a list of key-value pairs as a bytearray
         */
        fun encode(data: Map<String, Any?>): ByteArray {
            val buffer = ByteBuffer.allocate(4096)
            buffer.putInt(data.size)
            data.forEach {
                // Key encoding
                encodeString(buffer, it.key)

                // Value encoding
                val valueType = getValueType(it.value)
                buffer.put(valueType.ordinal.toByte()) // Type encoding
                when (valueType) {
                    ValueType.BOOL -> buffer.put(if (it.value as Boolean) 1 else 0)
                    ValueType.STRING -> encodeString(buffer, it.value as String)
                    ValueType.INT -> buffer.putInt(it.value as Int)
                    ValueType.FLOAT -> buffer.putFloat(it.value as Float)
                    ValueType.NONE -> {}
                }
            }
            val end = buffer.position()
            return buffer.array().copyOfRange(0, end)
        }

        /**
         * Decodes a bytearray which contains a list of key-value pairs
         */
        fun decode(array: ByteArray): Map<String, Any?> {
            val elementCount = ByteBuffer.wrap(array, 0, 4).getInt()
            val keyValuePairs = mutableMapOf<String, Any?>()
            var position = 4 // next index in the ByteArray to be read
            repeat(elementCount) {
                val (key, pos) = decodeString(array, position)
                position = pos
                val type = ValueType.values()[array[position++].toInt()]
                val value = when (type) {
                    ValueType.BOOL -> array[position++] > 0
                    ValueType.STRING -> {
                        val (str, pos) = decodeString(array, position)
                        position = pos
                        str
                    }
                    ValueType.INT -> ByteBuffer.wrap(array, position, 4).getInt().also { position += 4 }
                    ValueType.FLOAT -> ByteBuffer.wrap(array, position, 4).getFloat().also { position += 4 }
                    ValueType.NONE -> null
                }
                keyValuePairs.put(key, value)
            }

            return keyValuePairs
        }

        /**
         * A helper method for encoding strings.
         * The first 4 bytes store the length, the rest the string itself, encoded in utf-8
         */
        private fun encodeString(buf: ByteBuffer, string: String) {
            val stringByteArray = string.toByteArray(utf8)
            buf.putInt(stringByteArray.size)
            buf.put(stringByteArray)
        }

        /**
         * A helper method for decoding strings.
         * The first 4 bytes store the length, the rest the string itself, encoded in utf-8
         */
        private fun decodeString(array: ByteArray, position: Int): Pair<String, Int> {
            var position = position
            val stringByteLength = ByteBuffer.wrap(array, position, 4).getInt()
            position += 4
            return Pair(String(array, position, stringByteLength, utf8), position + stringByteLength)
        }

        /**
         * Returns the type of given value
         * If the Type is not known, "MessageType.STRING" is returned
         */
        private fun getValueType(value: Any?): ValueType {
            if (value == null) {
                return ValueType.NONE
            }
            return when (value) {
                is Boolean -> ValueType.BOOL
                is String -> ValueType.STRING
                is Int -> ValueType.INT
                is Float -> ValueType.FLOAT
                else -> ValueType.STRING
            }
        }
    }
}
