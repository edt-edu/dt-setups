package JSON.Parsing;

import JSON.EnumsAndParameters.CommandType;
import JSON.EnumsAndParameters.JSONOutputType;

import java.util.List;

/**
 * Interface that is a supertype to all classes meant for being held in JSONOutput class.
 */
public interface JSONParsable {
    int getOutputId();
    CommandType getType();
    JSONOutputType getJsonType();
}
