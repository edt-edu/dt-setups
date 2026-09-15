package JSON.EnumsAndParameters;

/**
 * status which the machines send back with the JSONfeedback - currently only INACTION and FINISHED used by RevPi TODO
 */
public enum ExecutionStatus {
    MESSAGERECEIVED,
    INACTION,
    INACTIONWITHOUTPACKET,
    FINISHED,
    ERROR
}
