package services.dtservices.services.query.query.context.value;

import services.dtservices.services.query.query.context.var.ContextVariable;
import services.dtservices.services.query.query.context.var.NumberContextVariable;

public class NumberContextValue extends ContextValue<Number> {

    public NumberContextValue(Number value) {
        super(value);
    }

    public NumberContextValue(String value) {        
        super(parseString(value));
    }

    private static Number parseString(String value) {
        // check if the number is an integer
        boolean containsDot = value.contains(".");
        if (!containsDot) {
            return Long.valueOf(value);
        }
        return Double.valueOf(value);
    }

    @Override
    public ContextValueType getType() {
        return ContextValueType.NUMBER;
    }

    @Override
    public ContextVariable<Number, ? extends ContextValue<Number>> toVariable(String varName) {
        return new NumberContextVariable(varName, this);
    }
    
}
