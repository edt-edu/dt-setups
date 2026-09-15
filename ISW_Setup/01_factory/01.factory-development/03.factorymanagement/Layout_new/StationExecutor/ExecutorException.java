package Layout_new.StationExecutor;

/**
 * is thrown if the logic cant handle the config
 */
public class ExecutorException extends Throwable {
    public ExecutorException(String message) {
        super(message);
    }
}
