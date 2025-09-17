package services.dtservices.services.query.query.responses;


import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.var.ContextVariable;

public class LetResponse {

    private final ContextValue<?> value;
    private final String type;


    public LetResponse(ContextVariable<?,?> variable) {
        this.value = variable.getValue();
        this.type = variable.getType().getTypeAsString();
    }

    public LetResponse(ContextValue<?> value, String type) {
        this.value = value;
        this.type = type;
    }

    /**
     * @return the value
     */
    public ContextValue<?> getValue() {
        return value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    
    
}
