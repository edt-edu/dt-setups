package de.unistuttgart.isw.query.context.var;

import java.util.Map;

import de.unistuttgart.isw.query.context.value.ContextValue;
import de.unistuttgart.isw.query.context.value.MapContextValue;

public class MapContextVariable extends ContextVariable<Map<String, ContextValue<?>>, MapContextValue> {

    public MapContextVariable(String name, MapContextValue value) {
        super(name, value);
    }

    
}
