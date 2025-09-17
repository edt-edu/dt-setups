package de.unistuttgart.isw.dtservices.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component 
public class Config {

    @Value("${SERVICE_TOPIC_NAME}")
    private String topicExchangeName;

    @Value("${SERVICE_QUEUE_NAME}")
    private String queueName;

    @Value("${GATEWAY_QUEUE_NAME}")
    private String gatewayQueueName;

    @Value("${SERVICE_RABBIT_MQ_URI}")
    private String rabbitMQUri;

    @Value("${GATEWAY_PORT}")
    private int gatewayPort;

    @Value("${MODUS}")
    private String modus = "gateway"; // "gateway" or "service"

    @Value("${SERVICE_CLASS}")
    private String serviceClass;

    public String getTopicExchangeName() {
        return topicExchangeName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getRabbitMQUri() {
        return rabbitMQUri;
    }

    public String getGatewayQueueName() {
        return gatewayQueueName;
    }

    public int getGatewayPort() {
        return gatewayPort;
    }

    public String getModus() {
        return modus;
    }

    public boolean isGateway() {
        return modus.toLowerCase().equals("gateway");
    }

    public boolean isService() {
        return modus.toLowerCase().equals("service");
    }

    public String getServiceClass() {
        return serviceClass;
    }
        
}
