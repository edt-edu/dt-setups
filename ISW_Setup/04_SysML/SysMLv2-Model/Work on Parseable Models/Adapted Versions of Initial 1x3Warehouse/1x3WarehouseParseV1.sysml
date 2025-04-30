attribute def String {}
attribute def Integer {}
attribute def Boolean {}
part def Hardware;
part def API {
    item stateAPI : State;
    attribute name : String;
    in port cmdAPIPort;
    in port itemPort;
    out port stateAPIPort;
}

part def System {
    part hardware : Hardware [0..*];
    part software : API [0..*];
}
part def Subsystem :> System {
    part subsystems : System [0..*];
}

part def FactorySystem :> System {
    in port factoryCmd {
        in item factoryCmdItem;
    }
    port factoryState {
        out item factoryStateItem;
    }
}

enum def WarehouseActionDef {
    RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM;
    RETRIEVE;
    STORE;
    IDLE;
}
enum def ConveyorBeltActionDef {
    STOP_MOVING;
    MOVE_FORWARD;
    MOVE_BACKWARD;
}
enum def Actuator1DActionDef {
    MOVE_FORWARD;
    MOVE_BACKWARD;
    TARGET;
    STOP;
    RESET;
}
enum def VerticalActionDef {
    MOVE_DOWN;
    MOVE_UP;
    TARGET;
    STOP;
    RESET;
}
enum def ConveyorBeltSimpleActionDef {
    STOP_MOVING;
    START_MOVING;
}
enum def HorizontalActionDef {
    MOVE_LEFT;
    MOVE_RIGHT;
    TARGET;
    STOP;
    RESET;
}
enum def CantileverActionDef {
    MOVE_FORWARD;
    MOVE_BACKWARD;
    TARGET;
    STOP;
    RESET;
}

enum def MovementState {
    STOPPED;
    TARGETING;
    RESETTING;
}
enum def Actuator1DStateDef {
    doc
    /*
     * Typical status report.
     */
    MOVING_FORWARD;
    MOVING_BACKWARD;
    TARGETING;
    STOPPED;
    RESETTING;
    AT_BACKWARD_LIMIT;
}
enum def CantileverMotorStateDef {
    MOVING_FORWARD;
    MOVING_BACKWARD;
    TARGETING;
    STOPPED;
    RESETTING;
    AT_BACKWARD_LIMIT;
    AT_FORWARD_LIMIT;
}
enum def ConveyorBeltStateDef {
    MOVING_RIGHT;
    MOVING_LEFT;
    TARGETING;
    STOPPED;
    RESETTING;
}
enum def VerticalStateDef {
    MOVING_UP;
    MOVING_DOWN;
    STOPPED;
    TARGETING;
    RESETTING;
    AT_TOP_LIMIT_SWITCH;
}
enum def WarehouseStateDef {
    IDLE;
    STORING;
    RETRIEVE_EMPTY_PALLET_GET_PALLET_FROM_STORAGE;
    RETRIEVE_EMPTY_PALLET_HAS_PALLET_FROM_STORAGE;
    RETRIEVE_EMPTY_PALLET_DROP_AT_LOADING_POSITION;
    RETRIEVING_PICKUP_PALLET_FROM_STORAGE;
    RETRIEVING_DROPPING_OFF_PALLET_AT_LOADING_POSITION;
    ACTION_FAILED;
    MOVING_TO_START_POSITION;
    AT_START_POSITION;
    AT_LOADING_POSITION;
    EMPTY_PALLET_AT_INPUT;
    RETRIEVING_ITEM;
    RESOLVING_FAILURE;
}
enum def Warehouse1x3StateDef {
    IDLE;
    MOVING_TO_PICKUP_PALLET;
    PICKING_UP_PALLET;
    DROPPING_DOWN_PALLET;
    MOVING_TO_LOADING_POSITION;
    LOADING_FILLED_WITH_EMPTY_PALLET;
    MOVE_TO_STORING_POSITION;
    EMPTY_PALLET_AT_INPUT;
    A_EMPTY_B_EMPTY_C_EMPTY;
    A_FULL_B_EMPTY_C_EMPTY;
    A_EMPTY_B_FULL_C_EMPTY;
    A_EMPTY_B_EMPTY_C_FULL;
    A_FULL_B_FULL_C_EMPTY;
    A_FULL_B_EMPTY_C_FULL;
    A_EMPTY_B_FULL_C_FULL;
    A_FULL_B_FULL_C_FULL;
    ERROR;
}

enum def ReasonStateDef {
    CMD {
        doc
        /*
         * The state corresponds to a command or the initial reset.
         */
    }
    TARGET_REACHED {
        doc
        /*
         * A targeting operation succeded. State is `stopped`.
         * The `target` is given.
         */
    }
    POSITION_LIMIT {
        doc
        /*
         * The previous action was stopped because of a position limit.
         */
    }
}
enum def MachineLifecycleDef {
    ONLINE;
    ALREADY_CREATED;
    DESTROYED;
    FAILURE;
    UNKNOWN_TYPE;
    DESTROY_ABORTED;
}
enum def ConveyorBeltSimpleStateDef {
    MOVING;
    STOPPED;
}
enum def HorizontalStateDef {
    MOVING_LEFT;
    MOVING_RIGHT;
    TARGETTING;
    STOPPING;
    RESETTING;
    AT_RIGHT_LIMIT_SWITCH;
}

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
    storedItem : MovableItem;
}

item def MovableItem {
    attribute color : SortingLineColors;
    attribute weight : Integer;
    attribute size : Integer;
    attribute fragile : Boolean;
    attribute storageLocation : StorageLocation1x3;
}
item def Cmd {
    attribute machineAction;
}

item def Actuator1DCmd :> Cmd {
    attribute action1DActuator : Actuator1DActionDef; //:>> machineAction;
    attribute targetPosition : Integer {
        doc
        /*
         * If there is a Cmd with no need to set "targetPosition", set "targetPosition" null.
         */
    }
}
item def ConveyorBeltSimpleCmd :> Cmd {
    attribute actionConveyorBeltSimple : ConveyorBeltSimpleActionDef; //:>> machineAction;
}
item def ConveyorBeltCmd :> ConveyorBeltSimpleCmd {
    attribute moveTo : Integer {
        doc
        /*
         * If there is a Cmd with no need to set "to", set "to" to null.
         */
    }
    attribute actionConveyorBelt : ConveyorBeltActionDef; // :>> actionConveyorBeltSimple;
}
item def WarehouseCmd :> Cmd {
    attribute actionWarehouse : WarehouseActionDef; // :>> machineAction;
    attribute storagePosition : StoragePosition;
}
item def Warehouse1x3Cmd :> WarehouseCmd {
    attribute storagePosition1x3 : StoragePosition1x3;
}

item def VerticalCmd :> Actuator1DCmd {
    attribute actionVertical : VerticalActionDef; // :>> action1DActuator;
}
item def HorizontalCmd :> Actuator1DCmd {
    attribute actionHorizontal : HorizontalActionDef; // :>> action1DActuator;
}
item def CantileverCmd :> Actuator1DCmd {
    attribute actionCantilever : CantileverActionDef;
}
item def State {
    attribute machineState;
    attribute reason : ReasonStateDef;
    attribute lifecycle : MachineLifecycleDef;
}
item def Actuator1DState :> State {
    attribute stateActuator1D : Actuator1DStateDef; // :>> machineState;
}
item def WarehouseState :> State {
    attribute stateWarehouse : WarehouseStateDef; // :>> machineState;
    attribute failMessage : String;
    item currentWarehouseCmdState : WarehouseCmd;
}
item def Warehouse1x3State specializes WarehouseState {
    attribute stateWarehouse : Warehouse1x3StateDef;
}
item def VerticalState :> Actuator1DState {
    attribute stateVertical : VerticalStateDef; // :>> stateActuator1D;
}
item def CantileverState :> Actuator1DState {
    attribute stateCantilever : CantileverMotorStateDef; // :>> stateActuator1D;
}
item def ConveyorBeltSimpleState :> State {
    attribute stateConveyorBeltSimple : ConveyorBeltSimpleStateDef; // :>> machineState;
}
item def ConveyorBeltState :> ConveyorBeltSimpleState {
    attribute stateConveyorBelt : ConveyorBeltStateDef; // :>> stateConveyorBeltSimple;
}
item def HorizontalState :> Actuator1DState {
    attribute stateHorizontal : HorizontalStateDef; // :>> stateActuator1D;
}

part def Actuator1DAPI :> API {
    action moveForward;
    action stopMoving;
    action moveBackward;
    action moveToPosition;
    action reset;
    attribute backwardLimitSwitch;
    out port forward1DActuatorPort {
        out forward1DActuatorAPI : Boolean;
    }
    out port backward1DActuatorPort {
        out backward1DActuatorAPI : Boolean;
    }
    in port backwardLimitSwitchPort {
        in backwardLimitSwitch1DActuatorAPI : Boolean;
    }
    out port actuator1DAPIStatePort {
        //:>> stateAPIPort
        out actuator1DAPIStateItem : Actuator1DState;
    }
    in port actuator1DAPICmdPort {
        //:>> cmdAPIPort
        in actuator1DAPICmdItem : Actuator1DCmd;
    }
    attribute forwardLimit : Integer {}

    item actuator1DState : Actuator1DState; //:>> stateAPI
    attribute accuracy : Integer;
    attribute stopOffset : Integer;
    attribute backwardLimit : Integer;
    attribute sensorBackwardLimitSwitch : Boolean;
    attribute actuatorForward : Boolean;
    attribute actuatorBackward : Boolean;
    attribute target : Integer;
    item currentCmd : Actuator1DCmd;
}

part def CantileverMotorAPI :> Actuator1DAPI {
    in port forwardLimitSwitchPort {
        in forwardLimitSwitch1DActuatorAPI : Boolean;
    }
}

part def Vertical1DActuatorAPI {
    //:> Actuator1DAPI nicht notwendig, aber vlt sowieso sinvoller...
    attribute sensorUpLimitSwitch : Boolean; //:>> sensorBackwardLimitSwitch
    attribute downLimit : Integer;
    attribute upLimit : Integer; //:>> backwardLimit
    attribute actuatorUp : Boolean; //:>> actuatorBackward
    attribute actuatorDown : Boolean; //:>> actuatorForward
    attribute limitUp : Integer; //:>> forwardLimit

    item currentState : VerticalState; //:>> actuator1DState
    item currentCmd : VerticalCmd; //:>> currentCmd

    out port actVertUpPort {
        //:>> forward1DActuatorPort
        out actVertUp : Boolean;
    }
    out port actVertDownPort {
        //:>> backward1DActuatorPort
        out actVertDown : Boolean;
    }
    in port senseVertEndDownPort {
        //:>> backwardLimitSwitchPort
        in senseVertEndDown : Boolean;
    }
    in port verticalCmdPort {
        //:>> actuator1DAPICmdPort
        in verticalCmdItem : VerticalCmd;
    }
    out port verticalStatePort {
        //:>> actuator1DAPIStatePort
        out verticalStateItem : VerticalState;
    }
}

part def Horizontal1DActuatorAPI {
    //:> Actuator1DAPI nicht notwendig, aber vlt sowieso sinvoller...
    attribute sensorUpLimitSwitch : Boolean; //:>> sensorBackwardLimitSwitch
    attribute rightLimit : Integer; //:>> backwardLimit
    attribute actuatorRight : Boolean; //:>> actuatorBackward
    attribute actuatorLeft : Boolean; //:>> actuatorForward
    attribute leftLimit : Integer; //:>> forwardLimit

    item currentState : HorizontalState; //:>> actuator1DState
    item currentCmd : HorizontalCmd; //:>> currentCmd

    out port actHorRightPort {
        //:>> forward1DActuatorPort
        out actHorRight : Boolean;
    }
    out port actHorLeftPort {
        //:>> backward1DActuatorPort
        out actHorLeft : Boolean;
    }
    in port senseHorEndLeftPort {
        //:>> backwardLimitSwitchPort
        in senseHorEndLeft : Boolean;
    }
    in port horAPICmdPort {
        //:>> actuator1DAPICmdPort
        in horAPICmdItem : HorizontalCmd;
    }
    out port horAPIStatePort {
        //:>> actuator1DAPIStatePort
        out horAPIStateItem : HorizontalState;
    }

    action horRight; //:>> moveForward
    action horLeft; //:>> moveBackward
}

part def ConveyorBeltSimpleAPI :> API {
    item conveyorBeltSimpleAPIState : ConveyorBeltSimpleState; //:>> stateAPI
    attribute isMoving : Boolean;
    in port conveyorBeltSimpleAPICmdPort {
        //:>> cmdAPIPort
        in item conveyorBeltSimpleAPICmdItem : ConveyorBeltSimpleCmd;
    }
    out port actConveyorBeltSimpleAPIPort {
        out actConveyorBeltSimple : Boolean;
    }
    out port conveyorBeltSimpleAPIStatePort {
        //:>> stateAPIPort
        out item conveyorBeltSimpleAPIStateItem : ConveyorBeltSimpleState;
    }
    action stopMoving;
    action move;
}
part def ConveyorBeltAPI :> ConveyorBeltSimpleAPI {
    action moveBackward;
    action moveForwardTo;
    action moveBackwardTo;
    in port conveyorBeltAPICmdPort {
        //:>> conveyorBeltSimpleAPICmdPort
        in item conveyorBeltAPICmdPort : ConveyorBeltCmd;
    }
    item currentCmd : ConveyorBeltCmd;
    item conveyorBeltAPIState : ConveyorBeltState; //:>> conveyorBeltSimpleAPIState
    attribute targetCount : Integer;
    attribute targeting : Boolean;
    out port actConveyorBeltAPIForwardPort {
        out actConveyorBeltAPIFOrward : Boolean;
    }
    out port actConveyorBeltAPIBackwardPort {
        out actConveyorBeltAPIBackward : Boolean;
    }
    action moveForward;
    out port conveyorBeltAPIStatePort {
        //:>> conveyorBeltSimpleAPIStatePort
        out item conveyorBeltStateItem : ConveyorBeltState;
    }
}

part def Actuator1D :> Hardware {
    in port forwardActuatorPort {
        in moveForward1D : Boolean;
    }
    out port backwardLimitSwitchPort {
        out backwardLimitSwitch1D : Boolean;
    }
    in port backwardActuatorPort {
        in moveBackward : Boolean;
    }
}
part def CantileverMotor {
    //:> Actuator1D nicht notwendig, aber vlt sowieso sinvoller...
    out port forwardLimitSwitchPort {
        out forwardLimitSwitch : Boolean;
    }
    out port backwardLimitSwitchPort {
        out backwardLimitSwitch1D : Boolean;
    }
    in port forwardActuatorPort {
        in moveForward : Boolean;
    }
    in port backwardActuatorPort {
        in moveBackward : Boolean;
    }
}
part def VerticalActuator1D {
    //:> Actuator1D nicht notwendig, aber vlt sowieso sinvoller...
    in port actVerticalDownPort {
        //:>> backwardActuatorPort
        in actVerticalDown : Boolean;
    }
    in port actVerticalUpPort {
        //:>> forwardActuatorPort
        in actVerticalUp : Boolean;
    }
    out port senseVerticalEndDownPort {
        //:>> backwardLimitSwitchPort
        out senseVerticalEndDown : Boolean;
    }
}

part def HorizontalActuator1D {
    //:> Actuator1D
    in port actHorRightPort {
        //:>> forwardActuatorPort
        in actHorRight : Boolean;
    }
    in port actHorLeftPort {
        //:>> backwardActuatorPort
        in actHorLeft : Boolean;
    }
    out port senseHorEndLeftPort {
        //:>> backwardLimitSwitchPort
        out senseHorEndLeft : Boolean;
    }
}

part def ConveyorBeltSimple :> Hardware {
    in port actConveyorBeltForwardPort {
        in actForwardSimpleConveyorBelt : Boolean;
    }
}
part def LightBarrier :> Hardware {
    out port lightBarrierPort {
        out LightBarrier : Boolean;
    }
}
part def Encoder :> Hardware {
    out port encoderSignalPort {
        out impulse1 : Boolean;
        out impulse2 : Boolean;
    }
}
part def HorizontalEncoder {
    //:> Encoder
    out port horizontalEncoderPort {
        //:>> encoderSignalPort
        impulseLeft : Boolean;
        impulseRight : Boolean;
    }
}
part def VerticalEncoder {
    //:> Encoder
    out port verticalEncoderPort {
        //:>> encoderSignalPort
        out impulseUp : Boolean;
        out impulseDown : Boolean;
    }
}
part def Phototransistor :> Hardware {
    out port phototransistorPort {
        out Phototransistor : Boolean;
    }
}
part def TrailSensor :> Hardware {
    out port trailSensorPort {
        out trailSensor : Boolean;
    }
}
part def ConveyorBelt :> ConveyorBeltSimple {
    in port actConveyorBeltBackwardPort {
        in actConveyorBeltBackward : Boolean;
    }
}

part def Warehouse1x3API :> API {
    in port warehouse1x3APICmdPort {
        //:>> cmdAPIPort
        in item warehouse1x3APICmdPort : Warehouse1x3Cmd;
    }
    in port warehouse1x3APIItemPort  {
        //:>> itemPort
        in item warehouse1x3APIItemPort : MovableItem;
    }
    out port warehouseStatePort {
        //:>> stateAPIPort
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
        item currentWarehouse1x3State : Warehouse1x3State {
            attribute stateWarehouse : Warehouse1x3StateDef := Warehouse1x3StateDef::IDLE; //:>> stateWarehouse
            attribute failMessage : String := ""; // the error message of the machine in the Warehouse1x3
            item currentWarehouseCmdState : Warehouse1x3Cmd { //:>> currentWarehouseCmdState
                attribute actionWarehouse : WarehouseActionDef := WarehouseActionDef::IDLE; //:>> actionWarehouse
                attribute storagePosition : StoragePosition1x3;
            }
        } // the current state of the machine in the Warehouse1x3 ...:>> stateAPI
        item currentWarehouse1x3Cmd : Warehouse1x3Cmd {
            attribute actionWarehouse : WarehouseActionDef := WarehouseActionDef::IDLE; //:>> actionWarehouse
            attribute storagePosition1x3 : StoragePosition1x3;
        } // the current command for the machine in the Warehouse1x3
        item currentCantileverCmd : CantileverCmd {
            attribute actionCantilever : CantileverActionDef := CantileverActionDef::STOP; //:>> actionCantilever
            attribute targetPosition : Integer := 0; // the target position of the cantilever motor (in sec)
        } // the current command for the cantilever motor
        item currentHorizontalCmd : HorizontalCmd {} // the current command for the horizontal motor
        item currentVerticalCmd : VerticalCmd {
            attribute actionVertical : VerticalActionDef; //:>> actionVertical
            attribute targetPosition : Integer; // the target position of the vertical motor (in sec)    
        } // the current command for the vertical motor
        item currentConveyorBeltCmd : ConveyorBeltCmd {} // the current command for the conveyor belt
        attribute currentTargetStorageLocation : StorageLocation1x3; // the current storage location of the machine in the Warehouse1x3

        attribute inputPosition : StorageLocation1x3 {
            storagePosition := StoragePosition::INPUT;
            VERTICAL_LOCATION_CONFIG := 1;
            HORIZONTAL_LOCATION_CONFIG := 1;
            DEPTH_LOCATION_CONFIG := 1;
            occupied :Boolean := false;
            storedItem := null;
        }
        attribute a : StorageLocation1x3 {
            storagePosition := StoragePosition1x3::A;
            VERTICAL_LOCATION_CONFIG := 1;
            HORIZONTAL_LOCATION_CONFIG := 1;
            DEPTH_LOCATION_CONFIG := 1;
            occupied :Boolean := false;
            storedItem := null;
        }
        attribute b : StorageLocation1x3 {
            storagePosition := StoragePosition1x3::B;
            VERTICAL_LOCATION_CONFIG := 1;
            HORIZONTAL_LOCATION_CONFIG := 1;
            DEPTH_LOCATION_CONFIG := 1;
            occupied :Boolean := false;
            storedItem := null;
        }
        attribute c : StorageLocation1x3 {
            storagePosition := StoragePosition1x3::C;
            VERTICAL_LOCATION_CONFIG := 1;
            HORIZONTAL_LOCATION_CONFIG := 1;
            DEPTH_LOCATION_CONFIG := 1;
            occupied :Boolean := false;
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
            in item warehouse1x3Cmd : Warehouse1x3Cmd {
                attribute actionWarehouse : WarehouseActionDef; //:>> actionWarehouse
                attribute storagePosition1x3 : StoragePosition1x3;
            }
        }
        in port warehouse1x3APIItemPort {
            in item warehouse1x3APIItem : MovableItem;
        }
        out port warehouse1x3StatePort {
            out item warehouse1x3State : Warehouse1x3State;
        }


        // This action needs to be implemented and should update the y-position of the machine in the Warehouse1x3 in seconds
        action def Position_Updater_Y {}
        // This action needs to be implemented and should update the x-position of the machine in the Warehouse1x3 in seconds
        action def Position_Updater_X {}

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
                then send Warehouse1x3API::engine::currentCantileverCmd via Warehouse1x3API::engine::cantileverMotorCmdPort;
            }
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            action stop_backward_cantilever
            if (
                cantileverMotorStatePort.cantileverMotorState.stateCantilever ==
                CantileverMotorStateDef::AT_BACKWARD_LIMIT
            ) {
                assign currentCantileverCmd.actionCantilever := CantileverActionDef::STOP;
                then send Warehouse1x3API::engine::currentCantileverCmd via Warehouse1x3API::engine::cantileverMotorCmdPort;
                then assign currentPosition.z := 0;
            }
        }

        // this action will move the cantilever forward to the forward reference point
        action def Cantilever_Forward_Till_Reference {
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            while cantileverMotorStatePort.cantileverMotorState != CantileverMotorStateDef::AT_FORWARD_LIMIT {
                accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
                then assign currentCantileverCmd.actionCantilever := CantileverActionDef::MOVE_FORWARD;
                then send Warehouse1x3API::engine::currentCantileverCmd via Warehouse1x3API::engine::cantileverMotorCmdPort;
            }
            accept cantileverMotorStatePort.cantileverMotorState via cantileverMotorStatePort;
            then action stop_cantilever_forward
            if (
                cantileverMotorStatePort.cantileverMotorState.stateCantilever ==
                CantileverMotorStateDef::AT_FORWARD_LIMIT
            ) {
                assign currentCantileverCmd.actionCantilever := CantileverActionDef::STOP;
                then send Warehouse1x3API::engine::currentCantileverCmd via Warehouse1x3API::engine::cantileverMotorCmdPort;
                then assign currentPosition.z := Warehouse1x3API::engine::FORWARD_LIMIT_TIME_CONFIG; // the time the machine needs to move to the forward limit, needs to be set (in sec)
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
                then send Warehouse1x3API::engine::currentHorizontalCmd via Warehouse1x3API::engine::horizontalMotorCmdPort;
            }
            accept horizontalMotorStatePort.horizontalMotorState via horizontalMotorStatePort;
            action stop_right_horizontal
            if (
                horizontalMotorStatePort.horizontalMotorState.stateHorizontal ==
                HorizontalStateDef::AT_RIGHT_LIMIT_SWITCH
            ) {
                assign currentHorizontalCmd.actionHorizontal := HorizontalActionDef::STOP;
                then send Warehouse1x3API::engine::currentHorizontalCmd via Warehouse1x3API::engine::horizontalMotorCmdPort;
                then assign currentPosition.x := 0;
            }
        }

        // this action will move the machine to the start position which is the most top right point of the Warehouse1x3, so the [0,0,0] position
        action def Move_To_Start {
            // move_backward_cantilever : Cantilever_Backward_Till_Reference{}
            //then perform action move_right_horizontal : Horizontal_Right_Till_Reference {}
            //then perform action move_top_vertical : Vertical_up_till_reference {}
        }
        action def Move_To_Input_Position_Handler {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := Warehouse1x3API::engine::currentWarehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVING_TO_LOADING_POSITION;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            //then perform action move_to_input : Move_To_Input {}
        }
        action def Move_To_Input_Position_To_Store_Item_Handler {
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVING_TO_LOADING_POSITION;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            //then perform action move_to_input : Move_To_Input {}
        }
        action def Move_To_Storage_Location_Handler {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := Warehouse1x3API::engine::currentWarehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVE_TO_STORING_POSITION;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            // then perform action move_to_current_cmd_location : Move_To_Storage_Location;
        }

        action def Move_To_Storage_Location_To_Store_Item_Handler {
            assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::MOVE_TO_STORING_POSITION;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            // then perform action move_to_current_cmd_location : Move_To_Storage_Location;
        }

        action def Dropdown_Pallet_At_Storage_Handler {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := Warehouse1x3API::engine::currentWarehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET; 
            // perform action dropdown_movement_at_storage_location : Dropdown_Movement_Storage {}
        }
        action def Dropdown_Pallet_At_Storage_To_Store_Item_Handler {
            assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            // perform action dropdown_movement_at_storage_location : Dropdown_Movement_Storage {}
        }
        action def Pickup_Pallet_At_Storage_Handler {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
           // assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := Warehouse1x3API::engine::currentWarehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            // perform action pickup_movement_at_storage_location : Pickup_Movement_Storage {}
            // then perform action change : Change_Occupied_Of_Current_Target_Location {}
        }
        action def Pickup_Pallet_At_Storage_To_Store_Item_Handler {
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            // perform action pickup_movement_at_storage_location : Pickup_Movement_Storage {}
        }
        action def Dropdown_Pallet_At_Input_Handlder {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := Warehouse1x3API::engine::currentWarehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            // perform action dropdown_movement_at_input : Dropdown_Movement_Input {}
            // perform action change : Change_Occupied_Of_Current_Target_Location {}
        }
        action def Dropdown_Pallet_At_Input_To_Store_Item_Handlder {
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::DROPPING_DOWN_PALLET;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            // perform action dropdown_movement_at_input : Dropdown_Movement_Input {}
        }
        action def Pickup_Pallet_At_Input_Handler {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := Warehouse1x3API::engine::currentWarehouse1x3Cmd;
            then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            // perform action pickup_movement_at_input : Pickup_Movement_Input {}
        }
        action def Pickup_Pallet_At_Input_To_Store_Item_Handler {
            assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::PICKING_UP_PALLET;
            then send Warehouse1x3API::engine::currentWarehouse1x3State via Warehouse1x3API::engine::warehouse1x3StatePort;
            // perform action pickup_movement_at_input : Pickup_Movement_Input {}
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
                //assign a.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::A and a.occupied == true {
                assign a.occupied := false;
                assign a.storedItem := null;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::B and b.occupied == false {
                assign b.occupied := true;
                //assign b.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::B and b.occupied == true {
                assign b.occupied := false;
                assign b.storedItem := null;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::C and c.occupied == false {
                assign c.occupied := true;
                //assign c.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::C and c.occupied == true {
                assign c.occupied := false;
                assign c.storedItem := null;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::INPUT and a.occupied == false {
                assign inputPosition.occupied := true;
                //assign inputPosition.storedItem := warehouse1x3APIItemPort.warehouse1x3APIItem;
            }
            if currentTargetStorageLocation.storagePosition == StoragePosition1x3::INPUT and a.occupied == true {
                assign inputPosition.occupied := false;
                assign inputPosition.storedItem := null;
            }
        }
        action def Pickup_Movement_Input {
            then action adjust_vertical_position_up
            while (
                Warehouse1x3API::engine::currentPosition.y <
                Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG + (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                then assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action adjust_vertical_position_down
            while (
                Warehouse1x3API::engine::currentPosition.y >
                Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG + (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement_initial_positioning
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG + (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign Warehouse1x3API::engine::currentPosition.y :=
                //     Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG - (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG);
            }
            //perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
            then accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //then perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_vertical_up
            while (
                Warehouse1x3API::engine::currentPosition.y >
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign Warehouse1x3API::engine::currentPosition.y :=
                //     Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                //     (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y);
            }
            //then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
        }

        // this action is the movement for the pickup of a pallet, the machine has to be at the right X and Y postion
        action def Pickup_Movement_Storage {
            //perform action vertical_adjustment_for_pickup : Align_Vertically_To_Current_Cmd_Location_For_Pickup;
            //perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
            then accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //then perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_vertical_up
            while (
                Warehouse1x3API::engine::currentPosition.y >
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign Warehouse1x3API::engine::currentPosition.y :=
                //     Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                //     (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y);
            }
            //then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
        }
        action def Dropdown_Movement_Input {
            action adjust_vertical_position_up
            while (
                Warehouse1x3API::engine::currentPosition.y <
                Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG - (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                then assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action adjust_vertical_position_down
            while (
                Warehouse1x3API::engine::currentPosition.y >
                Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG - (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement_initial_positioning
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG - (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign Warehouse1x3API::engine::currentPosition.y :=
                //     Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG + (Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG);
            }
            then action move_vertical_down
            while (
                Warehouse1x3API::engine::currentPosition.y <
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign Warehouse1x3API::engine::currentPosition.y :=
                //     Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                //     (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y);
            }
            //then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
        }
        // this action is the movement for the dropdown of a pallet to the storage, the machine has to be at the right X and Y postion
        action def Dropdown_Movement_Storage {
            //perform action vertical_adjustment_for_dropdown : Align_Vertically_To_Current_Cmd_Location_For_Dropdown;
            //then perform action move_cantilever_forward : Cantilever_Forward_Till_Reference {}
            //then perform action assign_current_vertical_target_location : Assign_Current_Target_Location {}
            then action move_vertical_down
            while (
                Warehouse1x3API::engine::currentPosition.y <
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                then assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign Warehouse1x3API::engine::currentPosition.y :=
                //     Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                //     (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y);
            }
            //then action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
            //then action Change_Occupied_Of_Current_Target_Location {}
        }

        // this is for the horizontal movement alignement of the machine to the current horizontal position given from the Warehouse1x3CmdPort
        // the horizontal positions are always given in perspective to X=0
        action def Align_Horizontally_To_Current_Cmd_Location {
            accept  Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //then perform action assign_current_target_location : Assign_Current_Target_Location {}
            action move_to_slot_horizontal_right
            while (
                 Warehouse1x3API::engine::currentPosition.x <  Warehouse1x3API::engine::currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG -  Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.x
            ) {
                //perform action position_updater_x : Position_Updater_X {}
                assign  Warehouse1x3API::engine::currentHorizontalCmd.actionHorizontal := HorizontalActionDef::MOVE_RIGHT;
                then send Warehouse1x3API::engine::currentHorizontalCmd via Warehouse1x3API::engine::horizontalMotorCmdPort;
            }
            then action move_to_slot_horizontal_left
            while (
                 Warehouse1x3API::engine::currentPosition.x >  Warehouse1x3API::engine::currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG -  Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.x
            ) {
                //perform action position_updater_x : Position_Updater_X {}
                assign  Warehouse1x3API::engine::currentHorizontalCmd.actionHorizontal := HorizontalActionDef::MOVE_LEFT;
                then send Warehouse1x3API::engine::currentHorizontalCmd via Warehouse1x3API::engine::horizontalMotorCmdPort;
            }
            then action stop_horizontal_movement
            if (
                 Warehouse1x3API::engine::currentPosition.x ==
                 Warehouse1x3API::engine::currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG -  Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.x
            ) {
                assign  Warehouse1x3API::engine::currentHorizontalCmd.actionHorizontal := HorizontalActionDef::STOP;
                then send Warehouse1x3API::engine::currentHorizontalCmd via Warehouse1x3API::engine::horizontalMotorCmdPort;
                // then assign currentPosition.x :=
                //     currentTargetStorageLocation.HORIZONTAL_LOCATION_CONFIG - INACCURACY_OFFSET_CONFIG.x;
            }
        }
        action def Align_Vertically_To_Current_Cmd_Location {
            accept warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_to_slot_vertical_up
            while (
                 Warehouse1x3API::engine::currentPosition.y >  Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +  Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign  Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action move_to_slot_vertical_down
            while (
                 Warehouse1x3API::engine::currentPosition.y <  Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +  Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign  Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if  Warehouse1x3API::engine::currentPosition.y ==  Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +  Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y {
                assign  Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                //then assign currentPosition.y := currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG + INACCURACY_OFFSET_CONFIG.y;
            }
        }
        // This action will Align vertically for pickup for a given storage location via the Warehouse1x3CmdPort (it will move the machine to the right Y position)
        action def Align_Vertically_To_Current_Cmd_Location_For_Pickup {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_to_slot_vertical_up
            while (
                Warehouse1x3API::engine::currentPosition.y >
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action move_to_slot_vertical_down
            while (
                Warehouse1x3API::engine::currentPosition.y <
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign currentPosition.y :=
                //     Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG +
                //     (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y);
            }
        }
        // This action will align vertically for dropdown for a given storage location via the Warehouse1x3CmdPort (it will move the machine to the right Y position)
        action def Align_Vertically_To_Current_Cmd_Location_For_Dropdown {
            accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
            //perform action assign_current_target_location : Assign_Current_Target_Location {}
            then action move_to_slot_vertical_to_store
            while (
                Warehouse1x3API::engine::currentPosition.y >
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action updatePosition : Position_Updater_Y {}
                 assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action move_to_slot_vertical_down
            while (
                Warehouse1x3API::engine::currentPosition.y <
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                //perform action position_updater_y : Position_Updater_Y {}
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if (
                Warehouse1x3API::engine::currentPosition.y ==
                Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y)
            ) {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                // then assign currentPosition.y :=
                //     Warehouse1x3API::engine::currentTargetStorageLocation.VERTICAL_LOCATION_CONFIG -
                //     (Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG + Warehouse1x3API::engine::INACCURACY_OFFSET_CONFIG.y);
            }
        }
        action def Move_To_Input {
            // perform action move_cantilever_backward : Cantilever_Backward_Till_Reference {}
            //then perform action move_right_horizontal_switch : Horizontal_Right_Till_Reference {}
            then action adjust_vertical_position_up
            while Warehouse1x3API::engine::currentPosition.y < Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG {
                //perform action position_updater_y : Position_Updater_Y {}
                 assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action adjust_vertical_position_down
            while currentPosition.y > inputPosition.VERTICAL_LOCATION_CONFIG + STORAGE_OFFSET_CONFIG {
                //perform action position_updater_y : Position_Updater_Y {}
                 assign currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_DOWN;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            then action stop_vertical_movement
            if Warehouse1x3API::engine::currentPosition.y == Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG + Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG { 
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                //then assign Warehouse1x3API::engine::currentPosition.y := Warehouse1x3API::engine::inputPosition.VERTICAL_LOCATION_CONFIG - Warehouse1x3API::engine::STORAGE_OFFSET_CONFIG;
            }
        }
        action def Move_To_Storage_Location {
            // perform action move_backward_cantilever : Cantilever_Backward_Till_Reference;
            // then perform action move_horizontaly_to_current_command_location
            //     : Align_Horizontally_To_Current_Cmd_Location;
            // then perform action move_vertically_to_current_command_location : Align_Vertically_To_Current_Cmd_Location;
        }

        // this action will move the machine to the top reference point
        action def Vertical_up_till_reference {
            accept Warehouse1x3API::engine::verticalMotorStatePort.verticalMotorState via verticalMotorStatePort;
            action move_top_vertical_switch
            while Warehouse1x3API::engine::verticalMotorStatePort.verticalMotorState.stateVertical != VerticalStateDef::AT_TOP_LIMIT_SWITCH {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::MOVE_UP;
                send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
            }
            accept Warehouse1x3API::engine::verticalMotorStatePort.verticalMotorState via verticalMotorStatePort;
            then action stop_top_vertical
            if verticalMotorStatePort.verticalMotorState.stateVertical == VerticalStateDef::AT_TOP_LIMIT_SWITCH {
                assign Warehouse1x3API::engine::currentVerticalCmd.actionVertical := VerticalActionDef::STOP;
                then send Warehouse1x3API::engine::currentVerticalCmd via Warehouse1x3API::engine::verticalMotorCmdPort;
                then assign Warehouse1x3API::engine::currentPosition.y := 0;
            }
        }
        state def Warehouse1x3EngineStates {
            in warehouse1x3API : Warehouse1x3API;
            entry;
            then a_empty_b_empty_c_empty;

            state retrieve_empty_pallet {
                entry action retrieve_empty_pallet {
                    // perform action move_to_storage_position : Warehouse1x3API::engine::Move_To_Storage_Location_Handler;
                    // then perform action pickup_pallet : Pickup_Pallet_At_Storage_To_Store_Item_Handler;
                    // then perform action move_to_input_position : Move_To_Input_Position_To_Store_Item_Handler;
                    // then perform action dropdown_pallet_at_input : Dropdown_Pallet_At_Input_To_Store_Item_Handlder;
                    // then perform action move_to_start : Move_To_Start;
                }
            }

            state empty_pallet_at_input {
                entry action dropdown_pallet_at_input {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::EMPTY_PALLET_AT_INPUT;
                    then assign inputPosition.occupied := true;
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    action wait_action
                    while Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse != WarehouseActionDef::STORE {
                        accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                        assign Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse :=
                            Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd.actionWarehouse;
                    }
                }
                exit action assign_current_warehouse_cmd {
                    assign currentWarehouse1x3Cmd.actionWarehouse := WarehouseActionDef::STORE;
                }
            }

            state store {
                entry action store {
                    // perform action move_to_input_position : Move_To_Input_Position_To_Store_Item_Handler {}
                    // perform action pickup_item_at_input_position : Pickup_Pallet_At_Input_To_Store_Item_Handler {}
                    // perform action move_to_storage_position : Move_To_Storage_Location_To_Store_Item_Handler {}
                    // perform action dropdown_item_at_storage_position
                    //     : Dropdown_Pallet_At_Storage_To_Store_Item_Handler {}
                }
            }
            state retrieve_item {
                entry action retrieve_item {
                    // perform action move_to_storage_position : Move_To_Storage_Location_Handler {}
                    // perform action pickup_item_at_storage_position : Pickup_Pallet_At_Storage_Handler {}
                    // perform action move_to_input_position : Move_To_Input_Position_Handler {}
                    // perform action dropdown_item_at_input : Dropdown_Pallet_At_Input_Handlder {}
                }
            }

            state a_empty_b_empty_c_empty {
                entry action move_to_start_position {
                    // perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_full_b_empty_c_empty {
                entry action move_to_start_position {
                    // perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_empty_b_full_c_empty {
                entry action move_to_start_position {
                    // perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_empty_b_empty_c_full {
                entry action move_to_start_position {
                    //perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_full_b_full_c_empty {
                entry action move_to_start_position {
                    //perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_full_b_empty_c_full {
                entry action move_to_start_position {
                    //perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_empty_b_full_c_full {
                entry action move_to_start_position {
                    //perform action move_to_start : Move_To_Start {}
                     assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state a_full_b_full_c_full {
                entry action move_to_start_position {
                    //perform action move_to_start : Move_To_Start {}
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::IDLE;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.currentWarehouseCmdState := null;
                    then send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                }
                do action wait_for_new_cmd {
                    accept Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd via warehouse1x3CmdPort;
                    //assign Warehouse1x3API::engine::currentWarehouse1x3Cmd := Warehouse1x3API::engine::warehouse1x3CmdPort.warehouse1x3Cmd;
                }
            }
            state store_error {
                entry action store_error {
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::ERROR;
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.failMessage := "Error: Storage position is occupied";
                    send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.failMessage := "";
                }
            }

            state retrieve_error {
                entry action retrieve_error {
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.stateWarehouse := Warehouse1x3StateDef::ERROR;
                    assign Warehouse1x3API::engine::currentWarehouse1x3State.failMessage := "Error: Storage position is empty";
                    send Warehouse1x3API::engine::currentWarehouse1x3State via warehouseStatePort;
                    then assign Warehouse1x3API::engine::currentWarehouse1x3State.failMessage := "";
                }
            }
            //transitions from each state to retrieve_empty_pallet
            transition a_empty_b_empty_c_empty_to_retrieve_empty_pallet
                first a_empty_b_empty_c_empty then retrieve_empty_pallet;
            transition a_full_b_empty_c_empty_to_retrieve_empty_pallet
                first a_full_b_empty_c_empty
                if (
                   Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM 
                )
                    then retrieve_empty_pallet;
            transition a_empty_b_full_c_empty_to_retrieve_empty_pallet
                first a_empty_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::B and
                     Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then retrieve_empty_pallet;
            transition a_empty_b_empty_c_full_to_retrieve_empty_pallet
                first a_empty_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then retrieve_empty_pallet;

            transition a_full_b_full_c_empty_to_retrieve_empty_pallet
                first a_full_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then retrieve_empty_pallet;
            transition a_full_b_empty_c_full_to_retrieve_empty_pallet
                first a_full_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then retrieve_empty_pallet;
            transition a_empty_b_full_c_full_to_retrieve_empty_pallet
                first a_empty_b_full_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then retrieve_empty_pallet;

            //error transitions from each state to retrieve_empty_pallet
            transition a_full_b_empty_c_empty_to_error
                first a_full_b_empty_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then store_error;
            transition a_empty_b_full_c_empty_to_error
                first a_empty_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then store_error;
            transition a_empty_b_empty_c_full_to_error
                first a_empty_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                )
                    then store_error;
            transition a_full_b_full_c_full_to_retrieve_empty_pallet
                first a_full_b_full_c_full
                if Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM
                    then store_error;
            //transition from retrieve_empty_pallet to empty_pallet_at_input
            transition retrieve_empty_pallet_to_empty_pallet_at_input
                first retrieve_empty_pallet then empty_pallet_at_input;

            //transition from empty_pallet_at_input to store
            transition empty_pallet_at_input_to_store
                first empty_pallet_at_input
                if Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE then store;

            //transition from store to the respective following state
            transition store_to_a_full_b_empty_C_empty
                first store
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_full_b_empty_c_empty;
            transition store_to_a_empty_b_full_C_empty
                first store
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_empty_b_full_c_empty;
            transition store_to_a_empty_b_empty_C_full
                first store
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_empty_b_empty_c_full;

            transition store_to_a_full_b_full_C_empty
                first store
                if (
                    //Problem liegt vermutlich an der Standard Library
                    // Warehouse1x3API::engine::a.occupied == true and
                    // Warehouse1x3API::engine::b.occupied == true and
                    // Warehouse1x3API::engine::c.occupied == false and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_full_b_full_c_empty;
            transition store_to_a_full_b_empty_C_full
                first store
                if (
                    // Warehouse1x3API::engine::a.occupied == true and
                    // Warehouse1x3API::engine::b.occupied == false and
                    // Warehouse1x3API::engine::c.occupied == true and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_full_b_empty_c_full;
            transition store_to_a_empty_b_full_c_full
                first store
                if (
                    // Warehouse1x3API::engine::a.occupied == false and
                    // Warehouse1x3API::engine::b.occupied == true and
                    // Warehouse1x3API::engine::c.occupied == true and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_empty_b_full_c_full;
            transition store_to_a_full_b_full_C_full
                first store
                if (
                    // Warehouse1x3API::engine::a.occupied == true and
                    // Warehouse1x3API::engine::b.occupied == true and
                    // Warehouse1x3API::engine::c.occupied == false and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::STORE
                )
                    then a_full_b_full_c_full;

            //transition from each state to retrieve_item
            transition a_full_b_empty_c_empty_retrieve_to_retrieve_item
                first a_full_b_empty_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_item;
            transition a_empty_b_full_c_empty_retrieve_to_retrieve_item
                first a_empty_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_item;
            transition a_empty_b_empty_c_full_retrieve_to_retrieve_item
                first a_empty_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_item;
            transition a_full_b_full_c_empty_retrieve_to_retrieve_item
                first a_full_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_item;
            transition a_full_b_empty_c_full_retrieve_to_retrieve_item
                first a_full_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_item;
            transition a_empty_b_full_c_full_retrieve_to_retrieve_item
                first a_empty_b_full_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_item;
            transition a_full_b_full_c_full_retrieve_to_retrieve_item
                first a_full_b_full_c_full
                if Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE then retrieve_item;

            //error transition from each state to retrieve_item
            transition a_empty_b_empty_c_empty_retrieve_to_retrieve_error
                first a_empty_b_empty_c_empty
                if Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE then store_error;
            transition a_full_b_empty_c_empty_retrieve_to_error
                first a_full_b_empty_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_error;
            transition a_empty_b_full_c_empty_retrieve_to_error
                first a_empty_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_error;
            transition a_empty_b_empty_c_full_retrieve_to_error
                first a_empty_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 != StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_error;
            transition a_full_b_full_c_empty_retrieve_to_error
                first a_full_b_full_c_empty
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_error;
            transition a_full_b_empty_c_full_retrieve_to_error
                first a_full_b_empty_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_error;
            transition a_empty_b_full_c_full_retrieve_to_error
                first a_empty_b_full_c_full
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then retrieve_error;

            //transition from retrieve_item to the respective following state
            transition retrieve_item_to_a_full_b_empty_C_empty
                first retrieve_item
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::A and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_full_b_empty_c_empty;
            transition retrieve_item_to_a_empty_b_full_C_empty
                first retrieve_item
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::B and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_empty_b_full_c_empty;
            transition retrieve_item_to_a_empty_b_empty_C_full
                first retrieve_item
                if (
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.storagePosition1x3 == StoragePosition1x3::C and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_empty_b_empty_c_full;
            transition retrieve_item_to_a_full_b_full_C_empty
                first retrieve_item
                if (
                    // Warehouse1x3API::engine::a.occupied == true and
                    // Warehouse1x3API::engine::b.occupied == true and
                    // Warehouse1x3API::engine::c.occupied == false and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_full_b_full_c_empty;
            transition retrieve_item_to_a_full_b_empty_C_full
                first retrieve_item
                if (
                    // Warehouse1x3API::engine::a.occupied == true and
                    // Warehouse1x3API::engine::b.occupied == false and
                    // Warehouse1x3API::engine::c.occupied == true and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_full_b_empty_c_full;
            transition retrieve_item_to_a_empty_b_full_C_full
                first retrieve_item
                if (
                    // Warehouse1x3API::engine::a.occupied == false and
                    // Warehouse1x3API::engine::b.occupied == true and
                    // Warehouse1x3API::engine::c.occupied == true and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_empty_b_full_c_full;
            transition retrieve_item_to_a_full_b_full_C_full
                first retrieve_item
                if (
                    // Warehouse1x3API::engine::a.occupied == true and
                    // Warehouse1x3API::engine::b.occupied == true and
                    // Warehouse1x3API::engine::c.occupied == false and
                    Warehouse1x3API::engine::currentWarehouse1x3Cmd.actionWarehouse == WarehouseActionDef::RETRIEVE
                )
                    then a_full_b_full_c_full;
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
    in port Warehouse1x3ItemPort {
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

    connect warehouse1x3API.engine.cantileverMotorCmdPort to warehouse1x3API.cantileverMotorAPI.actuator1DAPICmdPort;
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
