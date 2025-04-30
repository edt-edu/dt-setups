private import ScalarValues::*;

package DataTypes {
	enum def SortingLineColors {
		WHITE;
		RED;
		BLUE;
		OTHER;
	}
	enum def StoragePosition {
		A1;
		A2;
		A3;
		B1;
		B2;
		B3;
		C1;
		C2;
		C3;
		INPUT;
	}
	enum def StoragePosition1x3 {
		A;
		B;
		C;
		INPUT;
	}
	attribute def StorageLocation {
		storagePosition : StoragePosition;
		VERTICAL_LOCATION_CONFIG : Integer;
		HORIZONTAL_LOCATION_CONFIG : Integer;
		DEPTH_LOCATION_CONFIG : Integer;
		occupied : Boolean;
	}
	attribute def StorageLocation1x3 {
		storagePosition : StoragePosition1x3;
		VERTICAL_LOCATION_CONFIG : Integer;
		HORIZONTAL_LOCATION_CONFIG : Integer;
		DEPTH_LOCATION_CONFIG : Integer;
		occupied : Boolean;
		storedItem : MovableItemPackage::MovableItem;
	}
}

package MovableItemPackage{
	private import DataTypes::SortingLineColors;
	item def MovableItem {
		attribute color : SortingLineColors;
		attribute weight : Integer;
		attribute size : Integer;
		attribute fragile : Boolean;
		attribute storageLocation : DataTypes::StorageLocation1x3;
	}
}
package CmdTypes {
	private import ActionEnumerationDefinition::*;
	private import DataTypes::StoragePosition;
	private import DataTypes::StorageLocation;
	private import DataTypes::StorageLocation1x3;
	private import DataTypes::StoragePosition1x3;
	item def Cmd {
		attribute machineAction;
	}
	item def SortingLineCmd :> Cmd {
		attribute actionSortingLine : SortingLineActionDef :>> machineAction;
	}
	item def Actuator1DCmd :> Cmd {
		attribute action1DActuator : Actuator1DActionDef :>> machineAction;
		attribute targetPosition : Integer {
			doc
			/*
			 * If there is a Cmd with no need to set "targetPosition", set "targetPosition" null.
			 */
		}
	}
	item def ImpulseActuatorCmd :> Cmd {
		attribute actionImpulseActuator : ImpulseActuatorActionDef :>> machineAction;
	}
	item def ConveyorBeltSimpleCmd :> Cmd {
		attribute actionConveyorBeltSimple : ConveyorBeltSimpleActionDef :>> machineAction;
	}
	item def ConveyorBeltCmd :> ConveyorBeltSimpleCmd {
		attribute moveTo : Integer {
			doc
			/*
			 * If there is a Cmd with no need to set "to", set "to" to null.
			 */
		}
		attribute actionConveyorBelt : ConveyorBeltActionDef :>> actionConveyorBeltSimple;
	}
	item def IndexedLineCmd :> Cmd {
		attribute actionIndexedLine : IndexedLineActionDef :>> machineAction;
		attribute transfer : IndexedLineTransferActionDef {
			doc
			/*
			 * If there is a Cmd with no need to set "transfer", set "transfer" null.
			 */
		}
	}
	item def WarehouseCmd :> Cmd {
		attribute actionWarehouse : WarehouseActionDef :>> machineAction;
		attribute storagePosition : StoragePosition;
	}
	item def Warehouse1x3Cmd specializes WarehouseCmd {
		attribute storagePosition1x3 : StoragePosition1x3;
	}
	item def ClawGripperCmd :> Cmd {
		attribute actionClawGripper : ClawGripperActionDef :>> machineAction;
		attribute targetPosition : Integer {
			doc
			/*
			 * If there is a Cmd with no need to set "targetPosition", set "targetPosition" null.
			 */
		}
	}
	item def VacuumGripperCmd :> Cmd {
		attribute actionVacuumGripper : VacuumGripperActionDef :>> machineAction;
		attribute targetPosition : Integer {
			doc
			/*
			 * If there is a Cmd with no need to set "targetPosition", set "targetPosition" null.
			 */
		}
	}
	item def MillCmd :> Cmd {
		attribute actionMill : MillActionDef :>> machineAction;
	}
	item def DrillCmd :> Cmd {
		attribute actionDrill : DrillActionDef :>> machineAction;
	}

	item def ConveyorSliderCmd :> Cmd {
		attribute actionConveyorSlider : ConveyorSliderActionDef :>> machineAction;
	}

	item def ClawImpulseCmd :> ImpulseActuatorCmd {
		attribute actionClawImpulse : ClawImpulseActionDef :>> actionImpulseActuator;
	}
	item def ArmImpulseCmd :> ImpulseActuatorCmd {
		attribute actionArmImpulse : ArmImpulseActionDef :>> actionImpulseActuator;
	}

	item def RotorCmd :> Actuator1DCmd {
		attribute actionRotor : RotorActionDef :>> action1DActuator;
	}
	item def ArmCmd :> Actuator1DCmd {
		attribute actionArm : ArmActionDef :>> action1DActuator;
	}
	item def VerticalCmd :> Actuator1DCmd {
		attribute actionVertical : VerticalActionDef :>> action1DActuator;
	}
	item def HorizontalCmd :> Actuator1DCmd {
		attribute actionHorizontal : HorizontalActionDef :>> action1DActuator;
	}
	item def CantileverCmd :> Actuator1DCmd {
		attribute actionCantilever : CantileverActionDef;
	}
	/*
	 * item def VacuumModuleCmd :> Cmd {
	 * attribute action :> SysML::Systems::ActionDefinition::action : VacuumModuleAction;
	 * }
	 */

	item def ValveCmd :> Cmd {
		attribute actionValve : ValveActionDef :>> machineAction;
	}
	item def CompressorCmd :> Cmd {
		attribute actionCompressor : CompressorActionDef :>> machineAction;
	}
}
package StateTypes {
	private import StateEnumerationDefinitionDef::*;
	private import CmdTypes::*;
	item def State {
		attribute machineState;
		attribute reason : ReasonStateDef;
		attribute lifecycle : MachineLifecycleDef;
	}
	item def VacuumModuleState :> State {
		attribute stateVacuumModule : VacuumModuleStateDef :>> machineState;
	}
	item def SortingLineState :> State {
		attribute stateSortingLine : SortingLineStateDef :>> machineState;
	}
	item def Actuator1DState :> State {
		attribute stateActuator1D : Actuator1DStateDef :>> machineState;
	}
	item def ImpulseActuatorState :> State {
		attribute stateImpulseActuator : ImpulseActuatorStateDef :>> machineState;
	}
	item def IndexedLineState :> State {
		attribute stateIndexedLine : IndexedLineStateDef :>> machineState;
	}
	item def WarehouseState :> State {
		attribute stateWarehouse : WarehouseStateDef :>> machineState;
		attribute failMessage : String;
		item currentWarehouseCmdState : WarehouseCmd;
	}
	item def Warehouse1x3State specializes WarehouseState {
		attribute stateWarehouse : Warehouse1x3StateDef;
	}
	item def ClawGripperState :> State {
		attribute stateClawGripper : ClawGripperStateDef :>> machineState;
	}
	item def VacuumGripperState :> State {
		attribute stateVacuumGripper : VacuumGripperStateDef :>> machineState;
	}
	item def MillState :> State {
		attribute stateMill : MillStateDef :>> machineState;
	}
	item def DrillState :> State {
		attribute stateDrill : DrillStateDef :>> machineState;
	}
	item def ConveyorSliderState :> State {
		attribute stateConveyorlider : ConveyorSliderStateDef :>> machineState;
	}
	item def ArmState :> Actuator1DState {
		attribute stateArm : ArmStateDef :>> stateActuator1D;
	}
	item def RotorState :> Actuator1DState {
		attribute stateRotor : RotorStateDef :>> stateActuator1D;
	}
	item def ArmImpulseState :> ImpulseActuatorState {
		attribute stateArmImpulse : ImpulseArmStateDef :>> stateImpulseActuator;
	}
	item def ClawImpulseState :> ImpulseActuatorState {
		attribute stateClawImpulse : ImpulseClawStateDef :>> stateImpulseActuator;
	}
	item def VerticalState :> Actuator1DState {
		attribute stateVertical : VerticalStateDef :>> stateActuator1D;
	}
	item def CantileverState :> Actuator1DState {
		attribute stateCantilever : CantileverMotorStateDef :>> stateActuator1D;
	}
	item def ValveState :> State {
		attribute stateValve : ValveStateDef :>> machineState;
	}

	item def CompressorState :> State {
		attribute stateCompressor : CompressorStateDef :>> machineState;
	}
	item def ConveyorBeltSimpleState :> State {
		attribute stateConveyorBeltSimple : ConveyorBeltSimpleStateDef :>> machineState;
	}
	item def ConveyorBeltState :> ConveyorBeltSimpleState {
		attribute stateConveyorBelt : ConveyorBeltStateDef :>> stateConveyorBeltSimple;
	}
	item def HorizontalState :> Actuator1DState {
		attribute stateHorizontal : HorizontalStateDef :>> stateActuator1D;
	}
}
package MQTTMessageTypes {
	item def MQTTBooleanMsg {
		doc
		/*
		 * Represents the MQTT-Message from the API to the Machines
		 */
		attribute booleanValue : Boolean;
	}
}
