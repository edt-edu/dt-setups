package JSON.Parsing;

import JSON.FeedbackANDStatusANDOrders.CommandFeedback;
import JSON.FeedbackANDStatusANDOrders.StatusAnswer;
import JSON.FeedbackANDStatusANDOrders.YoghurtOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface that is a supertype to all classes meant for being held in JSONInput class.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "jsonType", visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = StatusAnswer.class, name = "STATUSANSWER"),
        @JsonSubTypes.Type(value = CommandFeedback.class, name = "FEEDBACK"),
        @JsonSubTypes.Type(value = YoghurtOrder.class, name = "ORDER")})
public interface JSONReadable {

}
