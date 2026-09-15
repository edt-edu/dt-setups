package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.CommandNames;
import JSON.EnumsAndParameters.JSONOutputType;
import JSON.Exceptions.ConfigException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Holds information for every single command about the necessary parameters
 */
public class AMachineCommandConfig {
    private final JSONOutputType jsonType;
    private final CommandNames name;
    private final List<Object> paramTypes;
    private final List<Integer> numOfParams;
    private final String description;

    public AMachineCommandConfig(@JsonProperty("jsonType")JSONOutputType jsonType,
                                 @JsonProperty("name")CommandNames name, @JsonProperty("paramTypes")List<Object> paramTypes,
                                 @JsonProperty("numOfParams")List<Integer> numOfParams, @JsonProperty("description") String description) throws ConfigException {
        this.jsonType = jsonType;
        this.name = name;
        this.paramTypes = paramTypes;
        this.numOfParams = numOfParams;
        this.description = description;
        if(jsonType == null || name == null){
            throw new ConfigException("missing type or name");
        }
        if(paramTypes.size() != numOfParams.size()){
            throw new ConfigException("number of parameters differs from number of types given");
        }
    }

    public JSONOutputType getJsonType() {
        return jsonType;
    }

    public CommandNames getName() {
        return name;
    }

    public List<Object> getParamTypes() {
        return paramTypes;
    }

    public List<Integer> getNumOfParams() {
        return numOfParams;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "\n AMachineCommandConfig{" +
                "jsonType=" + jsonType +
                ", name=" + name +
                ", paramTypes=" + paramTypes +
                ", numOfParams=" + numOfParams +
                ", description=" + description +
                "}";
    }
}
