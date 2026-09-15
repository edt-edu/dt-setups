package JSON.Exceptions;

/**
 * Is thrown if the machine doesn't offer the execution of this command. E.g. a conveyor can't perform a PICK operation.
 *
 * If you think that function should exist and this exception is falsely thrown,
 * take a look at the config file.
 */
public class CommandNotSupportedOnThisMachineException extends JsonApiExcpetion {
}
