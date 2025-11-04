private import ScalarValues::*;

package Warehouse {
    // Port Definitions
    private import Warehouse::Software::Definitions::SignalDefinitions::*;
    package Software {
        package Definitions {
            //Signal Definition 
            private import PortDefinitions::*;
            private import SignalDefinitions::*;
            private import Warehouse::Software::Definitions::*;
            part def WarehouseAPISystem {
                part cantileverMotorAPI : MotorAPIForwardBackwardReferenceSwitch;

                part horizontalImpulseCounter : Decoder;
                part horizontalMotorAPI : MotorAPIBackwardReferenceSwitch;

                part verticalImpulseCounter : Decoder;
                part verticalMotorAPI : MotorAPIBackwardReferenceSwitch;

                part conveyorMotorAPI : ConveyorBelt;
                part warehouseEngineAPI : WarehouseEngine;

                in port warehouseCmdAPICmdPort : APICmdPort;

                //port binding for decoder
                bind verticalImpulseCounter.decoderCount = verticalMotorAPI.decoderCount;
                bind verticalMotorAPI.decoderCount = warehouseEngineAPI.verticalDecoderCount; //additional binding since we have the engine that is responsible for resetting
                bind horizontalImpulseCounter.decoderCount = horizontalMotorAPI.decoderCount;
                bind horizontalMotorAPI.decoderCount = warehouseEngineAPI.horizontalDecoderCount; //additional binding since we have the engine that is responsible for resetting

                //Software internal binding between individual software components and engine
                bind cantileverMotorAPI.cmdPort = warehouseEngineAPI.CantileverCmdMsgPort;
                bind horizontalMotorAPI.cmdPort = warehouseEngineAPI.HorizontalCmdMsgPort;
                bind verticalMotorAPI.cmdPort = warehouseEngineAPI.VerticalCmdMsgPort;
                bind conveyorMotorAPI.cmdPort = warehouseEngineAPI.ConveyorCmdMsgPort;
                bind warehouseEngineAPI.cmdPort = warehouseCmdAPICmdPort;
                bind warehouseEngineAPI.cantileverForwardLimitSwitchPort = cantileverMotorAPI.forwardReferenceSwitch;
                bind warehouseEngineAPI.cantileverBackwardLimitSwitchPort
                    = cantileverMotorAPI.backwardReferenceSwitch;
                bind warehouseEngineAPI.horizontalBottomLimitSwitchPort = horizontalMotorAPI.backwardReferenceSwitch;
                bind warehouseEngineAPI.verticalBottomLimitSwitchPort = verticalMotorAPI.backwardReferenceSwitch;
            }
            part def MotorAPIForwardBackwardReferenceSwitch {
                doc
                /*
                 * This MotorAPI is used for motors that have a backward reference switch and a forward limit switch.
                 */
                in port cmdPort : MotorCmdPort;
                in port forwardReferenceSwitch : SensorPort;
                in port backwardReferenceSwitch : SensorPort;
                out port motorForwardPort : ActuatorPort;
                out port motorBackwardPort : ActuatorPort;
                attribute TOLERANCE_CONFIG : Integer := 20 {
                    doc
                    /*
                     * This attribute indicates the tolerance of the motor, it is used to calculate the position of the ForkLift when moving to a target position
                     */
                }
                state motorStates : MotorStates {
                    in selfMotor = MotorAPIForwardBackwardReferenceSwitch::self;
                }
                state def MotorStates {
                    in selfMotor : MotorAPIForwardBackwardReferenceSwitch;
                    entry;
                    then idle;
                    state idle {
                        entry action stopMotor {
                            send false via selfMotor.motorForwardPort.value;
                            send false via selfMotor.motorBackwardPort.value;
                        }
                    }
                    state backward_until_ref {}
                    state forward_until_target_pos {}

                    transition idle_to_backward_until_ref
                        first idle
                        accept msg : GoBackwardTillReferenceCmdMsg via selfMotor.cmdPort.cmdMsg
                        do action move {
                            send false via selfMotor.motorForwardPort.value;
                            send true via selfMotor.motorBackwardPort.value;
                        }
                        then backward_until_ref;
                    transition backward_until_ref_to_idle
                        first backward_until_ref accept when selfMotor.backwardReferenceSwitch.value == true then idle;

                    transition backward_until_ref_to_idle_on_stop
                        first backward_until_ref accept msg : StopMotorCmdMsg via selfMotor.cmdPort.cmdMsg then idle;
                    
                    transition idle_to_forward_until_ref
                        first idle
                        accept msg : GoForwardTillReferenceCmdMsg via selfMotor.cmdPort.cmdMsg
                        do action move {
                            send true via selfMotor.motorForwardPort.value;
                            send false via selfMotor.motorBackwardPort.value;
                        }
                        then forward_until_target_pos;
                    transition forward_until_ref_to_idle
                        first forward_until_target_pos accept when selfMotor.forwardReferenceSwitch.value == true then idle;
                    transition forward_until_ref_to_idle_on_stop
                        first forward_until_target_pos accept msg : StopMotorCmdMsg via selfMotor.cmdPort.cmdMsg then idle;
                }
            }

            part def MotorAPIBackwardReferenceSwitch {
                doc
                /*
                 * This MotorAPI is used for motors that have a backward reference .
                 */
                in port decoderCount : CounterPort;
                in port cmdPort : MotorCmdPort;
                in port backwardReferenceSwitch : SensorPort;

                out port motorForwardPort : ActuatorPort;
                out port motorBackwardPort : ActuatorPort;

                attribute TOLERANCE_CONFIG : Integer := 20 {
                    doc
                    /*
                     * This attribute indicates the tolerance of the motor, it is used to calculate the position of the ForkLift when moving to a target position
                     */
                }
                attribute currentTargetDecoderValue : Positive {}
                state motorStatesAPIBackwardReferenceSwitch : MotorStates {
                    in selfMotor = MotorAPIBackwardReferenceSwitch::self;
                }
                state def MotorStates {
                    in selfMotor : MotorAPIBackwardReferenceSwitch;
                    entry;
                    then idle;
                    state idle {
                        entry action stopMotor {
                            send false via selfMotor.motorForwardPort.value;
                            send false via selfMotor.motorBackwardPort.value;
                        }
                    }
                    state setup{}
                    state backward_until_ref {}
                    state forward_until_target_pos {}
                    state backward_until_target_pos {}

                    transition idle_to_setup
                        first idle
                        accept msg : SetupCmdMsg via selfMotor.cmdPort.cmdMsg
                        do action setupMotor {
                            send false via selfMotor.motorForwardPort.value;
                            send true via selfMotor.motorBackwardPort.value;
                        }
                        then backward_until_ref;
                    
                    transition idle_to_backward_until_ref
                        first idle
                        accept msg : GoBackwardTillReferenceCmdMsg via selfMotor.cmdPort.cmdMsg
                        do action move {
                            send false via selfMotor.motorForwardPort.value;
                            send true via selfMotor.motorBackwardPort.value;
                        }
                        then backward_until_ref;
                    transition idle_to_forward_until_target_pos
                        first idle
                        accept msg : GoToPosMotorCmdMsg via selfMotor.cmdPort.cmdMsg
                        if selfMotor.decoderCount.value < msg.targetPos - selfMotor.TOLERANCE_CONFIG
                            do action goForward {
                                send true via selfMotor.motorForwardPort.value;
                                send false via selfMotor.motorBackwardPort.value;
                            }
                        then forward_until_target_pos;
                    transition idle_to_backward_until_target_pos
                        first idle
                        accept msg : GoToPosMotorCmdMsg via selfMotor.cmdPort.cmdMsg
                        if selfMotor.decoderCount.value > msg.targetPos + selfMotor.TOLERANCE_CONFIG
                            do action goBackward {
                                send false via selfMotor.motorForwardPort.value;
                                send true via selfMotor.motorBackwardPort.value;
                            }
                        then backward_until_target_pos;

                    transition backward_until_ref_to_idle
                        first backward_until_ref accept when selfMotor.backwardReferenceSwitch.value == true then idle;

                    transition backward_until_ref_to_idle_on_stop
                        first backward_until_ref accept msg : StopMotorCmdMsg via selfMotor.cmdPort.cmdMsg then idle;
                    
    
                    transition forward_until_target_pos_to_idle
                        first forward_until_target_pos
                        if (
                            selfMotor.decoderCount.value <
                            selfMotor.currentTargetDecoderValue - selfMotor.TOLERANCE_CONFIG and
                            selfMotor.decoderCount.value >
                            selfMotor.currentTargetDecoderValue + selfMotor.TOLERANCE_CONFIG
                        )
                            then idle;
                    transition move_until_target_pos_to_idle_on_stop
                        first forward_until_target_pos
                        accept msg : StopMotorCmdMsg via selfMotor.cmdPort.cmdMsg
                        then idle;
                    transition backward_until_target_pos_to_idle
                        first backward_until_target_pos
                        if (
                            selfMotor.decoderCount.value <
                            selfMotor.currentTargetDecoderValue - selfMotor.TOLERANCE_CONFIG and
                            selfMotor.decoderCount.value >
                            selfMotor.currentTargetDecoderValue + selfMotor.TOLERANCE_CONFIG
                        )
                            then idle;
                    transition backward_until_target_pos_to_idle_on_stop
                        first backward_until_target_pos
                        accept msg : StopMotorCmdMsg via selfMotor.cmdPort.cmdMsg
                        then idle;
                    
                    transition forward_until_target_pos_to_backward_until_ref
                        first forward_until_target_pos
                        accept msg : GoBackwardTillReferenceCmdMsg via selfMotor.cmdPort.cmdMsg
                        then backward_until_ref; 
                    transition backward_until_target_pos_to_backward_until_ref
                        first backward_until_target_pos
                        accept msg : GoBackwardTillReferenceCmdMsg via selfMotor.cmdPort.cmdMsg
                        then backward_until_ref;
                    transition backward_until_target_pos_to_forward_until_target_pos
                        first backward_until_target_pos
                        accept msg : GoToPosMotorCmdMsg via selfMotor.cmdPort.cmdMsg
                            if selfMotor.decoderCount.value < msg.targetPos - selfMotor.TOLERANCE_CONFIG
                            do action goForward {
                                send true via selfMotor.motorForwardPort.value;
                                send false via selfMotor.motorBackwardPort.value;
                            }
                        then forward_until_target_pos;
                    transition forward_until_target_pos_to_backward_until_target_pos
                        first forward_until_target_pos
                        accept msg : GoToPosMotorCmdMsg via selfMotor.cmdPort.cmdMsg
                            if selfMotor.decoderCount.value > msg.targetPos + selfMotor.TOLERANCE_CONFIG
                            do action goBackward {
                                send false via selfMotor.motorForwardPort.value;
                                send true via selfMotor.motorBackwardPort.value;
                            }
                        then backward_until_target_pos;
                }
            }
            part def ConveyorBelt {
                in port cmdPort : ConveyorCmdPort;
                out port motorForwardPort : ActuatorPort;
                out port motorBackwardPort : ActuatorPort;
            }
            comment about Decoder
            /*
             * This is the QuadratureDecoder from Didier, which I used since he already modeled it to ensure compatibility.
             * Further the transitions are also causing the following MontiCore parsing error:
             * [ERROR] SmallWarehouseSystem.sysml:<187,227>: no viable alternative at input 'Software::' in rule stack: [SysMLModel, SysMLElement, SysMLPackage, SysMLElement, SysMLPackage, SysMLElement, SysMLType, PartDef, SysMLElement, SysMLType, StateDef, SysMLElement, SysMLTransition, InlineActionUsage]
             * if Software::SmallWarehouseAPI::Decoder::DecoderCounterStates::selfDecoder.signalA.value == true and Software::SmallWarehouseAPI::Decoder::DecoderCounterStates::selfDecoder.signalB.value == false do Software::SmallWarehouseAPI::Decoder::DecoderCounterStates::selfDecoder.increment
             */
            part def Decoder {
                in port signalA : SensorPort;
                in port signalB : SensorPort;

                out port decoderCount : CounterPort;

                action reset : Reset {
                    in selfDecoder = Decoder::self;
                }
                action def Reset {
                    in selfDecoder : Decoder;
                    action assign selfDecoder.decoderCount := 0;
                }
                action increment {
                    decoderCount := decoderCount + 1;
                }
                action decrement {
                    decoderCount := decoderCount - 1;
                }

                state decoderStates : DecoderCounterStates {
                    in selfDecoder = Decoder::self;
                }
                state def DecoderCounterStates {
                    in selfDecoder : Decoder;
                    entry;
                    then dd;
                    state uu {
                        doc
                        /*
                         * signalA is Up and signalB is Up
                         */
                    }
                    state ud {
                        doc
                        /*
                         * signalA is Up and signalB is Down
                         */
                    }
                    state du {
                        doc
                        /*
                         * signalA is Down	and signalB is Up
                         */
                    }
                    state dd {
                        doc
                        /*
                         * signalA is Down	and signalB is Down
                         */
                    }
                    transition dd_To_ud
                        first dd
                        if selfDecoder.signalA.value == true and selfDecoder.signalB.value == false
                            do selfDecoder.increment
                        then ud;
                    transition ud_To_dd
                        first ud
                        if selfDecoder.signalA.value == false and selfDecoder.signalB.value == false
                            do selfDecoder.decrement
                        then dd;

                    transition dd_To_du
                        first dd
                        if selfDecoder.signalA.value == false and selfDecoder.signalB.value == true
                            do selfDecoder.decrement
                        then du;
                    transition ud_To_uu
                        first ud
                        if selfDecoder.signalA.value == true and selfDecoder.signalB.value == true
                            do selfDecoder.increment
                        then uu;
                    transition du_To_dd
                        first du
                        if selfDecoder.signalA.value == false and selfDecoder.signalB.value == false
                            do selfDecoder.increment
                        then dd;
                    transition uu_To_ud
                        first uu
                        if selfDecoder.signalA.value == true and selfDecoder.signalB.value == false
                            do selfDecoder.decrement
                        then ud;
                    transition uu_To_du
                        first uu
                        if selfDecoder.signalA.value == false and selfDecoder.signalB.value == true
                            do selfDecoder.increment
                        then du;
                }
            }
            part def WarehouseEngine {
                in port cmdPort : APICmdPort;
                in port cantileverForwardLimitSwitchPort : SensorPort;
                in port cantileverBackwardLimitSwitchPort : SensorPort;
                in port horizontalBottomLimitSwitchPort : SensorPort;
                in port verticalBottomLimitSwitchPort : SensorPort;
                //We added these ports to be able to reset the decoders from the engine, since we have a state machine that is responsible for the actions of the ForkLift
                in port horizontalDecoderCount : CounterPort;
                in port verticalDecoderCount : CounterPort;

                in port lowerTrailSensor : SensorPort;
                in port upperTrailSensor : SensorPort;

                in port lightBarrierInside : SensorPort;
                in port lightBarrierOutside : SensorPort;

                out port CantileverCmdMsgPort : MotorCmdPort;
                out port HorizontalCmdMsgPort : MotorCmdPort;
                out port VerticalCmdMsgPort : MotorCmdPort;
                out port ConveyorCmdMsgPort : ConveyorCmdPort;
                //this port is currently not connected but can be understood as the interface of the engine which provides current error messages to other components
                out port errorPort : ErrorMsgPort;

                attribute currentForkLiftPosition : WarehouseRelativePosition {
                    doc
                    /*
                     * This attribute represents the current position of the ForkLift
                     */
                }
                attribute inputPosition : InputPositionAttribute {
                    doc
                    /*
                     * This attribute represents the input location of the Warehouse
                     */
                }
                <#list activeStorageUnits as a>
                    <#include "attribute.ftl">
                </#list>
                // attribute {container.container_name} : ContainerAttribute {}
                attribute c1 : ContainerAttribute{}
                
                attribute c2 : ContainerAttribute{}
               
                attribute c3 : ContainerAttribute{}
               
                attribute c4 : ContainerAttribute{}
                
                attribute c5 : ContainerAttribute{}
                
                attribute c6 : ContainerAttribute{}
                
                attribute c7 : ContainerAttribute{}
                
                attribute c8 : ContainerAttribute{}
                
                attribute c9 : ContainerAttribute{}
                attribute currentTargetPosition : WarehouseRelativePosition {
                    doc
                    /*
                     * This attribute represents the target position of the ForkLift
                     */
                }
                attribute currentCmdMsg {
                    attribute targetPosition : WarehouseRelativePosition {} 
                    attribute operationType : OperationType; 
                    attribute targetStorageUnit : UnitName {}
                    attribute unitId : Integer; 
                }
                attribute cantileverArmOperation : ArmOperationAttribute {}
                attribute horizontalArmOperation : ArmOperationAttribute {}
                attribute verticalArmOperation : ArmOperationAttribute {}

                attribute actionCommandDone : Boolean := false {
                    doc
                    /*
                     * This attribute indicates if the action command is done, can be used to know if a composite action is finished
                     */
                }
                attribute STORING_Y_OFFSET_CONFIG : Natural := 0 {
                    doc
                    /*
                     * NEEDS TO BE CONFIGURATED. This attribute indicates the Y offset of the storing position, it is used to calculate the position of the ForkLift when storing an object
                     */
                }
                attribute PICKUP_Y_OFFSET_CONFIG : Natural := 0 {
                    doc
                    /*
                     * NEEDS TO BE CONFIGURATED. This attribute indicates the Y offset of the pickup position, it is used to calculate the position of the ForkLift when picking up an object
                     */
                }
                attribute decoderIsInitialized : Boolean := false {
                    doc
                    /*
                     * This attribute indicates if the horizontal and vertical Decoders have been initialized, it is used to know if the ForkLift is ready to move
                     */
                }
                attribute initalPositionWarehouseCONFIG : WarehouseRelativePosition := WarehouseRelativePosition(3,3,-1);
                state smallWarehouseEngineStates : SmallWarehouseEngineStates {
                    in selfEngine = WarehouseEngine::self;
                }
                // State Machine of the Warehouse Engine that will only consider the three left Storage Units, cause otherwise the numbe of states/transitions would explode (9^2 States).
                state def SmallWarehouseEngineStates {
                    in selfEngine : WarehouseEngine;
                    entry;
                    then setup;

                    state setup {
                        entry setup {
                            perform selfEngine.initialize;
                            perform selfEngine.setupMotor;
                            assign selfEngine.cantileverArmOperation.operationType := OperationType::SETUP;
                            assign selfEngine.horizontalArmOperation.operationType := OperationType::SETUP;
                            assign selfEngine.verticalArmOperation.operationType := OperationType::SETUP;
                            assign selfEngine.cantileverArmOperation.relativePosition := WarehouseRelativePosition(0, 0, -1); //TODO: Just Example values
                            assign selfEngine.horizontalArmOperation.relativePosition := WarehouseRelativePosition(0, 3, -1); //TODO: Just Example values
                            assign selfEngine.verticalArmOperation.relativePosition := WarehouseRelativePosition(3, 3, -1); //TODO: Just Example values
                            assign selfEngine.cantileverArmOperation.triggerSource := "sensor";
                            assign selfEngine.horizontalArmOperation.triggerSource := "sensor";
                            assign selfEngine.verticalArmOperation.triggerSource := "sensor";
                        }
                        
                    }
                    state cmdReceived {
                        entry selfEngine.assignCurrentCmdValue;
                    }
                    state ErrorRetrieveEmptyPallet {
                        entry action handleError {
                            attribute errorMsg : String :=
                                "Error: Cant retrieve empty pallet from the targeted position, since an item is stored in the given position!";
                            send errorMsg via selfEngine.errorPort;
                        }
                    }
                    state ErrorStore {
                        entry action handleError {
                            attribute errorMsg : String :=
                                "Error: Cant store item in the targeted position, since an item is already stored in the given position!";
                            send errorMsg via selfEngine.errorPort;
                        }
                    }
                    state ErrorRetrieveItem {
                        entry action handleError {
                            attribute errorMsg : String :=
                                "Error: Cant retrieve item from the targeted position, since no item is stored in the given position!";
                            send errorMsg via selfEngine.errorPort;
                        }
                    }
                    <#list fixedSetupTransitions as t>
                        <#include "transition.ftl">
                    </#list>


                    //composite state to handle the retrieving of an empty pallet
                    state retrieveEmptyPallet {
                        entry action assign selfEngine.actionCommandDone := false;
                        then moveToStoragePositionForPickupState;
                        state moveToStoragePositionForPickupState {
                            entry selfEngine.moveToStoragePositionForPickup;
                        }
                        state goToForwardLimitForPickup {
                            entry selfEngine.moveCantileverForward;
                        }
                        state pickup {
                            entry selfEngine.pickup;
                        }
                        state moveToInputPositionForDropdownState {
                            entry selfEngine.moveToInputPositionForDropoff;
                        }
                        state dropoff {
                            entry selfEngine.dropoff;
                        }
                        state reset {
                            entry action resetting {
                                perform selfEngine.moveCantileverBack;
                                perform selfEngine.changeOccupiedOfCurrentTargetLocation;
                                perform selfEngine.resetCurrentCmdValue;
                                assign selfEngine.actionCommandDone := true;
                            }
                        }

                        comment about moveToStoragePositionForPickupState_To_pickup
                        /*
                         * This transition causes an MontiCore parsing error: 
                         * [ERROR] SmallWarehouseSystem.sysml:<201,139>: 0xA0241 No SymTypeExpression could be derived for the FieldAccessExpression Software.SmallWarehouseAPI.SmallWarehouseEngine.SmallWarehouseEngineStates.selfEngine.horizontalDecoderCount.value
                         */
                        //internal transitions of the retrieve empty pallet
                        transition moveToStoragePositionForPickupState_To_pickup
                            first moveToStoragePositionForPickupState
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then pickup;
                        transition pickup_to_moveToInputPositionForDropdownState
                            first pickup
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverForwardLimitSwitchPort.value == true
                            )
                                then moveToInputPositionForDropdownState;
                        transition moveToInputPositionForDropdownState_to_dropoff
                            first moveToInputPositionForDropdownState
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then dropoff;
                        transition dropoff_to_reset
                            first dropoff
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then reset;
                    }
                    //composite state to handle the storing of an item
                    state store {
                        entry action assign selfEngine.actionCommandDone := false;
                        then moveToInputPositionForPickupState;
                        state moveToInputPositionForPickupState {
                            entry selfEngine.moveToInputPositionForPickup;
                        }
                        state pickup {
                            entry selfEngine.pickup;
                        }
                        state moveToStoragePositionForDropdownState {
                            entry selfEngine.moveToStoragePositionForDropoff;
                        }
                        state dropoff {
                            entry selfEngine.dropoff;
                        }
                        state reset {
                            entry action resetting {
                                perform  selfEngine.moveCantileverBack;
                                perform  selfEngine.changeOccupiedOfCurrentTargetLocation;
                                perform  selfEngine.resetCurrentCmdValue;
                                assign selfEngine.actionCommandDone := true;
                            }
                        }
                        //internal transitions of the store state
                        transition moveToInputPositionForPickupState_To_pickup
                            first moveToInputPositionForPickupState
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then pickup;
                        transition pickup_to_moveToStoragePositionForDropdownState
                            first pickup
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverForwardLimitSwitchPort.value == true
                            )
                                then moveToStoragePositionForDropdownState;
                        transition moveToStoragePositionForDropdownState_to_dropoff
                            first moveToStoragePositionForDropdownState
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then dropoff;
                        transition dropoff_to_reset
                            first dropoff
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverForwardLimitSwitchPort.value == true
                            )
                                then reset;
                    }
                    //composite state to handle the retrieving of an item
                    state retrieveItem {
                        entry action assign selfEngine.actionCommandDone := false;
                        then moveToStoragePositionForPickupState;
                        state moveToStoragePositionForPickupState {
                            entry selfEngine.moveToStoragePositionForPickup;
                        }
                        state pickup {
                            entry selfEngine.pickup;
                        }
                        state moveToInputPositionForDropdownState {
                            entry selfEngine.moveToInputPositionForDropoff;
                        }
                        state dropoff {
                            entry selfEngine.dropoff;
                        }
                        state reset {
                            entry action resetting {
                                perform action moveCantileverBack;
                                perform action changeOccupiedOfCurrentTargetLocation;
                                perform action resetCurrentCmdValue;
                                assign selfEngine.actionCommandDone := true;
                            }
                        }
                        //internal transitions of the retrieve item state
                        transition moveToStoragePositionForPickupState_To_pickup
                            first moveToStoragePositionForPickupState
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then pickup;
                        transition pickup_to_moveToInputPositionForDropdownState
                            first pickup
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverForwardLimitSwitchPort.value == true
                            )
                                then moveToInputPositionForDropdownState;
                        transition moveToInputPositionForDropdownState_to_dropoff
                            first moveToInputPositionForDropdownState
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverBackwardLimitSwitchPort.value == true
                            )
                                then dropoff;
                        transition dropoff_to_reset
                            first dropoff
                            if (
                                selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x and
                                selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y and
                                selfEngine.cantileverForwardLimitSwitchPort.value == true
                            )
                                then reset;
                    }
                    //All transitions from states to cmdReceived

                    <#list relevantTransitionsToCmdReceived as t>
                        <#include "transition.ftl">
                    </#list>

                    //TODO: hier einfügen

                    <#list relevantTransitionsFixedGuardToStates as t>
                        <#include "transition.ftl">
                    </#list>

                    <#list relevantStates as s>
                        <#include "state.ftl">
                    </#list>
                }
                               perform action assignCurrentCmdValue {
                    if cmdPort.cmdMsg.cmdMsgType == OperationType::RETRIEVE_EMPTY_PALLET {
                        //Doesnt work for some reason havent figured out why yet, similiar case worked out in my WarehouseParse.sysml (line 833 e.g.)
                        //Probably caused by: The Package structure
                        //MontiCore Parsing Error: [ERROR] SmallWarehouseSystem.sysml:<178,107>: 0xA0240 Cannot find symbol WarehouseCmdMsgType
                        assign WarehouseEngine::currentCmdMsg.operationType :=
                            OperationType::RETRIEVE_EMPTY_PALLET;
                    }
                    if cmdPort.cmdMsg.cmdMsgType == OperationType::RETRIEVE {
                        assign currentCmdMsg.operationType := SignalDefinitions::OperationType::RETRIEVE;
                    }
                    if cmdPort.cmdMsg.cmdMsgType == OperationType::STORE {
                        assign currentCmdMsg.operationType := OperationType::STORE;
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P1 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P1;
                        assign currentCmdMsg.unitId := 0;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p1.relativePosition.X_CONFIG,
                            // y = p1.relativePosition.Y_CONFIG,
                            // z = p1.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P2 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P2;
                        assign currentCmdMsg.unitId := 1;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p2.relativePosition.X_CONFIG,
                            // y = p2.relativePosition.Y_CONFIG,
                            // z = p2.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P3 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P3;
                        assign currentCmdMsg.unitId := 2;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p3.relativePosition.X_CONFIG,
                            // y = p3.relativePosition.Y_CONFIG,
                            // z = p3.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P4 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P4;
                        assign currentCmdMsg.unitId := 3;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p4.relativePosition.X_CONFIG,
                            // y = p4.relativePosition.Y_CONFIG,
                            // z = p4.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P5 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P5;
                        assign currentCmdMsg.unitId := 4;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p5.relativePosition.X_CONFIG,
                            // y = p5.relativePosition.Y_CONFIG,
                            // z = p5.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P6 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P6;
                        assign currentCmdMsg.unitId := 5;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p6.relativePosition.X_CONFIG,
                            // y = p6.relativePosition.Y_CONFIG,
                            // z = p6.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P7 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P7;
                        assign currentCmdMsg.unitId := 6;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p7.relativePosition.X_CONFIG,
                            // y = p7.relativePosition.Y_CONFIG,
                            // z = p7.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P8 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P8;
                        assign currentCmdMsg.unitId := 7;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p8.relativePosition.X_CONFIG,
                            // y = p8.relativePosition.Y_CONFIG,
                            // z = p8.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::P9 {
                        assign currentCmdMsg.targetStorageUnit := UnitName::P9;
                        assign currentCmdMsg.unitId := 8;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            // x = p9.relativePosition.X_CONFIG,
                            // y = p9.relativePosition.Y_CONFIG,
                            // z = p9.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == UnitName::INPUT {
                        assign currentCmdMsg.targetStorageUnit := UnitName::INPUT;
                        assign currentCmdMsg.unitId := 9;
                        assign currentCmdMsg.targetPosition := WarehouseRelativePosition(
                            x = inputPosition.relativePosition.X_CONFIG,
                            y = inputPosition.relativePosition.Y_CONFIG,
                            z = inputPosition.relativePosition.Z_CONFIG
                        );
                    }
                }
                perform action resetCurrentCmdValue {
                    assign WarehouseEngine::currentCmdMsg.operationType := null;
                    assign WarehouseEngine::currentCmdMsg.targetPosition := null;
                    assign WarehouseEngine::currentCmdMsg.targetStorageUnit := null;
                }
                perform action setCantileverArmOperation{
                    if currentCmdMsg.operationType == OperationType::STORE {
                         assign WarehouseEngine::cantileverArmOperation.operationType := OperationType::STORE;
                        assign WarehouseEngine::cantileverArmOperation.containerId := currentCmdMsg.unitId;
                        assign WarehouseEngine::cantileverArmOperation.triggerSource := "sensor";
                        assign WarehouseEngine::cantileverArmOperation.relativePosition := currentCmdMsg.targetPosition; //simplified
                    }
                   if currentCmdMsg.operationType == OperationType::RETRIEVE {
                        assign WarehouseEngine::cantileverArmOperation.operationType := OperationType::RETRIEVE;
                        assign WarehouseEngine::cantileverArmOperation.containerId := currentCmdMsg.unitId;
                        assign WarehouseEngine::cantileverArmOperation.triggerSource := "sensor";
                        assign WarehouseEngine::cantileverArmOperation.relativePosition := currentCmdMsg.targetPosition; //simplified
                    }
                }
                perform action setHorizontalArmOperation{
                    if currentCmdMsg.operationType == OperationType::STORE {
                        assign WarehouseEngine::horizontalArmOperation.operationType := OperationType::STORE;
                        assign WarehouseEngine::horizontalArmOperation.containerId := currentCmdMsg.unitId;
                        assign WarehouseEngine::horizontalArmOperation.triggerSource := "sensor";
                        assign WarehouseEngine::horizontalArmOperation.relativePosition := currentCmdMsg.targetPosition; //simplified
                    }
                    if currentCmdMsg.operationType == OperationType::RETRIEVE {
                        assign WarehouseEngine::horizontalArmOperation.operationType := OperationType::RETRIEVE;
                        assign WarehouseEngine::horizontalArmOperation.containerId := currentCmdMsg.unitId;
                        assign WarehouseEngine::horizontalArmOperation.triggerSource := "sensor";
                        assign WarehouseEngine::horizontalArmOperation.relativePosition := currentCmdMsg.targetPosition; //simplified
                    }
                }
                perform action setVerticalArmOperation{
                    if currentCmdMsg.operationType == OperationType::STORE {
                        assign WarehouseEngine::verticalArmOperation.operationType := OperationType::STORE;
                        assign WarehouseEngine::verticalArmOperation.containerId := currentCmdMsg.unitId;
                        assign WarehouseEngine::verticalArmOperation.triggerSource := "sensor";
                        assign WarehouseEngine::verticalArmOperation.relativePosition := currentCmdMsg.targetPosition; //simplified
                    }
                    if currentCmdMsg.operationType == OperationType::RETRIEVE {
                        assign WarehouseEngine::verticalArmOperation.operationType := OperationType::RETRIEVE;
                        assign WarehouseEngine::verticalArmOperation.containerId := currentCmdMsg.unitId;
                        assign WarehouseEngine::verticalArmOperation.triggerSource := "sensor";
                        assign WarehouseEngine::verticalArmOperation.relativePosition := currentCmdMsg.targetPosition; //simplified
                    }
                }
                //comment about moveToInputPositionForPickup, moveToInputPositionForDropdown, moveToStoragePositionForPickup, moveToStoragePositionForDropdown
                //the upper line apparently also runs in an parsing error since the parser doesnt allow multiple items to a command therefore I will just comment about the first, whereby the comment is for all
                comment about moveToInputPositionForPickup
                /*
                 * These actions should send the respective cmd messages to the motors, which will then move to the target position.
                 * The send command leads to the following parsing error:
                 * [ERROR] SmallWarehouseSystem.sysml:<163,63>: 0xA0240 Cannot find symbol targetPos
                 * The assign command leads to the following parsing error: 
                 * [ERROR] SmallWarehouseSystem.sysml:<187,20>: 0xA0240 Cannot find symbol x
                 */
                perform action moveToInputPositionForPickup {
                    assign WarehouseEngine::currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::inputPosition.relativePosition.X_CONFIG,
                        y = WarehouseEngine::inputPosition.relativePosition.Y_CONFIG - PICKUP_Y_OFFSET_CONFIG,
                        z = -1
                    );
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=WarehouseEngine::inputPosition.relativePosition.X_CONFIG)
                        via WarehouseEngine::HorizontalCmdMsgPort.cmdMsg;
                    perform setHorizontalArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=inputPosition.relativePosition.Y_CONFIG - PICKUP_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    perform setVerticalArmOperation;
                }

                perform action moveToInputPositionForDropoff {
                    assign WarehouseEngine::currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::inputPosition.relativePosition.X_CONFIG,
                        y = WarehouseEngine::inputPosition.relativePosition.Y_CONFIG + STORING_Y_OFFSET_CONFIG,
                        z = -1
                    );
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=WarehouseEngine::inputPosition.relativePosition.X_CONFIG)
                        via WarehouseEngine::HorizontalCmdMsgPort.cmdMsg;
                    perform setHorizontalArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=inputPosition.relativePosition.Y_CONFIG + STORING_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    perform setVerticalArmOperation;
                }

                perform action moveToStoragePositionForPickup {
                    assign currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::currentCmdMsg.targetPosition.x,
                        y = WarehouseEngine::currentCmdMsg.targetPosition.y - PICKUP_Y_OFFSET_CONFIG,
                        z = 1
                    );
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=WarehouseEngine::currentCmdMsg.targetPosition.x)
                        via WarehouseEngine::HorizontalCmdMsgPort;
                    perform setHorizontalArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=currentCmdMsg.targetPosition.y - PICKUP_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    perform setVerticalArmOperation;
                }
                perform action moveToStoragePositionForDropoff {
                    assign currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::currentCmdMsg.targetPosition.x,
                        y = WarehouseEngine::currentCmdMsg.targetPosition.y + STORING_Y_OFFSET_CONFIG,
                        z = -1
                    );
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=WarehouseEngine::currentCmdMsg.targetPosition.x)
                        via WarehouseEngine::HorizontalCmdMsgPort;
                    perform setHorizontalArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=currentCmdMsg.targetPosition.y + STORING_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    perform setVerticalArmOperation;
                }
                perform action pickup {
                    assign WarehouseEngine::currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::currentTargetPosition.x,
                        y = WarehouseEngine::currentTargetPosition.y + PICKUP_Y_OFFSET_CONFIG,
                        z = 1
                    );
                    send GoToPosMotorCmdMsg(targetPos=currentTargetPosition.y + PICKUP_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    perform setVerticalArmOperation;
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                }
                perform action dropoff {
                    assign WarehouseEngine::currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::currentTargetPosition.x,
                        y = WarehouseEngine::currentTargetPosition.y - STORING_Y_OFFSET_CONFIG,
                        z = 1
                    );
                    send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                    send GoToPosMotorCmdMsg(targetPos=currentTargetPosition.y - STORING_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    perform setVerticalArmOperation;
                }
                perform action moveCantileverBack {
                    assign WarehouseEngine::currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::currentTargetPosition.x,
                        y = WarehouseEngine::currentTargetPosition.y,
                        z = -1
                    );
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                }
                perform action moveCantileverForward {
                    assign WarehouseEngine::currentTargetPosition := WarehouseRelativePosition(
                        x = WarehouseEngine::currentTargetPosition.x,
                        y = WarehouseEngine::currentTargetPosition.y,
                        z = 1
                    );
                    send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    perform setCantileverArmOperation;
                }
                perform action changeOccupiedOfCurrentTargetLocation {
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P1 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p1.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P1 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p1.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P2 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p2.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P2 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p2.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P3 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p3.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P3 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p3.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P4 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p4.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P4 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p4.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P5 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p5.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P5 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p5.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P6 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p6.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P6 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p6.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P7 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p7.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P7 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p7.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P8 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p8.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P8 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p8.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P9 and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        // assign WarehouseEngine::p9.occupied := false;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::P9 and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        // assign WarehouseEngine::p9.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::INPUT and
                        currentCmdMsg.operationType == OperationType::STORE
                    ) {
                        assign WarehouseEngine::inputPosition.occupied := true;
                    }
                    if (
                        currentCmdMsg.targetStorageUnit == UnitName::INPUT and
                        currentCmdMsg.operationType == OperationType::RETRIEVE_EMPTY_PALLET
                    ) {
                        assign WarehouseEngine::inputPosition.occupied := false;
                    }
                }

                comment about setupMotor
                /*
                 * This action is used to setup the motors, it is called when the system is started.
                 * It sends the setup command to the motors, which will then move to the reference position.
                 * The send command leads to a the following parsing error:
                 * [ERROR] SmallWarehouseSystem.sysml:<152,32>: 0xA1242 No matching function found.
                 * Exception in thread "main" de.se_rwth.commons.logging.MCFatalError: SmallWarehouseSystem.sysml:<152,32>: 0xA1242 No matching function found.
                 * NOTE: Doing it the exact same way as Didier in the SmallWarehouseAPI::MotorAPIForwardBackwardReferenceSwitch::setupMotor action leads to the same error.
                 */
                perform action setupMotor {
                    send SetupCmdMsg() via HorizontalCmdMsgPort; // Didier did this in a similiar yet slightly different way, but since we use a separate engine part I think this could be better
                    send SetupCmdMsg() via VerticalCmdMsgPort;
                    send SetupCmdMsg() via CantileverCmdMsgPort; // TODO: Behaviour here is not defined yet, since there is no state machine for the cantilever motor
                }
                perform action initialize{
                    //Initialize the storage units

                    // assign {storage_unit.unit_name.value}.{storage_unit.relative_position} := {storage_unit.relative_position.value[]}; // this part .value[] should reference to the value that is stored in the respective column 
               // assign p1.relativePosition := PositionConfig(X_CONFIG = 0, Y_CONFIG = 0, Z_CONFIG = 1);
                    // assign p2.relativePosition := PositionConfig(X_CONFIG = 0, Y_CONFIG = 1, Z_CONFIG = 1);
                    // assign p3.relativePosition := PositionConfig(X_CONFIG = 0, Y_CONFIG = 2, Z_CONFIG = 1);
                    // assign p4.relativePosition := PositionConfig(X_CONFIG = 1, Y_CONFIG = 0, Z_CONFIG = 1);
                    // assign p5.relativePosition := PositionConfig(X_CONFIG = 1, Y_CONFIG = 1, Z_CONFIG = 1);
                    // assign p6.relativePosition := PositionConfig(X_CONFIG = 1, Y_CONFIG = 2, Z_CONFIG = 1);
                    // assign p7.relativePosition := PositionConfig(X_CONFIG = 2, Y_CONFIG = 0, Z_CONFIG = 1);
                    // assign p8.relativePosition := PositionConfig(X_CONFIG = 2, Y_CONFIG = 1, Z_CONFIG = 1);
                    // assign p9.relativePosition := PositionConfig(X_CONFIG = 2, Y_CONFIG = 2, Z_CONFIG = 1); 
                    
                    //assign {storage_unit.unit_name.value}.{storage_unit.id} := {storage_unit.id.value[]}
                    // assign p1.unitId := 0;
                    // assign p2.unitId := 1;
                    // assign p3.unitId := 2;
                    // assign p4.unitId := 3;
                    // assign p5.unitId := 4;
                    // assign p6.unitId := 5;
                    // assign p7.unitId := 6;
                    // assign p8.unitId := 7;
                    // assign p9.unitId := 8;

                    // assign {storage_unit.unit_name.value}.{storage_unit.unit_name} := {storage_unit.unit_name}::{storage_unit.unit_name.value}
                    // assign p1.unitName := UnitName::P1;
                    // assign p2.unitName := UnitName::P2;
                    // assign p3.unitName := UnitName::P3;
                    // assign p4.unitName := UnitName::P4;
                    // assign p5.unitName := UnitName::P5;
                    // assign p6.unitName := UnitName::P6;
                    // assign p7.unitName := UnitName::P7;
                    // assign p8.unitName := UnitName::P8;
                    // assign p9.unitName := UnitName::P9;

                    //Initialize the Container Units
                    // assign {container.container_name.value}.{container.container_id} := {container.container_id.value}
                    assign c1.containerId := 0;
                    assign c2.containerId := 1;
                    assign c3.containerId := 2;
                    assign c4.containerId := 3;
                    assign c5.containerId := 4;
                    assign c6.containerId := 5;
                    assign c7.containerId := 6;
                    assign c8.containerId := 7;
                    assign c9.containerId := 8;
                    // assign {container.container_name.value}.{container.unit_id} := {storage_unit.unit_id.value}.{container.unit_id}
                    // assign c1.unitId := p1.unitId;
                    // assign c2.unitId := p2.unitId;
                    // assign c3.unitId := p3.unitId;
                    // assign c4.unitId := p4.unitId;
                    // assign c5.unitId := p5.unitId;
                    // assign c6.unitId := p6.unitId;
                    // assign c7.unitId := p7.unitId;
                    // assign c8.unitId := p8.unitId;
                    // assign c9.unitId := p9.unitId;

                    // assign {container.container_name.value}.{container.container_name} := {container.container_name.value}
                    assign c1.containerName := "C1";
                    assign c2.containerName := "C2";
                    assign c3.containerName := "C3";
                    assign c4.containerName := "C4";
                    assign c5.containerName := "C5";
                    assign c6.containerName := "C6";
                    assign c7.containerName := "C7";
                    assign c8.containerName := "C8";
                    assign c9.containerName := "C9";

                    // assign {container.container_name.value}.{container.item_type} := {container.item_type}::EMPTY;
                    assign c1.itemType := ItemType::EMPTY;
                    assign c2.itemType := ItemType::EMPTY;
                    assign c3.itemType := ItemType::EMPTY;
                    assign c4.itemType := ItemType::EMPTY;
                    assign c5.itemType := ItemType::EMPTY;
                    assign c6.itemType := ItemType::EMPTY;
                    assign c7.itemType := ItemType::EMPTY;
                    assign c8.itemType := ItemType::EMPTY;
                    assign c9.itemType := ItemType::EMPTY;
                    
                    // assign cantileverArmOperation.{arm_operation.arm_id} := {arm_operation.arm_id.value}
                    assign cantileverArmOperation.armId := 0;
                    assign verticalArmOperation.armId := 1; 
                    assign horizontalArmOperation.armId := 2;

                    //Initialize the input position
                    assign inputPosition.relativePosition := PositionConfig(X_CONFIG=3,Y_CONFIG=1,Z_CONFIG=-1);
                }
                comment about resetDecoderCounters
                /*
                 * The action definition / usage pattern runs into parsing errors, not sure why.
                 * The following parsing error occurs:
                 * [ERROR] SmallWarehouseSystem.sysml:<413,54>: 0xA0324 Cannot find symbol Decoder.Reset
                 */
                action resetDecoderCounters {
                    action resetHorizontalDecoder : Decoder::Reset {
                        in selfDecoder = WarehouseAPISystem::horizontalImpulseCounter;
                    }
                    action resetVerticalDecoder : Decoder::Reset {
                        in selfDecoder = WarehouseAPISystem::verticalImpulseCounter;
                    }
                    assign decoderIsInitialized := true;
                }
            }
            private import Time::*;
            private import PortDefinitions::*;
            package SignalDefinitions {
                //Top-Level Warehouse Cmds
                item def WarehouseCmdMsg {
                    attribute storagePosition : UnitName;
                    attribute cmdMsgType : OperationType;
                }

                //Motor Cmds
                item def MotorCmdMsg {}
                item def GoToPosMotorCmdMsg :> MotorCmdMsg {
                    attribute targetPos : Integer;
                }
                item def StopMotorCmdMsg :> MotorCmdMsg {}
                item def SetupCmdMsg :> MotorCmdMsg {}
                item def GoBackwardTillReferenceCmdMsg :> MotorCmdMsg {}
                item def GoForwardTillReferenceCmdMsg :> MotorCmdMsg {}
                //Conveyor Cmds
                item def ConveyorCmdMsg {}
                item def ForwardCmdMsg :> ConveyorCmdMsg {}
                item def BackwardCmdMsg :> ConveyorCmdMsg {}
                item def StopConveyorCmdMsg :> ConveyorCmdMsg {}
                item def GoToPosConveyorCmdMsg :> ConveyorCmdMsg {
                    attribute targetPos : Integer;
                }
                // enum def {arm_operation.operation_type} {
                enum def OperationType {
                    RETRIEVE_EMPTY_PALLET;
                    RETRIEVE;
                    STORE;
                    SETUP;
                }
                // enum def {storage_units.unit_name} {}
                enum def UnitName {
                    P1;
                    P2;
                    P3;
                    P4;
                    P5;
                    P6;
                    P7;
                    P8;
                    P9;
                    INPUT;
                }
                // attribute def {container.item_type} {
                enum def ItemType{
                    EMPTY;
                    NUTS;
                    SCREWS;
                    BOLTS;
                }
                // attribute def {arm_operation.relative_position} {
                attribute def WarehouseRelativePosition {
                    attribute x : Integer;
                    attribute y : Integer;
                    attribute z : Integer;
                }
                // attribute def {storage_units.relative_position} {
                attribute def PositionConfig {
                    attribute X_CONFIG : Rational;
                    attribute Y_CONFIG : Rational;
                    attribute Z_CONFIG : Rational;
                }
                // Maybe also rename Attribute? attribute def {arm_operation} {
                attribute def ArmOperationAttribute{
                    // attribute {arm_operation.arm_id} : Integer;
                    attribute armId: Integer;
                    // attribute {arm_operation.container_id} : Integer := null;
                    attribute containerId : Integer := null;
                    // attribute {arm_operation.operation_type} : OperationType := null;
                    attribute operationType : OperationType := null;
                    // attribute {arm_operation.operation_timestamp} : DateTime := null;
                    attribute operationTimestamp : DateTime := null;
                    // attribute {arm_operation.trigger_source} : String := null;
                    attribute triggerSource : String := null; // DB-Attribute trigger_source, DISCUSS: i dont know the trigger source in the sysml model...
                    // attribute {arm_operation.relative_position} : WarehouseRelativePosition := null;
                    attribute relativePosition : WarehouseRelativePosition := null; 
                }
                // Maybe also rename Attribute? attribute def {container} {}
                attribute def ContainerAttribute{
                    // attribute {container.container_id} : Integer; 
                    attribute containerId : Integer;
                    // attribute {container.container_name} : String; 
                    attribute containerName : String;
                    // attribute {container.unit_id} : Integer; 
                    attribute unitId : StorageAttribute := null;
                    // attribute {container.item_type} : ItemType := null;
                    attribute itemType : ItemType := null;
                    // attribute {container.item_quantity} : Integer := null;
                    attribute itemQuantity : Integer := null; 
                    // attribute {container.item_weight_kg} : Rational := null;
                    attribute itemWeightKG : Rational := null;
                    // attribute {container.item_date_in} : DateTime := null;
                    attribute itemDateIn : DateTime := null;
                    // attribute {container.status_updated_at} : DateTime := null;
                    attribute statusUpdatedAt : DateTime := null;
                }
                // Maybe also rename Attribute? attribute def {storage_unit} {
                attribute def StorageAttribute {
                    // attribute {storage.unit_id} : Integer;
                    attribute unitId : Integer;
                    // attribute {storage.unit_name} : UnitName;
                    attribute unitName : UnitName; 
                    // attribute {storage.relative_position} : PositionConfig; 
                    attribute relativePosition : PositionConfig;
                    attribute occupied : Boolean := false; // TODO: DISCUSS, currently not used in the DB but would be manageable with a trigger? 
                }

                attribute def InputPositionAttribute :> StorageAttribute {}
            }
        }
    }
    package Hardware {
        private import Warehouse::PortDefinitions::*;
        package Definitions {
            part def WarehouseMachine {
                part cantileverMotor : MotorForwardBackwardReferenceSwitch;
                part cantileverForwardSwitch : ElectricalSwitch;
                part cantileverBackwardSwitch : ElectricalSwitch;

                part verticalMotor : EncoderMotor;
                part verticalBackwardSwitch : ElectricalSwitch;

                part horizontalMotor : EncoderMotor;
                part horizontalBackwardSwitch : ElectricalSwitch;

                part conveyorMotor : ConveyorMotor;

                //currently not used, for control but can be used in the future
                part lowerTrailSensor : TrailSensor;
                part upperTrailSensor : TrailSensor;

                part lightBarrierInside : LightBarrier;
                part lightBarrierOutside : LightBarrier;
            }

            part def ElectricalSwitch {
                out port switchSensor : SensorPort;
            }
            part def TrailSensor {
                out port signalPort : SensorPort;
            }
            part def MotorForwardBackwardReferenceSwitch {
                in port forwardActuator : ActuatorPort;
                in port backwardActuator : ActuatorPort;
            }
            part def EncoderMotor {
                in port forwardActuator : ActuatorPort;
                in port backwardActuator : ActuatorPort;
                out port incrementSignal : SensorPort;
                out port decrementSignal : SensorPort;
            }
            part def ConveyorMotor {
                in port forwardActuator : ActuatorPort;
                in port backwardActuator : ActuatorPort;
            }
            part def LightBarrier {
                out port signalPort : SensorPort;
            }
        }
    }
    package Definitions {
        part def WarehouseSystem {}
    }
    package PortDefinitions {
        port def APICmdPort {
            in item cmdMsg : WarehouseCmdMsg;
        }
        port def MotorCmdPort {
            in item cmdMsg : MotorCmdMsg;
        }
        port def ConveyorCmdPort {
            in item cmdMsg : ConveyorCmdMsg;
        }
        port def SensorPort {
            attribute value : Boolean;
        }
        port def ActuatorPort {
            attribute value : Boolean;
        }
        port def CounterPort {
            attribute value : Integer;
        }
        port def ErrorMsgPort {
            attribute errorMsg : String := "";
        }
    }
    private import Software::Definitions::*;
    private import Hardware::Definitions::*;
    private import SignalDefinitions::*;
    package System {
        part warehouseSystem {
            part warehouseAPI : WarehouseAPISystem {}
            part warehouseMachine : WarehouseMachine {}

            bind warehouseMachine.verticalMotor.incrementSignal = warehouseAPI.verticalImpulseCounter.signalA;
            bind warehouseMachine.verticalMotor.decrementSignal = warehouseAPI.verticalImpulseCounter.signalB;
            bind warehouseMachine.verticalMotor.forwardActuator = warehouseAPI.verticalMotorAPI.motorForwardPort;
            bind warehouseMachine.verticalMotor.backwardActuator
                = warehouseAPI.verticalMotorAPI.motorBackwardPort;
            bind warehouseMachine.verticalBackwardSwitch.switchSensor
                = warehouseAPI.verticalMotorAPI.backwardReferenceSwitch;

            bind warehouseMachine.horizontalMotor.incrementSignal = warehouseAPI.horizontalImpulseCounter.signalA;
            bind warehouseMachine.horizontalMotor.decrementSignal = warehouseAPI.horizontalImpulseCounter.signalB;
            bind warehouseMachine.horizontalMotor.forwardActuator
                = warehouseAPI.horizontalMotorAPI.motorForwardPort;
            bind warehouseMachine.horizontalMotor.backwardActuator
                = warehouseAPI.horizontalMotorAPI.motorBackwardPort;
            bind warehouseMachine.horizontalBackwardSwitch.switchSensor
                = warehouseAPI.horizontalMotorAPI.backwardReferenceSwitch;

            bind warehouseMachine.cantileverMotor.forwardActuator
                = warehouseAPI.cantileverMotorAPI.motorForwardPort;
            bind warehouseMachine.cantileverMotor.backwardActuator
                = warehouseAPI.cantileverMotorAPI.motorBackwardPort;
            bind warehouseMachine.cantileverForwardSwitch.switchSensor
                = warehouseAPI.cantileverMotorAPI.forwardReferenceSwitch;
            bind warehouseMachine.cantileverBackwardSwitch.switchSensor
                = warehouseAPI.cantileverMotorAPI.backwardReferenceSwitch;

            bind warehouseMachine.conveyorMotor.forwardActuator = warehouseAPI.conveyorMotorAPI.motorForwardPort;
            bind warehouseMachine.conveyorMotor.backwardActuator
                = warehouseAPI.conveyorMotorAPI.motorBackwardPort;

            //binding of light sensors and trail sensors currently not used
            bind warehouseMachine.lightBarrierInside.signalPort
                = warehouseAPI.warehouseEngineAPI.lightBarrierInside;
            bind warehouseMachine.lightBarrierOutside.signalPort
                = warehouseAPI.warehouseEngineAPI.lightBarrierOutside;
            bind warehouseMachine.lowerTrailSensor.signalPort
                = warehouseAPI.warehouseEngineAPI.lowerTrailSensor;
            bind warehouseMachine.upperTrailSensor.signalPort
                = warehouseAPI.warehouseEngineAPI.upperTrailSensor;
        }
        //binding between the API and the machine
    }
}

