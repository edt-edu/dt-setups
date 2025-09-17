package services.dtservices.services.query;

import services.dtservices.services.query.query.context.value.ContextValue;

import java.util.Map;


public interface UpdateOperation {
    public String getServiceName();
    public Map<String, ContextValue<?>> getValues();
    // those should be the id values to find records
    public Map<String, ContextValue<?>> getWhereValues();
}
