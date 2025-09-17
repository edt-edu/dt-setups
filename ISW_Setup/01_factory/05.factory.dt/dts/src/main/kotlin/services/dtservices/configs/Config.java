package services.dtservices.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class Config {

    @Value("${SERVICE_TOPIC_NAME:sample_computation_service}")
    private String topicExchangeName;

    @Value("${SERVICE_QUEUE_NAME:sample_computation_service_queue}")
    private String queueName;

    @Value("${GATEWAY_QUEUE_NAME:gateway_queue}")
    private String gatewayQueueName;

    @Value("${SERVICE_RABBIT_MQ_URI:amqp://user:password@localhost:5672}")
    private String rabbitMQUri;

    @Value("${GATEWAY_PORT:8085}")
    private int gatewayPort;

    @Value("${MODUS:gateway}")
    private String modus = "gateway"; // "gateway" or "service"

    @Value("${SERVICE_CLASS:null}")
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
