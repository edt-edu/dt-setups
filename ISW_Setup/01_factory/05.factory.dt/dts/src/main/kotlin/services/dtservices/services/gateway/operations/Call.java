package services.dtservices.services.gateway.operations;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import services.dtservices.services.query.CallOperation;


public class Call implements CallOperation {

    @JsonProperty("function")
    private String function;
    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("parameters")
    private Map<String, String> parameters;

    @Override
    @JsonIgnore
    public String getFunction() {
        return function;
    }

    @Override
    @JsonIgnore
    public String getServiceName() {
        return serviceName;
    }

    @Override
    @JsonIgnore
    public Map<String, String> getParameters() {
        return parameters;
    }
    
}
