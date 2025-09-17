package services.dtservices.services.configuration.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JsonContextFile {

    private List<GatewayContext> context;
    @JsonProperty("default")
    private String defaultContext;
    
    public JsonContextFile(List<GatewayContext> context){
        this.context = context;
    } 

    private JsonContextFile(){
    } 

    public List<GatewayContext> getContext() {
        return context;
    }

    public String getDefaultContext() {
        return defaultContext;
    }
}
