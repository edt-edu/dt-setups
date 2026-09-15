package JSON.EnumsAndParameters;

/**
 * Names of the methods that are being invoked at the machine.
 */
public enum CommandNames {
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
    EXECUTE, //Förderbänder, Punching, Multiprocessing #TODO get rid of execute
    GOTOCONFIG, //Roboter, Warehouse
    STOP, //all
    SETUP
}
