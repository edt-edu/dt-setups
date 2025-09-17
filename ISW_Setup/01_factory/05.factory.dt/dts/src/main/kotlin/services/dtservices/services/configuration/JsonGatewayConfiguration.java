package services.dtservices.services.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import services.dtservices.services.configuration.gateway.GatewayContext;
import services.dtservices.services.configuration.gateway.JsonContextFile;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class JsonGatewayConfiguration implements GatewayCofiguration {
    
    private final JsonContextFile jsonContextFile;

    public JsonGatewayConfiguration(final String contextJsonFile) {
        this.jsonContextFile = parse(contextJsonFile);
    }

    public JsonGatewayConfiguration(final File jsonContextFile) {
        this.jsonContextFile = parse(jsonContextFile);
    }

    private JsonContextFile parse(final File jsonFile) {
        // parse json file
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonFile, JsonContextFile.class);           
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private JsonContextFile parse(final String json) {
        // parse json file
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, JsonContextFile.class);           
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<GatewayContext> getGatewayContext() {
        return jsonContextFile.getContext();
    }

    @Override
    public String getDefaultContext() {
        return jsonContextFile.getDefaultContext();
    }
    

    @Override
    public GatewayContext.Service findService(String serviceName) {
        for(GatewayContext context : jsonContextFile.getContext()){
            if(jsonContextFile.getDefaultContext().equals(context.getName())) {
                GatewayContext.Service service = context.getServices().get(serviceName);
                if(service != null){
                    return service;
                }
            }            
        }
        return null;
    }

    @Override
    public List<String> getServices(String context) {
        Optional<GatewayContext> cont = this.jsonContextFile.getContext()
            .stream()
            .filter(c -> c.getName().equals(context))
            .findFirst();

        if(cont.isPresent()){
            return cont.get().getServices().keySet().stream().toList();
        }

        return List.of();
    }

    @Override
    public List<String> getContexts() {
        return this.jsonContextFile.getContext().stream().map(GatewayContext::getName).toList();
    }
}
