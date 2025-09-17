package de.unistuttgart.isw.dtservices.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext.DtConfig;

public class EventPublisher<DTEventType extends DtEvent> {
    private final DtConfig dtConfig;
    private Connection connection;
    private Channel channel;
    private ObjectMapper objectMapper = new ObjectMapper();

    public EventPublisher(DtConfig dtConfig) {
        this.dtConfig = dtConfig;        
    }

    private Channel getChannel() {
        if (channel == null || !channel.isOpen()) {
            try {
                String connString = dtConfig.getRabbitMQConnection();
                ConnectionFactory factory = new ConnectionFactory();                  
                factory.setUri(connString);
                this.connection = factory.newConnection();
                channel = connection.createChannel();
                return channel;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create channel", e);
            }
        }
        return channel;
    }

    /**
     * Publish an event to the RabbitMQ queue. It serializes the event to JSON
     */
    public void publishEvent(Event event) {
        try {
            String serialized = objectMapper.writeValueAsString(event.getEvent());
            Channel channel = getChannel();
            // use empty exchange to directly publish to the queue
            String exchange = "";
            String routingKey =  dtConfig.getOutboundQueue();
            channel.basicPublish(exchange, routingKey, null, serialized.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
}
