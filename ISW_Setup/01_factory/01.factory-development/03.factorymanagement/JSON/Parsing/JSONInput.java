package JSON.Parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Class which is the basis for all objects that are being read.
 *
 * It defines some kind of common header structure for all json inputs.
 */
@JsonPropertyOrder({"topicName", "timestamp", "message"})
public class JSONInput {

    private final String topicName; //as machine ID
    private final ZonedDateTime timestamp;
    private final JSONReadable message;

    /**
     * Constructor of a JSONInput object, that is coming from deserialization.
     *
     * @param topicName
     * @param message
     */
    public JSONInput(@JsonProperty("topicName") String topicName,
                     @JsonProperty("timestamp") ZonedDateTime timestamp,
                     @JsonProperty("message") JSONReadable message) {
        this.topicName = topicName;
        //this.timestamp = ZonedDateTime.now();
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getTopicName() {
        return topicName;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public JSONReadable getMessage() {
        return message;
    }

    public String toString() {
        return "JSONInput{" +
                "topicName=" + topicName +
                ", timestamp=" + timestamp +
                ", message=" + message +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JSONInput)) return false;
        JSONInput jsonInput = (JSONInput) o;
        return Objects.equals(topicName, jsonInput.topicName) && Objects.equals(message, jsonInput.message);
    }

}
