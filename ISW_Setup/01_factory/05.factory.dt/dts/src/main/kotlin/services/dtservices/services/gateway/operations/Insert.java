package services.dtservices.services.gateway.operations;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import services.dtservices.services.query.InsertableOperation;
import services.dtservices.services.query.query.context.value.ContextValue;


public class Insert implements InsertableOperation {

    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("values")
    private Map<String, ContextValue<?>> values;

    @Override
    @JsonIgnore
    public String getServiceName() {
        return serviceName;
    }

    @Override
    @JsonIgnore
    public Map<String, ContextValue<?>> getValues() {
        return values;
    }

    

    
    
}
