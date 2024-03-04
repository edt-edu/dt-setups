package JSON.Exceptions;

/**
 * Is thrown if the Output Builder receives empty fields due to them not being assigned prior.
 */
public class MissingBuildParameterException extends JsonApiExcpetion {
    public MissingBuildParameterException() {
        super();
    }
}
