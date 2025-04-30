package Software {
    //Helper to parse Standard Library Elements
    package LibraryHelper {
        attribute def Boolean {}
        attribute def Integer {}
        attribute def String {}
        attribute def Natural {}
    }
    private import PortDefinitions::*; 
    //  action restHorizontalImpulseCounter : Decoder::Reset {
    //      in selfDecoder = horizontalDecoderCount;
    //  }
    // action restVerticalImpulseCounter : Decoder::Reset {
    //     in selfDecoder = verticalDecoderCount;
    // }
    package SmallWarehouseAPI {
        part def SmallWarehouseSystemAPI {
            part cantileverMotorAPI : Software::SmallWarehouseAPI::MotorAPIForwardBackwardReferenceSwitch;

            part horizontalImpulseCounter : Software::SmallWarehouseAPI::Decoder;
            part horizontalMotorAPI : Software::SmallWarehouseAPI::MotorAPIBackwardReferenceSwitch;

            part verticalImpulseCounter : Software::SmallWarehouseAPI::MotorAPIBackwardReferenceSwitch;
            part verticalMotorAPI : Software::SmallWarehouseAPI::MotorAPIBackwardReferenceSwitch;

            part conveyorMotorAPI : Software::SmallWarehouseAPI::ConveyorBelt;
            part smallWarehouseEngineAPI : Software::SmallWarehouseAPI::SmallWarehouseEngine;

            in port warehouseCmdAPICmdPort : Software::PortDefinitions::APICmdPort;

            //port binding for decoder
            bind verticalImpulseCounter.decoderCount = verticalMotorAPI.decoderCount;
            bind verticalMotorAPI.decoderCount = smallWarehouseEngineAPI.verticalDecoderCount; //additional binding since we have the engine that is responsible for resetting
            bind horizontalImpulseCounter.decoderCount = horizontalMotorAPI.decoderCount;
            bind horizontalMotorAPI.decoderCount = smallWarehouseEngineAPI.horizontalDecoderCount; //additional binding since we have the engine that is responsible for resetting

            //DISCUSS: Should we add the attributes here or in the engine (currently in the engine)?
        }
        part def MotorAPIForwardBackwardReferenceSwitch {
            doc
            /*
             * This MotorAPI is used for motors that have a backward reference switch and a forward limit switch.
             */
            in port cmdPort : Software::PortDefinitions::MotorCmdPort;
            in port forwardLimitSwitchPort : Software::PortDefinitions::SensorPort;
            in port backwardLimitSwitchport : Software::PortDefinitions::SensorPort;

            out port motorForwardPort : Software::PortDefinitions::ActuatorPort;
            out port motorBackwardPort : Software::PortDefinitions::ActuatorPort;
        }

        part def MotorAPIBackwardReferenceSwitch {
            doc
            /*
             * This MotorAPI is used for motors that have a backward reference .
             */
            in port decoderCount : Software::PortDefinitions::CounterPort;
            in port cmdPort : Software::PortDefinitions::MotorCmdPort;
            in port backwardLimitSwitchPort : Software::PortDefinitions::SensorPort;

            out port motorForwardPort : Software::PortDefinitions::ActuatorPort;
            out port motorBackwardPort : Software::PortDefinitions::ActuatorPort;

            attribute TOLERANCE_CONFIG : Software::LibraryHelper::Integer := 20 {
                doc
                /*
                 * This attribute indicates the tolerance of the motor, it is used to calculate the position of the ForkLift when moving to a target position
                 */
            }
        }
        part def ConveyorBelt {
            in port cmdPort : Software::PortDefinitions::ConveyorCmdPort;
            out port forwardPort : Software::PortDefinitions::ActuatorPort;
            out port backwardPort : Software::PortDefinitions::ActuatorPort;
        }
        part def SmallWarehouseEngine {
            in port cmdPort : Software::PortDefinitions::APICmdPort;
            in port cantileverForwardLimitSwitchPort : Software::PortDefinitions::SensorPort;
            in port cantileverBackwardLimitSwitchPort : Software::PortDefinitions::SensorPort;
            in port horizontalBottomLimitSwitchPort : Software::PortDefinitions::SensorPort;
            in port verticalBottomLimitSwitchPort : Software::PortDefinitions::SensorPort;
            //We added these ports to be able to reset the decoders from the engine, since we have a state machine that is responsible for the actions of the ForkLift
            in port horizontalDecoderCount : Software::PortDefinitions::CounterPort;
            in port verticalDecoderCount : Software::PortDefinitions::CounterPort;

            out port CantileverCmdMsgPort : Software::PortDefinitions::MotorCmdPort;
            out port HorizontalCmdMsgPort : Software::PortDefinitions::MotorCmdPort;
            out port VerticalCmdMsgPort : Software::PortDefinitions::MotorCmdPort;
            out port ConveyorCmdMsgPort : Software::PortDefinitions::ConveyorCmdPort;

            attribute currentForkLiftPosition : Software::SignalDefinitions::SmallWarehouseRelativePosition {
                doc
                /*
                 * This attribute represents the current position of the ForkLift
                 */
            }
            attribute inputPosition : Software::SignalDefinitions::StorageLocationAttribute {
                doc
                /*
                 * This attribute represents the input location of the Warehouse
                 */
            }
            attribute topPosition : Software::SignalDefinitions::StorageLocationAttribute {
                doc
                /*
                 * This attribute represents the top location of the Warehouse
                 */
            }
            attribute middlePosition : Software::SignalDefinitions::StorageLocationAttribute {
                doc
                /*
                 * This attribute represents the middle location of the Warehouse
                 */
            }
            attribute bottomPosition : Software::SignalDefinitions::StorageLocationAttribute {
                doc
                /*
                 * This attribute represents the bottom location of the Warehouse
                 */
            }
            attribute currentTargetPosition : Software::SignalDefinitions::SmallWarehouseRelativePosition {
                doc
                /*
                 * This attribute represents the target position of the ForkLift
                 */
            }
            attribute currentCommand {
                attribute targetPosition : Software::SignalDefinitions::SmallWarehouseRelativePosition {
                    doc
                    /*
                     * This attribute represents the target cmd position of the ForkLift, which always will representate a relative position of either INPUT, TOP, MIDDLE or BOTTOM
                     */
                }
                attribute cmdMsgGoal : Software::SignalDefinitions::WarehouseCmdMsgType;
                attribute storagePositionEnum : Software::SignalDefinitions::StoragePosition {}
            }
            attribute actionCommandDone : Software::LibraryHelper::Boolean := false {
                doc
                /*
                 * This attribute indicates if the action command is done, can be used to know if a composite action is finished
                 */
            }
            attribute STORING_Y_OFFSET_CONFIG : Software::LibraryHelper::Natural := 0 {
                doc
                /*
                 * NEEDS TO BE CONFIGURATED. This attribute indicates the Y offset of the storing position, it is used to calculate the position of the ForkLift when storing an object
                 */
            }
            attribute PICKUP_Y_OFFSET_CONFIG : Software::LibraryHelper::Natural := 0 {
                doc
                /*
                 * NEEDS TO BE CONFIGURATED. This attribute indicates the Y offset of the pickup position, it is used to calculate the position of the ForkLift when picking up an object
                 */
            }

            state smallWarehouseEngineStates : SmallWarehouseEngineStates {
                in selfEngine = SmallWarehouseEngine::self;
            }
            state def SmallWarehouseEngineStates {
                in selfEngine : SmallWarehouseEngine;
                entry;
                then setup;

                state setup {
                    entry selfEngine.setupMotor;
                }
                state cmdReceived { 
                    entry selfEngine.assignCurrentCmdValue;
                }
                transition setup_To_topEmpt_midEmpt_bot_Empt
                    first setup
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then topEmpt_midEmpt_bot_Empt;
                //TODO: These transitions need to be checked if legal, e.g. if selfEngine.topPosition.occupied == false and all others are false, then it causes an error to transition from cmdReceived to retrieveItem
                transition cmdReceived_To_retrieveEmptyPallet
                    first cmdReceived
                    //if selfEngine.currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                    then retrieveEmptyPallet;
                transition cmdReceived_To_store
                    first cmdReceived
                    //if selfEngine.currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::STORE
                    then store;
                transition cmdReceived_To_retrieveItem
                    first cmdReceived
                    //if selfEngine.currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::RETRIEVE
                    then retrieveItem;
                // if selfEngine.horizontalDecoderCount.value == 0 and selfEngine.verticalDecoderCount.value == 0 then cmdReceived;
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
                    //TODO : Change this, this is not right
                    state reset {
                        entry action resetting {
                            perform action moveCantileverBack;
                            perform action changeOccupiedOfCurrentTargetLocation;
                            perform action resetCurrentCmdValue;
                            assign selfEngine.actionCommandDone := true;
                        }
                    }
                    comment about moveToStoragePositionForPickupState_To_pickup
                    /*
                     * This transition causes an parsing error: 
                     * [ERROR] SmallWarehouseSystem.sysml:<201,139>: 0xA0241 No SymTypeExpression could be derived for the FieldAccessExpression Software.SmallWarehouseAPI.SmallWarehouseEngine.SmallWarehouseEngineStates.selfEngine.horizontalDecoderCount.value
                     */
                    transition moveToStoragePositionForPickupState_To_pickup
                        first moveToStoragePositionForPickupState
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and Software::SmallWarehouseAPI::SmallWarehouseEngine::SmallWarehouseEngineStates::selfEngine.verticalDecoderCount.value == Software::SmallWarehouseAPI::SmallWarehouseEngine::SmallWarehouseEngineStates::selfEngine.currentTargetPosition.y
                        // and Software::SmallWarehouseAPI::SmallWarehouseEngine::SmallWarehouseEngineStates::selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then pickup;
                    transition pickup_to_moveToInputPositionForDropdownState
                        first pickup
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverForwardLimitSwitchPort.value == true
                        then moveToInputPositionForDropdownState;
                    transition moveToInputPositionForDropdownState_to_dropoff
                        first moveToInputPositionForDropdownState
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then dropoff;
                    transition dropoff_to_reset
                        first dropoff
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then reset;
                }
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
                        //TODO: This is not right yet
                        entry action resetting {
                            perform action moveCantileverBack;
                            perform action changeOccupiedOfCurrentTargetLocation;
                            perform action resetCurrentCmdValue;
                            assign selfEngine.actionCommandDone := true;
                        }
                    }
                    transition moveToInputPositionForPickupState_To_pickup
                        first moveToInputPositionForPickupState
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then pickup;
                    transition pickup_to_moveToStoragePositionForDropdownState
                        first pickup
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverForwardLimitSwitchPort.value == true
                        then moveToStoragePositionForDropdownState;
                    transition moveToStoragePositionForDropdownState_to_dropoff
                        first moveToStoragePositionForDropdownState
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then dropoff;
                    transition dropoff_to_reset
                        first dropoff
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverForwardLimitSwitchPort.value == true
                        then reset;
                }
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
                    transition moveToStoragePositionForPickupState_To_pickup
                        first moveToStoragePositionForPickupState
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then pickup;
                    transition pickup_to_moveToInputPositionForDropdownState
                        first pickup
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverForwardLimitSwitchPort.value == true
                        then moveToInputPositionForDropdownState;
                    transition moveToInputPositionForDropdownState_to_dropoff
                        first moveToInputPositionForDropdownState
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverBackwardLimitSwitchPort.value == true
                        then dropoff;
                    transition dropoff_to_reset
                        first dropoff
                        // if selfEngine.horizontalDecoderCount.value == selfEngine.currentTargetPosition.x
                        // and selfEngine.verticalDecoderCount.value == selfEngine.currentTargetPosition.y
                        // and selfEngine.cantileverForwardLimitSwitchPort.value == true
                        then reset;
                }
                //All transitions from states to cmdReceived
                transition topEmpt_midEmpt_bot_Empt_To_cmdReceived
                    first topEmpt_midEmpt_bot_Empt
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topFull_mid_empty_bot_Empt_To_cmdReceived
                    first topFull_midEmpt_bot_Empt
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topEmpt_midFull_bot_Empt_To_cmdReceived
                    first topEmpt_midFull_bot_Empt
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topEmpt_midEmpt_bot_Full_To_cmdReceived
                    first topEmpt_midEmpt_bot_Full
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topFull_midEmpt_bot_Full_To_cmdReceived
                    first topFull_midEmpt_bot_Full
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topFull_midFull_bot_Empt_To_cmdReceived
                    first topFull_midFull_bot_Empt
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topEmpt_midFull_botFull_To_cmdReceived
                    first topEmpt_midFull_botFull
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;

                transition topFull_midFull_bot_Full_To_cmdReceived
                    first topFull_midFull_bot_Full
                    accept cmdPort.cmdMsg via Software::SmallWarehouseAPI::SmallWarehouseEngine::cmdPort
                    then cmdReceived;
                comment about retrieveEmptyPallet_To_topEmpt_midEmpt_bot_Empt
                /*This runs into the following parser error:
                *[ERROR] SmallWarehouseSystem.sysml:<401,46>: 0xA0241 No SymTypeExpression could be derived for the FieldAccessExpression selfEngine.topPosition.occupied
                *I think this is due to not actually using the Boolean type
                */
                //All Transitions from retrieveEmptyPallet to states
                transition retrieveEmptyPallet_To_topEmpt_midEmpt_bot_Empt
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true 
                    // )
                        then topEmpt_midEmpt_bot_Empt;

                transition retrieveEmptyPallet_To_topFull_midEmpt_bot_Empt
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Empt;

                transition retrieveEmptyPallet_To_topEmpt_midFull_bot_Empt
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midFull_bot_Empt;

                transition retrieveEmptyPallet_To_topEmpt_midEmpt_bot_Full
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midEmpt_bot_Full;

                transition retrieveEmptyPallet_To_topFull_midEmpt_bot_Full
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Full;

                transition retrieveEmptyPallet_To_topFull_midFull_bot_Empt
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midFull_bot_Empt;

                transition retrieveEmptyPallet_To_topEmpt_midFull_botFull
                    first retrieveEmptyPallet
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midFull_botFull;

                //Transitions from store to states

                transition store_To_topFull_midEmpt_bot_Empt
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Empt;

                transition store_To_topEmpt_midFull_bot_Empt
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midFull_bot_Empt;

                transition store_To_topEmpt_midEmpt_bot_Full
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midEmpt_bot_Full;

                transition store_To_topFull_midEmpt_bot_Full
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Full;

                transition store_To_topFull_midFull_bot_Empt
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midFull_bot_Empt;

                transition store_To_topEmpt_midFull_botFull
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midFull_botFull;

                transition store_To_topFull_midFull_botFull
                    first store
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midFull_bot_Full;

                //Transitions from retrieveItem to states
                transition retrieveItem_To_topEmpt_midEmpt_bot_Empt
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Empt;

                transition retrieveItem_To_topFull_midEmpt_bot_Empt
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Empt;

                transition retrieveItem_To_topEmpt_midFull_bot_Empt
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midFull_bot_Empt;

                transition retrieveItem_To_topEmpt_midEmpt_bot_Full
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topEmpt_midEmpt_bot_Full;

                transition retrieveItem_To_topFull_midEmpt_bot_Full
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == false and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midEmpt_bot_Full;

                transition retrieveItem_To_topFull_midFull_bot_Empt
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == true and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == false and
                    //     selfEngine.actionCommandDone == true
                    // )
                        then topFull_midFull_bot_Empt;

                transition retrieveItem_To_topEmpt_midFull_botFull
                    first retrieveItem
                    // if (
                    //     selfEngine.topPosition.occupied == false and
                    //     selfEngine.middlePosition.occupied == true and
                    //     selfEngine.bottomPosition.occupied == true and
                    //     selfEngine.actionCommandDone == true
                    // )
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
                    //Error: [ERROR] SmallWarehouseSystem.sysml:<178,107>: 0xA0240 Cannot find symbol WarehouseCmdMsgType
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.cmdMsgGoal :=
                    //     WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET;
                }
                if cmdPort.cmdMsg.cmdMsgType == WarehouseCmdMsgType::RETRIEVE {
                    //assign currentCommand.cmdMsgGoal := SignalDefinitions::WarehouseCmdMsgType::RETRIEVE;
                }
                if cmdPort.cmdMsg.cmdMsgType == WarehouseCmdMsgType::STORE {
                    //assign currentCommand.cmdMsgGoal := Software::SignalDefinitions::WarehouseCmdMsgType::STORE;
                }
                if cmdPort.cmdMsg.storagePosition == StoragePosition::TOP {
                    // assign currentCommand.storagePositionEnum := StoragePosition::TOP;
                    // assign currentCommand.targetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                    //     x = topPosition.relativePosition.X_CONFIG,
                    //     y = topPosition.relativePosition.Y_CONFIG,
                    //     z = topPosition.relativePosition.Z_CONFIG
                    // );
                }
                if cmdPort.cmdMsg.storagePosition == StoragePosition::MIDDLE {
                    // assign currentCommand.storagePositionEnum := StoragePosition::MIDDLE;
                    // assign currentCommand.targetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                    //     x = middlePosition.relativePosition.X_CONFIG,
                    //     y = middlePosition.relativePosition.Y_CONFIG,
                    //     z = middlePosition.relativePosition.Z_CONFIG
                    // );
                }
                if cmdPort.cmdMsg.storagePosition == StoragePosition::BOTTOM {
                    // //  assign currentCommand.storagePositionEnum := StoragePosition::BOTTOM;
                    // assign currentCommand.targetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                    //     x = bottomPosition.relativePosition.X_CONFIG,
                    //     y = bottomPosition.relativePosition.Y_CONFIG,
                    //     z = bottomPosition.relativePosition.Z_CONFIG
                    // );
                }
                if cmdPort.cmdMsg.storagePosition == StoragePosition::INPUT {
                    // assign currentCommand.storagePositionEnum := StoragePosition::INPUT;
                    // assign currentCommand.targetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                    //     x = inputPosition.relativePosition.X_CONFIG,
                    //     y = inputPosition.relativePosition.Y_CONFIG,
                    //     z = inputPosition.relativePosition.Z_CONFIG
                    // );
                }
            }
            perform action resetCurrentCmdValue {
                assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.cmdMsgGoal := null;
                assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition := null;
                assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.storagePositionEnum := null;
            }
            //comment about moveToInputPositionForPickup, moveToInputPositionForDropdown, moveToStoragePositionForPickup, moveToStoragePositionForDropdown
            //the upper line apparently also runs in an parsing error since the parser doesnt allow multiple items to a command therefore I will just comment about the first, wherebu the comment is for all
            comment about moveToInputPositionForPickup
            /*
             * These actions should send the respective cmd messages to the motors, which will then move to the target position.
             * The send command leads to the following parsing error:
             * [ERROR] SmallWarehouseSystem.sysml:<163,63>: 0xA0240 Cannot find symbol targetPos
             * TODO: Check how to fix this error
             * The assign command leads to the following parsing error: 
             * [ERROR] SmallWarehouseSystem.sysml:<187,20>: 0xA0240 Cannot find symbol x
             */
            perform action moveToInputPositionForPickup {
                // send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // send Software::SignalDefinitions::GoToPosMotorCmdMsg(targetPos = Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.relativePosition.X_CONFIG) via Software::SmallWarehouseAPI::SmallWarehouseEngine::HorizontalCmdMsgPort.cmdMsg;
                // send GoToPosMotorCmdMsg(targetPos = inputPosition.relativePosition.Y_CONFIG - PICKUP_Y_OFFSET_CONFIG) via VerticalCmdMsgPort;
                // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //     x = Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.relativePosition.X_CONFIG,
                //     y = Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.relativePosition.Y_CONFIG - PICKUP_Y_OFFSET_CONFIG,
                //     z = CantileverPosition::BACKWARD_LIMIT
                // );
            }

            perform action moveToInputPositionForDropoff {
                // send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // send Software::SignalDefinitions::GoToPosMotorCmdMsg(targetPos = Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.relativePosition.X_CONFIG) via Software::SmallWarehouseAPI::SmallWarehouseEngine::HorizontalCmdMsgPort.cmdMsg;
                // send GoToPosMotorCmdMsg(targetPos = inputPosition.relativePosition.Y_CONFIG + STORING_Y_OFFSET_CONFIG) via VerticalCmdMsgPort;
                // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //     x = Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.relativePosition.X_CONFIG,
                //     y = Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.relativePosition.Y_CONFIG + STORING_Y_OFFSET_CONFIG,
                //     z = CantileverPosition::BACKWARD_LIMIT
                // );
            }

            perform action moveToStoragePositionForPickup {
                // send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // send Software::SignalDefinitions::GoToPosMotorCmdMsg(targetPos = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition.x) via Software::SmallWarehouseAPI::SmallWarehouseEngine::HorizontalCmdMsgPort;
                // send GoToPosMotorCmdMsg(targetPos = currentCommand.targetPosition.y - PICKUP_Y_OFFSET_CONFIG) via VerticalCmdMsgPort;
                // assign currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //     x = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition.x,
                //     y = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition.y - PICKUP_Y_OFFSET_CONFIG,
                //     z = CantileverPosition::FORWARD_LIMIT
                // );
            }
            perform action moveToStoragePositionForDropoff {
                // send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // send Software::SignalDefinitions::GoToPosMotorCmdMsg(targetPos = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition.x) via Software::SmallWarehouseAPI::SmallWarehouseEngine::HorizontalCmdMsgPort;
                // send GoToPosMotorCmdMsg(targetPos = currentCommand.targetPosition.y + STORING_Y_OFFSET_CONFIG) via VerticalCmdMsgPort;
                // assign currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //     x = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition.x,
                //     y = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentCommand.targetPosition.y + STORING_Y_OFFSET_CONFIG,
                //     z = CantileverPosition::BACKWARD_LIMIT
                // );
            }
            perform action pickup {
                // send GoToPosMotorCmdMsg(targetPos = currentTargetPosition.y + PICKUP_Y_OFFSET_CONFIG) via VerticalCmdMsgPort;
                // send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //    x = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.x,
                //    y = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.y + PICKUP_Y_OFFSET_CONFIG,
                //    z = CantileverPosition::FORWARD_LIMIT
                // );
            }
            perform action dropoff {
                // send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // send GoToPosMotorCmdMsg(targetPos = currentTargetPosition.y - STORING_Y_OFFSET_CONFIG) via VerticalCmdMsgPort;
                // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //    x = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.x,
                //    y = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.y - STORING_Y_OFFSET_CONFIG,
                //    z = CantileverPosition::FORWARD_LIMIT
                // );
            }
            perform action moveCantileverBack {
                // send GoBackwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //    x = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.x,
                //    y = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.y,
                //    z = CantileverPosition::BACKWARD_LIMIT
                // );
            }
            perform action moveCantileverForward {
                // send GoForwardTillReferenceCmdMsg() via CantileverCmdMsgPort;
                // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition := Software::SignalDefinitions::SmallWarehouseRelativePosition(
                //    x = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.x,
                //    y = Software::SmallWarehouseAPI::SmallWarehouseEngine::currentTargetPosition.y,
                //    z = CantileverPosition::FORWARD_LIMIT
                // );
            }
            perform action changeOccupiedOfCurrentTargetLocation {
                if (
                    currentCommand.storagePositionEnum == StoragePosition::TOP and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::STORE
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::topPosition.occupied := true;
                }
                if (
                    currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::middlePosition.occupied := false;
                }
                if (
                    currentCommand.storagePositionEnum == StoragePosition::MIDDLE and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::STORE
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::middlePosition.occupied := true;
                }
                if (
                    currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::bottomPosition.occupied := false;
                }
                if (
                    currentCommand.storagePositionEnum == StoragePosition::BOTTOM and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::STORE
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::bottomPosition.occupied := true;
                }
                if (
                    currentCommand.storagePositionEnum == StoragePosition::INPUT and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::STORE
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.occupied := true;
                }
                if (
                    currentCommand.storagePositionEnum == StoragePosition::INPUT and
                    currentCommand.cmdMsgGoal == Software::SignalDefinitions::WarehouseCmdMsgType::RETRIEVE_EMPTY_PALLET
                ) {
                    // assign Software::SmallWarehouseAPI::SmallWarehouseEngine::inputPosition.occupied := false;
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
             * TODO: Check how to fix this error
             */
            perform action setupMotor {
                //send Software::SignalDefinitions::SetupCmdMsg() via Software::SmallWarehouseAPI::SmallWarehouseEngine::HorizontalCmdMsgPort; // Didier did this in a similiar yet slightly different way, but since we use a separate engine part I think this could be better
                // send Software::SignalDefinitions::SetupCmdMsg() via VerticalCmdMsgPort;
                // send Software::SignalDefinitions::SetupCmdMsg() via CantileverCmdMsgPort;
            }
            comment about resetDecoderCounters
            /*
             * The action definition / usage pattern runs into parsing errors, not sure why. Therefore commented it out.
             * The following parsing error occurs:
             * [ERROR] SmallWarehouseSystem.sysml:<413,54>: 0xA0324 Cannot find symbol Decoder.Reset
             */
            action resetDecoderCounters {}
        }
        comment about Decoder
        /*
         * This is the QuadratureDecoder from Didier, which I used since he already modeled it to ensure compatibility.
         * Since the action definition / usage pattern runs into parsing errors, I changed reset to just the action def. 
         * Further the transitions are also causing the following parsing error:
         * [ERROR] SmallWarehouseSystem.sysml:<187,227>: no viable alternative at input 'Software::' in rule stack: [SysMLModel, SysMLElement, SysMLPackage, SysMLElement, SysMLPackage, SysMLElement, SysMLType, PartDef, SysMLElement, SysMLType, StateDef, SysMLElement, SysMLTransition, InlineActionUsage]
         * if Software::SmallWarehouseAPI::Decoder::DecoderCounterStates::selfDecoder.signalA.value == true and Software::SmallWarehouseAPI::Decoder::DecoderCounterStates::selfDecoder.signalB.value == false do Software::SmallWarehouseAPI::Decoder::DecoderCounterStates::selfDecoder.increment
         * TODO: Check how to fix the transitions
         */
        part def Decoder {
            in port signalA : Software::PortDefinitions::SensorPort;
            in port signalB : Software::PortDefinitions::SensorPort;

            out port decoderCount : Software::PortDefinitions::CounterPort;

            action def Reset {
                in selfDecoder : Software::SmallWarehouseAPI::Decoder;
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
                // transition dd_To_ud
                //     first dd
                //     if selfDecoder.signalA.value == true and selfDecoder.signalB.value == false do selfDecoder.increment
                //     then ud;
                // transition ud_To_dd
                //     first ud
                //     if selfDecoder.signalA.value == false and selfDecoder.signalB.value == false
                //         do selfDecoder.decrement
                //     then dd;

                // transition dd_To_du
                //     first dd
                //     if selfDecoder.signalA.value == false and selfDecoder.signalB.value == true do selfDecoder.decrement
                //     then du;
                // transition ud_To_uu
                //     first ud
                //     if selfDecoder.signalA.value == true and selfDecoder.signalB.value == true do selfDecoder.increment
                //     then uu;
                // transition du_To_dd
                //     first du
                //     if selfDecoder.signalA.value == false and selfDecoder.signalB.value == false
                //         do selfDecoder.increment
                //     then dd;
                // transition uu_To_ud
                //     first uu
                //     if selfDecoder.signalA.value == true and selfDecoder.signalB.value == false do selfDecoder.decrement
                //     then ud;
                // transition uu_To_du
                //     first uu
                //     if selfDecoder.signalA.value == false and selfDecoder.signalB.value == true do selfDecoder.increment
                //     then du;
            }
        }
    }
    //Signal Definition
    package SignalDefinitions {
        //Top-Level Warehouse Cmds
        item def WarehouseCmdMsg {
            attribute storagePosition : StoragePosition;
            attribute cmdMsgType : Software::SignalDefinitions::WarehouseCmdMsgType;
        }

        //Motor Cmds (I think it is not necessary to define a new message for each motor, since the direction is just dependent on the orientation of the motor)
        item def MotorCmdMsg {}
        item def GoToPosMotorCmdMsg :> Software::SignalDefinitions::MotorCmdMsg {
            attribute targetPos : Software::LibraryHelper::Integer;
        }
        item def StopMotorCmdMsg :> Software::SignalDefinitions::MotorCmdMsg {}
        item def SetupCmdMsg :> Software::SignalDefinitions::MotorCmdMsg {}
        item def GoBackwardTillReferenceCmdMsg :> Software::SignalDefinitions::MotorCmdMsg {}
        item def GoForwardTillReferenceCmdMsg :> Software::SignalDefinitions::MotorCmdMsg {}
        //Conveyor Cmds
        item def ConveyorCmdMsg {}
        item def ForwardCmdMsg :> Software::SignalDefinitions::ConveyorCmdMsg {}
        item def BackwardCmdMsg :> Software::SignalDefinitions::ConveyorCmdMsg {}
        item def StopConveyorCmdMsg :> Software::SignalDefinitions::ConveyorCmdMsg {}
        item def GoToPosConveyorCmdMsg :> Software::SignalDefinitions::ConveyorCmdMsg {
            attribute targetPos : Software::LibraryHelper::Integer;
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
            attribute x : Software::LibraryHelper::Integer;
            attribute y : Software::LibraryHelper::Integer;
            attribute z : Software::SignalDefinitions::CantileverPosition;
        }
        attribute def PositionConfig {
            attribute X_CONFIG : Software::LibraryHelper::Integer;
            attribute Y_CONFIG : Software::LibraryHelper::Integer;
            attribute Z_CONFIG : Software::LibraryHelper::Integer;
        }
        attribute def StorageLocationAttribute {
            attribute storagePosition : Software::SignalDefinitions::StoragePosition;
            attribute relativePosition : Software::SignalDefinitions::PositionConfig;
            attribute occupied : Software::LibraryHelper::Boolean;
        }
    }

    // Port Definitions
    private import SignalDefinitions::*;
    private import LibraryHelper::*;
    package PortDefinitions {
        port def APICmdPort {
            in item cmdMsg : Software::SignalDefinitions::WarehouseCmdMsg;
        }
        port def MotorCmdPort {
            in item cmdMsg : Software::SignalDefinitions::MotorCmdMsg;
        }
        port def ConveyorCmdPort {
            in item cmdMsg : Software::SignalDefinitions::ConveyorCmdMsg;
        }
        port def SensorPort {
            attribute value : Software::LibraryHelper::Boolean;
        }
        port def ActuatorPort {
            attribute value : Software::LibraryHelper::Boolean;
        }
        port def CounterPort {
            attribute value : Software::LibraryHelper::Integer;
        }
    }
}