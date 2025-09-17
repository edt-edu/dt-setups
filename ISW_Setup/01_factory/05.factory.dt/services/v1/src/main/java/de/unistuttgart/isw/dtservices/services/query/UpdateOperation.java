package de.unistuttgart.isw.dtservices.services.query;

import java.util.Map;

import de.unistuttgart.isw.query.context.value.ContextValue;

public interface UpdateOperation {
    public String getServiceName();
    public Map<String, ContextValue<?>> getValues();
}
