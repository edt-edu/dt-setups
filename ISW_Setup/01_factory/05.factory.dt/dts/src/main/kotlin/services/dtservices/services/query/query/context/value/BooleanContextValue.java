package services.dtservices.services.query.query.context.value;

import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.value.ContextValueType;
import services.dtservices.services.query.query.context.var.BooleanContextVariable;
import services.dtservices.services.query.query.context.var.ContextVariable;

public class BooleanContextValue extends ContextValue<Boolean> {

    public BooleanContextValue(Boolean value) {
        super(value);
    }

    @Override
    public ContextValueType getType() {
        return ContextValueType.BOOLEAN;
    }

    @Override
    public ContextVariable<Boolean, ? extends ContextValue<Boolean>> toVariable(String varName) {
        return new BooleanContextVariable(varName, this);
    }
    
}
