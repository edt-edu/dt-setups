package dts.gateway.implementations.mqttGateway.commands

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class MachineCommand

// Conveyor Commands
data class ConveyorCommand(
    val motor: String
) : MachineCommand()

// Gripper Commands
data class GripperRotateCommand(
    val rotate: String
) : MachineCommand()

data class GripperArmCommand(
    val arm: String
) : MachineCommand()

data class GripperVerticalCommand(
    val vertical: String
) : MachineCommand()

data class GripperClawCommand(
    val claw: String
) : MachineCommand()

// Vacuum Gripper Commands
data class VacuumGripperRotateCommand(
    val rotate: String
) : MachineCommand()

data class VacuumGripperArmCommand(
    val arm: String
) : MachineCommand()

data class VacuumGripperVerticalCommand(
    val vertical: String
) : MachineCommand()

data class VacuumGripperCompressorCommand(
    val compressor: Boolean
) : MachineCommand()

data class VacuumGripperValveCommand(
    val valve: Boolean
) : MachineCommand()

// Indexed Line Commands
data class IndexedLineActionCommand(
    val action: String,
    @JsonProperty("transfer_from_to")
    val transferFromTo: String? = null
) : MachineCommand()

// Sorting Line Commands
data class SortingLineActionCommand(
    val action: String,
    val color: String? = null
) : MachineCommand()

// Warehouse Commands
data class WarehouseStoreCommand(
    val action: String = "store",
    val row: Int,
    val column: Int
) : MachineCommand()

data class WarehouseRetrieveCommand(
    val action: String = "retrieve",
    val row: Int,
    val column: Int
) : MachineCommand()