package services.dtservices.impl;


import org.springframework.beans.factory.annotation.Autowired;
import services.dtservices.configs.Config;
import services.dtservices.events.EventRegistry;
import services.dtservices.services.GatewayService;
import services.dtservices.services.auth.ServicePermission;
import services.dtservices.services.configuration.GatewayCofiguration;
import services.dtservices.services.gateway.GatewayRequest;
import services.dtservices.services.gateway.GatewayResponse;
import services.util.routing.DTResonse;
import services.util.routing.ServiceRoute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultGateway extends GatewayService {

    private Config config;
  
    private GatewayCofiguration configuration;

    private EventRegistry eventRegistry;

    @Autowired
    public DefaultGateway(Config config, GatewayCofiguration configuration) {        
        super(configuration); 
        this.config = config;
        this.eventRegistry = new EventRegistry<>(configuration);
    }

    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

    @Override
    protected String mailboxName() {
        return config.getGatewayQueueName();
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public DTResonse pushTaskToDigitalTwin(String dtID, String task) {
        DTResonse response = new DTResonse("Unimplemented method 'pushTaskToDigitalTwin'");
        return response;

    }

    @Override
    public Map<String, String> getDigitalTwinUri() {
        return new HashMap<>();
    }

    @Override
    public List<ServiceRoute> getRoutes() {
        return List.of();
    }

    @Override
    public List<ServicePermission> getAllPermissions() {
        return List.of();
    }

    @Override
    public GatewayResponse sendRequest(GatewayRequest request) {
        return null;
    }

    @Override
    public GatewayResponse sendRequest(String Query) {
        return null;
    }
}
