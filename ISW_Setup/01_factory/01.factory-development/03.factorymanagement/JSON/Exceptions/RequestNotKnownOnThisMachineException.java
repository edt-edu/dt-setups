package JSON.Exceptions;

/**
 * Is thrown if this request can not be sent to this machine, as this parameter does not exist.
 *
 * If you think that parameter should exist and this exception is falsely thrown,
 * take a look at the config file.
 */
public class RequestNotKnownOnThisMachineException extends JsonApiExcpetion {
}
