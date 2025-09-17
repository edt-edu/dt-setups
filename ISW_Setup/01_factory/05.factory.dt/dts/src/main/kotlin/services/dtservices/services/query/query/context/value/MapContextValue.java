package services.dtservices.services.query.query.context.value;


import services.dtservices.services.query.query.context.var.ContextVariable;
import services.dtservices.services.query.query.context.var.MapContextVariable;

import java.util.Map;

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
