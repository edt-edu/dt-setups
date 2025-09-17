package de.unistuttgart.isw.query.context.value;

import java.util.Map;

import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.context.var.MapContextVariable;

public class MapContextValue extends ContextValue<Map<String, ContextValue<?>>> {

    public MapContextValue(Map<String, ContextValue<?>> value) {
        super(value);
    }

    @Override
    public ContextValueType getType() {
        return ContextValueType.MAP;
    }

    @Override
    public ContextVariable<Map<String, ContextValue<?>>, ? extends ContextValue<Map<String, ContextValue<?>>>> toVariable(
            String varName) {
        return new MapContextVariable(varName, this);
    }
    
}
