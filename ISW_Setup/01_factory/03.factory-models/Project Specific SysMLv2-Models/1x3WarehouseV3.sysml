private import ScalarValues::*;
package Warehouse1x3 {
    // Port Definitions
    private import Warehouse1x3::Software::Definitions::SignalDefinitions::*;
    package Software {
        package Definitions {
            //Signal Definition
            private import PortDefinitions::*;
            private import SignalDefinitions::*;
            private import Warehouse1x3::Software::Definitions::*;
            part def Warehouse1x3APISystem {
                part cantileverMotorAPI : MotorAPIForwardBackwardReferenceSwitch;

                part horizontalImpulseCounter : Decoder;
                part horizontalMotorAPI : MotorAPIBackwardReferenceSwitch;

                part verticalImpulseCounter : Decoder;
                part verticalMotorAPI : MotorAPIBackwardReferenceSwitch;

                part conveyorMotorAPI : ConveyorBelt;
                part warehouse1x3EngineAPI : Warehouse1x3Engine;

                in port warehouseCmdAPICmdPort : APICmdPort;

                //port binding for decoder
                bind verticalImpulseCounter.decoderCount = verticalMotorAPI.decoderCount;
                bind verticalMotorAPI.decoderCount = warehouse1x3EngineAPI.verticalDecoderCount; //additional binding since we have the engine that is responsible for resetting
                bind horizontalImpulseCounter.decoderCount = horizontalMotorAPI.decoderCount;
                bind horizontalMotorAPI.decoderCount = warehouse1x3EngineAPI.horizontalDecoderCount; //additional binding since we have the engine that is responsible for resetting

                //Software internal binding between individual software components and engine
                bind cantileverMotorAPI.cmdPort = warehouse1x3EngineAPI.CantileverCmdMsgPort;
                bind horizontalMotorAPI.cmdPort = warehouse1x3EngineAPI.HorizontalCmdMsgPort;
                bind verticalMotorAPI.cmdPort = warehouse1x3EngineAPI.VerticalCmdMsgPort;
                bind conveyorMotorAPI.cmdPort = warehouse1x3EngineAPI.ConveyorCmdMsgPort;
                bind warehouse1x3EngineAPI.cmdPort = warehouseCmdAPICmdPort;
                bind warehouse1x3EngineAPI.cantileverForwardLimitSwitchPort = cantileverMotorAPI.forwardReferenceSwitch;
                bind warehouse1x3EngineAPI.cantileverBackwardLimitSwitchPort
                    = cantileverMotorAPI.backwardReferenceSwitch;
                bind warehouse1x3EngineAPI.horizontalBottomLimitSwitchPort = horizontalMotorAPI.backwardReferenceSwitch;
                bind warehouse1x3EngineAPI.verticalBottomLimitSwitchPort = verticalMotorAPI.backwardReferenceSwitch;
                //DISCUSS: Should we add the attributes here or in the engine (currently in the engine)?
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
                state motorStates : MotorStates {
                    in selfMotor = MotorAPIBackwardReferenceSwitch::self;
                }
                //TODO: Check the statemachine for errors again 
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
                    state backward_until_ref {}
                    state forward_until_target_pos {}
                    state backward_until_target_pos {}

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
            part def Warehouse1x3Engine {
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

                attribute currentForkLiftPosition : SmallWarehouseRelativePosition {
                    doc
                    /*
                     * This attribute represents the current position of the ForkLift
                     */
                }
                attribute inputPosition : StorageLocationAttribute {
                    doc
                    /*
                     * This attribute represents the input location of the Warehouse
                     */
                }
                attribute topPosition : StorageLocationAttribute {
                    doc
                    /*
                     * This attribute represents the top location of the Warehouse
                     */
                }
                attribute middlePosition : StorageLocationAttribute {
                    doc
                    /*
                     * This attribute represents the middle location of the Warehouse
                     */
                }
                attribute bottomPosition : StorageLocationAttribute {
                    doc
                    /*
                     * This attribute represents the bottom location of the Warehouse
                     */
                }
                attribute currentTargetPosition : SmallWarehouseRelativePosition {
                    doc
                    /*
                     * This attribute represents the target position of the ForkLift
                     */
                }
                attribute currentCommand {
                    attribute targetPosition : SmallWarehouseRelativePosition {
                        doc
                        /*
                         * This attribute represents the target cmd position of the ForkLift, which always will representate a relative position of either INPUT, TOP, MIDDLE or BOTTOM
                         */
                    }
                    attribute cmdMsgGoal : WarehouseCmdMsgType;
                    attribute storagePositionEnum : StoragePosition {}
                }
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
                attribute isInitialized : Boolean := false {
                    doc
                    /*
                     * This attribute indicates if the horizontal and vertical Decoders have been initialized, it is used to know if the ForkLift is ready to move
                     */
                }

                state smallWarehouseEngineStates : SmallWarehouseEngineStates {
                    in selfEngine = Warehouse1x3Engine::self;
                }
                state def SmallWarehouseEngineStates {
                    in selfEngine : Warehouse1x3Engine;
                    entry;
                    then setup;

                    state setup {
                        entry selfEngine.setupMotor;
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
                    transition setup_To_topEmpt_midEmpt_bot_Empt
                        first setup accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort then topEmpt_midEmpt_bot_Empt;
                    //transitions from cmdReceived to the respective states
                    transition cmdReceived_To_retrieveEmptyPallet
                        first cmdReceived
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then retrieveEmptyPallet;
                    transition cmdReceived_To_store
                        first cmdReceived
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then store;
                    transition cmdReceived_To_retrieveItem
                        first cmdReceived
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then retrieveItem;
                    then cmdReceived;

                    //handle error transitions
                    transition cmdReceived_To_ErrorRetrieveEmptyPallet
                        first cmdReceived
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then ErrorRetrieveEmptyPallet;
                    transition cmdReceived_To_ErrorStore
                        first cmdReceived
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then ErrorStore;
                    transition cmdReceived_To_ErrorRetrieveItem
                        first cmdReceived
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then ErrorRetrieveItem;

                    //return to respective Warehouse state after error handling

                    transition ErrorRetrieveItem_To_topEmpt_midEmpt_bot_Empt
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then topEmpt_midEmpt_bot_Empt;
                    transition ErrorRetrieveItem_To_topFull_midEmpt_bot_Empt
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midEmpt_bot_Empt;
                    transition ErrorRetrieveItem_To_topEmpt_midFull_bot_Empt
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then topEmpt_midFull_bot_Empt;
                    transition ErrorRetrieveItem_To_topEmpt_midEmpt_bot_Full
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midEmpt_bot_Full;
                    transition ErrorRetrieveItem_To_topFull_midEmpt_bot_Full
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then topFull_midEmpt_bot_Full;
                    transition ErrorRetrieveItem_To_topFull_midFull_bot_Empt
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == true) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == true))
                        )
                            then topFull_midFull_bot_Empt;
                    transition ErrorRetrieveItem_To_topEmpt_midFull_botFull
                        first ErrorRetrieveItem
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midFull_botFull;

                    transition ErrorRetrieveEmptyPallet_To_topFull_midEmpt_bot_Empt
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midEmpt_bot_Empt;
                    transition ErrorRetrieveEmptyPallet_To_topEmpt_midFull_bot_Empt
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midFull_bot_Empt;
                    transition ErrorRetrieveEmptyPallet_To_topEmpt_midEmpt_bot_Full
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midEmpt_bot_Full;
                    transition ErrorRetrieveEmptyPallet_To_topFull_midEmpt_bot_Full
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midEmpt_bot_Full;
                    transition ErrorRetrieveEmptyPallet_To_topFull_midFull_bot_Empt
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midFull_bot_Empt;
                    transition ErrorRetrieveEmptyPallet_To_topEmpt_midFull_botFull
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midFull_botFull;
                    transition ErrorRetrieveEmptyPallet_To_topFull_midFull_bot_Full
                        first ErrorRetrieveEmptyPallet
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midFull_bot_Full;

                    transition ErrorStore_To_topFull_midEmpt_bot_Empt
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midEmpt_bot_Empt;
                    transition ErrorStore_To_topEmpt_midFull_bot_Empt
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midFull_bot_Empt;
                    transition ErrorStore_To_topEmpt_midEmpt_bot_Full
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midEmpt_bot_Full;
                    transition ErrorStore_To_topFull_midEmpt_bot_Full
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midEmpt_bot_Full;
                    transition ErrorStore_To_topFull_midFull_bot_Empt
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midFull_bot_Empt;
                    transition ErrorStore_To_topEmpt_midFull_botFull
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topEmpt_midFull_botFull;
                    transition ErrorStore_To_topFull_midFull_bot_Full
                        first ErrorStore
                        if (
                            selfEngine.currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE and
                            ((selfEngine.currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                                    selfEngine.bottomPosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                                    selfEngine.middlePosition.occupied == false) or
                                (selfEngine.currentCommand.storagePositionEnum == StoragePosition::TOP and
                                    selfEngine.topPosition.occupied == false))
                        )
                            then topFull_midFull_bot_Full;

                    //composite state to handle the retrieving of an empty pallet
                    state retrieveEmptyPallet {
                        entry action assign selfEngine.actionCommandDone := false;
                        then moveToStoragePositionForPickupState;
                        state moveToStoragePositionForPickupState {
                            entry selfEngine.moveToStoragePositionForPickup;
                        }
                        //TODO: Check if this state is needed
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
                                perform  selfEngine.moveCantileverBack;
                                perform  selfEngine.changeOccupiedOfCurrentTargetLocation;
                                perform  selfEngine.resetCurrentCmdValue;
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
                    transition topEmpt_midEmpt_bot_Empt_To_cmdReceived
                        first topEmpt_midEmpt_bot_Empt
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topFull_mid_empty_bot_Empt_To_cmdReceived
                        first topFull_midEmpt_bot_Empt
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topEmpt_midFull_bot_Empt_To_cmdReceived
                        first topEmpt_midFull_bot_Empt
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topEmpt_midEmpt_bot_Full_To_cmdReceived
                        first topEmpt_midEmpt_bot_Full
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topFull_midEmpt_bot_Full_To_cmdReceived
                        first topFull_midEmpt_bot_Full
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topFull_midFull_bot_Empt_To_cmdReceived
                        first topFull_midFull_bot_Empt
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topEmpt_midFull_botFull_To_cmdReceived
                        first topEmpt_midFull_botFull
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;

                    transition topFull_midFull_bot_Full_To_cmdReceived
                        first topFull_midFull_bot_Full
                        accept cmdPort.cmdMsg via Warehouse1x3Engine::cmdPort
                        then cmdReceived;
                    comment about retrieveEmptyPallet_To_topEmpt_midEmpt_bot_Empt
                    /*
                     * This runs into the following MontiCore parser error:
                     * [ERROR] SmallWarehouseSystem.sysml:<401,46>: 0xA0241 No SymTypeExpression could be derived for the FieldAccessExpression selfEngine.topPosition.occupied
                     * I think this is due to not actually using the Boolean type
                     */
                    //All Transitions from retrieveEmptyPallet to states
                    transition retrieveEmptyPallet_To_topEmpt_midEmpt_bot_Empt
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midEmpt_bot_Empt;

                    transition retrieveEmptyPallet_To_topFull_midEmpt_bot_Empt
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Empt;

                    transition retrieveEmptyPallet_To_topEmpt_midFull_bot_Empt
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midFull_bot_Empt;

                    transition retrieveEmptyPallet_To_topEmpt_midEmpt_bot_Full
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midEmpt_bot_Full;

                    transition retrieveEmptyPallet_To_topFull_midEmpt_bot_Full
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Full;

                    transition retrieveEmptyPallet_To_topFull_midFull_bot_Empt
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midFull_bot_Empt;

                    transition retrieveEmptyPallet_To_topEmpt_midFull_botFull
                        first retrieveEmptyPallet
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midFull_botFull;

                    //Transitions from store to states

                    transition store_To_topFull_midEmpt_bot_Empt
                        first store
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Empt;

                    transition store_To_topEmpt_midFull_bot_Empt
                        first store
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midFull_bot_Empt;

                    transition store_To_topEmpt_midEmpt_bot_Full
                        first store
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midEmpt_bot_Full;

                    transition store_To_topFull_midEmpt_bot_Full
                        first store
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Full;

                    transition store_To_topFull_midFull_bot_Empt
                        first store
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midFull_bot_Empt;

                    transition store_To_topEmpt_midFull_botFull
                        first store
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midFull_botFull;

                    transition store_To_topFull_midFull_botFull
                        first store
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midFull_bot_Full;

                    //Transitions from retrieveItem to states
                    transition retrieveItem_To_topEmpt_midEmpt_bot_Empt
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Empt;

                    transition retrieveItem_To_topFull_midEmpt_bot_Empt
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Empt;

                    transition retrieveItem_To_topEmpt_midFull_bot_Empt
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midFull_bot_Empt;

                    transition retrieveItem_To_topEmpt_midEmpt_bot_Full
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midEmpt_bot_Full;

                    transition retrieveItem_To_topFull_midEmpt_bot_Full
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == false and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midEmpt_bot_Full;

                    transition retrieveItem_To_topFull_midFull_bot_Empt
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == true and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == false and
                            selfEngine.actionCommandDone == true
                        )
                            then topFull_midFull_bot_Empt;

                    transition retrieveItem_To_topEmpt_midFull_botFull
                        first retrieveItem
                        if (
                            selfEngine.topPosition.occupied == false and
                            selfEngine.middlePosition.occupied == true and
                            selfEngine.bottomPosition.occupied == true and
                            selfEngine.actionCommandDone == true
                        )
                            then topEmpt_midFull_botFull;

                    state topEmpt_midEmpt_bot_Empt {}
                    state topEmpt_midEmpt_bot_Full {}
                    state topEmpt_midFull_bot_Empt {}
                    state topFull_midEmpt_bot_Empt {}
                    state topFull_midEmpt_bot_Full {}
                    state topFull_midFull_bot_Empt {}
                    state topEmpt_midFull_botFull {}
                    state topFull_midFull_bot_Full {}
                }
                perform action assignCurrentCmdValue {
                    if cmdPort.cmdMsg.cmdMsgType == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET {
                        //Doesnt work for some reason havent figured out why yet, similiar case worked out in my 1x3WarehouseParse.sysml (line 833 e.g.)
                        //Probably caused by: The Package structure
                        //MontiCore Parsing Error: [ERROR] SmallWarehouseSystem.sysml:<178,107>: 0xA0240 Cannot find symbol WarehouseCmdMsgType
                        assign Warehouse1x3Engine::currentCommand.cmdMsgGoal :=
                            WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET;
                    }
                    if cmdPort.cmdMsg.cmdMsgType == WarehouseCmdMsgType::RETRIEVE {
                        assign currentCommand.cmdMsgGoal := SignalDefinitions::WarehouseCmdMsgType::RETRIEVE;
                    }
                    if cmdPort.cmdMsg.cmdMsgType == WarehouseCmdMsgType::STORE {
                        assign currentCommand.cmdMsgGoal := WarehouseCmdMsgType::STORE;
                    }
                    if cmdPort.cmdMsg.storagePosition == StoragePosition::TOP {
                        assign currentCommand.storagePositionEnum := StoragePosition::TOP;
                        assign currentCommand.targetPosition := SmallWarehouseRelativePosition(
                            x = topPosition.relativePosition.X_CONFIG,
                            y = topPosition.relativePosition.Y_CONFIG,
                            z = topPosition.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == StoragePosition::MIDDLE {
                        assign currentCommand.storagePositionEnum := StoragePosition::MIDDLE;
                        assign currentCommand.targetPosition := SmallWarehouseRelativePosition(
                            x = middlePosition.relativePosition.X_CONFIG,
                            y = middlePosition.relativePosition.Y_CONFIG,
                            z = middlePosition.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == StoragePosition::BOTTOM {
                        assign currentCommand.storagePositionEnum := StoragePosition::BOTTOM;
                        assign currentCommand.targetPosition := SmallWarehouseRelativePosition(
                            x = bottomPosition.relativePosition.X_CONFIG,
                            y = bottomPosition.relativePosition.Y_CONFIG,
                            z = bottomPosition.relativePosition.Z_CONFIG
                        );
                    }
                    if cmdPort.cmdMsg.storagePosition == StoragePosition::INPUT {
                        assign currentCommand.storagePositionEnum := StoragePosition::INPUT;
                        assign currentCommand.targetPosition := SmallWarehouseRelativePosition(
                            x = inputPosition.relativePosition.X_CONFIG,
                            y = inputPosition.relativePosition.Y_CONFIG,
                            z = inputPosition.relativePosition.Z_CONFIG
                        );
                    }
                }
                perform action resetCurrentCmdValue {
                    assign Warehouse1x3Engine::currentCommand.cmdMsgGoal := null;
                    assign Warehouse1x3Engine::currentCommand.targetPosition := null;
                    assign Warehouse1x3Engine::currentCommand.storagePositionEnum := null;
                }
                //comment about moveToInputPositionForPickup, moveToInputPositionForDropdown, moveToStoragePositionForPickup, moveToStoragePositionForDropdown
                //the upper line apparently also runs in an parsing error since the parser doesnt allow multiple items to a command therefore I will just comment about the first, wherebu the comment is for all
                comment about moveToInputPositionForPickup
                /*
                 * These actions should send the respective cmd messages to the motors, which will then move to the target position.
                 * The send command leads to the following parsing error:
                 * [ERROR] SmallWarehouseSystem.sysml:<163,63>: 0xA0240 Cannot find symbol targetPos
                 * The assign command leads to the following parsing error: 
                 * [ERROR] SmallWarehouseSystem.sysml:<187,20>: 0xA0240 Cannot find symbol x
                 */
                perform action moveToInputPositionForPickup {
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=Warehouse1x3Engine::inputPosition.relativePosition.X_CONFIG)
                        via Warehouse1x3Engine::HorizontalCmdMsgPort.cmdMsg;
                    send GoToPosMotorCmdMsg(targetPos=inputPosition.relativePosition.Y_CONFIG - PICKUP_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    assign Warehouse1x3Engine::currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::inputPosition.relativePosition.X_CONFIG,
                        y = Warehouse1x3Engine::inputPosition.relativePosition.Y_CONFIG - PICKUP_Y_OFFSET_CONFIG,
                        z = CantileverPosition::BACKWARD_LIMIT
                    );
                }

                perform action moveToInputPositionForDropoff {
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=Warehouse1x3Engine::inputPosition.relativePosition.X_CONFIG)
                        via Warehouse1x3Engine::HorizontalCmdMsgPort.cmdMsg;
                    send GoToPosMotorCmdMsg(targetPos=inputPosition.relativePosition.Y_CONFIG + STORING_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    assign Warehouse1x3Engine::currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::inputPosition.relativePosition.X_CONFIG,
                        y = Warehouse1x3Engine::inputPosition.relativePosition.Y_CONFIG + STORING_Y_OFFSET_CONFIG,
                        z = CantileverPosition::BACKWARD_LIMIT
                    );
                }

                perform action moveToStoragePositionForPickup {
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=Warehouse1x3Engine::currentCommand.targetPosition.x)
                        via Warehouse1x3Engine::HorizontalCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=currentCommand.targetPosition.y - PICKUP_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    assign currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::currentCommand.targetPosition.x,
                        y = Warehouse1x3Engine::currentCommand.targetPosition.y - PICKUP_Y_OFFSET_CONFIG,
                        z = CantileverPosition::FORWARD_LIMIT
                    );
                }
                perform action moveToStoragePositionForDropoff {
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=Warehouse1x3Engine::currentCommand.targetPosition.x)
                        via Warehouse1x3Engine::HorizontalCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=currentCommand.targetPosition.y + STORING_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    assign currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::currentCommand.targetPosition.x,
                        y = Warehouse1x3Engine::currentCommand.targetPosition.y + STORING_Y_OFFSET_CONFIG,
                        z = CantileverPosition::BACKWARD_LIMIT
                    );
                }
                perform action pickup {
                    send GoToPosMotorCmdMsg(targetPos=currentTargetPosition.y + PICKUP_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    assign Warehouse1x3Engine::currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::currentTargetPosition.x,
                        y = Warehouse1x3Engine::currentTargetPosition.y + PICKUP_Y_OFFSET_CONFIG,
                        z = CantileverPosition::FORWARD_LIMIT
                    );
                }
                perform action dropoff {
                    send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    send GoToPosMotorCmdMsg(targetPos=currentTargetPosition.y - STORING_Y_OFFSET_CONFIG)
                        via VerticalCmdMsgPort;
                    assign Warehouse1x3Engine::currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::currentTargetPosition.x,
                        y = Warehouse1x3Engine::currentTargetPosition.y - STORING_Y_OFFSET_CONFIG,
                        z = CantileverPosition::FORWARD_LIMIT
                    );
                }
                perform action moveCantileverBack {
                    send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    assign Warehouse1x3Engine::currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::currentTargetPosition.x,
                        y = Warehouse1x3Engine::currentTargetPosition.y,
                        z = CantileverPosition::BACKWARD_LIMIT
                    );
                }
                perform action moveCantileverForward {
                    send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                    assign Warehouse1x3Engine::currentTargetPosition := SmallWarehouseRelativePosition(
                        x = Warehouse1x3Engine::currentTargetPosition.x,
                        y = Warehouse1x3Engine::currentTargetPosition.y,
                        z = CantileverPosition::FORWARD_LIMIT
                    );
                }
                perform action changeOccupiedOfCurrentTargetLocation {
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::TOP and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE
                    ) {
                        assign Warehouse1x3Engine::topPosition.occupied := true;
                    }
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                    ) {
                        assign Warehouse1x3Engine::middlePosition.occupied := false;
                    }
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE
                    ) {
                        assign Warehouse1x3Engine::middlePosition.occupied := true;
                    }
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                    ) {
                        assign Warehouse1x3Engine::bottomPosition.occupied := false;
                    }
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE
                    ) {
                        assign Warehouse1x3Engine::bottomPosition.occupied := true;
                    }
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::INPUT and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::STORE
                    ) {
                        assign Warehouse1x3Engine::inputPosition.occupied := true;
                    }
                    if (
                        currentCommand.storagePositionEnum == StoragePosition::INPUT and
                        currentCommand.cmdMsgGoal == WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                    ) {
                        assign Warehouse1x3Engine::inputPosition.occupied := false;
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
                    send SetupCmdMsg() via CantileverCmdMsgPort;
                }
                comment about resetDecoderCounters
                /*
                 * The action definition / usage pattern runs into parsing errors, not sure why. Therefore commented it out.
                 * The following parsing error occurs:
                 * [ERROR] SmallWarehouseSystem.sysml:<413,54>: 0xA0324 Cannot find symbol Decoder.Reset
                 */
                action resetDecoderCounters {
                    action resetHorizontalDecoder : Decoder::Reset {
                        in selfDecoder = Warehouse1x3APISystem::horizontalImpulseCounter;
                    }
                    action resetVerticalDecoder : Decoder::Reset {
                        in selfDecoder = Warehouse1x3APISystem::verticalImpulseCounter;
                    }
                    assign isInitialized := true;
                }
            }
            private import PortDefinitions::*;
            package SignalDefinitions {
                //Top-Level Warehouse Cmds
                item def WarehouseCmdMsg {
                    attribute storagePosition : StoragePosition;
                    attribute cmdMsgType : WarehouseCmdMsgType;
                }

                //Motor Cmds (I think it is not necessary to define a new message for each motor, since the direction is just dependent on the orientation of the motor)
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
                enum def WarehouseCmdMsgType {
                    RETRIEVE_EMPTY_PALLET;
                    RETRIEVE;
                    STORE;
                }
                enum def StoragePosition {
                    TOP;
                    MIDDLE;
                    BOTTOM;
                    INPUT;
                }
                enum def CantileverPosition {
                    FORWARD_LIMIT;
                    BACKWARD_LIMIT;
                }
                attribute def SmallWarehouseRelativePosition {
                    attribute x : Integer;
                    attribute y : Integer;
                    attribute z : CantileverPosition;
                }
                attribute def PositionConfig {
                    attribute X_CONFIG : Integer;
                    attribute Y_CONFIG : Integer;
                    attribute Z_CONFIG : Integer;
                }
                attribute def StorageLocationAttribute {
                    attribute storagePosition : StoragePosition;
                    attribute relativePosition : PositionConfig;
                    attribute occupied : Boolean;
                }
            }
        }
    }
    package Hardware {
        private import Warehouse1x3::PortDefinitions::*;
        package Definitions {
            part def Warehouse1x3Machine {
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
        part def Warehouse1x3System {}
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
        part warehouse1x3System {
            part warehouse1x3API : Warehouse1x3APISystem {}
            part warehouse1x3Machine : Warehouse1x3Machine {}

            bind warehouse1x3Machine.verticalMotor.incrementSignal = warehouse1x3API.verticalImpulseCounter.signalA;
            bind warehouse1x3Machine.verticalMotor.decrementSignal = warehouse1x3API.verticalImpulseCounter.signalB;
            bind warehouse1x3Machine.verticalMotor.forwardActuator = warehouse1x3API.verticalMotorAPI.motorForwardPort;
            bind warehouse1x3Machine.verticalMotor.backwardActuator
                = warehouse1x3API.verticalMotorAPI.motorBackwardPort;
            bind warehouse1x3Machine.verticalBackwardSwitch.switchSensor
                = warehouse1x3API.verticalMotorAPI.backwardReferenceSwitch;

            bind warehouse1x3Machine.horizontalMotor.incrementSignal = warehouse1x3API.horizontalImpulseCounter.signalA;
            bind warehouse1x3Machine.horizontalMotor.decrementSignal = warehouse1x3API.horizontalImpulseCounter.signalB;
            bind warehouse1x3Machine.horizontalMotor.forwardActuator
                = warehouse1x3API.horizontalMotorAPI.motorForwardPort;
            bind warehouse1x3Machine.horizontalMotor.backwardActuator
                = warehouse1x3API.horizontalMotorAPI.motorBackwardPort;
            bind warehouse1x3Machine.horizontalBackwardSwitch.switchSensor
                = warehouse1x3API.horizontalMotorAPI.backwardReferenceSwitch;

            bind warehouse1x3Machine.cantileverMotor.forwardActuator
                = warehouse1x3API.cantileverMotorAPI.motorForwardPort;
            bind warehouse1x3Machine.cantileverMotor.backwardActuator
                = warehouse1x3API.cantileverMotorAPI.motorBackwardPort;
            bind warehouse1x3Machine.cantileverForwardSwitch.switchSensor
                = warehouse1x3API.cantileverMotorAPI.forwardReferenceSwitch;
            bind warehouse1x3Machine.cantileverBackwardSwitch.switchSensor
                = warehouse1x3API.cantileverMotorAPI.backwardReferenceSwitch;

            bind warehouse1x3Machine.conveyorMotor.forwardActuator = warehouse1x3API.conveyorMotorAPI.motorForwardPort;
            bind warehouse1x3Machine.conveyorMotor.backwardActuator
                = warehouse1x3API.conveyorMotorAPI.motorBackwardPort;

            //binding of light sensors and trail sensors currently not used
            bind warehouse1x3Machine.lightBarrierInside.signalPort
                = warehouse1x3API.warehouse1x3EngineAPI.lightBarrierInside;
            bind warehouse1x3Machine.lightBarrierOutside.signalPort
                = warehouse1x3API.warehouse1x3EngineAPI.lightBarrierOutside;
            bind warehouse1x3Machine.lowerTrailSensor.signalPort
                = warehouse1x3API.warehouse1x3EngineAPI.lowerTrailSensor;
            bind warehouse1x3Machine.upperTrailSensor.signalPort
                = warehouse1x3API.warehouse1x3EngineAPI.upperTrailSensor;
        }
        //binding between the API and the machine
    }
}
