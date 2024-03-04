package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.CommandNames;
import JSON.EnumsAndParameters.CommandType;
import JSON.EnumsAndParameters.JSONOutputType;
import JSON.EnumsAndParameters.RequestedParameter;
import JSON.Exceptions.ConfigException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * This class holds information about the parameters that can be requested from one machine
 */
public class AnyMachineStatusRequestConfig {
    private final CommandType type;
    private final JSONOutputType jsonType;
    private final List<RequestedParameter> requestables;

    public AnyMachineStatusRequestConfig(@JsonProperty("type") CommandType type, @JsonProperty("jsonType") JSONOutputType jsonType, @JsonProperty("requestables") List<RequestedParameter> requestables) throws ConfigException {
        this.type = type;
        this.jsonType = jsonType;
        //(Maybe make duplicate requestables illegal - they dont mess anything up but slightly limit performance)
        this.requestables = requestables;
        List<RequestedParameter> requests = new LinkedList<>();
        for(RequestedParameter rqs: requestables){
            if(!requests.contains(rqs)){
                requests.add(rqs);
            } else {
                throw new ConfigException("there are duplicate requests within one machine");
            }
        }
    }

    public CommandType getType() {
        return type;
    }

    public JSONOutputType getJsonType() {
        return jsonType;
    }

    public List<RequestedParameter> getRequestables() {
        return requestables;
    }

    @Override
    public String toString() {
        return "AnyMachineStatusRequestConfig{" +
                "type=" + type +
                ", jsonType=" + jsonType +
                ", requestables=" + requestables +
                '}';
    }
}
