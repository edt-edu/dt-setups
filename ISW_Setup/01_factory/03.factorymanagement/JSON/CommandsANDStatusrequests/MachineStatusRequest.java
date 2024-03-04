package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.CommandType;
import JSON.EnumsAndParameters.JSONOutputType;
import JSON.EnumsAndParameters.RequestedParameter;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONParsable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

/**
 * all json requests are parsed from this class
 */

@JsonTypeName("STATUSREQUEST")
@JsonPropertyOrder({"jsonType", "type", "requestId", "params"})
public class MachineStatusRequest implements JSONParsable {
    private final JSONOutputType jsonType;
    private final CommandType type;
    private final int requestId;
    private final List<RequestedParameter> params;


    /**
     * Creates a request, that contains all necessary information for evaluation on the machine
     *
     * @param jsonType the JSONOutputType, STATUSREQUEST in this case
     * @param type
     * @param requestId
     * @param params
     */
    public MachineStatusRequest(@JsonProperty("jsonType") JSONOutputType jsonType,
                                @JsonProperty("type") CommandType type,
                                @JsonProperty("requestId") int requestId,
                                @JsonProperty("params") List<RequestedParameter> params) throws JsonApiExcpetion {
        if(jsonType != JSONOutputType.STATUSREQUEST){
            throw new JsonApiExcpetion(); //TODO change to something more clear
        }
        this.jsonType = jsonType;
        this.type = type;
        this.requestId = requestId;
        this.params = params;
    }

    public JSONOutputType getJsonType() {
        return jsonType;
    }

    public CommandType getType() {
        return type;
    }

    public int getOutputId() {
        return requestId;
    }

    public List<RequestedParameter> getParameters() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineStatusRequest)) return false;
        MachineStatusRequest that = (MachineStatusRequest) o;
        return requestId == that.requestId && Objects.equals(jsonType, that.jsonType) && type == that.type && Objects.equals(params, that.params);
    }

    @Override
    public String toString() {
        return "MachineStatusRequest{" +
                "jsonType=" + jsonType +
                ", type=" + type +
                ", requestId=" + requestId +
                ", params=" + params +
                '}';
    }
}
