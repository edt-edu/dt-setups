package JSON.Exceptions;

/**
 * This Exception is thrown, whenever something goes wrong while reading the config files
 */
public class ConfigException extends Exception {
    public ConfigException(String s) {
        super(s);
    }
}
