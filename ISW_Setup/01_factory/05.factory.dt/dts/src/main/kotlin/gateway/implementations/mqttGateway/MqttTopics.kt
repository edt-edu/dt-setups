package dts.gateway.implementations.mqttGateway

/**
 * Utility object that encapsulates the topic structure used by the
 * model factory MQTT interface.  Using the helper functions keeps the
 * topic generation in a single place and avoids hard coded strings
 * throughout the gateway implementation.
 */
object MqttTopics {
    fun machineStatus(machineName: String) = "$machineName/status"
    fun machineControl(machineName: String) = "$machineName/control"

    fun islandStatus(islandName: String) = "$islandName/status"
    fun islandErrors(islandName: String) = "$islandName/errors"
    fun islandQueryAll(islandName: String) = "$islandName/QueryAll"

    fun islandStop(islandName: String) = "$islandName/stop"
    fun islandCreate(islandName: String, machineName: String) = "$islandName/create/$machineName"
    fun islandDestroy(islandName: String, machineName: String) = "$islandName/destroy/$machineName"
    fun islandMachineLifecycle(islandName: String, machineName: String) = "$islandName/machines/$machineName"
}
