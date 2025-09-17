package services.dtservices.services.gateway.operations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import services.dtservices.services.query.SelectionOperation;


public class Select implements SelectionOperation {

    @JsonProperty("fields")
    private List<String> fields;
    @JsonProperty("serviceName")
    private String serviceName;

    @Override
    @JsonIgnore
    public List<String> getFields() {
        return fields;
    }

    @Override
    @JsonIgnore
    public String getServiceName() {
        return serviceName;
    }

 
    
}
