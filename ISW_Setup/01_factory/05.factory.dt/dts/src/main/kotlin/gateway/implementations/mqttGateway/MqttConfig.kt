package dts.gateway.implementations.mqttGateway

import org.eclipse.paho.client.mqttv3.MqttConnectOptions

data class MqttConfig(
    val configId: String,
    val brokerUrl: String,
    val clientId: String = "dts-mqtt-client-${System.currentTimeMillis()}-${configId}",
    val topicsToSubscribe: Map<String, Int> = emptyMap(),
    val islandName: String? = null,
    val machineNames: List<String> = emptyList(),
    val qosPublish: Int = 1,
    val isRetainedPublish: Boolean = false,
    val username: String? = null,
    val password: String? = null,
    val connectionTimeout: Int = 10,
    val keepAliveInterval: Int = 60,
    val cleanSession: Boolean = true,
    val automaticReconnect: Boolean = true
) {

    fun toConnectOptions(): MqttConnectOptions = MqttConnectOptions().apply {
        connectionTimeout = this@MqttConfig.connectionTimeout
        keepAliveInterval = this@MqttConfig.keepAliveInterval
        isCleanSession = this@MqttConfig.cleanSession
        isAutomaticReconnect = this@MqttConfig.automaticReconnect
        username?.let { userName = it }
        this@MqttConfig.password?.let { password = it.toCharArray() }
    }
}