private import CmdTypes::*;
private import DataTypes::*;
private import StateTypes::*;
private import Factory::SoftwarePackage::API;
private import MachineModulesAPIs::*;
private import Factory::HardwarePackage::Hardware;
private import ModuleHardware::*;
private import Factory::*;
private import ScalarValues::*;
package Warehouse {
    part def WarehouseAPI :> API {
        in port warehouseAPICmdPort :>> cmdAPIPort {
            in item warehouseAPICmdPort : WarehouseCmd;
        }
        out port warehouseStatePort :>> stateAPIPort {
            out item warehouseAPIStatePort : WarehouseState;
        }

        part cantileverMotorAPI : CantileverMotorAPI;
        part horizontalMotorAPI : Horizontal1DActuatorAPI;
        part verticalMotorAPI : Vertical1DActuatorAPI;
        part conveyorBeltAPI : ConveyorBeltAPI;

        part engine {
            private import StateEnumerationDefinitionDef::*;
            private import ActionEnumerationDefinition::*;
            private import DataTypes::StorageLocation;
            attribute STORAGE_OFFSET_CONFIG = 1; // the height offset applied for each storage cell before pickup (in sec)
            attribute PICKUP_HEIGHT_OFFSET_CONFIG = 1; // the height applied for the pick up movement of a pallet (in sec)
            attribute INACCURACY_OFFSET_CONFIG {
                attribute x = 0;
                attribute y = 0;
                attribute z = 0;
            }
            attribute init_forward_limit_time = 0; // the time the machine needs to move to the forward limit, needs to be set (in sec)
            // the current position of the machine in the warehouse (in sec)
            attribute currentPosition {
                attribute x = 0;
                attribute y = 0;
                attribute z = 0;
            }

            item currentWarehouseState :>> stateAPI : WarehouseState; // the current state of the machine in the warehouse
            item currentWarehouseCmd : WarehouseCmd; // the current command for the machine in the warehouse
            item currentCantileverCmd : CantileverCmd {} // the current command for the cantilever motor
            item currentHorizontalCmd : HorizontalCmd {} // the current command for the horizontal motor
            item currentVerticalCmd : VerticalCmd {} // the current command for the vertical motor
            item currentConveyorBeltCmd : ConveyorBeltCmd {} // the current command for the conveyor belt
            // assume every storage slot has a empty pallet stored in it, if the machine has stored an item in a space, set occupied to true
            // the storage locations have to be defined
            attribute InputPosition : StorageLocation {
                storagePosition := StoragePosition::INPUT;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupiedByPallet :>> occupied := false;
            }
            attribute A1Position : StorageLocation {
                storagePosition := StoragePosition::A1;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute A2Position : StorageLocation {
                storagePosition := StoragePosition::A2;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute A3Position : StorageLocation {
                storagePosition := StoragePosition::A3;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute B1Position : StorageLocation {
                storagePosition := StoragePosition::B1;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute B2Position : StorageLocation {
                storagePosition := StoragePosition::B2;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute B3Position : StorageLocation {
                storagePosition := StoragePosition::B3;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute C1Position : StorageLocation {
                storagePosition := StoragePosition::C1;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute C2Position : StorageLocation {
                storagePosition := StoragePosition::C2;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            attribute C3Position : StorageLocation {
                storagePosition := StoragePosition::C3;
                VERTICAL_LOCATION_CONFIG := 1;
                HORIZONTAL_LOCATION_CONFIG := 1;
                DEPTH_LOCATION_CONFIG := 1;
                occupied := false;
            }
            
            attribute currentTargetStorageLocation : StorageLocation; // the current storage location of the machine in the warehouse

            attribute verticalEncoderPosition : Integer; // to inaccurate to use the real encoder value, so we use a movement duration in seconds instead
            attribute horizontalEncoderPosition : Integer; // to inaccurate to use the real encoder value, so we use a movement duration in seconds instead

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
            in port warehouseCmdPort {
                in item warehouseCmd : WarehouseCmd;
            }
            out port warehouseStatePort {
                out item warehouseState : WarehouseState;
            }

            // This action needs to be implemented and should update the y-position of the machine in the warehouse in seconds
            action def Position_Updater_Y {}
            // This action needs to be implemented and should update the x-position of the machine in the warehouse in seconds
            action def Position_Updater_X {}

            // this action will move the machine to the start position which is the most top right point of the warehouse, so the [0,0,0] position
            action def Move_To_Start {
                assign currentWarehouseState.stateWarehouse := WarehouseStateDef::MOVING_TO_START_POSITION;
                then send currentWarehouseState via warehouseStatePort;
                perform action move_backward_cantilever : Cantilever_Backward_Till_Reference {}
                then perform action move_right_horizontal : Horizontal_Right_Till_Reference {}
                then perform action move_top_vertical : Vertical_up_till_reference {}
            }
            // this action will assign the current target storage location to the current target storage location variable
            action def Assign_Current_Target_Location {
                accept warehouseCmdPort.warehouseCmd via warehouseCmdPort;
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A1 {
                    assign currentTargetStorageLocation := A1Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A2 {
                    assign currentTargetStorageLocation := A2Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A3 {
                    assign currentTargetStorageLocation := A3Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B1 {
                    assign currentTargetStorageLocation := B1Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B2 {
                    assign currentTargetStorageLocation := B2Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B3 {
                    assign currentTargetStorageLocation := B3Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C1 {
                    assign currentTargetStorageLocation := C1Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C2 {
                    assign currentTargetStorageLocation := C2Position;
                }
                if warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C3 {
                    assign currentTargetStorageLocation := C3Position;
                }
            }
            // this action is the movement for the pickup of a pallet, the machine has to be at the right X and Y postion
            action def Pickup_movement {
                perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
                then accept warehouseCmdPort.warehouseCmd via warehouseCmdPort;
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
            // this action is the movement for the dropdown of a pallet to the storage, the machine has to be at the right X and Y postion
            action def Dropdown_Movement {
                perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
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
            }
            // this is for the horizontal movement alignement of the machine to the current horizontal position given from the warehouseCmdPort
            // the horizontal positions are always given in perspective to X=0
            action def Align_Horizontally_To_Current_Cmd_Location {
                accept warehouseCmdPort.warehouseCmd via warehouseCmdPort;
                then perform action assign_current_target_location : Assign_Current_Target_Location {}
                action move_to_slot_horizontal_right
                while (
                    currentPosition.x <
                    currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x
                ) {
                    perform action position_updater_x : Position_Updater_X {}
                    then assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::MOVE_RIGHT;
                    then send currentHorizontalCmd via horizontalMotorCmdPort;
                }
                then action move_to_slot_horizontal_left
                while (
                    currentPosition.x >
                    currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x
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

            // This action will Align vertically for pickup for a given storage location via the warehouseCmdPort (it will move the machine to the right Y position)
            action def Align_Vertically_To_Current_Cmd_Location_For_Pickup {
                accept warehouseCmdPort.warehouseCmd via warehouseCmdPort;
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
            // This action will align vertically for dropdown for a given storage location via the warehouseCmdPort (it will move the machine to the right Y position)
            action def Align_Vertically_To_Current_Cmd_Location_For_Dropdown {
                accept warehouseCmdPort.warehouseCmd via warehouseCmdPort;
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
            // this action will move the machine to the right position for the pickup of a pallet at the input station
            action def Move_To_Input_For_Pickup {
                perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
                then perform action move_right_horizontal_switch : Horizontal_Right_Till_Reference {}
                then action adjust_vertical_position_up
                while (
                    currentPosition.y <
                    InputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
                ) {
                    perform action position_updater_y : Position_Updater_Y {}
                    then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                    then send currentVerticalCmd via verticalMotorCmdPort;
                }
                then action adjust_vertical_position_down
                while (
                    currentPosition.y >
                    InputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
                ) {
                    perform action position_updater_y : Position_Updater_Y {}
                    then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                    then send currentVerticalCmd via verticalMotorCmdPort;
                }
                then action stop_vertical_movement
                if (
                    currentPosition.y ==
                    InputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
                ) {
                    assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                    then send currentVerticalCmd via verticalMotorCmdPort;
                    then assign currentPosition.y :=
                        InputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG);
                }
            }
            // this action will move the machine to the right position for the dropdown of a pallet at the input station
            action def Move_To_Input_For_Dropdown {
                perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
                then perform action move_right_horizontal_switch : Horizontal_Right_Till_Reference {}
                then action adjust_vertical_position_up
                while (
                    currentPosition.y <
                    InputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
                ) {
                    perform action position_updater_y : Position_Updater_Y {}
                    then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                    then send currentVerticalCmd via verticalMotorCmdPort;
                }
                then action adjust_vertical_position_down
                while (
                    currentPosition.y >
                    InputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
                ) {
                    perform action position_updater_y : Position_Updater_Y {}
                    then assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                    then send currentVerticalCmd via verticalMotorCmdPort;
                }
                then action stop_vertical_movement
                if (
                    currentPosition.y ==
                    InputPosition.VERTICAL_LOCATION_CONFIG - (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG)
                ) {
                    assign currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                    then send currentVerticalCmd via verticalMotorCmdPort;
                    then assign currentPosition.y :=
                        InputPosition.VERTICAL_LOCATION_CONFIG + (INACCURACY_OFFSET_CONFIG.y + STORAGE_OFFSET_CONFIG);
                }
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
                    then assign currentPosition.z := init_forward_limit_time; // the time the machine needs to move to the forward limit, needs to be set (in sec)
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
            state warhouseState : WarehouseAPIStates {
                in warehouseAPI = WarehouseAPI::self;
            }

            state def WarehouseAPIStates {
                in warehouseAPI : WarehouseAPI;
                entry;
                then idle;
                state idle {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::IDLE;
                        then assign currentWarehouseState.currentWarehouseCmdState := null;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                state start_position {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::AT_START_POSITION;
                        then assign currentWarehouseState.currentWarehouseCmdState := null;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                state pallet_picked_up {
                    action update_state {
                        assign currentWarehouseState.stateWarehouse :=
                            WarehouseStateDef::RETRIEVE_EMPTY_PALLET_HAS_PALLET_FROM_STORAGE;
                        then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                    }
                    entry send currentWarehouseState via warehouseStatePort;
                }
                state at_loading_position {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::AT_LOADING_POSITION;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                state loading_filled_with_pallet {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::EMPTY_PALLET_AT_INPUT;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                state store {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::STORING;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                state picked_up_item_from_storage {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::RETRIEVING_ITEM;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                state resolveable_error {
                    entry action update_state {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::RESOLVING_FAILURE;
                        then send currentWarehouseState via warehouseStatePort;
                    }
                }
                // in this transition the machine moves to the start position which is the most top right point of the warehouse, so the [0,0,0] position, whereby movement distance (duration) is measured in seconds
                transition idle_to_start_position
                    first idle do action move_to_start_action : Move_To_Start {} then start_position;

                // this transition starts the process of picking up a pallet from the high bay and delivering it to the input
                transition start_position_to_pallet_picked_up
                    first start_position
                    accept warehouseCmdPort.warehouseCmd via warehouseCmdPort
                    if (
                        (warehouseCmdPort.warehouseCmd.actionWarehouse ==
                            WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM and
                            (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A1 and
                                A1Position.occupied == false)) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A2 and
                            A2Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A3 and
                            A3Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B1 and
                            B1Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B2 and
                            B2Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B3 and
                            B3Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C1 and
                            C1Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C2 and
                            C2Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C3 and
                            C3Position.occupied == false)
                    )
                        do action get_pallet {
                            assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                            then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                            then assign currentWarehouseState.stateWarehouse :=
                                WarehouseStateDef::RETRIEVE_EMPTY_PALLET_GET_PALLET_FROM_STORAGE;
                            then send currentWarehouseState via warehouseStatePort;
                            then perform action vertical_movement_to_storage_location
                                : Align_Vertically_To_Current_Cmd_Location_For_Pickup {}
                            then perform action horizontal_movement_to_storage_location
                                : Align_Horizontally_To_Current_Cmd_Location {}
                            then perform action pickup_movement_from_storage_location : Pickup_movement {}
                        }
                    then pallet_picked_up;

                // this transition is for the movement of the machine to the input position for the dropdown of a pallet
                transition pallet_picked_up_to_at_loading_position
                    first pallet_picked_up
                    do action move_to_loading_position {
                        assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                        then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                        then assign currentWarehouseState.stateWarehouse :=
                            WarehouseStateDef::RETRIEVE_EMPTY_PALLET_HAS_PALLET_FROM_STORAGE;
                        then send currentWarehouseState via warehouseStatePort;
                        then perform action move_to_input_for_dropdown : Move_To_Input_For_Dropdown {}
                    }
                    then at_loading_position;

                //this transition is for the movement and control of the conveyor belts in order to dropdown a pallet at the input station
                transition at_loading_position_to_loading_filled_pallet
                    first at_loading_position
                    do action drop_pallet {
                        assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                        then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                        then assign currentWarehouseState.stateWarehouse :=
                            WarehouseStateDef::RETRIEVING_DROPPING_OFF_PALLET_AT_LOADING_POSITION;
                        then send currentWarehouseState via warehouseStatePort;
                        then assign currentConveyorBeltCmd.actionConveyorBelt := ConveyorBeltActionDef::MOVE_FORWARD;
                        then send currentConveyorBeltCmd via conveyorBeltCmdPort;
                        then perform action dropdown_movement : Dropdown_Movement {}
                        then assign currentConveyorBeltCmd.actionConveyorBelt := ConveyorBeltActionDef::STOP_MOVING;
                        then send currentConveyorBeltCmd via conveyorBeltCmdPort;
                    }
                    then loading_filled_with_pallet;

                //this transition sets the palletAtInput to true if a pallet is at the input station
                transition loading_filled_pallet_to_idle
                    first loading_filled_with_pallet
                    do action set_pallet_value {
                        assign InputPosition.occupiedByPallet := true;
                    }
                    then idle;

                // this transition move the machine to the input position and picks up a pallet from the input station
                transition start_position_to_store
                    first start_position
                    accept warehouseCmdPort.warehouseCmd via warehouseCmdPort
                    if (
                        warehouseCmdPort.warehouseCmd.actionWarehouse == WarehouseActionDef::STORE and
                        InputPosition.occupiedByPallet == true
                    )
                        do action pickup {
                            assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                            then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                            then assign currentWarehouseState.stateWarehouse := WarehouseStateDef::STORING;
                            then send currentWarehouseState via warehouseStatePort;
                            then assign currentConveyorBeltCmd.actionConveyorBelt :=
                                ConveyorBeltActionDef::MOVE_BACKWARD;
                            then send currentConveyorBeltCmd via conveyorBeltCmdPort;
                            then perform action move_to_input_for_pickup : Move_To_Input_For_Pickup {}
                            then perform action pickup_movement : Pickup_movement {}
                            then assign currentConveyorBeltCmd.actionConveyorBelt := ConveyorBeltActionDef::STOP_MOVING;
                            then send currentConveyorBeltCmd via conveyorBeltCmdPort;
                            then assign InputPosition.occupiedByPallet := false;
                        }
                    then store;

                //this transition is for the dropoff of a pallet to the storage given location
                transition store_to_idle
                    first store
                    do action storing {
                        perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
                        then perform action align_vertically_for_dropdown
                            : Align_Vertically_To_Current_Cmd_Location_For_Dropdown {}
                        then perform action align_horizontally_for_dropdown
                            : Align_Horizontally_To_Current_Cmd_Location {}
                        then perform action drop_pallet : Dropdown_Movement {}
                    }
                    then idle;

                //this transition is for the pickup of a pallet with an item from the storage and is the start of the Retrieving process of an item from the storage
                transition start_position_to_picked_up_pallet_with_item
                    first start_position
                    accept warehouseCmdPort.warehouseCmd via warehouseCmdPort
                    if (
                        (warehouseCmdPort.warehouseCmd.actionWarehouse == WarehouseActionDef::RETRIEVE and
                            (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A1 and
                                A1Position.occupied == true)) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A2 and
                            A2Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A3 and
                            A3Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B1 and
                            B1Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B2 and
                            B2Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B3 and
                            B3Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C1 and
                            C1Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C2 and
                            C2Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C3 and
                            C3Position.occupied == true)
                    )
                        do action get_pallet_with_item {
                            assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                            then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                            then assign currentWarehouseState.stateWarehouse := WarehouseStateDef::RETRIEVING_ITEM;
                            then send currentWarehouseState via warehouseStatePort;
                            then perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
                            then perform action vertical_alignment_for_pickup
                                : Align_Vertically_To_Current_Cmd_Location_For_Pickup {}
                            then perform action horizontal_alignement_for_pickup
                                : Align_Horizontally_To_Current_Cmd_Location {}
                            then perform action pick_up_pallet : Pickup_movement {}
                        }
                    then picked_up_item_from_storage;

                //this transition is for the movement of the machine to the input position and the dropdown of a pallet
                transition picked_up_pallet_with_item_to_idle
                    first picked_up_item_from_storage
                    do action deliver_pallet {
                        assign currentWarehouseState.stateWarehouse := WarehouseStateDef::RETRIEVING_ITEM;
                        then send currentWarehouseState via warehouseStatePort;
                        then perform action move_to_pickup : Move_To_Input_For_Dropdown {}
                        then perform action drop_pallet_with_item {
                            perform action start_conveyor_forward {
                                assign currentConveyorBeltCmd.actionConveyorBelt := ConveyorBeltActionDef::MOVE_FORWARD;
                                then send currentConveyorBeltCmd via conveyorBeltCmdPort;
                            }
                            then perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
                            then perform action stop_conveyor_forward {
                                assign currentConveyorBeltCmd.actionConveyorBelt := ConveyorBeltActionDef::STOP_MOVING;
                                then send currentConveyorBeltCmd via conveyorBeltCmdPort;
                            }
                            then perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
                        }
                    }
                    then idle;

                //this transition is to resolve the error and move the machine in the idle state
                transition resolvable_error_to_idle first resolveable_error then idle;

                // this transition is for the error case, if an pallet should be retrieved from the high bay, but the input is already occupied
                transition start_position_input_occupied_to_error
                    first start_position
                    accept warehouseCmdPort.warehouseCmd via warehouseCmdPort
                    if (
                        (warehouseCmdPort.warehouseCmd.actionWarehouse ==
                            WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM and
                            (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A1 and
                                A1Position.occupied == true)) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A2 and
                            A2Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A3 and
                            A3Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B1 and
                            B1Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B2 and
                            B2Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B3 and
                            B3Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C1 and
                            C1Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C2 and
                            C2Position.occupied == true) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C3 and
                            C3Position.occupied == true)
                    )
                        do action send_fail_state {
                            assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                            then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                            then assign currentWarehouseState.stateWarehouse := WarehouseStateDef::ACTION_FAILED;
                            then assign currentWarehouseState.failMessage := "Storage location is already occupied";
                            then send currentWarehouseState via warehouseStatePort;
                            then assign currentWarehouseState.failMessage := "";
                        }
                    then resolveable_error;

                //this transition is for the error case, if an item should be retrieved from the storage, but the given storage location is empty
                transition start_position_error_retrieve_empty_to_resolvable_error
                    first start_position
                    accept warehouseCmdPort.warehouseCmd via warehouseCmdPort
                    if (
                        (warehouseCmdPort.warehouseCmd.actionWarehouse == WarehouseActionDef::RETRIEVE and
                            (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A1 and
                                A1Position.occupied == false)) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A2 and
                            A2Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::A3 and
                            A3Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B1 and
                            B1Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B2 and
                            B2Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::B3 and
                            B3Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C1 and
                            C1Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C2 and
                            C2Position.occupied == false) or
                        (warehouseCmdPort.warehouseCmd.storagePosition == StoragePosition::C3 and
                            C3Position.occupied == false)
                    )
                        do action fail {
                            assign currentWarehouseCmd := warehouseCmdPort.warehouseCmd;
                            then assign currentWarehouseState.currentWarehouseCmdState := currentWarehouseCmd;
                            then assign currentWarehouseState.stateWarehouse := WarehouseStateDef::ACTION_FAILED;
                            then assign currentWarehouseState.failMessage := "Storage location is empty";
                            then send currentWarehouseState via warehouseStatePort;
                            then assign currentWarehouseState.failMessage := "";
                        }
                    then resolveable_error;

                //this transition is for the error case, if an item should be retrieved from the storage, but the input is occupied by a pallet
                transition start_position_error_retrieve_blocked_to_resolvable_error
                    first start_position
                    if (
                        warehouseCmdPort.warehouseCmd.actionWarehouse == WarehouseActionDef::RETRIEVE and
                        InputPosition.occupiedByPallet == true
                    )
                        do action fail {
                            assign currentWarehouseState.stateWarehouse := WarehouseStateDef::ACTION_FAILED;
                            then assign currentWarehouseState.failMessage := "Input is occupied by a pallet";
                            then send currentWarehouseState via warehouseStatePort;
                            then assign currentWarehouseState.failMessage := "";
                        }
                    then resolveable_error;
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

    part def WarehouseSystem :> System {
        private import StateEnumerationDefinitionDef::*;
        private import ActionEnumerationDefinition::*;
        private import DataTypes::StorageLocation;
        in port warehouseCmdPort {
            in item warehouseCmdItem : WarehouseCmd;
        }
        out port warehouseState {
            out item warehouseStateItem : WarehouseState;
        }
        part warehouseAPI : WarehouseAPI;
        part warehouseHardware : WarehouseHardware;

        connect warehouseCmdPort to warehouseAPI.warehouseAPICmdPort;
        connect warehouseAPI.warehouseAPICmdPort to warehouseAPI.engine.warehouseCmdPort;
        connect warehouseAPI.engine.warehouseStatePort to warehouseAPI.warehouseStatePort;
        connect warehouseAPI.warehouseStatePort to warehouseState;

        connect warehouseAPI.engine.cantileverMotorCmdPort to warehouseAPI.cantileverMotorAPI.actuator1DAPICmdPort;
        connect warehouseAPI.cantileverMotorAPI.forward1DActuatorPort
            to warehouseHardware.cantileverMotor.forwardActuatorPort;
        connect warehouseAPI.cantileverMotorAPI.backward1DActuatorPort
            to warehouseHardware.cantileverMotor.backwardActuatorPort;
        connect warehouseHardware.cantileverMotor.backwardLimitSwitchPort
            to warehouseAPI.cantileverMotorAPI.backwardLimitSwitchPort;
        connect warehouseHardware.cantileverMotor.forwardLimitSwitchPort
            to warehouseAPI.cantileverMotorAPI.forwardLimitSwitchPort;
        connect warehouseAPI.cantileverMotorAPI.actuator1DAPIStatePort to warehouseAPI.engine.cantileverMotorStatePort;

        connect warehouseAPI.engine.horizontalMotorCmdPort to warehouseAPI.horizontalMotorAPI.horAPICmdPort;
        connect warehouseAPI.horizontalMotorAPI.actHorLeftPort to warehouseHardware.horizontalMotor.actHorLeftPort;
        connect warehouseAPI.horizontalMotorAPI.actHorRightPort to warehouseHardware.horizontalMotor.actHorRightPort;
        connect warehouseHardware.horizontalMotor.senseHorEndLeftPort
            to warehouseAPI.horizontalMotorAPI.senseHorEndLeftPort;
        connect warehouseAPI.horizontalMotorAPI.horAPIStatePort to warehouseAPI.engine.horizontalMotorStatePort;

        connect warehouseHardware.encoderHorizontal.encoderSignalPort to warehouseAPI.engine.horizontalEncoderPort;

        connect warehouseAPI.engine.verticalMotorCmdPort to warehouseAPI.verticalMotorAPI.verticalCmdPort;
        connect warehouseAPI.verticalMotorAPI.actVertUpPort to warehouseHardware.verticalMotor.actVerticalUpPort;
        connect warehouseAPI.verticalMotorAPI.actVertDownPort to warehouseHardware.verticalMotor.actVerticalDownPort;
        connect warehouseHardware.verticalMotor.senseVerticalEndDownPort
            to warehouseAPI.verticalMotorAPI.senseVertEndDownPort;
        connect warehouseAPI.verticalMotorAPI.verticalStatePort to warehouseAPI.engine.verticalMotorStatePort;

        connect warehouseHardware.encoderVertical.encoderSignalPort to warehouseAPI.engine.verticalEncoderPort;

        connect warehouseAPI.engine.conveyorBeltCmdPort to warehouseAPI.conveyorBeltAPI.conveyorBeltAPICmdPort;
        connect warehouseAPI.conveyorBeltAPI.actConveyorBeltAPIForwardPort
            to warehouseHardware.conveyorBelt.actConveyorBeltForwardPort;
        connect warehouseAPI.conveyorBeltAPI.actConveyorBeltAPIBackwardPort
            to warehouseHardware.conveyorBelt.actConveyorBeltBackwardPort;
        connect warehouseAPI.conveyorBeltAPI.conveyorBeltAPIState to warehouseAPI.engine.conveyorBeltStatePort;

        connect warehouseHardware.trailSensorLower to warehouseAPI.engine.trailSensorLowerPort;
        connect warehouseHardware.trailSensorUpper to warehouseAPI.engine.trailSensorUpperPort;

        connect warehouseHardware.lightBarrierInside to warehouseAPI.engine.lightBarrierInsidePort;
        connect warehouseHardware.lightBarrierOutside to warehouseAPI.engine.lightBarrierOutsidePort;
    }
}