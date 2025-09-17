package de.unistuttgart.isw.query.context.value;

import de.unistuttgart.isw.query.context.var.BooleanContextVariable;
import de.unistuttgart.isw.query.context.var.ContextVariable;

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
