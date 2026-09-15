package JSON.FeedbackANDStatusANDOrders;

import JSON.EnumsAndParameters.ExecutionStatus;
import JSON.EnumsAndParameters.JSONOutputType;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONReadable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

/**
 * The feedback that machines produce during the execution of commands fits into that class.
 */
@JsonTypeName("FEEDBACK")
@JsonPropertyOrder({"jsonType", "commandId", "status", "info"})
public class CommandFeedback implements JSONReadable {


    private final JSONOutputType jsonType;
    private final int commandId;
    private final ExecutionStatus status;
    private final String info;

    /**
     * the constructor for objects of this class
     *
     * @param jsonType the JSONOutputType, FEEDBACK in this case
     * @param commandId the id of the command the feedback is related to
     * @param status A status word of type ExecutionStatus
     * @param info optionally, a message
     */
    @JsonCreator
    public CommandFeedback(@JsonProperty("jsonType") JSONOutputType jsonType,
                           @JsonProperty("commandId") int commandId,
                           @JsonProperty("status") ExecutionStatus status,
                           @JsonProperty("info") String info) throws JsonApiExcpetion {
        if(jsonType != JSONOutputType.FEEDBACK){
            throw new JsonApiExcpetion(); //TODO change to something more clear
        }
        this.jsonType = jsonType;
        this.commandId = commandId;
        this.status = status;
        this.info = info;
    }

    public int getCommandId() {
        return commandId;
    }

    public JSONOutputType getJsonType(){
        return this.jsonType;
    }


    public ExecutionStatus getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandFeedback)) return false;
        CommandFeedback that = (CommandFeedback) o;
        return commandId == that.commandId && Objects.equals(jsonType, that.jsonType) && status == that.status && Objects.equals(info, that.info);
    }

    @Override
    public String toString() {
        return "CommandFeedback{" +
                "jsonType=" + jsonType +
                ", commandId=" + commandId +
                ", status=" + status +
                ", info=" + info +
                '}';
    }
}
