package de.unistuttgart.isw.dtservices.services.configuration.gateway;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
