package services.dtservices.services.query;

import services.dtservices.services.query.query.context.value.ContextValue;

import java.util.Map;


public interface InsertableOperation {
    public String getServiceName();
    public Map<String, ContextValue<?>> getValues();
}
