package EnumsAndParameters;

/**
 * Names of the methods that are being invoked at the machine.
 */
public enum Command {
    PICK, //Roboter
    PLACE, //Roboter
    MOVE, //pick&Place
    STORE, //Warehouse
    MOVELB,
    GET, //Warehouse
    INOUT, //Warehouse
    EJECT,
    STIRR,
    FREEZE,
    PRESS,
    EXECUTE, //Förderbänder, Punching, Multiprocessing
    GOTOCONFIG, //Roboter, Warehouse
    STOP, //all
    SETUP
}
