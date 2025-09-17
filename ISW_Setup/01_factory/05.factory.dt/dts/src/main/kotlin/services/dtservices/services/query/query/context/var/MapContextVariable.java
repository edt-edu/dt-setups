package services.dtservices.services.query.query.context.var;


import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.value.MapContextValue;

import java.util.Map;

public class MapContextVariable extends ContextVariable<Map<String, ContextValue<?>>, MapContextValue> {

    public MapContextVariable(String name, MapContextValue value) {
        super(name, value);
    }

    
}
