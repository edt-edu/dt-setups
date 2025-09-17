package de.unistuttgart.isw.dtservices.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;

import de.unistuttgart.isw.dts.gateway.ObjectFactory;
import de.unistuttgart.isw.dts.gateway.TGatewayRequest;
import de.unistuttgart.isw.dtservices.configs.Config;
import de.unistuttgart.isw.dtservices.events.DtEvent;
import de.unistuttgart.isw.dtservices.events.EventRegistry;
import de.unistuttgart.isw.dtservices.services.auth.ServicePermission;
import de.unistuttgart.isw.dtservices.services.configuration.GatewayCofiguration;
import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext.Service;
import de.unistuttgart.isw.query.QueryContext;
import de.unistuttgart.isw.util.routing.DTResonse;
import de.unistuttgart.isw.util.routing.ServiceRoute;
import jakarta.annotation.PostConstruct;

/**
 * The Gateway service will be configured via a DSL. Which allows to union or device features.
 * 
 */
public abstract class GatewayService implements IBootable {

    
    protected Map<String, String> digitalTwinUris;
    protected Map<String, ServiceRoute> routesMap = new HashMap<>();
    private ServiceMailbox<TGatewayRequest, ObjectFactory> mailbox;
    private GatewayCofiguration configuration;
    private final EventRegistry<DtEvent> eventHandler;


    @Autowired
    private Config config;

    public GatewayService(final GatewayCofiguration configuration) {
        this.configuration = configuration;
        this.eventHandler = new EventRegistry<>(configuration);
        this.reloadConfig();
    }

    @PostConstruct
    @Override
    public void start(){   	
        try {            
            this.mailbox = new ServiceMailbox(TGatewayRequest.class, ObjectFactory.class, mailboxName(), config.getRabbitMQUri());
            this.mailbox.onParsedMessageListeners.add(this::onParsedMessage);
            this.mailbox.startRabbitMQConsumer();
        } catch (IOException | TimeoutException | URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called when a message is received and parsed
     */
    public void onParsedMessage(TGatewayRequest request){
        this.executeRequest(request);
    }

    protected abstract String mailboxName();


    /**
     * reloads the config of the service
     */
    protected void reloadConfig(){
        List<ServiceRoute> r = this.getRoutes();
        Map<String, ServiceRoute> newMap = new HashMap<>();
        if (r != null) {
            r.forEach(route -> newMap.put(route.getServiceName(), route));
        }
        this.routesMap = newMap;
        this.digitalTwinUris = this.getDigitalTwinUri();
    }

    /**
     * this method handles the request output to the digital twin. I.e. call for models
     */
    public abstract DTResonse pushTaskToDigitalTwin(final String dtID, final String task);

    /**
     * this method handles incoming requests from the digital twin or other services
     */
    public void executeRequest(final TGatewayRequest request) {
        System.out.println("Executed request: " + request);
        // TODO forward to server
    }

    public abstract Map<String, String> getDigitalTwinUri();

    /**
     * Get the routes of the service
     * @return
     */
    public abstract List<ServiceRoute> getRoutes();

    /**
     * broadcast a message to all services to get all available routes and permissions
     */
    public abstract List<ServicePermission> getAllPermissions();

    public Service findService(String serviceName) {
        return this.configuration.findService(serviceName);
    }

    public QueryContext newQueryContext() {
        return new QueryContext(this.configuration.getDefaultContext(), this.eventHandler);
    }

    public GatewayCofiguration getConfiguration() {
        return configuration;
    }

    public List<String> getServices(final String context){
        return this.configuration.getServices(context);
    }

    public List<String> getContexts(){
        return this.configuration.getContexts();
    }

    public void setGatewayOutboundConnection(GatewayOutboundConnection gatewayOutboundConnection) {
        throw new UnsupportedOperationException("Unimplemented method 'setGatewayOutboundConnection'");
    }

    /**
     * Get the event registry for this service
     * @return
     */
    public abstract EventRegistry<DtEvent> getEventRegistry();
    
}
