package Structure {
	import 'State Types'::*;
	import 'Machine APIs'::*;
	import 'Machine APIs'::VacuumGripperAPI::*;
	import ActionEnumerationDefinition::*;
	import StateEnumerationDefinition::*;
	import SysML::Systems::StateDefinition::*;
	import ScalarValues::*;
	part def API {
		attribute state : State;
		attribute name : String;
		in port cmd;
		out port state;
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
			START_MILL {
				doc /* Activates the mill for two seconds if a package is available at the milling gate. */
			}
			START_DRILL {
				doc /* Activates the drill for two seconds if a package is available at the drilling gate. */
			}
			TRANSFER {
				doc /*  Transfers the package from start to finish specified in `"transferFromTo"`. The machine only acts if no  package is at the target or for `"MILL_TO_DRILL"` and `"DRILL_TO_END"` if a package is at the starting point.
				 */
			}
			STOP_MILL;
			STOP_DRILL;
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
		}
		enum def MillAction {
			MILL;
			STOP_MILLING;
		}
		enum def DrillAction {
			DRILL;
			STOP_DRILLING;
		}
		enum def VacuumGripperAction {
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
	package 'Machine Modules APIs' {
		import 'Cmd Types'::*;
		import *;
		import DrillAPI::*;
		import 'Machine APIs'::WarehouseAPI::*;
		import 'Machine APIs'::IndexedLineAPI::*;
		import API::*;
		import *;
		import '1DActuatorAPI'::*;
		part def '1DActuatorAPI' :> API {
			attribute positionEncoder : Integer;
			action moveForward;
			action stopMoving;
			action moveBackward;
			action moveToPosition : Integer;
			out port forwardActuator : Boolean;
			out port backwardActuator : Boolean;
			in port backwardLimitSwitch : Boolean;
			action reset;
			out port '1DActuatorStateOut' : StateEnumerationDefinition::'1DActuatorState';
			attribute forwardLimit : Integer {
				doc /* forwardPositionLimit >= 0 && forwardPositionLimit <= 2147483648 */
			}
			in port cmd :> Structure::API::cmd : '1DActuatorCmd';
			attribute state :> SysML::Systems::StateDefinition::state : '1DActuatorState';
			attribute accuracy : Integer;
			attribute stopOffset : Integer;
			attribute backwardLimit : Integer;
			attribute sensorBackwardLimitSwitch : Boolean;
			attribute actuatorForward : Boolean;
			attribute actuatorBackward : Boolean;
			attribute target : Integer;
			attribute currentCmd : '1DActuatorCmd';
		}
		part def ImpulseActuatorAPI :> API {
			action moveForward;
			action moveBackwardForReset;
			out port forwardActuator : Boolean;
			out port backwardActuator : Boolean;
			in port backwardLimitSwitch : Boolean;
			attribute impulseCounter;
			attribute impulseForwardLimit : Integer;
			out port impulseActuatorStateOut : StateEnumerationDefinition::ImpulseActuatorState;
			attribute state :> SysML::Systems::StateDefinition::state : ImpulseActuatorState;
			in port cmd :> Structure::API::cmd : ImpulseActuatorCmd;
			attribute sensorBackwardLimitSwitch : Boolean;
			attribute actuatorForward : Boolean;
			attribute actuatorBackward : Boolean;
			attribute target : Integer;
			attribute currentCmd : ImpulseActuatorCmd;
		}
		part def VacuumModuleAPI :> API {
			port actCompressorOn : Boolean;
			port actValve : Boolean;
			port vacuumModuleStateOut : StateEnumerationDefinition::VacuumModuleState;
			attribute state :> SysML::Systems::StateDefinition::state : VacuumModuleState;
			in port cmd :> Structure::API::cmd : VacuumModuleCmd;
			attribute actuatorCompressor : Boolean;
			attribute actuatorValve : Boolean;
			attribute currentCmd : VacuumModuleCmd;
			action compressorOn;
			action openValve;
			action compressorOff;
			action closeValve;
		}
		part def ConveyorSliderAPI :> API {
			port actMotorSliderForward : Boolean;
			port sensePushButtonFront : Boolean;
			port actMotorSliderBackward : Boolean;
			port sensePushButtonBack : Boolean;
			attribute location : IndexedLineTransferAction;
			port conveyorSliderCmdPortIn :> VacuumGripperAPI::cmd : ConveyorSliderCmd;
			port conveyorSliderStateOut : StateEnumerationDefinition::ConveyorSliderState;
			attribute state :> SysML::Systems::StateDefinition::state : ConveyorSliderState;
			attribute currentCmd : ConveyorSliderCmd;
			action motorSliderForward;
			action motorSliderBackward;
		}
		part def ConveyorBeltAPI :> API {
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
			in port conveyorBeltCmdIn :> VacuumGripperAPI::cmd : ConveyorBeltCmd;
			out port actConveyorBelt : Boolean;
			out port conveyorBeltStateOut : StateEnumerationDefinition::ConveyorBeltState;
			in port senseConveyor : Boolean;
			attribute currentCmd : ConveyorBeltCmd;
			attribute state :> SysML::Systems::StateDefinition::state : ConveyorBeltState;
			attribute targetCount : Integer;
			attribute targeting : Boolean;
			attribute isMoving : Boolean;
		}
		part def DrillAPI :> API {
			out port actDrillingOut : Boolean;
			action drill;
			action stop;
			in port drillCmd :> VacuumGripperAPI::cmd : MillCmd;
			out port millStateOut : StateEnumerationDefinition::DrillState;
			attribute state :> SysML::Systems::StateDefinition::state : DrillState;
			attribute currentCmd : DrillCmd;
			attribute isDrilling : Boolean;
			attribute drillTime : Integer;
		}
		part def MillAPI :> API {
			out port actMillingOut : Boolean;
			action mill;
			action stop;
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::MillState;
			in port millCmd :> VacuumGripperAPI::cmd : MillCmd;
			out port millStateOut : StateEnumerationDefinition::MillState;
			attribute isMilling : Boolean;
			attribute currentCmd : MillCmd;
			attribute millTime : Integer;
		}
		attribute def conveyorBeltCmd;
		attribute def float;
		attribute def Float;
	}
	package 'Data Types' {
		item def Matrix {
			attribute row : Integer;
			attribute column : Integer;
		}
		enum def SortingLineColors {
			WHITE;
			RED;
			BLUE;
			OTHER;
		}
	}
	package 'Machine APIs' {
		import 'Machine Systems'::IndexedLineSystem::*;
		import 'Machine Systems'::*;
		import 'Machine Modules APIs'::ConveyorBeltAPI::*;
		import 'Machine Modules APIs'::MillAPI::*;
		import Structure::*;
		import Modules::ImpulseActuator::*;
		import 'Interconnection Views and Part Trees'::clawGripperSystem::clawGripperAPI::impulseActuatorArmAPI::*;
		import 'Interconnection Views and Part Trees'::clawGripperSystem::clawGripperAPI::'1DActuatorRotorAPI'::*;
		import IndexedLineAPI::*;
		import WarehouseAPI::*;
		import 'Machine Modules APIs'::DrillAPI::*;
		import ClawGripperAPI::*;
		import API::*;
		import 'Machine Modules APIs'::ConveyorSliderAPI::*;
		import ImageMetadata::Image::*;
		import 'Cmd Types'::*;
		import *;
		import 'Machine Modules APIs'::ImpulseActuatorAPI::*;
		import 'Machine Modules APIs'::'1DActuatorAPI'::*;
		import *;
		import 'Data Types'::*;
		import *;
		part def VacuumGripperAPI :> API {
			part '1DActuator' : '1DActuator', 'Machine Modules APIs'::'1DActuatorAPI' [3];
			part rotor : 'Machine Modules APIs'::'1DActuatorAPI' {
				port actRotLeft :> forwardActuator;
				port actRotRight :> backwardActuator;
				port senseRotEndRight :> backwardLimitSwitch;
				attribute limitLeft :> forwardLimit;
				action rotateLeft :> moveForward;
				action rotateRight :> moveBackward;
				attribute rotorState : ImpulseArmState;
			}
			part arm : 'Machine Modules APIs'::'1DActuatorAPI' {
				port actArmOut :> forwardActuator;
				port actArmIn :> backwardActuator;
				port senseArmEndIn :> backwardLimitSwitch;
				attribute limitOut :> forwardLimit;
				action armOut :> moveForward;
				action armIn :> moveBackward;
				attribute armState : ImpulseArmState;
			}
			part vertical : 'Machine Modules APIs'::'1DActuatorAPI' {
				out port actVerticalDown :> forwardActuator;
				out port actVerticalUp :> backwardActuator;
				in port senseVerticalEndUp :> Structure::'Machine Modules APIs'::ImpulseActuatorAPI::backwardLimitSwitch;
				attribute limitDown :> forwardLimit;
				action moveDown :> moveForward;
				action moveUp :> moveBackward;
				attribute armState : StateEnumerationDefinition::VerticalState;
			}
			part vacuumModule;
			doc /* The VacuumGripper utilizes 3 1DActuator */
			in port cmd :> API::cmd : VacuumGripperCmd;
			in port vacuumGripperStateOut : StateEnumerationDefinition::VacuumGripperState;
			attribute state :> SysML::Systems::StateDefinition::state : VacuumGripperState;
		}
		part def SortingLineAPI :> API {
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
			in port sortingLineCmd :> VacuumGripperAPI::cmd : SortingLineCmd;
			out port sortingLineStateOut : StateEnumerationDefinition::SortingLineState;
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::SortingLineState;
		}
		part def IndexedLineAPI :> API {
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
			part conveyorBelt : 'Machine Modules APIs'::ConveyorBeltAPI [4];
			attribute target : Integer;
			action transfer : IndexedLineTransferAction;
			in port indexedLineCmd :> VacuumGripperAPI::cmd : IndexedLineCmd;
			out port indexedLineState : StateEnumerationDefinition::IndexedLineState;
			attribute state :> SysML::Systems::StateDefinition::state : IndexedLineState;
		}
		port conveyourBeltStateOut;
		port def ConveyourBeltState;
		part def ClawGripperAPI :> API {
			doc /* The ClawGripper utilzes 2 1DActuator and 2 impulseActuator */
			part impulseActuator : 'Machine Modules APIs'::ImpulseActuatorAPI [2];
			attribute state :> SysML::Systems::StateDefinition::state : ClawGripperState;
			in port cmd :> API::cmd : ClawGripperCmd;
			out port clawGripperStateOut : StateEnumerationDefinition::ClawGripperState;
		}
		part def WarehouseAPI :> API {
			doc /* The pick-up zone is where the vacuum gripper can grip or place items. 
			There are nine rack positions at rows 0, 1 and 2 and columns 0, 1 and 2. */
			part vacuumGripper : VacuumGripperAPI;
			in port warehouseCmd :> VacuumGripperAPI::cmd : WarehouseCmd;
			in port containerIn : Boolean;
			in port warehouseStateOut : StateEnumerationDefinition::WarehouseState;
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
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::WarehouseState;
		}
		action sliderForwardMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actSliderBackwardMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action SenseButtonBackMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseButtonFrontMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actMill : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actDrill : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actMotorSliderForward : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actMotorSliderBackward : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseButtonPushBackMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseButtonPushBackMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actConveyorFeed : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseConveyorFeed : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actConveyorBeltMill : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseConveyorMill : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actConveyorBeltDrill : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseDrillConveyor : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseConveyorEnd : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actConveyorBeltEnd : Structure::'MQTT Message Types'::MQTTBooleanMsg;
	}
	package 'Cmd Types' {
		import SysML::Systems::ActionDefinition::*;
		item def Cmd {
			attribute action;
		}
		item def SortingLineCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : SortingLineAction;
		}
		item def '1DActuatorCmd' :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : '1DActuatorAction';
			attribute targetPosition : Integer {
				doc /* If there is a Cmd with no need to set "targetPosition", set "targetPosition" null. */
			}
		}
		item def ImpulseActuatorCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : ImpulseActuatorAction;
		}
		item def ConveyorBeltCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : ConveyorBeltAction;
			attribute to : Integer {
				doc /* If there is a Cmd with no need to set "to", set "to" to null. */
			}
		}
		item def IndexedLineCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : IndexedLineAction;
			attribute transfer : IndexedLineTransferAction {
				doc /* If there is a Cmd with no need to set "transfer", set "transfer" null. */
			}
		}
		item def WarehouseCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : WarehouseAction;
			attribute row : Integer;
			attribute column : Integer;
		}
		item def ClawGripperCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : ClawGripperAction;
			attribute targetPosition : Integer {
				doc /* If there is a Cmd with no need to set "targetPosition", set "targetPosition" null. */
			}
		}
		item def VacuumGripperCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : VacuumGripperAction;
			attribute targetPosition : Integer {
				doc /* If there is a Cmd with no need to set "targetPosition", set "targetPosition" null. */
			}
		}
		item def MillCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : MillAction;
		}
		item def DrillCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : DrillAction;
		}
		item def ConveyorSliderCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : ConveyorSliderAction;
		}
		item def ClawImpulseCmd :> ImpulseActuatorCmd {
			attribute action :> SysML::Systems::ActionDefinition::action : ClawImpulseAction;
		}
		item def RotorCmd :> '1DActuatorCmd' {
			attribute action :> SysML::Systems::ActionDefinition::action : RotorAction;
		}
		item def ArmCmd :> '1DActuatorCmd' {
			attribute action :> SysML::Systems::ActionDefinition::action : ArmAction;
		}
		item def ArmImpulseCmd :> ImpulseActuatorCmd {
			attribute action :> SysML::Systems::ActionDefinition::action : ArmImpulseAction;
		}
		item def VacuumModuleCmd :> Cmd {
			attribute action :> SysML::Systems::ActionDefinition::action : VacuumModuleAction;
		}
		item def VerticalCmd :> '1DActuatorCmd' {
			attribute action :> SysML::Systems::ActionDefinition::action : VerticalAction;
		}
	}
	package Modules {
		part def '1DActuator' :> Hardware {
			in port forwardActuator : Boolean;
			out port backwardLimitSwitch : Boolean;
			in port backwardActuator : Boolean;
		}
		part def ImpulseActuator :> Hardware {
			out port forwardActuator : Boolean;
			in port backwardActuator : Boolean;
			out port backwardLimitSwitch : Boolean;
		}
		part def VacuumModule :> Hardware {
			port actCompressorOn : Boolean;
			port actValve : Boolean;
		}
		part def Mill :> Hardware {
			port actMillingOut : Boolean;
			port sensor : Boolean;
		}
		part def ConveyorSlider :> Hardware {
			port actMotorSliderForward : Boolean;
			port actMotorSliderBackward : Boolean;
			port senseButtonPushForward : Boolean;
			port senseButtonPushBackward : Boolean;
		}
		part def ConveyorBelt :> Hardware {
			port actConveyorBelt : Boolean;
			port senseConveyorStart : Boolean;
		}
		part def Drill :> Hardware {
			port actDrillOut : Boolean;
			port sensor : Boolean;
		}
		part def LightSensor :> Hardware {
			out port sensor : Boolean;
		}
	}
	package StateEnumerationDefinition {
		enum def MovementState {
			STOPPED;
			TARGETING;
			RESETTING;
		}
		enum def '1DActuatorState' :> MovementState {
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
		enum def VerticalState :> '1DActuatorState' {
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
		enum def RotorState :> '1DActuatorState' {
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
		enum def ImpulseClawState :> ImpulseActuatorState {
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
		enum def ArmState :> '1DActuatorState' {
			MOVING_ARM_OUT;
			MOVING_ARM_IN;
			TARGETTING;
			STOPPING;
			RESETTING;
		}
		enum def VacuumGripperState {
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
		enum def MachineLifecycle {
			ONLINE;
			ALREADY_CREATED;
			DESTROYED;
			FAILURE;
			UNKNOWN_TYPE;
			DESTROY_ABORTED;
		}
	}
	part def System {
		part hardware : Hardware [0..*];
		part software : API [0..*];
	}
	package 'State Types' {
		import 'Machine Modules APIs'::ImpulseActuatorAPI::*;
		item def State {
			attribute state;
			attribute reason : ReasonState;
			attribute Mach : MachineLifecycle;
		}
		item def VacuumModuleState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::VacuumModuleState;
		}
		item def SortingLineState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::SortingLineState;
		}
		item def '1DActuatorState' :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::'1DActuatorState';
		}
		item def ImpulseActuatorState :> State, State {
			attribute state :> SysML::Systems::StateDefinition::state;
		}
		item def ConveyorBeltState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::ConveyorBeltState;
		}
		item def IndexedLineState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::IndexedLineState;
		}
		item def WarehouseState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::WarehouseState;
		}
		item def ClawGripperState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::ClawGripperState;
		}
		item def VacuumGripperState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::VacuumGripperState;
		}
		item def MillState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::MillState;
		}
		item def DrillState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::DrillState;
		}
		item def ConveyorSliderState :> State {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::ConveyorSliderState;
		}
		item def ArmState :> '1DActuatorState' {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::ArmState;
		}
		item def RotorState :> '1DActuatorState' {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::RotorState;
		}
		item def ArmImpulseState :> ImpulseActuatorState {
			attribute state :> SysML::Systems::StateDefinition::state : ImpulseArmState;
		}
		item def ClawImpulseState :> ImpulseActuatorState {
			attribute state :> SysML::Systems::StateDefinition::state : ImpulseClawState;
		}
		item def VerticalState :> '1DActuatorState' {
			attribute state :> SysML::Systems::StateDefinition::state : StateEnumerationDefinition::VerticalState;
		}
	}
	package 'MQTT Message Types' {
		item def MQTTBooleanMsg {
			doc /* Represents the MQTT-Message from the API to the Machines */
			attribute booleanValue : Boolean;
		}
	}
	enum def IndexedLineLocation {
		FEED_TO_MILL;
		MILL_TO_DRILL;
		DRILL_TO_END;
	}
	package 'Machine Systems' {
		import 'Interconnection Views and Part Trees'::clawGripperSystem::*;
		import 'Interconnection Views and Part Trees'::vacuumGripperSystem::*;
		import ClawGripperSystem::*;
		import 'Interconnection Views and Part Trees'::clawGripperSystem::clawGripperAPI::clawGripperEngine::*;
		import 'Interconnection Views and Part Trees'::clawGripperSystem::clawGripperAPI::*;
		import VacuumGripperSystem::*;
		import Modules::*;
		import API::*;
		import 'MQTT Message Types'::*;
		import Modules::ConveyorSlider::*;
		import 'Machine Modules APIs'::ConveyorSliderAPI::*;
		import Modules::Mill::*;
		import 'Machine Modules APIs'::MillAPI::*;
		import 'Machine Modules APIs'::DrillAPI::*;
		import Modules::Drill::*;
		import 'Interconnection Views and Part Trees'::indexedLineSystem::indexedLineAPI::conveyorBeltMillingAPI::*;
		import Modules::ConveyorBelt::*;
		import 'Machine Modules APIs'::ConveyorBeltAPI::*;
		import 'Machine Modules APIs'::VacuumModuleAPI::*;
		import *;
		import Structure::*;
		import 'Machine APIs'::ClawGripperAPI::*;
		import 'Machine Modules APIs'::ImpulseActuatorAPI::*;
		import Modules::'1DActuator'::*;
		import Modules::ImpulseActuator::*;
		import 'Machine Modules APIs'::'1DActuatorAPI'::*;
		import 'Cmd Types'::*;
		import ControlFunctions::if::*;
		part def ClawGripperSystem :> System {
			in port clawGripperCmdPortIn : ClawGripperCmd;
			out port clawGripperStateOut : StateEnumerationDefinition::ClawGripperState;
		}
		port 'controllerRotorCmd In';
		part def VacuumGripperSystem :> System {
			in port vacuumGripperCmdPortIn : VacuumGripperCmd;
			out port vacuumGripperStateOut : StateEnumerationDefinition::VacuumGripperState;
		}
		port def '> backwardLimitSwitch';
		port backwardActuaotr;
		port CmdPortIn;
		port VacuumGripperCmdPortIn;
		part def IndexedLineSystem :> System {
			in port indexedLineCmd : IndexedLineCmd;
			out port indexedLineState : StateEnumerationDefinition::IndexedLineState;
		}
		action 'actRotorRightMsg ' : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseRotorEndRight : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actArmInMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actArmOutMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseArmEndMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actVerticalDownMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actVerticalUpMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseVerticalEndUpMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actValveMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actCompressorMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actRotLeftMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actRotRightMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseRotEndMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actVerticalUpMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actVerticalDownMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseVerticalEndUp : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action armOutMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actArmInMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseArmEndInMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actClawCloseMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action actClawOpenMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		action senseClawOpenMsg : Structure::'MQTT Message Types'::MQTTBooleanMsg;
		part def SortingLineSystem :> System {
			in port sortingLineCmd : SortingLineCmd;
			out port sortingLineState : SortingLineState;
		}
		part def FactorySystem :> System {
			in port factoryCmd;
			port factoryState;
		}
	}
	package 'Interconnection Views and Part Trees' {
		import Modules::'1DActuator'::*;
		import Modules::ImpulseActuator::*;
		import Structure::*;
		import 'Machine Modules APIs'::ConveyorSliderAPI::*;
		import 'Cmd Types'::*;
		import 'Machine Modules APIs'::'1DActuatorAPI'::*;
		import Machines::*;
		import indexedLineSystem::*;
		import 'Machine Modules APIs'::VacuumModuleAPI::*;
		import 'Machine APIs'::ClawGripperAPI::*;
		import Modules::*;
		import 'Machine Modules APIs'::ImpulseActuatorAPI::*;
		import API::*;
		part vacuumGripperSystem : VacuumGripperSystem {
			part vacuumGripperAPI : VacuumGripperAPI {
				part vertical1DActuatorAPI : 'Machine Modules APIs'::'1DActuatorAPI' {
					out port actVerticalDown :> backwardActuator : Boolean;
					out port actVerticalUp :> forwardActuator : Boolean;
					in port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
					in port verticalCmdIn :> cmd : VerticalCmd;
					out port verticalStateOut :> '1DActuatorStateOut' : StateEnumerationDefinition::VerticalState;
					action moveDown :> moveForward;
					action moveUp :> moveBackward;
					attribute upLimit :> forwardLimit : Integer;
					attribute downLimit :> backwardLimit : Integer;
					attribute state :> SysML::Systems::StateDefinition::state : VerticalState;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : VerticalCmd;
					attribute actuatorDown :> actuatorBackward : Boolean;
					attribute actuatorUp :> actuatorForward : Boolean;
					attribute sensorDownLimitSwitch :> sensorBackwardLimitSwitch : Boolean;
				}
				part rotor1DActuatorAPI : 'Machine Modules APIs'::'1DActuatorAPI' {
					out port actRotorLeft :> forwardActuator : Boolean;
					out port actRotorRight :> backwardActuator : Boolean;
					in port senseRotorEndRight :> backwardLimitSwitch : Boolean;
					in port rotorCmdIn :> cmd : RotorCmd;
					out port rotorStateOut :> '1DActuatorStateOut' : StateEnumerationDefinition::RotorState;
					action rotateLeft :> moveForward;
					action rotateRight :> moveBackward;
					attribute limitRight :> forwardLimit : Integer;
					attribute state :> SysML::Systems::StateDefinition::state : RotorState;
					attribute limitLeft :> backwardLimit : Integer;
					attribute sensorLeftLimitSwitch :> sensorBackwardLimitSwitch : Boolean;
					attribute actuatorRight :> actuatorForward : Boolean;
					attribute actuatorLeft :> actuatorBackward : Boolean;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : RotorCmd;
				}
				part arm1DActuatorAPI : 'Machine Modules APIs'::'1DActuatorAPI' {
					out port actArmOut :> forwardActuator : Boolean;
					out port actArmIn :> backwardActuator : Boolean;
					in port senseArmEndIn :> backwardLimitSwitch : Boolean;
					in port armCmdIn :> cmd : ArmCmd;
					out port armStateOut :> '1DActuatorStateOut' : StateEnumerationDefinition::ArmState;
					action actArmOut :> moveForward;
					action actArmIn :> moveBackward;
					attribute armOutLimit :> forwardLimit : Integer;
					attribute armInLimit :> backwardLimit : Integer;
					attribute state :> SysML::Systems::StateDefinition::state : ArmState;
					attribute sensorArmBackLimitSwitch :> sensorBackwardLimitSwitch : Boolean;
					attribute armForward :> actuatorForward : Boolean;
					attribute armBackward :> actuatorBackward : Boolean;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : ArmCmd;
				}
				in port vaccumGripperCmdPortIn :> cmd : VacuumGripperCmd;
				out port vacuumGripperStateOut :> VacuumGripperAPI::vacuumGripperStateOut : StateEnumerationDefinition::VacuumGripperState;
				part vacuumModuleAPI : 'Machine Modules APIs'::VacuumModuleAPI {
					out port actCompressorOn :> Structure::'Machine Modules APIs'::VacuumModuleAPI::actCompressorOn : Boolean;
					out port actValve :> Structure::'Machine Modules APIs'::VacuumModuleAPI::actValve : Boolean;
					in port vacuumModuleCmdPort :> cmd : VacuumModuleCmd;
					out port vacuumModuleState :> vacuumModuleStateOut : StateEnumerationDefinition::VacuumModuleState;
				}
				part vacuumGripperEngine {
					out port vacuumModuleCmdOut : StateEnumerationDefinition::VacuumModuleState;
					out port armCmdOut : ArmCmd;
					out port rotorCmdOut : RotorCmd;
					out port verticalCmdOut : VerticalCmd;
					out port vacuumGripperStateOut : StateEnumerationDefinition::VacuumGripperState;
					in port vacuumGripperCmdPortIn : VacuumGripperCmd;
					in port verticalStateIn : StateEnumerationDefinition::VerticalState;
					in port rotorStateIn : StateEnumerationDefinition::RotorState;
					in port armStateIn : StateEnumerationDefinition::ArmState;
					in port vacuumModuleStateIn : StateEnumerationDefinition::VacuumModuleState;
				}
				attribute state :> SysML::Systems::StateDefinition::state : VacuumGripperState;
			}
			in port cmd :> vacuumGripperCmdPortIn : VacuumGripperCmd;
			out port state :> vacuumGripperStateOut : StateEnumerationDefinition::VacuumGripperState;
			part 'vacuumGripperHardware ' : VacuumGripperHardware {
				part vertical1DActuator : '1DActuator' {
					in port actVerticalDown :> forwardActuator : Boolean;
					in port actVerticalUp :> backwardActuator : Boolean;
					out port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
				}
				part arm1DActuator : '1DActuator' {
					in port actArmOut :> forwardActuator : Boolean;
					in port actArmIn :> backwardActuator : Boolean;
					out port senseArmEndIn :> backwardLimitSwitch : Boolean;
				}
				part rotor1DActuator : '1DActuator' {
					in port actRotorLeft :> forwardActuator : Boolean;
					in port actRotorRight :> backwardActuator : Boolean;
					out port senseRotorEndRight :> backwardLimitSwitch : Boolean;
				}
				part vacuumGripperModule : VacuumModule {
					in port actCompressorOn :> Structure::'Machine Modules APIs'::VacuumModuleAPI::actCompressorOn : Boolean;
					in port actValve :> Structure::'Machine Modules APIs'::VacuumModuleAPI::actValve : Boolean;
				}
			}
		}
		part sortingLineSystem : SortingLineSystem {
			part inputLightSensor : LightSensor;
			part middleLightSensor : LightSensor;
			part whiteLightSensor : LightSensor;
			part redLightSensor : LightSensor;
			part blueLightSensor : LightSensor;
			part sortingLineAPI : SortingLineAPI;
			part conveyorBelt : ConveyorBelt;
			part blueEjector : '1DActuator';
			part redEjector : '1DActuator';
			part whiteEjector : '1DActuator';
		}
		part indexedLineSystem : IndexedLineSystem {
			part indexedLineAPI : IndexedLineAPI {
				out part conveyorSliderFeedToMillAPI : 'Machine Modules APIs'::ConveyorSliderAPI {
					attribute location :> Structure::'Machine Modules APIs'::ConveyorSliderAPI::location : IndexedLineLocation {
						doc /* Location must be Feed To Mill */
					}
					out port actMotorSliderForwardFeedToMIll :> actMotorSliderForward : Boolean;
					in port sensePushButtonFrontFeedToMill :> sensePushButtonFront : Boolean;
					out port actMotorSliderBackwardFeedToMill :> actMotorSliderBackward : Boolean;
					in port sensePushButtonBackFeedToMill :> sensePushButtonBack : Boolean;
					in port conveyorSliderFeedToMillCmd :> conveyorSliderCmdPortIn : ConveyorSliderCmd;
					out port conveyorSliderFeedToMillStateOut :> conveyorSliderStateOut : StateEnumerationDefinition::ConveyorSliderState;
				}
				part conveyorSliderDrillToEndAPI : 'Machine Modules APIs'::ConveyorSliderAPI {
					attribute location :> Structure::'Machine Modules APIs'::ConveyorSliderAPI::location : IndexedLineLocation {
						doc /* Location must be Drill To End */
					}
					out port actMotorSliderForwardDrillToEnd :> actMotorSliderForward : Boolean;
					in port sensePushButtonFrontDrillToEnd : sensePushButtonFront;
					out port actMotorSliderBackwardDrillToEnd :> actMotorSliderBackward : Boolean;
					in port sensePushButtonBackDrillToEnd :> sensePushButtonBack : Boolean;
					in port conveyorSliderDrillToEndCmd :> conveyorSliderCmdPortIn : ConveyorSliderCmd;
					out port conveyorSliderDrillToEndStateOut :> conveyorSliderStateOut : StateEnumerationDefinition::ConveyorSliderState;
				}
				part millAPI : 'Machine Modules APIs'::MillAPI {
					out port actMilling :> actMillingOut : Boolean;
					in port millCmd :> Structure::'Machine Modules APIs'::MillAPI::millCmd : MillCmd;
					out port millState :> millStateOut : StateEnumerationDefinition::MillState;
				}
				part drillAPI : 'Machine Modules APIs'::DrillAPI {
					out port actDrillingOut :> Structure::'Machine Modules APIs'::DrillAPI::actDrillingOut : Boolean;
					in port drillCmd :> Structure::'Machine Modules APIs'::DrillAPI::drillCmd : DrillCmd;
					out port drillState :> millStateOut : StateEnumerationDefinition::DrillState;
				}
				part conveyorBeltFeedAPI : 'Machine Modules APIs'::ConveyorBeltAPI {
					in port conveyorBeltFeedCmd :> conveyorBeltCmdIn : ConveyorBeltCmd;
					out port actConveyorBeltFeed :> actConveyorBelt : Boolean;
					out port conveyorBeltFeedState :> conveyorBeltStateOut : StateEnumerationDefinition::ConveyorBeltState;
					in port feedSenseConveyorStart :> senseConveyor : Boolean;
					attribute location : IndexedLineLocation;
				}
				part conveyorBeltMillingAPI : 'Machine Modules APIs'::ConveyorBeltAPI {
					in port conveyorBeltMillCmd :> conveyorBeltCmdIn : ConveyorBeltCmd;
					out port actConveyorBeltMill :> actConveyorBelt : Boolean;
					out port conveyorBeltMillState :> conveyorBeltStateOut : StateEnumerationDefinition::ConveyorBeltState;
					in port millSenseConveyorStart :> senseConveyor : Boolean;
					attribute location : IndexedLineLocation;
				}
				part conveyorBeltDrillingAPI : 'Machine Modules APIs'::ConveyorBeltAPI {
					in port conveyorBeltDrillCmd :> conveyorBeltCmdIn : ConveyorBeltCmd;
					out port actConveyorBeltDrill :> actConveyorBelt : Boolean;
					out port conveyorBeltDrillState :> conveyorBeltStateOut : StateEnumerationDefinition::ConveyorBeltState;
					in port drillSenseConveyorStart :> senseConveyor : Boolean;
					attribute location : IndexedLineLocation;
				}
				part conveyorBeltEndAPI : 'Machine Modules APIs'::ConveyorBeltAPI {
					in port conveyorBeltEndCmd :> conveyorBeltCmdIn : ConveyorBeltCmd;
					out port actConveyorBeltEnd :> actConveyorBelt : Boolean;
					out port conveyorBeltEndState :> conveyorBeltStateOut : StateEnumerationDefinition::ConveyorBeltState;
					in port endSenseConveyorEnd :> senseConveyor : Boolean;
					attribute location : IndexedLineLocation;
				}
				part indexedLineEngine {
					in port cmd : IndexedLineCmd;
					in port conveyorSliderFeedToMillState : ConveyorSliderState;
					in port conveyorSliderDrillToEndState : ConveyorSliderState;
					in port millState : MillState;
					in port conveyorBeltFeedState : ConveyorBeltState;
					in port conveyorBeltMillState : ConveyorBeltState;
					in port conveyorBeltDrillState : ConveyorBeltState;
					in port conveyorBeltEndState : ConveyorBeltState;
					in port drillState : DrillState;
					out port conveyorSliderFeedToMillCmd : ConveyorSliderCmd;
					out port conveyorSliderDrillToEndCmd : ConveyorSliderCmd;
					out port conveyorBeltFeedCmd : ConveyorBeltCmd;
					out port conveyorBeltMillCmd : ConveyorBeltCmd;
					out port conveyorBeltDrillCmd : ConveyorBeltCmd;
					out port conveyorBeltEndCmd : ConveyorBeltCmd;
					out port millCmd : MillCmd;
					out port drillCmd : DrillCmd;
					out port state : IndexedLineState;
				}
				in port cmd :> indexedLineCmd : IndexedLineCmd;
				out port state :> indexedLineState : IndexedLineState;
			}
			in port cmd :> indexedLineCmd : IndexedLineState;
			out port state :> indexedLineState : IndexedLineState;
			part indexedLineHardware : IndexedLineHardware {
				part mill : Mill {
					in port actMilling :> 'Machine Modules APIs'::MillAPI::actMillingOut : Boolean;
				}
				part conveyorSliderDrilltoEnd : ConveyorSlider {
					in port actMotorSliderDrillToEndForward :> actMotorSliderForward : Boolean;
					in port actMotorSliderDrillToEndBackward :> actMotorSliderBackward : Boolean;
					out port senseButtonPushDrillToEndForward :> senseButtonPushForward : Boolean;
					out port senseButtonPushDrillToEndBackward :> senseButtonPushBackward : Boolean;
				}
				part conveyorBeltDrill : ConveyorBelt {
					in port actConveyorBeltDrill :> actConveyorBelt : Boolean;
					out port senseConveyorDrillStart :> senseConveyorStart : Boolean;
				}
				in part conveyorBeltMill : ConveyorBelt {
					in port actConveyorBeltMill :> actConveyorBelt : Boolean;
					out port senseConveyorMillStart :> senseConveyorStart : Boolean;
				}
				part conveyorSliderFeedToMill : ConveyorSlider {
					in port actMotorSliderFeedToMillForward :> actMotorSliderForward : Boolean;
					in port actMotorSliderFeedToMillBackward :> actMotorSliderBackward : Boolean;
					out port senseButtonPushFeedToMillForward :> senseButtonPushForward : Boolean;
					out port senseButtonPushFeedToMillBackward :> senseButtonPushBackward : Boolean;
				}
				part conveyorBeltFeed : ConveyorBelt {
					in port actConveyorBeltFeed :> actConveyorBelt : Boolean;
					out port senseConveyorFeedStart :> senseConveyorStart : Boolean;
				}
				part drill : Drill {
					in port actDrill :> actDrillOut : Boolean;
				}
				part conveyorBeltEnd : ConveyorBelt {
					in port actConveyorBeltEnd :> actConveyorBelt : Boolean;
					out port senseConveyorEndStart :> senseConveyorStart : Boolean;
				}
			}
		}
		part clawGripperSystem : ClawGripperSystem {
			part clawGripperAPI : ClawGripperAPI {
				in port clawGripperCmdPortIn :> cmd : ClawGripperCmd;
				out port clawGripperStateOut :> ClawGripperAPI::clawGripperStateOut : StateEnumerationDefinition::ClawGripperState;
				part '1DActuatorRotorAPI' : 'Machine Modules APIs'::'1DActuatorAPI' {
					out port actRotLeft :> forwardActuator : Boolean;
					out port actRotRight :> backwardActuator : Boolean;
					in port senseRotEndRight :> backwardLimitSwitch : Boolean;
					action rotateLeft :> moveForward;
					action rotateRight :> moveBackward;
					attribute limitLeft :> forwardLimit : Integer;
					in port rotorCmdIn :> cmd : RotorCmd;
					out port rotorStateOut :> '1DActuatorStateOut' : StateEnumerationDefinition::RotorState;
					attribute sensorRightLimitSwitch :> sensorBackwardLimitSwitch : Boolean;
					attribute state :> SysML::Systems::StateDefinition::state : RotorState;
					attribute rightLimit :> backwardLimit : Integer;
					attribute leftLimit :> forwardLimit : Integer;
					attribute actuatorRight :> actuatorBackward : Boolean;
					attribute actuatorLeft :> actuatorForward : Boolean;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : RotorCmd;
				}
				part '1DActuatorVerticalAPI' : 'Machine Modules APIs'::'1DActuatorAPI' {
					out port actVerticalDown :> backwardActuator : Boolean;
					attribute limitDown :> forwardLimit : Integer;
					out port actVerticalUp :> forwardActuator : Boolean;
					in port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
					action moveUp :> moveForward;
					action moveDown :> moveBackward;
					in port verticalCmdIn :> cmd : VerticalCmd;
					out port verticalStateOut :> '1DActuatorStateOut' : StateEnumerationDefinition::VerticalState;
					attribute state :> SysML::Systems::StateDefinition::state : VerticalState;
					attribute limitUp :> backwardLimit : Integer;
					attribute sensorLimitUpSwitch :> sensorBackwardLimitSwitch : Boolean;
					attribute actuatorUp :> actuatorBackward : Boolean;
					attribute actuatorDown :> actuatorForward : Boolean;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : VerticalCmd;
				}
				part impulseActuatorArmAPI : 'Machine Modules APIs'::ImpulseActuatorAPI {
					out port actArmOut :> forwardActuator : Boolean;
					out port actArmIn :> backwardActuator : Boolean;
					in port senseArmEndIn :> backwardLimitSwitch : Boolean;
					attribute impulseCounterArm :> impulseCounter;
					attribute limitOut :> impulseForwardLimit : Boolean;
					action armOut :> moveForward;
					action armIn :> moveBackwardForReset;
					out port armStateOut :> impulseActuatorStateOut : ImpulseArmState;
					in port armCmdIn :> cmd : ArmImpulseCmd;
					attribute armOut :> actuatorForward : Boolean;
					attribute armIn :> actuatorBackward : Boolean;
					attribute senseorArmEnd :> sensorBackwardLimitSwitch : Boolean;
					attribute state :> SysML::Systems::StateDefinition::state : ArmState;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : ArmCmd;
				}
				part clawGripperEngine {
					in port 'clawGripperCmdPortIn ' : ClawGripperCmd;
					in port 'rotorStateIn : RotorState';
					in port verticalStateIn : StateEnumerationDefinition::VerticalState;
					in port armStateIn : ImpulseArmState;
					in port clawStateIn : ImpulseClawState;
					out port 'clawGripperStateOut ' : StateEnumerationDefinition::ClawGripperState;
					out port verticalCmdOut : VerticalCmd;
					out port rotorCmdOut : RotorCmd;
					out port armCmdOut : ArmImpulseCmd;
					out port clawCmdOut : ClawImpulseCmd;
				}
				part impulseActuatorClawAPI : 'Machine Modules APIs'::ImpulseActuatorAPI {
					out port actClawClose :> forwardActuator : Boolean;
					out port actClawOpen :> backwardActuator : Boolean;
					in port senseClawOpen :> backwardLimitSwitch : Boolean;
					attribute impulseCounterClaw :> impulseCounter : Integer;
					attribute limitOut :> impulseForwardLimit : Boolean;
					action closeClaw :> moveForward;
					action openClaw :> moveBackwardForReset;
					in port clawCmdIn :> cmd : ClawImpulseCmd;
					out port clawStateOut :> impulseActuatorStateOut : ImpulseClawState;
					attribute closeClaw :> actuatorForward : Boolean;
					attribute openClaw :> actuatorBackward : Boolean;
					attribute state :> SysML::Systems::StateDefinition::state : ClawImpulseState;
					attribute currentCmd :> 'Machine Modules APIs'::'1DActuatorAPI'::currentCmd : ClawImpulseCmd;
					attribute sensorClawOpen :> sensorBackwardLimitSwitch : Boolean;
				}
			}
			in port cmd :> clawGripperCmdPortIn : ClawGripperCmd;
			out port state :> clawGripperStateOut : StateEnumerationDefinition::ClawGripperState;
			part clawGripperHardware : ClawGripperHardware {
				part armImpulseActuator : ImpulseActuator {
					in port actArmOut :> forwardActuator : Boolean;
					in port actArmIn :> backwardActuator : Boolean;
					out port senseArmEndIn :> backwardLimitSwitch : Boolean;
				}
				part vertical1DActuator : '1DActuator' {
					in port actVerticalDown :> backwardActuator : Boolean;
					in port actVerticalUp :> forwardActuator : Boolean;
					out port senseVerticalEndUp :> backwardLimitSwitch : Boolean;
				}
				part clawImpulseActuator : ImpulseActuator {
					in port actClawClose :> forwardActuator : Boolean;
					in port actClawOpen :> backwardActuator : Boolean;
					out port senseClawOpen :> backwardLimitSwitch : Boolean;
				}
				part rotor1DActuator : '1DActuator' {
					in port actRotorLeft :> forwardActuator : Boolean;
					in port actRotorRight :> backwardActuator : Boolean;
					out port senseRotorEndRight :> backwardLimitSwitch : Boolean;
				}
			}
		}
		attribute def Ineger;
		attribute def Itneger;
	}
	package Machines {
		part def ClawGripperHardware :> Hardware;
		part def VacuumGripperHardware :> Hardware;
		part def IndexedLineHardware :> Hardware;
		part def SortingLineHardware :> Hardware;
	}
	part def Hardware;
	part def Subsystem :> System {
		part subsystems : System [0..*];
	}
}