package de.unistuttgart.isw.dtservices.events;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.unistuttgart.isw.dtservices.services.configuration.GatewayCofiguration;
import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext;


public class EventRegistry<DTEventType extends DtEvent> {
    
    private final EventFetcher<DTEventType> fetcher;
    private final Set<String> contexts = new HashSet<>();
    private final GatewayCofiguration gatewayConfiguration;
    private final Map<String, EventPublisher<DTEventType>> publishers = new HashMap<>();
    
    public EventRegistry(GatewayCofiguration gatewayConfiguration) {
        this.gatewayConfiguration = gatewayConfiguration;    
        this.fetcher = new EventFetcher<>(gatewayConfiguration);
    }

    public void addContexts(Collection<String> contexts) {        
        for (String context : contexts) {
            fetcher.addContext(context);
        }
    }

    public Set<String> getActivatedContexts() {
        return contexts;
    }

    public GatewayContext getContextConfig(String context) {
        return gatewayConfiguration.getGatewayContext().stream()
                .filter(c -> c.getName().equals(context))
                .findFirst()
                .orElse(null);
    }

    public void addContext(String context) {
        this.contexts.add(context);
        fetcher.addContext(context);
    }

    public void removeContext(String context){  
        this.contexts.remove(context);
        fetcher.removeContext(context);
    }

    public void removeContexts(Collection<String> contexts) {
        for (String context : contexts) {
            this.contexts.remove(context);
        }
    }

    public void sendEvent(String context, String eventName, DTEventType event) {
        // call DT endpoint for sending event
        if(publishers.containsKey(context)) {
            publishers.get(context).publishEvent(new Event(System.currentTimeMillis(), event));
        } else {
            EventPublisher<DTEventType> publisher = new EventPublisher<>(getContextConfig(context).getDigitalTwin());
            publisher.publishEvent(new Event(System.currentTimeMillis(), event));
            publishers.put(context, publisher);
        }
    }

    public void waitForEvents(
        final String context, final String eventName, 
        final int count, final int maxAge, final int timeout, 
        final Function<List<DtEvent>, Void> callback, final Function<List<DtEvent>, Void> timeoutCallback) {
            fetcher.getEvent(context, eventName, count, maxAge, timeout, callback, timeoutCallback);
    }

    public void start() {
        fetcher.start();
    }

    public void stop() {
        fetcher.stop();
    }
    
}
