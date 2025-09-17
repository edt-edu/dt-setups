package dts.gateway.implementations.mqttGateway.status

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MachineStatus(
    // Common status
    val status: String? = null,

    // Conveyor
    val motor: String? = null,
    val position: Int? = null,

    // Gripper / Vacuum Gripper
    val rotation: Int? = null,
    val arm: Int? = null,
    val vertical: Int? = null,
    val claw: String? = null,
    val compressor: Boolean? = null,
    val valve: Boolean? = null,

    // Indexed Line / Sorting Line / Warehouse
    val state: String? = null,

    // Sorting Line
    val color: String? = null,

    // Warehouse
    val stock: Map<String, Any>? = null,

    // Error messages
    val error: String? = null,
    val message: String? = null
)