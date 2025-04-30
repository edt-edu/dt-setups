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
    attribute storagePosition : StoragePosition;
    attribute VERTICAL_LOCATION_CONFIG : Integer;
    attribute HORIZONTAL_LOCATION_CONFIG : Integer;
    attribute DEPTH_LOCATION_CONFIG : Integer;
    attribute occupied : Boolean;
}
attribute def StorageLocation1x3 {
    attribute storagePosition : StoragePosition1x3;
    attribute VERTICAL_LOCATION_CONFIG : Integer;
    attribute HORIZONTAL_LOCATION_CONFIG : Integer;
    attribute DEPTH_LOCATION_CONFIG : Integer;
    attribute occupied : Boolean;
    item storedItem : MovableItem;
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
    in port warehouse1x3APIItemPort {
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
        item currentWarehouse1x3State : Warehouse1x3State; // the current state of the machine in the Warehouse1x3 ...:>> stateAPI
        item currentWarehouse1x3Cmd : Warehouse1x3Cmd; // the current command for the machine in the Warehouse1x3
        item currentCantileverCmd : CantileverCmd {} // the current command for the cantilever motor
        item currentHorizontalCmd : HorizontalCmd {} // the current command for the horizontal motor
        item currentVerticalCmd : VerticalCmd {} // the current command for the vertical motor
        item currentConveyorBeltCmd : ConveyorBeltCmd {} // the current command for the conveyor belt
        attribute currentTargetStorageLocation : StorageLocation1x3; // the current storage location of the machine in the Warehouse1x3

        attribute inputPosition : StorageLocation1x3 {
            attribute storagePosition := StoragePosition::INPUT;
            attribute VERTICAL_LOCATION_CONFIG := 1;
            attribute HORIZONTAL_LOCATION_CONFIG := 1;
            attribute DEPTH_LOCATION_CONFIG := 1;
            attribute occupied := false;
            item storedItem := null;
        }
        attribute a : StorageLocation1x3 {
            attribute storagePosition := StoragePosition1x3::A;
            attribute VERTICAL_LOCATION_CONFIG := 1;
            attribute HORIZONTAL_LOCATION_CONFIG := 1;
            attribute DEPTH_LOCATION_CONFIG := 1;
            attribute occupied := false;
            item storedItem := null;
        }
        attribute b : StorageLocation1x3 {
            attribute storagePosition := StoragePosition1x3::B;
            attribute VERTICAL_LOCATION_CONFIG := 1;
            attribute HORIZONTAL_LOCATION_CONFIG := 1;
            attribute DEPTH_LOCATION_CONFIG := 1;
            attribute occupied := false;
            item storedItem := null;
        }
        attribute c : StorageLocation1x3 {
            attribute storagePosition := StoragePosition1x3::C;
            attribute VERTICAL_LOCATION_CONFIG := 1;
            attribute HORIZONTAL_LOCATION_CONFIG := 1;
            attribute DEPTH_LOCATION_CONFIG := 1;
            attribute occupied := false;
            item storedItem := null;
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
        in port warehouse1x3APIItemPort {
            in item warehouse1x3APIItem : MovableItem;
        }
        out port warehouse1x3StatePort {
            out item warehouse1x3State : Warehouse1x3State;
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
