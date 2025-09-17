package de.unistuttgart.isw.dtservices.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.unistuttgart.isw.dtservices.services.configuration.GatewayCofiguration;


public class EventFetcher<EventType extends DtEvent> implements Runnable {
    
    // each context / DT has its own cache
    private final Map<String, EventCache<EventType>> caches = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayCofiguration gatewayConfiguration;
    
    // used for cleaning up the cache and fetching new events
    private final int refreshRate = 100;
    private final Thread t;
    public EventFetcher(GatewayCofiguration gatewayConfiguration) {
        this.t = new Thread(this);       
        this.gatewayConfiguration = gatewayConfiguration;
    }


    public void start() {
        t.start();
    }

    public void stop() {
        t.interrupt();
    }

    public void addContext(String context) {
        caches.put(context, new EventCache<>(5000));
    }

    public void removeContext(String context) {
        caches.remove(context);
    }

    public void getEvent(
        final String context, 
        final String eventName, 
        final int count, 
        final int maxAge, 
        final int timeout, 
        final Function<List<DtEvent>, Void> callback,
        final Function<List<DtEvent>, Void> timeoutCallback) {
        
            final List<DtEvent> events = new LinkedList<>();
            // fetch as many events from the cache that are not older than maxAge, stop if count is reached
            EventCache<EventType> cache = caches.get(context);
            Set<String> alreadyRetrievedHashes = new HashSet<>();
            // start time
            long startTime = System.currentTimeMillis();
            while(events.size() < count && System.currentTimeMillis() - startTime < timeout) {
                if (cache != null) {
                    for(DtEvent e : cache.getEvent(eventName, count, maxAge)){
                        try {
                            String val = objectMapper.writeValueAsString(e);
                            if (alreadyRetrievedHashes.add(val)) {
                                events.add(e);
                            }
                        } catch (JsonProcessingException ex) {
                        }
                    }
                }                
            }   
            if (events.size() < count) {
                timeoutCallback.apply(events);
            } else {
                callback.apply(events);
            }
    }

    public void fetchEvents() {
        // fetch events
        this.gatewayConfiguration.getGatewayContext().forEach(context -> {
            String contextName = context.getName();
            String rabbitMQCon = context.getDigitalTwin().getRabbitMQConnection();
            String inboutQueue = context.getDigitalTwin().getInboundQueue();
            // create a new connection to the rabbitmq server                      
            try {
                ConnectionFactory factory = new ConnectionFactory();                  
                factory.setUri(rabbitMQCon);
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();               
                // declare the queue if it does not exist
                channel.queueDeclare(inboutQueue, true, false, false, null);

                // create a consumer for the queue
                channel.basicConsume(inboutQueue, true, (consumerTag, message) -> {
                    // get the event from the message
                    try {
                        DtEvent event = objectMapper.readValue(message.getBody(), DtEvent.class);
                    // add the event to the cache
                        EventCache<EventType> cache = caches.get(contextName);
                        if (cache != null) {
                            cache.addEvent(contextName, event);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }, consumerTag -> { });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        // this start to listen to a rabbitmq queue
        fetchEvents();
        while (!Thread.interrupted()) {            
            caches.values().forEach(EventCache::cleanUp);            
            try {
                Thread.sleep(refreshRate);
            } catch (InterruptedException e) {
               
            }
        }
    }

}
