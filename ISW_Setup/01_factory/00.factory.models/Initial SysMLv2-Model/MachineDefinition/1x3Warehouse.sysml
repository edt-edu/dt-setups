private import CmdTypes::*;
private import DataTypes::*;
private import StateTypes::*;
private import Factory::SoftwarePackage::API;
private import MachineModulesAPIs::*;
private import Factory::HardwarePackage::Hardware;
private import ModuleHardware::*;
private import Factory::*;
private import ScalarValues::*;
private import StateEnumerationDefinitionDef::*;
private import ActionEnumerationDefinition::*;
private import DataTypes::StorageLocation;
private import MovableItemPackage::MovableItem;
package Warehouse1x3 {
    private import ActionDefsWarehouse::*;
    part def Warehouse1x3API :> API {
        in port warehouse1x3APICmdPort :>> cmdAPIPort {
            in item warehouse1x3APICmdPort : Warehouse1x3Cmd;
        }
        in port warehouse1x3APIItemPort :>> itemPort {
            in item warehouse1x3APIItemPort : MovableItem;
        }
        out port warehouseStatePort :>> stateAPIPort {
            out item warehouse1x3APIStatePort : Warehouse1x3State;
        }

        part cantileverMotorAPI : CantileverMotorAPI;
        part horizontalMotorAPI : Horizontal1DActuatorAPI;
        part verticalMotorAPI : Vertical1DActuatorAPI;
        part conveyorBeltAPI : ConveyorBeltAPI;
        part engine {
            attribute STORAGE_OFFSET_CONFIG = 1; // the height offset applied for each storage cell before pickup (in sec)
            attribute PICKUP_HEIGHT_OFFSET_CONFIG = 1; // the height applied for the pick up movement of a pallet (in sec)
            attribute FORWARD_LIMIT_TIME_CONFIG = 0;
            attribute INACCURACY_OFFSET_CONFIG {
                attribute x = 0;
                attribute y = 0;
                attribute z = 0;
            }
            attribute currentPosition {
                attribute x = 0;
                attribute y = 0;
                attribute z = 0;
            }
            item currentWarehouse1x3State :>> stateAPI : Warehouse1x3State; // the current state of the machine in the Warehouse1x3
            item currentWarehouse1x3Cmd : Warehouse1x3Cmd; // the current command for the machine in the Warehouse1x3
            item currentCantileverCmd : CantileverCmd {} // the current command for the cantilever motor
            item currentHorizontalCmd : HorizontalCmd {} // the current command for the horizontal motor
            item currentVerticalCmd : VerticalCmd {} // the current command for the vertical motor
            item currentConveyorBeltCmd : ConveyorBeltCmd {} // the current command for the conveyor belt
            attribute currentTargetStorageLocation : StorageLocation1x3; // the current storage location of the machine in the Warehouse1x3

            attribute inputPosition : StorageLocation1x3 {
                storagePosition := StoragePosition::INPUT;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupiedByPallet :>> occupied := false;
                itemAtInput :>> storedItem := null;
            }
            attribute a : StorageLocation1x3 {
                storagePosition := StoragePosition1x3::A;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
                storedItem := null;
            }
            attribute b : StorageLocation1x3 {
                storagePosition := StoragePosition1x3::B;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
                storedItem := null;
            }
            attribute c : StorageLocation1x3 {
                storagePosition := StoragePosition1x3::C;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
                storedItem := null;
            }

            in port cantileverMotorStatePort {
                in item cantileverMotorState : CantileverState;
            }
            in port horizontalMotorStatePort {
                in item horizontalMotorState : HorizontalState;
            }
            in port verticalMotorStatePort {
                in item verticalMotorState : VerticalState;
            }
            in port conveyorBeltStatePort {
                in item conveyorBeltState : ConveyorBeltState;
            }
            out port cantileverMotorCmdPort {
                out item cantileverMotorCmd : CantileverCmd;
            }
            out port horizontalMotorCmdPort {
                out item horizontalMotorCmd : HorizontalCmd;
            }
            out port verticalMotorCmdPort {
                out item verticalMotorCmd : VerticalCmd;
            }
            out port conveyorBeltCmdPort {
                out item conveyorBeltCmd : ConveyorBeltCmd;
            }
            in port trailSensorUpperPort {
                in trailSensorUpper : Boolean;
            }
            in port trailSensorLowerPort {
                in trailSensorLower : Boolean;
            }
            in port lightBarrierInsidePort {
                in lightBarrierInside : Boolean;
            }
            in port lightBarrierOutsidePort {
                in lightBarrierOutside : Boolean;
            }
            in port horizontalEncoderPort {
                in horizontalEncoderImpulse1 : Boolean;
                in horizontalEncoderImpulse2 : Boolean;
            }
            in port verticalEncoderPort {
                in verticalEncoderImpulse1 : Boolean;
                in verticalEncoderImpulse2 : Boolean;
            }
            in port warehouse1x3CmdPort {
                in item warehouse1x3Cmd : Warehouse1x3Cmd;
            }
            in port warehouse1x3APIItemPort{
                in item warehouse1x3APIItem : MovableItem;
            }
            out port warehouse1x3StatePort {
                out item warehouse1x3State : Warehouse1x3State;
            }

            state def Warehouse1x3EngineStates {
                in warehouse1x3API : Warehouse1x3API;
                entry;
                then a_empty_b_empty_c_empty;

                state retrieve_empty_pallet {
                    entry action retrieve_empty_pallet {
                        perform action move_to_storage_position : Move_To_Storage_Location_Handler;
                        then perform action pickup_pallet : Pickup_Pallet_At_Storage_To_Store_Item_Handler;
                        then perform action move_to_input_position : Move_To_Input_Position_To_Store_Item_Handler;
                        then perform action dropdown_pallet_at_input : Dropdown_Pallet_At_Input_To_Store_Item_Handlder;
                        then perform action move_to_start : Move_To_Start;
                    }
                }

                state empty_pallet_at_input {
                    entry action dropdown_pallet_at_input {
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        then assign currentWarehouse1x3State.stateWarehouse :=
                            Warehouse1x3StateDef::EMPTY_PALLET_AT_INPUT;
                        then assign inputPosition.occupiedByPallet := true;
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        action wait_action while(currentWarehouse1x3Cmd.actionWarehouse != WarehouseActionDef::STORE) {
                            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                            assign currentWarehouse1x3Cmd.actionWarehouse := warehouse1x3CmdPort.warehouse1x3Cmd.actionWarehouse;
                        }
                    } 
                    exit action assign_current_warehouse_cmd {
                        assign currentWarehouse1x3Cmd.actionWarehouse := WarehouseActionDef::STORE;
                    }
                }

                state store {
                    entry action store {
                        perform action move_to_input_position : Move_To_Input_Position_To_Store_Item_Handler {}
                        perform action pickup_item_at_input_position : Pickup_Pallet_At_Input_To_Store_Item_Handler{}
                        perform action move_to_storage_position : Move_To_Storage_Location_To_Store_Item_Handler{}
                        perform action dropdown_item_at_storage_position : Dropdown_Pallet_At_Storage_To_Store_Item_Handler{}
                    } 
                }
                state retrieve_item{
                    entry action retrieve_item{
                        perform action move_to_storage_position : Move_To_Storage_Location_Handler{}
                        perform action pickup_item_at_storage_position : Pickup_Pallet_At_Storage_Handler{}
                        perform action move_to_input_position : Move_To_Input_Position_Handler{}
                        perform action dropdown_item_at_input : Dropdown_Pallet_At_Input_Handlder{}
                    }
                }

                state a_empty_b_empty_c_empty {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_full_b_empty_c_empty {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_empty_b_full_c_empty {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_empty_b_empty_c_full {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_full_b_full_c_empty {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_full_b_empty_c_full {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_empty_b_full_c_full {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state a_full_b_full_c_full {
                    entry action move_to_start_position {
                        perform action move_to_start : Move_To_Start {}
                        then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                        then assign currentWarehouse1x3State.currentWarehouseCmdState := null;
                        then send currentWarehouse1x3State via warehouseStatePort;
                    }
                    do action wait_for_new_cmd{
                        accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                    }
                }
                state store_error {
                    entry action store_error {
                        assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::ERROR;
                        assign currentWarehouse1x3State.failMessage := "Error: Storage position is occupied";
                        send currentWarehouse1x3State via warehouseStatePort;
                        then assign currentWarehouse1x3State.failMessage := "";
                    }
                }

                state retrieve_error{
                    entry action retrieve_error{
                        assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::ERROR;
                        assign currentWarehouse1x3State.failMessage := "Error: Storage position is empty";
                        send currentWarehouse1x3State via warehouseStatePort;
                        then assign currentWarehouse1x3State.failMessage := "";
                    }
                }
                //transitions from each state to retrieve_empty_pallet
                transition a_empty_b_empty_c_empty_to_retrieve_empty_pallet
                    first a_empty_b_empty_c_empty then retrieve_empty_pallet;
                transition a_full_b_empty_c_empty_to_retrieve_empty_pallet
                    first a_full_b_empty_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then retrieve_empty_pallet;
                transition a_empty_b_full_c_empty_to_retrieve_empty_pallet
                    first a_empty_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then retrieve_empty_pallet;
                transition a_empty_b_empty_c_full_to_retrieve_empty_pallet
                    first a_empty_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then retrieve_empty_pallet;

                transition a_full_b_full_c_empty_to_retrieve_empty_pallet
                    first a_full_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then retrieve_empty_pallet;
                transition a_full_b_empty_c_full_to_retrieve_empty_pallet
                    first a_full_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then retrieve_empty_pallet;
                transition a_empty_b_full_c_full_to_retrieve_empty_pallet
                    first a_empty_b_full_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then retrieve_empty_pallet;
                
                //error transitions from each state to retrieve_empty_pallet
                transition a_full_b_empty_c_empty_to_error
                    first a_full_b_empty_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then store_error; 
                transition a_empty_b_full_c_empty_to_error
                    first a_empty_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then store_error;
                transition a_empty_b_empty_c_full_to_error
                    first a_empty_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then store_error;
                transition a_full_b_full_c_full_to_retrieve_empty_pallet
                    first a_full_b_full_c_full if(currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM) then store_error;
                //transition from retrieve_empty_pallet to empty_pallet_at_input
                transition retrieve_empty_pallet_to_empty_pallet_at_input
                    first retrieve_empty_pallet then empty_pallet_at_input;

                //transition from empty_pallet_at_input to store
                transition empty_pallet_at_input_to_store first empty_pallet_at_input if (currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then store;

                //transition from store to the respective following state
                transition store_to_a_full_b_empty_C_empty first store if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_full_b_empty_c_empty;
                transition store_to_a_empty_b_full_C_empty first store if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_empty_b_full_c_empty;
                transition store_to_a_empty_b_empty_C_full first store if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_empty_b_empty_c_full;

                transition store_to_a_full_b_full_C_empty first store if (a.occupied == true and b.occupied == true and c.occupied == false and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_full_b_full_c_empty;
                transition store_to_a_full_b_empty_C_full first store if (a.occupied == true and b.occupied == false and c.occupied == true and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_full_b_empty_c_full;
                transition store_to_a_empty_b_full_c_full first store if (a.occupied == false and b.occupied == true and c.occupied == true and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_empty_b_full_c_full;
                transition store_to_a_full_b_full_C_full first store if (a.occupied == true and b.occupied == true and c.occupied == false and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE) then a_full_b_full_c_full;

                //transition from each state to retrieve_item
                transition a_full_b_empty_c_empty_retrieve_to_retrieve_item
                    first a_full_b_empty_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                transition a_empty_b_full_c_empty_retrieve_to_retrieve_item
                    first a_empty_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                transition a_empty_b_empty_c_full_retrieve_to_retrieve_item
                    first a_empty_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                transition a_full_b_full_c_empty_retrieve_to_retrieve_item
                    first a_full_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                transition a_full_b_empty_c_full_retrieve_to_retrieve_item
                    first a_full_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                transition a_empty_b_full_c_full_retrieve_to_retrieve_item
                    first a_empty_b_full_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                transition a_full_b_full_c_full_retrieve_to_retrieve_item
                    first a_full_b_full_c_full if (currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_item;
                
                //error transition from each state to retrieve_item 
                transition a_empty_b_empty_c_empty_retrieve_to_retrieve_error
                    first a_empty_b_empty_c_empty if (currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then store_error;
                transition a_full_b_empty_c_empty_retrieve_to_error
                    first a_full_b_empty_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_error;
                transition a_empty_b_full_c_empty_retrieve_to_error
                    first a_empty_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_error;
                transition a_empty_b_empty_c_full_retrieve_to_error
                    first a_empty_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_error;
                transition a_full_b_full_c_empty_retrieve_to_error
                    first a_full_b_full_c_empty if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_error;
                transition a_full_b_empty_c_full_retrieve_to_error
                    first a_full_b_empty_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_error;
                transition a_empty_b_full_c_full_retrieve_to_error
                    first a_empty_b_full_c_full if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then retrieve_error;
                
                //transition from retrieve_item to the respective following state
                transition retrieve_item_to_a_full_b_empty_C_empty first retrieve_item if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_full_b_empty_c_empty;
                transition retrieve_item_to_a_empty_b_full_C_empty first retrieve_item if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_empty_b_full_c_empty;
                transition retrieve_item_to_a_empty_b_empty_C_full first retrieve_item if (currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_empty_b_empty_c_full;
                transition retrieve_item_to_a_full_b_full_C_empty first retrieve_item if (a.occupied == true and b.occupied == true and c.occupied == false and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_full_b_full_c_empty;
                transition retrieve_item_to_a_full_b_empty_C_full first retrieve_item if (a.occupied == true and b.occupied == false and c.occupied == true and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_full_b_empty_c_full;
                transition retrieve_item_to_a_empty_b_full_C_full first retrieve_item if (a.occupied == false and b.occupied == true and c.occupied == true and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_empty_b_full_c_full;
                transition retrieve_item_to_a_full_b_full_C_full first retrieve_item if (a.occupied == true and b.occupied == true and c.occupied == false and currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE) then a_full_b_full_c_full;

            } 
        }
    }
    part def WarehouseHardware :> Hardware {
        part trailSensorLower : TrailSensor;
        part trailSensorUpper : TrailSensor;
        part horizontalMotor : HorizontalActuator1D;
        part verticalMotor : VerticalActuator1D;
        part cantileverMotor : CantileverMotor;
        part conveyorBelt : ConveyorBelt;
        part encoderHorizontal : Encoder;
        part encoderVertical : Encoder;
        part lightBarrierInside : LightBarrier;
        part lightBarrierOutside : LightBarrier;
    }
    part def Warehouse1x3System :> System {
        in port Warehouse1x3CmdPort {
            in item warehouse1x3CmdItem : Warehouse1x3Cmd;
        }
        in port Warehouse1x3ItemPort{
            in item warehouse1x3Item : MovableItem;
        }
        out port Warehouse1x3StatePort {
            out item stateWarehouse : Warehouse1x3State;
        }
        part warehouse1x3API : Warehouse1x3API;
        part warehouseHardware : WarehouseHardware;

        connect Warehouse1x3CmdPort to warehouse1x3API.warehouse1x3APICmdPort;
        connect warehouse1x3API.warehouse1x3APICmdPort to warehouse1x3API.engine.warehouse1x3CmdPort;
        connect warehouse1x3API.engine.warehouse1x3StatePort to warehouse1x3API.warehouseStatePort;
        connect warehouse1x3API.warehouseStatePort to Warehouse1x3StatePort;
        connect Warehouse1x3ItemPort to warehouse1x3API.warehouse1x3APIItemPort;

        connect warehouse1x3API.engine.cantileverMotorCmdPort
            to warehouse1x3API.cantileverMotorAPI.actuator1DAPICmdPort;
        connect warehouse1x3API.cantileverMotorAPI.forward1DActuatorPort
            to warehouseHardware.cantileverMotor.forwardActuatorPort;
        connect warehouse1x3API.cantileverMotorAPI.backward1DActuatorPort
            to warehouseHardware.cantileverMotor.backwardActuatorPort;
        connect warehouseHardware.cantileverMotor.backwardLimitSwitchPort
            to warehouse1x3API.cantileverMotorAPI.backwardLimitSwitchPort;
        connect warehouseHardware.cantileverMotor.forwardLimitSwitchPort
            to warehouse1x3API.cantileverMotorAPI.forwardLimitSwitchPort;
        connect warehouse1x3API.cantileverMotorAPI.actuator1DAPIStatePort
            to warehouse1x3API.engine.cantileverMotorStatePort;

        connect warehouse1x3API.engine.horizontalMotorCmdPort to warehouse1x3API.horizontalMotorAPI.horAPICmdPort;
        connect warehouse1x3API.horizontalMotorAPI.actHorLeftPort to warehouseHardware.horizontalMotor.actHorLeftPort;
        connect warehouse1x3API.horizontalMotorAPI.actHorRightPort to warehouseHardware.horizontalMotor.actHorRightPort;
        connect warehouseHardware.horizontalMotor.senseHorEndLeftPort
            to warehouse1x3API.horizontalMotorAPI.senseHorEndLeftPort;
        connect warehouse1x3API.horizontalMotorAPI.horAPIStatePort to warehouse1x3API.engine.horizontalMotorStatePort;

        connect warehouseHardware.encoderHorizontal.encoderSignalPort to warehouse1x3API.engine.horizontalEncoderPort;

        connect warehouse1x3API.engine.verticalMotorCmdPort to warehouse1x3API.verticalMotorAPI.verticalCmdPort;
        connect warehouse1x3API.verticalMotorAPI.actVertUpPort to warehouseHardware.verticalMotor.actVerticalUpPort;
        connect warehouse1x3API.verticalMotorAPI.actVertDownPort to warehouseHardware.verticalMotor.actVerticalDownPort;
        connect warehouseHardware.verticalMotor.senseVerticalEndDownPort
            to warehouse1x3API.verticalMotorAPI.senseVertEndDownPort;
        connect warehouse1x3API.verticalMotorAPI.verticalStatePort to warehouse1x3API.engine.verticalMotorStatePort;

        connect warehouseHardware.encoderVertical.encoderSignalPort to warehouse1x3API.engine.verticalEncoderPort;

        connect warehouse1x3API.engine.conveyorBeltCmdPort to warehouse1x3API.conveyorBeltAPI.conveyorBeltAPICmdPort;
        connect warehouse1x3API.conveyorBeltAPI.actConveyorBeltAPIForwardPort
            to warehouseHardware.conveyorBelt.actConveyorBeltForwardPort;
        connect warehouse1x3API.conveyorBeltAPI.actConveyorBeltAPIBackwardPort
            to warehouseHardware.conveyorBelt.actConveyorBeltBackwardPort;
        connect warehouse1x3API.conveyorBeltAPI.conveyorBeltAPIState to warehouse1x3API.engine.conveyorBeltStatePort;

        connect warehouseHardware.trailSensorLower to warehouse1x3API.engine.trailSensorLowerPort;
        connect warehouseHardware.trailSensorUpper to warehouse1x3API.engine.trailSensorUpperPort;

        connect warehouseHardware.lightBarrierInside to warehouse1x3API.engine.lightBarrierInsidePort;
        connect warehouseHardware.lightBarrierOutside to warehouse1x3API.engine.lightBarrierOutsidePort;
    }
    package ActionDefsWarehouse {
        private import Warehouse1x3API::engine::*;
        // This action needs to be implemented and should update the y-position of the machine in the Warehouse1x3 in seconds
        action def Position_Updater_Y {}
        // This action needs to be implemented and should update the x-position of the machine in the Warehouse1x3 in seconds
        action def Position_Updater_X {}

        // this action will move the machine to the start position which is the most top right point of the Warehouse1x3, so the [0,0,0] position
        action def Move_To_Start {
            perform action move_backward_cantilever : Cantilever_Backward_Till_Reference {}
            then perform action move_right_horizontal : Horizontal_Right_Till_Reference {}
            then perform action move_top_vertical : Vertical_up_till_reference {}
        }
        action def Move_To_Input_Position_Handler {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign currentWarehouse1x3State.currentWarehouseCmdState := currentWarehouse1x3Cmd;
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVING_TO_LOADING_POSITION;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            then perform action move_to_input : Move_To_Input {}
        }
         action def Move_To_Input_Position_To_Store_Item_Handler {
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVING_TO_LOADING_POSITION;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            then perform action move_to_input : Move_To_Input {}
         }
        action def Move_To_Storage_Location_Handler {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign currentWarehouse1x3State.currentWarehouseCmdState := currentWarehouse1x3Cmd;
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVE_TO_STORING_POSITION;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            then perform action move_to_current_cmd_location : Move_To_Storage_Location;
        }

        action def Move_To_Storage_Location_To_Store_Item_Handler{
            assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVE_TO_STORING_POSITION;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            then perform action move_to_current_cmd_location : Move_To_Storage_Location;
        }

        action def Dropdown_Pallet_At_Storage_Handler{
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign currentWarehouse1x3State.currentWarehouseCmdState := currentWarehouse1x3Cmd;
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            perform action dropdown_movement_at_storage_location : Dropdown_Movement_Storage {}
        }
        action def Dropdown_Pallet_At_Storage_To_Store_Item_Handler{
            assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            perform action dropdown_movement_at_storage_location : Dropdown_Movement_Storage {}
        }
        action def Pickup_Pallet_At_Storage_Handler {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign currentWarehouse1x3State.currentWarehouseCmdState := currentWarehouse1x3Cmd;
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            perform action pickup_movement_at_storage_location : Pickup_Movement_Storage {}
            then perform action change : Change_Occupied_Of_Current_Target_Location {}
        }
        action def Pickup_Pallet_At_Storage_To_Store_Item_Handler {
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            perform action pickup_movement_at_storage_location : Pickup_Movement_Storage {}
        }
        action def Dropdown_Pallet_At_Input_Handlder {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign currentWarehouse1x3State.currentWarehouseCmdState := currentWarehouse1x3Cmd;
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            perform action dropdown_movement_at_input : Dropdown_Movement_Input {}
            perform action change : Change_Occupied_Of_Current_Target_Location {}
        }
        action def Dropdown_Pallet_At_Input_To_Store_Item_Handlder{
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            perform action dropdown_movement_at_input : Dropdown_Movement_Input {}
        }
        action def Pickup_Pallet_At_Input_Handler {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            assign currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign currentWarehouse1x3State.currentWarehouseCmdState := currentWarehouse1x3Cmd;
            then assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            perform action pickup_movement_at_input : Pickup_Movement_Input {}
        }
        action def Pickup_Pallet_At_Input_To_Store_Item_Handler{
             assign currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            then send currentWarehouse1x3State via warehouse1x3StatePort;
            perform action pickup_movement_at_input : Pickup_Movement_Input {}
        }
        // this action will assign the current target storage location to the current target storage location variable
        action def Assign_Current_Target_Location {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            if warehouse1x3CmdPort.warehouse1x3Cmd.storagePosition == StoragePosition::A1 {
                assign currentTargetStorageLocation := a;
            }
            if warehouse1x3CmdPort.warehouse1x3Cmd.storagePosition == StoragePosition::A2 {
                assign currentTargetStorageLocation := b;
            }
            if warehouse1x3CmdPort.warehouse1x3Cmd.storagePosition == StoragePosition::A3 {
                assign currentTargetStorageLocation := c;
            }
        }
        action def Change_Occupied_Of_Current_Target_Location {
            accept warehouse1x3APIItemPort.warehouse1x3APIItem via warehouse1x3APIItemPort;
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::A and a.occupied == false {
                assign a.occupied := true;
                assign a.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::A and a.occupied == true {
                assign a.occupied := false;
                assign a.storedItem := null;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::B and b.occupied == false {
                assign b.occupied := true;
                assign b.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::B and b.occupied == true {
                assign b.occupied := false;
                assign b.storedItem := null;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::C and c.occupied == false {
                assign c.occupied := true;
                assign c.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::C and c.occupied == true {
                assign c.occupied := false;
                assign c.storedItem := null;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::INPUT and a.occupied == false {
                assign inputPosition.occupiedByPallet := true;
                assign inputPosition.itemAtInput := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::INPUT and a.occupied == true {
                assign inputPosition.occupiedByPallet := false;
                assign inputPosition.itemAtInput := null;
            }
        }
        action def Pickup_Movement_Input {
            then action adjust_vertical_position_up
            while (
                currentPosition.y <
                inputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action adjust_vertical_position_down
            while (
                currentPosition.y >
                inputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement_initial_positioning
            if (
                currentPosition.y ==
                inputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    inputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG);
            }
            perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
            then accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            then perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_vertical_up
            while (
                currentPosition.y >
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                currentPosition.y ==
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                    (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y);
            }
            then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
        }

        // this action is the movement for the pickup of a pallet, the machine has to be at the right X and Y postion
        action def Pickup_Movement_Storage {
            perform action vertical_adjustment_for_pickup : Align_Vertically_To_Current_Cmd_Location_For_Pickup;
            perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
            then accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            then perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_vertical_up
            while (
                currentPosition.y >
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                currentPosition.y ==
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                    (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y);
            }
            then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
        }
        action def Dropdown_Movement_Input {
            action adjust_vertical_position_up
            while (
                currentPosition.y <
                inputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action adjust_vertical_position_down
            while (
                currentPosition.y >
                inputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement_initial_positioning
            if (
                currentPosition.y ==
                inputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    inputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG);
            }
            then action move_vertical_down
            while (
                currentPosition.y <
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                currentPosition.y ==
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                    (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y);
            }
            then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
        }
        // this action is the movement for the dropdown of a pallet to the storage, the machine has to be at the right X and Y postion
        action def Dropdown_Movement_Storage {
            perform action vertical_adjustment_for_dropdown : Align_Vertically_To_Current_Cmd_Location_For_Dropdown;
            then perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
            then perform action assign_current_vertical_target_location : Assign_Current_Target_Location {}
            then action move_vertical_down
            while (
                currentPosition.y <
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                currentPosition.y ==
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                    (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y);
            }
            then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
            then action Change_Occupied_Of_Current_Target_Location {}
        }

        // this is for the horizontal movement alignement of the machine to the current horizontal position given from the Warehouse1x3CmdPort
        // the horizontal positions are always given in perspective to X=0
        action def Align_Horizontally_To_Current_Cmd_Location {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            then perform action assign_current_target_location : Assign_Current_Target_Location {}
            action move_to_slot_horizontal_right
            while (
                currentPosition.x < currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x
            ) {
                perform action position_updater_x : Position_Updater_X {}
                then assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::MOVE_RIGHT;
                then send currentHorizontalCmd via horizontalMotorCmdPort;
            }
            then action move_to_slot_horizontal_left
            while (
                currentPosition.x > currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x
            ) {
                perform action position_updater_x : Position_Updater_X {}
                then assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::MOVE_LEFT;
                then send currentHorizontalCmd via horizontalMotorCmdPort;
            }
            then action stop_horizontal_movement
            if (
                currentPosition.x ==
                currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x
            ) {
                assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::STOP;
                then send currentHorizontalCmd via horizontalMotorCmdPort;
                then assign currentPosition.x :=
                    currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x;
            }
        }
        action def Align_Vertically_To_Current_Cmd_Location {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_to_slot_vertical_up
            while (
                currentPosition.y > currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG + INACCURACY_OFFSET_CONFIG.y
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action move_to_slot_vertical_down
            while (
                currentPosition.y < currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG + INACCURACY_OFFSET_CONFIG.y
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if currentPosition.y == currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG + INACCURACY_OFFSET_CONFIG.y {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                currentYPosition := currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG + INACCURACY_OFFSET_CONFIG.y;
            }
        }
        // This action will Align vertically for pickup for a given storage location via the Warehouse1x3CmdPort (it will move the machine to the right Y position)
        action def Align_Vertically_To_Current_Cmd_Location_For_Pickup {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_to_slot_vertical_up
            while (
                currentPosition.y >
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action move_to_slot_vertical_down
            while (
                currentPosition.y <
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                currentPosition.y ==
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                currentYPosition :=
                    currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                    (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y);
            }
        }
        // This action will align vertically for dropdown for a given storage location via the Warehouse1x3CmdPort (it will move the machine to the right Y position)
        action def Align_Vertically_To_Current_Cmd_Location_For_Dropdown {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_to_slot_vertical_to_store
            while (
                currentPosition.y >
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action updatePosition : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action move_to_slot_vertical_down
            while (
                currentPosition.y <
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                currentPosition.y ==
                currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y :=
                    currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                    (STORAGE_OFFSET_CONFIG + INACCURACY_OFFSET_CONFIG.y);
            }
        }
        action def Move_To_Input {
            perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
            then perform action move_right_horizontal_switch : Horizontal_Right_Till_Reference {}
            then action adjust_vertical_position_up
            while currentPosition.y < inputPosition.VERTICAL_LOCATION_CONFIG + STORAGE_OFFSET_CONFIG {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action adjust_vertical_position_down
            while currentPosition.y > inputPosition.VERTICAL_LOCATION_CONFIG + STORAGE_OFFSET_CONFIG {
                perform action position_updater_y : Position_Updater_Y {}
                then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send currentVerticalCmd via verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if currentPosition.y == inputPosition.VERTICAL_LOCATION_CONFIG + STORAGE_OFFSET_CONFIG {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y := inputPosition.VERTICAL_LOCATION_CONFIG - STORAGE_OFFSET_CONFIG;
            }
        }
        action def Move_To_Storage_Location {
            perform action move_backward_cantilever : Cantilever_Backward_Till_Reference;
            then perform action move_horizontaly_to_current_command_location
                : Align_Horizontally_To_Current_Cmd_Location;
            then perform action move_vertically_to_current_command_location : Align_Vertically_To_Current_Cmd_Location;
        }
        // this action will move the cantilever back to the backward reference point
        action def Cantilever_Backward_Till_Reference {
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            action move_backward_cantilever
            while (
                cantileverMotorStatePort.cantileverMotorState.stateCantilever !=
                CantileverMotorStateDef::AT_BACKWARD_LIMIT
            ) {
                accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort; // not sure if necessary
                then assign currentCantileverCmd.actionCantilever := CantileverActionDef::MOVE_BACKWARD;
                then send currentCantileverCmd via cantileverMotorCmdPort;
            }
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            action stop_backward_cantilever
            if (
                cantileverMotorStatePort.cantileverMotorState.stateCantilever ==
                CantileverMotorStateDef::AT_BACKWARD_LIMIT
            ) {
                assign currentCantileverCmd.actionCantilever := CantileverActionDef::STOP;
                then send currentCantileverCmd via cantileverMotorCmdPort;
                then assign currentPosition.z := 0;
            }
        }

        // this action will move the cantilever forward to the forward reference point
        action def Cantilever_Forward_Till_Reference {
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            while cantileverMotorStatePort.cantileverMotorState != CantileverMotorStateDef::AT_FORWARD_LIMIT {
                accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
                then assign currentCantileverCmd.actionCantilever := CantileverActionDef::MOVE_FORWARD;
                then send currentCantileverCmd via cantileverMotorCmdPort;
            }
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            then action stop_cantilever_forward
            if (
                cantileverMotorStatePort.cantileverMotorState.stateCantilever ==
                CantileverMotorStateDef::AT_FORWARD_LIMIT
            ) {
                assign currentCantileverCmd.actionCantilever := CantileverActionDef::STOP;
                then send currentCantileverCmd via cantileverMotorCmdPort;
                then assign currentPosition.z := FORWARD_LIMIT_TIME_CONFIG; // the time the machine needs to move to the forward limit, needs to be set (in sec)
            }
        }

        // this action will move the machine to the right reference point
        action def Horizontal_Right_Till_Reference {
            accept horizontalMotorStatePort.horizontalMotorState via horizontalMotorStatePort;
            action move_right_horizontal_switch
            while (
                horizontalMotorStatePort.horizontalMotorState.stateHorizontal !=
                HorizontalStateDef::AT_RIGHT_LIMIT_SWITCH
            ) {
                accept horizontalMotorStatePort.horizontalMotorState via horizontalMotorStatePort;
                then assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::MOVE_RIGHT;
                then send currentHorizontalCmd via horizontalMotorCmdPort;
            }
            accept horizontalMotorStatePort.horizontalMotorState via horizontalMotorStatePort;
            action stop_right_horizontal
            if (
                horizontalMotorStatePort.horizontalMotorState.stateHorizontal ==
                HorizontalStateDef::AT_RIGHT_LIMIT_SWITCH
            ) {
                assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::STOP;
                then send currentHorizontalCmd via horizontalMotorCmdPort;
                then assign currentPosition.x := 0;
            }
        }

        // this action will move the machine to the top reference point
        action def Vertical_up_till_reference {
            accept verticalMotorStatePort.verticalMotorState via verticalMotorStatePort;
            action move_top_vertical_switch
            while verticalMotorStatePort.verticalMotorState.stateVertical != VerticalStateDef::AT_TOP_LIMIT_SWITCH {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                send currentVerticalCmd via verticalMotorCmdPort;
            }
            accept verticalMotorStatePort.verticalMotorState via verticalMotorStatePort;
            then action stop_top_vertical
            if verticalMotorStatePort.verticalMotorState.stateVertical == VerticalStateDef::AT_TOP_LIMIT_SWITCH {
                assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send currentVerticalCmd via verticalMotorCmdPort;
                then assign currentPosition.y := 0;
            }
        }
    }
}