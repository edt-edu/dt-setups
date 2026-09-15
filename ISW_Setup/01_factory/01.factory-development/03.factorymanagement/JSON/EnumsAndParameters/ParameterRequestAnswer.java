package JSON.EnumsAndParameters;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * class, into which paramterRequestAnswers are deserialized when this program receives the proper answer string
 */
public class ParameterRequestAnswer {
    private final RequestedParameter requestedParameter;
    private final Object value;
    private final String valueClass;

    public ParameterRequestAnswer(@JsonProperty("requestedParameter") RequestedParameter requestedParameter,
                                  @JsonProperty("value") Object value,
                                  @JsonProperty("valueClass") String valueClass) {
        this.requestedParameter = requestedParameter;
        this.value = value;
        this.valueClass = valueClass;
    }

    public RequestedParameter getRequestedParameter() {
        return requestedParameter;
    }

    public Object getValue() {
        return value;
    }

    public String getValueClass() {
        return valueClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterRequestAnswer)) return false;
        ParameterRequestAnswer that = (ParameterRequestAnswer) o;
        return requestedParameter == that.requestedParameter && Objects.equals(value, that.value) && Objects.equals(valueClass, that.valueClass);
    }

    @Override
    public String toString() {
        return "ParameterRequestAnswer{" +
                "requestedParameter=" + requestedParameter +
                ", value=" + value +
                ", valueClass='" + valueClass + '\'' +
                '}';
    }
}
