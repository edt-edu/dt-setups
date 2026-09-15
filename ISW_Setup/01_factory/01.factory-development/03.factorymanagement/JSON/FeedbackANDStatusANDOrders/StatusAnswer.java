package JSON.FeedbackANDStatusANDOrders;

import JSON.EnumsAndParameters.JSONOutputType;
import JSON.EnumsAndParameters.ParameterRequestAnswer;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONReadable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

/**
 * The status that machines return after a status request fits into this class.
 */
@JsonTypeName("STATUSANSWER")
public class StatusAnswer implements JSONReadable {

    private final JSONOutputType jsonType;
    private final int requestId;
    private final List<ParameterRequestAnswer> answers;


    /**
     * the constructor for objects of this class
     *
     * @param jsonType
     * @param requestId
     * @param answers
     * @throws JsonApiExcpetion
     */
    @JsonCreator
    public StatusAnswer(@JsonProperty("jsonType") JSONOutputType jsonType,
                        @JsonProperty("requestId") int requestId,
                        @JsonProperty("answers") List<ParameterRequestAnswer> answers) throws JsonApiExcpetion {
        if(jsonType != JSONOutputType.STATUSANSWER){
            throw new JsonApiExcpetion(); //TODO change to something more clear
        }
        this.jsonType = jsonType;
        this.requestId = requestId;
        this.answers = answers;
    }

    public JSONOutputType getJsonType() { return jsonType; }

    public int getRequestId() {
        return requestId;
    }

    public List<ParameterRequestAnswer> getAnswers() {
        return answers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusAnswer)) return false;
        StatusAnswer that = (StatusAnswer) o;
        return requestId == that.requestId &&  Objects.equals(answers, that.answers);
    }

    @Override
    public String toString() {
        return "StatusAnswer{" +
                "jsonType=" + jsonType +
                ", requestId=" + requestId +
                ", answers=" + answers +
                '}';
    }
}
