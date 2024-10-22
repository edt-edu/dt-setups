package Structure {
	import 'Machine UIs'::*;
	import 'Machine UIs'::VacuumGripperUI::*;
	import ActionEnumerationDefinition::*;
	import StateEnumerationDefinition::*;
	import SysML::Systems::StateDefinition::*;
	import ScalarValues::*;
	enum def SortingLineColors {
		WHITE;
		RED;
		BLUE;
		OTHER;
	}
	part def SystemComponent {
		attribute state;
		attribute lifeCycleState;
	}
	package ActionEnumerationDefinition {
		enum def SortingLineAction {
			MOVE_TO_EJECTORS {
				doc /* Moves the current package to the middle light barrier. */
			}
			SORT {
				doc /* Sorts the package waiting at the middle light barrier in the storage container specified by `"color"`. */
			}
			RESOLVE_FAILURE {
				doc /* Manually resolves critical failures. Calling this action needs to ensure that the issue is resolved */
			}
		}
		enum def IndexedLineAction {
			MILL {
				doc /* Activates the mill for two seconds if a package is available at the milling gate. */
			}
			DRILL {
				doc /* Activates the drill for two seconds if a package is available at the drilling gate. */
			}
			TRANSFER {
				doc /*  Transfers the package from start to finish specified in `"transferFromTo"`. The machine only acts if no  package is at the target or for `"MILL_TO_DRILL"` and `"DRILL_TO_END"` if a package is at the starting point.
				 */
			}
			QUERY;
		}
		enum def IndexedLineTransferAction {
			FEED_TO_MILL;
			MILL_TO_DRILL;
			DRILL_TO_END;
		}
		enum def WarehouseAction {
			RETRIEVE;
			STORE;
		}
		enum def ConveyorBeltAction {
			STOP_MOVING;
			MOVE_RIGHT;
			MOVE_LEFT;
			MOVE_RIGHT_TO;
			MOVE_LEFT_TO;
		}
		enum def '1DActuatorAction' {
			MOVE_FORWARD;
			MOVE_BACKWARD;
			TARGET;
			STOP;
			RESET;
		}
		enum def ImpulseActuatorAction {
			MOVE_FORWARD;
			MOVE_BACKWARD_FOR_RESET;
		}
		enum def VerticalAction :> '1DActuatorAction' {
			MOVE_DOWN;
			MOVE_UP;
			TARGET;
			STOP;
			RESET;
		}
		enum def ArmAction :> '1DActuatorAction' {
			MOVE_ARM_OUT;
			MOVE_ARM_IN;
			TARGET;
			STOP;
			RESET;
		}
		enum def RotorAction :> '1DActuatorAction' {
			ROTATE_LEFT;
			ROTATE_RIGHT;
			TARGET;
			STOP;
			RESET;
		}
		enum def ArmImpulseAction :> ImpulseActuatorAction {
			MOVE_ARM_OUT;
			MOVE_ARM_IN;
		}
		enum def ClawImpulseAction :> ImpulseActuatorAction {
			CLOSE_CLAW;
			OPEN_CLAW;
			enumeration;
		}
		enum def MillAction {
			MILL;
			STOP_MILLING;
		}
		enum def DrillAction {
			DRILL;
			STOP_DRILLING;
		}
		enum def VacuumGripperMachineAction {
			ROTATE_LEFT;
			ROTATE_RIGHT;
			ROTATE_TARGET;
			ROTATE_STOP;
			ROTATE_RESET;
			MOVE_ARM_OUT;
			MOVE_ARM_IN;
			ARM_TARGET;
			ARM_STOP;
			ARM_RESET;
			VERTICAL_DOWN;
			VERTICAL_UP;
			VERTICAL_TARGET;
			VERTICAL_STOP;
			VERTICAL_RESET;
		}
		enum def ClawGripperAction {
			VERTICAL_DOWN;
			VERTICAL_UP;
			VERTICAL_TARGET;
			VERTICAL_STOP;
			VERTICAL_RESET;
			ROTATE_LEFT;
			ROTATE_RIGHT;
			ROTATE_TARGET;
			ROTATE_STOP;
			ROTATE_RESET;
			ARM_OUT;
			ARM_IN;
			CLAW_CLOSE;
			CLAW_OPEN;
			ARM_TO;
			ARM_STOP;
			CLAW_TO;
			CLAW_RESET;
		}
		enum def ConveyorSliderAction {
			EXTEND;
			RETRACT;
			STOP;
		}
		enum def VacuumModuleAction {
			VALVE_OPEN;
			VALVE_CLOSED;
			COMPRESSOR_ON;
			COMPRESSOR_OFF;
		}
	}
	package 'Machine Modules UIs' {
		import CmdObjects::*;
		import MachineModuleUI::*;
		import 'Machine UIs'::DrillUI::*;
		import 'Machine UIs'::WarehouseUI::*;
		import 'Machine UIs'::IndexedLineUI::*;
		import SystemComponent::*;
		import *;
		import '1DActuatorUI'::*;
		part def '1DActuatorUI' :> MachineModuleUI {
			attribute positionEncoder : Integer;
			action moveForward;
			action stopMoving;
			action moveBackward;
			action moveToPosition : Integer;
			out port forwardActuator : Boolean;
			out port backwardActuator : Boolean;
			in port backwardLimitSwitch : Boolean;
			action reset;
			in port '1DActuatorCmdIn' : CmdPort1DActuator;
			out port '1DActuatorStateOut' : ActuatorState;
			attribute forwardPositionLimit;
			attribute actuatorState;
			attribute '1DActuatorState' :> state;
		}
		part def ImpulseActuatorUI :> MachineModuleUI {
			action moveForward;
			action moveBackwardForReset;
			out port forwardActuator : Boolean;
			out port backwardActuator : Boolean;
			in port backwardLimitSwitch : Boolean;
			attribute impulseCounter;
			attribute impulseForwardLimit;
			attribute targetPosition;
			in port impulseActuatorCmdIn : CmdPortImpulseActuator;
			out port impulseActuatorStateOut : ImpulseActuatorState;
			attribute impulseActuatorState :> state;
		}
		part def VacuumGripperModuleUI :> MachineModuleUI {
			port actCompressorOn : Boolean;
			port actValve : Boolean;
			attribute vacuumGripperState :> state : VacuumModuleState;
			port vacuumModuleCmdIn : CmdPortVacuumModule;
			port vacuumModuleStateOut : VacuumModuleState;
		}
		part def MachineModuleUI :> SystemComponent {
			attribute reason : ReasonState;
			attribute target : Integer;
			attribute state;
		}
	}
	package 'Type Definitions' {
		item def Matrix {
			attribute row : Integer;
			attribute column : Integer;
		}
	}
	package 'Machine UIs' {
		import Machines::*;
		import ConveyorBeltUI::*;
		import MillUI::*;
		import Structure::*;
		import Modules::ImpulseActuator::*;
		import Machines::ClawGripper::clawGripperUI::impulseActuatorArmUI::*;
		import Machines::ClawGripper::clawGripperUI::'1DActuatorRotorUI'::*;
		import IndexedLineUI::*;
		import WarehouseUI::*;
		import DrillUI::*;
		import ClawGripperUI::*;
		import SystemComponent::*;
		import ConveyorSliderUI::*;
		import ImageMetadata::Image::*;
		import CmdObjects::*;
		import UI::*;
		import 'Machine Modules UIs'::ImpulseActuatorUI::*;
		import 'Machine Modules UIs'::'1DActuatorUI'::*;
		import *;
		import 'Type Definitions'::*;
		import *;
		part def ClawGripperUI :> UI {
			doc /* The ClawGripper utilzes 2 1DActuator and 2 impulseActuator */
			part impulseActuator : 'Machine Modules UIs'::ImpulseActuatorUI [2];
			attribute target : Integer;
			attribute state :> SysML::Systems::StateDefinition::state : ClawState;
			in port clawGripperCmdPortIn :> cmdPortIn : CmdPortClawGripper;
			out port clawGripperStateOut :> stateOut : ClawGripperState;
		}
		part def UI :> SystemComponent {
			attribute reason : ReasonState;
			attribute name : String;
			attribute ip : String;
			attribute stateChanged : Boolean;
			in port cmdPortIn;
			out port stateOut;
		}
		part def VacuumGripperUI :> UI {
			part '1DActuator' : '1DActuator', 'Machine Modules UIs'::'1DActuatorUI' [3];
			part rotor : 'Machine Modules UIs'::'1DActuatorUI' {
				port actRotLeft :> forwardActuator;
				port actRotRight :> backwardActuator;
				port senseRotEndRight :> backwardLimitSwitch;
				attribute limitLeft :> forwardPositionLimit;
				action rotateLeft :> moveForward;
				action rotateRight :> moveBackward;
				attribute rotorState :> '1DActuatorState' : ImpulseArmState;
			}
			part arm : 'Machine Modules UIs'::'1DActuatorUI' {
				port actArmOut :> forwardActuator;
				port actArmIn :> backwardActuator;
				port senseArmEndIn :> backwardLimitSwitch;
				attribute limitOut :> forwardPositionLimit;
				action armOut :> moveForward;
				action armIn :> moveBackward;
				attribute armState :> '1DActuatorState' : ImpulseArmState;
			}
			part vertical : 'Machine Modules UIs'::'1DActuatorUI' {
				out port actVerticalDown :> forwardActuator;
				out port actVerticalUp :> backwardActuator;
				in port senseVerticalEndUp :> Structure::'Machine Modules UIs'::ImpulseActuatorUI::backwardLimitSwitch;
				attribute limitDown :> forwardPositionLimit;
				action moveDown :> moveForward;
				action moveUp :> moveBackward;
				attribute armState :> '1DActuatorState' : VerticalState;
			}
			part vacuumModule;
			doc /* The VacuumGripper utilizes 3 1DActuator */
			in port vacuumGripperCmdPortIn :> cmdPortIn : CmdPortVacuumGripper;
			in port vacuumGripperStateOut :> stateOut : VacuumGripperMachineState;
			attribute state :> SysML::Systems::StateDefinition::state;
			attribute vacuumState;
			attribute attribute;
			attribute valveState : Boolean;
		}
		part def DrillUI :> UI {
			out port actDrillingOut : Boolean;
			action drill;
			action stop;
			in port drillCmdPortIn :> cmdPortIn : CmdPortMill;
			out port millStateOut :> stateOut : DrillState;
			attribute state :> SysML::Systems::StateDefinition::state;
			port sensor : Boolean;
		}
		part def ConveyorSliderUI :> UI {
			port actMotorSliderForward : Boolean;
			port sensePushButtonFront : Boolean;
			port actMotorSliderBackward : Boolean;
			port sensePushButtonBack : Boolean;
			attribute location : IndexedLineTransferAction;
			port conveyorSliderCmdPortIn :> cmdPortIn : CmdPortConveyorSlider;
			port conveyorSliderStateOut :> stateOut : ConveyorSliderState;
			attribute state :> SysML::Systems::StateDefinition::state : ConveyorSliderState;
		}
		part def SortingLineUI :> UI {
			attribute color : String;
			attribute isWhiteStored : Boolean;
			attribute isRedStored : Boolean;
			attribute isBlueStored : Boolean;
			attribute isAtStart : Boolean;
			action sort;
			action moveToEjectors;
			attribute target : Integer;
			in port impulseCounter : Integer;
			in port senseInputLightBarrier : Boolean;
			in port senseMiddleLightBarrier : Boolean;
			in port senseWhiteLightBarrier : Boolean;
			in port senseRedLightBarrier : Boolean;
			in port senseBlueLightBarrier : Boolean;
			out port actMotorConveyor : Boolean;
			out port actCompressorOn : Boolean;
			out port actWhiteEjector : Boolean;
			out port actRedEjector : Boolean;
			out port actBlueEjector : Boolean;
			in port sortingLineCmdIn :> cmdPortIn : CmdPortSortingLine;
			out port sortingLineStateOut :> stateOut : SortingLineState;
			attribute state :> SysML::Systems::StateDefinition::state : SortingLineState;
		}
		part def ConveyorBeltUI :> UI {
			action stopMoving;
			action moveRight;
			action moveLeft;
			action moveRightTo : Integer;
			action moveLeftTo : Integer;
			part conveyorController {
				in port photoTransistor1In : Rational;
				in port photoTransistor2In : Rational;
				out port backwardsMotorOut : Boolean;
				out port ForwardMotorOut : Boolean;
			}
			in port conveyorBeltCmdIn :> cmdPortIn : CmdPortConveyorBelt;
			out port actConveyorBelt : Boolean;
			out port conveyorBeltStateOut :> stateOut : ConveyorBeltState;
			port senseConveyorStart : Boolean;
		}
		part def IndexedLineUI :> UI {
			attribute packageAtMill : Boolean {
				doc /* Sensor at mill */
			}
			attribute packageAtDrill : Boolean {
				doc /* Sensor at drill */
			}
			attribute packageAtStart : Boolean {
				doc /* Sensor at the start of the feeding conveyor */
			}
			attribute packageAtEnd : Boolean {
				doc /* Sensor after slider two (slider that transfers from drill to end) */
			}
			attribute packageAtSlider : Boolean {
				doc /* Sensor right before slider one (slider that transfers from feed to mill) */
			}
			attribute transferFromTo : String {
				doc /* isOptional */
			}
			doc /* The indexed line consists of 4 conveyors, a drilling machine, a milling machine and 2 conveyor sliders. */
			part conveyorBelt : ConveyorBeltUI [4];
			attribute target : Integer;
			action transfer : IndexedLineTransferAction;
			in port indexedLineCmdIn :> cmdPortIn : CmdPortIndexedLine;
			port indexedLineStateOut :> stateOut : IndexedLineState;
			attribute state :> SysML::Systems::StateDefinition::state : IndexedLineState;
		}
		part def MillUI :> UI {
			out port actMillingOut : Boolean;
			action mill;
			action stop;
			attribute state :> SysML::Systems::StateDefinition::state : MillState;
			in port millCmdPortIn :> cmdPortIn : CmdPortMill;
			out port millStateOut :> stateOut : MillState;
			port sensor : Boolean;
		}
		port conveyourBeltStateOut;
		port def ConveyourBeltState;
		part def WarehouseUI :> UI {
			doc /* The pick-up zone is where the vacuum gripper can grip or place items. 
			There are nine rack positions at rows 0, 1 and 2 and columns 0, 1 and 2. */
			part vacuumGripper : VacuumGripperUI;
			in port warehouseCmdIn :> cmdPortIn : CmdPortWarehouse;
			in port containerIn : Boolean;
			in port warehouseStateOut :> stateOut : WarehouseState;
			action retrieve {
				doc /* Retrieve a container from the rack and put it onto the pickup zone on the conveyor
				
				        1. Move to rack position
				        2. Extend arm
				        3. Pickup container
				        4. Retract arm
				        5. Move to conveyor
				        6. Move conveyor out until light barrier */
			}
			action store {
				doc /* Get a container from the conveyor and store it in the rack
				
				        1. Move to conveyor drop
				        2. Move conveyor in until ...
				        3. Move to rack target
				        4. Store container */
			}
			action home {
				doc /* Homes all axis
				
				The home position is at the top of the conveyor side with the arm retraced. */
			}
			in port senseHorizontalEnd : Boolean;
			in port senseLightBarrierIn : Boolean;
			in port senseLightBarrierOut : Boolean;
			in port senseVerticalEndUp : Boolean;
			in port encoderHorizontal;
			in port encoderVertical;
			in port senseArmOut : Boolean;
			in port senseArmIn : Boolean;
			out port actConveyorOut : Boolean;
			out port actConveyorIn : Boolean;
			out port actHorizontalToRack : Boolean;
			out port actHorizontalToConveyor : Boolean;
			out port actVerticalDown : Boolean;
			out port actVerticalUp : Boolean;
			out port actArmOut : Boolean;
			out port actArmIn : Boolean;
			attribute currentContainer : Matrix;
			attribute nextContainer : Matrix;
			attribute horizontalPositions : Matrix;
			attribute verticalPositions : Matrix;
			attribute state :> SysML::Systems::StateDefinition::state : WarehouseState;
		}
	}
	package CmdObjects {
		import SysML::Systems::ActionDefinition::*;
		item def CmdPort {
			attribute action;
		}
		item def CmdPortSortingLine :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : SortingLineAction;
		}
		item def CmdPort1DActuator :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : '1DActuatorAction';
			attribute targetPosition : Integer {
				doc /* If there is a Cmd with no need to set "targetPosition", set "targetPosition" null. */
			}
		}
		item def CmdPortImpulseActuator :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : ImpulseActuatorAction;
		}
		item def CmdPortConveyorBelt :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : ConveyorBeltAction;
			attribute to : Integer {
				doc /* If there is a Cmd with no need to set "to", set "to" to null. */
			}
		}
		item def CmdPortIndexedLine :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : IndexedLineAction;
			attribute transfer : IndexedLineTransferAction {
				doc /* If there is a Cmd with no need to set "transfer", set "transfer" null. */
			}
		}
		item def CmdPortWarehouse :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : WarehouseAction;
			attribute row : Integer;
			attribute column : Integer;
		}
		item def CmdPortClawGripper :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : ClawGripperAction;
			attribute targetPosition : Integer {
				doc /* If there is a Cmd with no need to set "targetPosition", set "targetPosition" null. */
			}
		}
		item def CmdPortVacuumGripper :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : VacuumGripperMachineAction;
			attribute targetPosition : Integer {
				doc /* If there is a Cmd with no need to set "targetPosition", set "targetPosition" null. */
			}
		}
		item def CmdPortMill :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : MillAction;
		}
		item def CmdPortDrill :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : DrillAction;
		}
		item def CmdPortConveyorSlider :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : ConveyorSliderAction;
		}
		item def CmdPortClawImpulse :> CmdPortImpulseActuator {
			attribute action :> SysML::Systems::ActionDefinition::action : ClawImpulseAction;
		}
		item def CmdPortVertical :> CmdPort1DActuator {
			attribute action :> SysML::Systems::ActionDefinition::action : VerticalAction;
		}
		item def CmdPortRotor :> CmdPort1DActuator {
			attribute action :> SysML::Systems::ActionDefinition::action : RotorAction;
		}
		item def CmdPortArm :> CmdPort1DActuator {
			attribute action :> SysML::Systems::ActionDefinition::action : ArmAction;
		}
		item def CmdPortArmImpulse :> CmdPortImpulseActuator {
			attribute action :> SysML::Systems::ActionDefinition::action : ArmImpulseAction;
		}
		item def CmdPortVacuumModule :> CmdPort {
			attribute action :> SysML::Systems::ActionDefinition::action : VacuumModuleAction;
		}
	}
	package Modules {
		part def '1DActuator' {
			in port forwardActuator : Boolean;
			out port backwardLimitSwitch : Boolean;
			in port backwardActuator : Boolean;
		}
		part def ImpulseActuator {
			out port forwardActuator : Boolean;
			in port backwardActuator : Boolean;
			out port backwardLimitSwitch : Boolean;
		}
		part def VacuumModule {
			port actCompressorOn : Boolean;
			port actValve : Boolean;
		}
		part def Mill {
			port actMillingOut : Boolean;
			port sensor : Boolean;
		}
		part def ConveyorSlider {
			port actMotorSliderForward : Boolean;
			port actMotorSliderBackward : Boolean;
			port senseButtonPushForward : Boolean;
			port senseButtonPushBackward : Boolean;
		}
		part def ConveyorBelt {
			port actConveyorBelt : Boolean;
			port senseConveyorStart : Boolean;
		}
		part def Drill {
			port actDrillOut : Boolean;
			port sensor : Boolean;
		}
	}
	package Machines {
		import MQTTMessages::*;
		import Modules::ConveyorSlider::*;
		import 'Machine UIs'::ConveyorSliderUI::*;
		import Modules::Mill::*;
		import 'Machine UIs'::MillUI::*;
		import 'Machine UIs'::DrillUI::*;
		import Modules::Drill::*;
		import IndexedLine::indexedLineController::conveyorBeltMillingController::*;
		import Modules::ConveyorBelt::*;
		import 'Machine UIs'::ConveyorBeltUI::*;
		import 'Machine Modules UIs'::VacuumGripperModuleUI::*;
		import 'Machine UIs'::UI::*;
		import Structure::*;
		import 'Machine UIs'::ClawGripperUI::*;
		import 'Machine Modules UIs'::ImpulseActuatorUI::*;
		import Modules::'1DActuator'::*;
		import Modules::ImpulseActuator::*;
		import 'Machine Modules UIs'::'1DActuatorUI'::*;
		import CmdObjects::*;
		import ControlFunctions::if::*;
		part def ClawGripper {
			part rotor1DActuator : Modules::'1DActuator' {
				in port actRotorLeft :> forwardActuator : Boolean;
				in port actRotorRight :> backwardActuator : Boolean;
				out port senseRotorEndRight :> backwardLimitSwitch : Boolean;
			}
			part clawImpulseActuator : Modules::'1DActuator' {
				in port actClawClose :> forwardActuator : Boolean;
				in port actClawOpen :> backwardActuator : Boolean;
				out port senseClawOpen :> backwardLimitSwitch : Boolean;
			}
			part vertical1DActuator : Modules::ImpulseActuator {
				in port actVerticalDown :> backwardActuator : Boolean;
				in port actVerticalUp :> forwardActuator : Boolean;
				out port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
			}
			in port clawGripperCmdPortIn : CmdPortClawGripper;
			out port clawGripperStateOut : ClawGripperState;
			part clawGripperUI : ClawGripperUI {
				in port clawGripperCmdPortIn :> Structure::'Machine UIs'::ClawGripperUI::clawGripperCmdPortIn : CmdPortClawGripper;
				out port clawGripperStateOut :> Structure::'Machine UIs'::ClawGripperUI::clawGripperStateOut : ClawGripperState;
				part '1DActuatorRotorUI' : 'Machine Modules UIs'::'1DActuatorUI' {
					out port actRotLeft :> forwardActuator : Boolean;
					out port actRotRight :> backwardActuator : Boolean;
					in port senseRotEndRight :> backwardLimitSwitch : Boolean;
					action rotateLeft :> moveForward;
					action rotateRight :> moveBackward;
					attribute limitLeft :> forwardPositionLimit;
					in port rotorCmdIn :> '1DActuatorCmdIn' : CmdPortRotor;
					out port rotorStateOut :> '1DActuatorStateOut' : RotorState;
				}
				part '1DActuatorVerticalUI' : 'Machine Modules UIs'::'1DActuatorUI' {
					out port actVerticalDown :> backwardActuator : Boolean;
					attribute limitDown :> forwardPositionLimit : Integer;
					out port actVerticalUp :> forwardActuator : Boolean;
					in port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
					action moveUp :> moveForward;
					action moveDown :> moveBackward;
					in port verticalCmdIn :> '1DActuatorCmdIn' : CmdPortVertical;
					out port verticalStateOut :> '1DActuatorStateOut' : VerticalState;
				}
				part impulseActuatorArmUI : 'Machine Modules UIs'::ImpulseActuatorUI {
					out port actArmOut :> 'Machine Modules UIs'::'1DActuatorUI'::forwardActuator : Boolean;
					out port actArmIn :> 'Machine Modules UIs'::'1DActuatorUI'::backwardActuator : Boolean;
					in port senseArmEndIn :> 'Machine Modules UIs'::'1DActuatorUI'::backwardLimitSwitch : Boolean;
					attribute impulseCounterArm :> impulseCounter;
					attribute limitOut :> impulseForwardLimit;
					action armOut :> 'Machine Modules UIs'::'1DActuatorUI'::moveForward;
					action armIn :> moveBackwardForReset;
					out port armStateOut :> impulseActuatorStateOut : ImpulseArmState;
					in port armCmdIn :> impulseActuatorCmdIn : CmdPortArmImpulse;
				}
				part impulseActuatorClawUI : 'Machine Modules UIs'::ImpulseActuatorUI {
					out port actClawClose :> forwardActuator : Boolean;
					out port actClawOpen :> backwardActuator : Boolean;
					in port senseClawOpen :> backwardLimitSwitch : Boolean;
					attribute impulseCounterClaw :> impulseCounter;
					attribute limitOut :> impulseForwardLimit;
					action closeClaw :> moveForward;
					action openClaw :> moveBackwardForReset;
					in port clawCmdIn :> impulseActuatorCmdIn : CmdPortClawImpulse;
					out port clawStateOut :> impulseActuatorStateOut : ClawState;
				}
				part clawGripperEngine {
					in port 'clawGripperCmdPortIn ' : CmdPortClawGripper;
					in port 'rotorStateIn : RotorState';
					in port verticalStateIn : VerticalState;
					in port armStateIn : ImpulseArmState;
					in port clawStateIn : ClawState;
					out port 'clawGripperStateOut ' : ClawGripperState;
					out port verticalCmdOut : CmdPortVertical;
					out port rotorCmdOut : CmdPortRotor;
					out port armCmdOut : CmdPortArmImpulse;
					out port clawCmdOut : CmdPortClawImpulse;
				}
			}
			part armImpulseActuator : Modules::ImpulseActuator {
				in port actArmOut :> forwardActuator : Boolean;
				in port actArmIn :> backwardActuator : Boolean;
				out port senseArmEndIn :> backwardLimitSwitch : Boolean;
			}
			action actRotorRightMsg : MQTTBooleanMsg;
			action actRotorLeftMsg : MQTTBooleanMsg;
			action senseRotorEndMsg : MQTTBooleanMsg;
			action senseVerticalEndMsg : MQTTBooleanMsg;
			action actVerticalDownMsg : MQTTBooleanMsg;
			action actVerticalUpMsg : MQTTBooleanMsg;
			action senseClawOpenMsg : MQTTBooleanMsg;
			action actClawCloseMsg : MQTTBooleanMsg;
			action actClawOpenMsg : MQTTBooleanMsg;
			action senseArmEndMsg : MQTTBooleanMsg;
			action actArmInMsg : MQTTBooleanMsg;
			action actArmOutMsg : MQTTBooleanMsg;
		}
		port 'controllerRotorCmd In';
		part def VacuumGripper {
			in port vacuumGripperCmdPortIn : CmdPortVacuumGripper;
			out port vacuumGripperStateOut : VacuumGripperMachineState;
			part rotor1DActuator : Modules::'1DActuator' {
				in port actRotorLeft :> forwardActuator : Boolean;
				in port actRotorRight :> backwardActuator : Boolean;
				out port senseRotorEndRight :> backwardLimitSwitch : Boolean;
			}
			part arm1DActuator : Modules::'1DActuator' {
				in port actArmOut :> forwardActuator : Boolean;
				in port actArmIn :> backwardActuator : Boolean;
				out port senseArmEndIn :> backwardLimitSwitch : Boolean;
			}
			part vertical1DActuator : Modules::'1DActuator' {
				in port actVerticalDown :> forwardActuator : Boolean;
				in port actVerticalUp :> backwardActuator : Boolean;
				out port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
			}
			part vacuumGripperModule : Modules::VacuumModule {
				in port actCompressorOn :> Structure::'Machine Modules UIs'::VacuumGripperModuleUI::actCompressorOn : Boolean;
				in port actValve :> Structure::'Machine Modules UIs'::VacuumGripperModuleUI::actValve : Boolean;
			}
			part vacuumGripperUI : VacuumGripperUI {
				part vertical1DActuatorUI : 'Machine Modules UIs'::'1DActuatorUI' {
					out port actVerticalDown :> backwardActuator : Boolean;
					out port actVerticalUp :> forwardActuator : Boolean;
					in port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
					in port verticalCmdIn :> '1DActuatorCmdIn' : CmdPortVertical;
					out port verticalStateOut :> '1DActuatorStateOut' : VerticalState;
				}
				part rotor1DActuatorUI : 'Machine Modules UIs'::'1DActuatorUI' {
					out port actRotorLeft :> forwardActuator : Boolean;
					out port actRotorRight :> backwardActuator : Boolean;
					in port senseRotorEndRight :> backwardLimitSwitch : Boolean;
					in port rotorCmdIn :> '1DActuatorCmdIn' : CmdPortRotor;
					out port rotorStateOut :> '1DActuatorStateOut' : RotorState;
				}
				part arm1DActuatorUI : 'Machine Modules UIs'::'1DActuatorUI' {
					out port actArmOut :> forwardActuator : Boolean;
					out port actArmIn :> backwardActuator : Boolean;
					in port senseArmEndIn :> backwardLimitSwitch : Boolean;
					in port armCmdIn :> '1DActuatorCmdIn' : CmdPortArm;
					out port armStateOut :> '1DActuatorStateOut' : ArmState;
				}
				in port vaccumGripperCmdPortIn :> vacuumGripperCmdPortIn : CmdPortVacuumGripper;
				out port vacuumGripperStateOut :> Structure::'Machine UIs'::VacuumGripperUI::vacuumGripperStateOut : VacuumGripperMachineState;
				part vacuumModuleUI : 'Machine Modules UIs'::VacuumGripperModuleUI {
					out port actCompressorOn :> Structure::'Machine Modules UIs'::VacuumGripperModuleUI::actCompressorOn : Boolean;
					out port actValve :> Structure::'Machine Modules UIs'::VacuumGripperModuleUI::actValve : Boolean;
					in port vacuumModuleCmdPort :> vacuumModuleCmdIn : CmdPortVacuumModule;
					out port vacuumModuleState :> vacuumModuleStateOut : VacuumModuleState;
				}
				part vacuumGripperEngine {
					out port vacuumModuleCmdOut : VacuumModuleState;
					out port armCmdOut : CmdPortArm;
					out port rotorCmdOut : CmdPortRotor;
					out port verticalCmdOut : CmdPortVertical;
					out port vacuumGripperStateOut : VacuumGripperMachineState;
					in port vacuumGripperCmdPortIn : CmdPortVacuumGripper;
					in port verticalStateIn : VerticalState;
					in port rotorStateIn : RotorState;
					in port armStateIn : ArmState;
					in port vacuumModuleStateIn : VacuumModuleState;
				}
			}
			action senseRotorEndRightMsg : MQTTBooleanMsg;
			action actRotorLeftMsg : MQTTBooleanMsg;
			action actRotorLeftMsg : MQTTBooleanMsg;
			action senseVerticalEndUpMsg : MQTTBooleanMsg;
			action actVerticalDownMsg : MQTTBooleanMsg;
			action actVerticalUpMsg : MQTTBooleanMsg;
			action actArmInMsg : MQTTBooleanMsg;
			action actArmOutMsg : MQTTBooleanMsg;
			action senseArmEndMsg : MQTTBooleanMsg;
			action actCompressorMsg : MQTTBooleanMsg;
			action actValveMsg : MQTTBooleanMsg;
		}
		port def '> backwardLimitSwitch';
		port backwardActuaotr;
		port CmdPortIn;
		port VacuumGripperCmdPortIn;
		part def IndexedLine {
			port indexedLineCmdPortIn : CmdPortIndexedLine;
			port indexedLineStateOut : IndexedLineState;
			part indexedLineController : IndexedLineUI {
				part conveyorSliderFeedToMillController : ConveyorSliderUI {
					attribute locationFeedToMill :> location;
					port actMotorSliderForwardFeedToMIll :> actMotorSliderForward : Boolean;
					port sensePushButtonFrontFeedToMill :> sensePushButtonFront : Boolean;
					port actMotorSliderBackwardFeedToMill :> actMotorSliderBackward : Boolean;
					port sensePushButtonBackFeedToMill :> sensePushButtonBack : Boolean;
					port conveyorSliderFeedToMillCmdPort :> conveyorSliderCmdPortIn : CmdPortConveyorSlider;
					port conveyorSliderFeedToMillStateOut :> conveyorSliderStateOut : ConveyorSliderState;
				}
				part conveyorSliderDrillToEndController : ConveyorSliderUI {
					attribute locationDrillToEnd :> location;
					port actMotorSliderDrillToEnd :> actMotorSliderForward : Boolean;
					port sensePushButtonFrontDrillToEnd : sensePushButtonFront;
					port actMotorSliderBackwardDrillToEnd :> actMotorSliderBackward : Boolean;
					port sensePushButtonBackDrillToEnd :> sensePushButtonBack : Boolean;
					port conveyorSliderDrillToEndCmdPort :> conveyorSliderCmdPortIn : CmdPortConveyorSlider;
					port conveyorSliderDrillToEndStateOut : conveyorSliderStateOut;
				}
				part millController : MillUI {
					out port actMillingOut :> MillUI::actMillingOut : Boolean;
					in port millCmdPort :> millCmdPortIn : CmdPortMill;
					in port millState :> millStateOut : MillState;
					port positionSensor :> DrillUI::sensor : Boolean;
				}
				part drillController : DrillUI {
					port actDrillingOut :> Structure::'Machine UIs'::DrillUI::actDrillingOut : Boolean;
					port drillCmdPort :> drillCmdPortIn : CmdPortDrill;
					port drillStateOut :> millStateOut : DrillState;
					port positionSensor :> sensor : Boolean;
				}
				part conveyorBeltFeedController : ConveyorBeltUI {
					port conveyorBeltFeedCmdIn :> conveyorBeltCmdIn : CmdPortConveyorBelt;
					port actConveyorBeltFeed :> actConveyorBelt : Boolean;
					port conveyorBeltFeedStateOut :> conveyorBeltStateOut : ConveyorBeltState;
					port feedSenseConveyorStart :> senseConveyorStart : Boolean;
				}
				part conveyorBeltMillingController : ConveyorBeltUI {
					port conveyorBeltMillCmdIn :> conveyorBeltCmdIn : CmdPortConveyorBelt;
					port actConveyorBeltMill :> actConveyorBelt : Boolean;
					port conveyorBeltMillStateOut :> conveyorBeltStateOut : ConveyorBeltState;
					port millSenseConveyorStart :> senseConveyorStart : Boolean;
				}
				part conveyorBeltDrillingController : ConveyorBeltUI {
					port conveyorBeltDrillCmdIn :> conveyorBeltCmdIn : CmdPortConveyorBelt;
					port actConveyorBeltDrill :> actConveyorBelt : Boolean;
					port conveyorBeltDrillStateOut :> conveyorBeltStateOut : ConveyorBeltState;
					port drillSenseConveyorStart :> senseConveyorStart : Boolean;
				}
				part conveyorBeltEndController : ConveyorBeltUI {
					port conveyorBeltEndCmdIn :> conveyorBeltCmdIn : CmdPortConveyorBelt;
					port actConveyorBeltEnd :> actConveyorBelt : Boolean;
					port conveyorBeltEndStateOut :> conveyorBeltStateOut : ConveyorBeltState;
				}
			}
			part mill : Modules::Mill {
				port sensorMill :> sensor : Boolean;
				port actMillingOut :> MillUI::actMillingOut : Boolean;
			}
			part drill : Modules::Drill {
				port actDrillOut :> Structure::Modules::Drill::actDrillOut : Boolean;
				port sensorDrill :> sensor : Boolean;
			}
			part conveyorSliderDrilltoEnd : Modules::ConveyorSlider {
				port actMotorSliderDrillToEndForward :> actMotorSliderForward : Boolean;
				port actMotorSliderDrillToEndBackward :> actMotorSliderBackward : Boolean;
				port senseButtonPushDrillToEndForward :> senseButtonPushForward : Boolean;
				port senseButtonPushDrillToEndBackward :> senseButtonPushBackward : Boolean;
			}
			part conveyorSliderFeedToMill : Modules::ConveyorSlider {
				port actMotorSliderFeedToMillForward :> actMotorSliderForward : Boolean;
				port actMotorSliderFeedToMillBackward :> actMotorSliderBackward : Boolean;
				port senseButtonPushFeedToMillForward :> senseButtonPushForward : Boolean;
				port senseButtonPushFeedToMillBackward :> senseButtonPushBackward : Boolean;
			}
			part conveyorBeltMill : Modules::ConveyorBelt {
				port actConveyorBeltMill :> actConveyorBelt : Boolean;
				port senseConveyorMillStart :> senseConveyorStart : Boolean;
			}
			part conveyorBeltDrill : Modules::ConveyorBelt {
				port actConveyorBeltDrill :> actConveyorBelt : Boolean;
				port senseConveyorDrillStart :> senseConveyorStart : Boolean;
			}
			part conveyorBeltEnd : Modules::ConveyorBelt {
				port actConveyorBeltEnd :> actConveyorBelt : Boolean;
				port senseConveyorEndStart :> senseConveyorStart : Boolean;
			}
			part conveyorBeltFeed : Modules::ConveyorBelt {
				port actConveyorBeltFeed :> actConveyorBelt : Boolean;
				port senseConveyorFeedStart :> senseConveyorStart : Boolean;
			}
		}
	}
	package StateEnumerationDefinition {
		enum def MovementState {
			STOPPED;
			TARGETING;
			RESETTING;
		}
		enum def ActuatorState :> MovementState {
			doc /* Typical status report. */
			MOVING_FORWARD;
			MOVING_BACKWARD;
			TARGETING;
			STOPPED;
			RESETTING;
		}
		enum def ConveyorBeltState :> MovementState {
			MOVING_RIGHT;
			MOVING_LEFT;
			TARGETING;
			STOPPED;
			RESETTING;
		}
		enum def VerticalState :> ActuatorState {
			MOVING_UP;
			MOVING_LEFT;
			STOPPED;
			TARGETING;
			RESETTING;
		}
		enum def ImpulseArmState :> ImpulseActuatorState {
			IS_READY;
			RESETTING;
			MOVING_OUT;
		}
		enum def RotorState :> ActuatorState {
			ROTATING_RIGHT;
			ROTATING_LEFT;
			STOPPED;
			TARGETING;
			RESETTING;
		}
		enum def WarehouseState {
			READY;
			AT_PICKUP;
			RETRIEVING;
			STORING;
			RETRIEVE_FAILED;
			TASK_FAILED;
			UNKOWN_PICKUP;
		}
		enum def SortingLineState {
			READY {
				doc /* No package available. Ready to receive package */
			}
			MOVING_TO_EJECTORS {
				doc /* Currently moving package to ejector light barrier. */
			}
			PACKAGE_WAITING_AT_EJECTORS {
				doc /* Package waiting at ejector light barrier. Ready to sort package. */
			}
			SORTING {
				doc /*  Sorts the package waiting at the middle light barrier in a storage container. */
			}
			CRITICAL_FAILURE_WHILE_SORTING {
				doc /* Occurs when an ejector misfires and timeouts. This state can only be exited by calling `"resolve failure"`. There is no internal machine resolve for this. Therefore calling this action needs to ensure that the issue is resolved otherwise (e.g. via vacuum_gripper). */
			}
		}
		enum def ReasonState {
			CMD {
				doc /* The state corresponds to a command or the initial reset. */
			}
			TARGET_REACHED {
				doc /* A targeting operation succeded. State is `stopped`.
				  The `target` is given. */
			}
			POSITION_LIMIT {
				doc /* The previous action was stopped because of a position limit. */
			}
		}
		enum def ClawState :> ImpulseActuatorState {
			CLOSING;
			IS_READY;
			RESETTING;
		}
		enum def MillState {
			IS_MILLING;
			IS_NOT_MILLING;
		}
		enum def ClawGripperState {
			VERTICAL_MOVING_UP;
			VERTICAL_TARGETING;
			VERTICAL_STOPPING;
			VERTICAL_RESETTING;
			ROTATING_LEFT;
			ROTATING_RIGHT;
			ROTATING_TARGETING;
			ROTATING_STOP;
			ROTATING_RESETTING;
			VERTICAL_MOVING_DOWN;
			ARM_MOVING_OUT;
			ARM_MOVING_IN;
			CLAW_OPENING;
			CLAW_CLOSING;
			ARM_MOVING_TO;
			ARM_STOPPING;
			CLAW_STOPPING;
			CLAW_RESETTING;
		}
		enum def ConveyorSliderState {
			doc /* add doc here */
			EXTENDING;
			RETRACTING;
			STOPPING;
		}
		enum def DrillState {
			IS_DRILLING;
			IS_MILLING;
		}
		enum def IndexedLineState {
			READY;
			MILLING;
			DRILLING;
			TRANSFERRING;
		}
		enum def ImpulseActuatorState {
			MOVING_FORWARD;
			MOVING_BACKWARD_FOR_RESET;
			IS_READY;
		}
		enum def VacuumModuleState {
			COMPRESSOR_ON;
			COMPRESSOR_OFF;
			VALVE_OPEN;
			VALVE_CLOSED;
		}
		enum def ArmState :> ActuatorState {
			MOVING_ARM_OUT;
			MOVING_ARM_IN;
			TARGETTING;
			STOPPING;
			RESETTING;
		}
		enum def VacuumGripperMachineState {
			ROTATING_LEFT;
			ROTATING_RIGHT;
			ROTATING_TARGETING;
			ROTATING_STOPPING;
			ROTATING_RESETTING;
			ARM_MOVING_OUT;
			ARM_MOVING_IN;
			ARM_TARGETTING;
			ARM_STOPPING;
			ARM_RESETTING;
			VERTICAL_MOVING_DOWN;
			VERTICAL_MOVING_UP;
			VERTICAL_TARGETING;
			VERTICAL_STOPPING;
			VERTICAL_RESETTING;
			COMPRESSOR_ON;
			VALVE_OPEN;
			COMPRESSOR_OFF;
			VALVE_CLOSED;
		}
	}
	package MQTTMessages {
		item def MQTTBooleanMsg {
			doc /* Represents the MQTT-Message from the UI to the Machines */
			attribute booleanValue : Boolean;
		}
	}
}