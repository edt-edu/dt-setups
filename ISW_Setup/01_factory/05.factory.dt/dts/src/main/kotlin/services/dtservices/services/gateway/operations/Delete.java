package services.dtservices.services.gateway.operations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import services.dtservices.services.query.DeleteOperation;


public class Delete implements DeleteOperation {


    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("fields")
    private List<String> fields;

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
