package de.unistuttgart.isw.dtservices.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import de.unistuttgart.isw.dtservices.configs.Config;
import de.unistuttgart.isw.dtservices.events.EventRegistry;
import de.unistuttgart.isw.dtservices.services.GatewayService;
import de.unistuttgart.isw.dtservices.services.auth.ServicePermission;
import de.unistuttgart.isw.dtservices.services.configuration.GatewayCofiguration;
import de.unistuttgart.isw.util.routing.DTResonse;
import de.unistuttgart.isw.util.routing.ServiceRoute;

public class DefaultGateway extends GatewayService{

    
    private Config config;
  
    private GatewayCofiguration configuration;

    private EventRegistry eventRegistry;

    @Autowired
    public DefaultGateway(Config config, GatewayCofiguration configuration) {        
        super(configuration); 
        this.config = config;
        this.eventRegistry = new EventRegistry<>(configuration);
    }

    @Override
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
        throw new UnsupportedOperationException("Unimplemented method 'pushTaskToDigitalTwin'");
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

    
   
    
}
