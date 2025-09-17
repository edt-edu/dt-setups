package mqtt;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttClientISW
{
    private String publisherTopic;
    private String subscriberTopic;
    private int publisherQos;
    private int subscriberQos;
    private String publisherBroker;
    private String subscriberBroker;
    private String publisherId;
    private String subscriberId;
    private MemoryPersistence publsiherPersistence;
    private MemoryPersistence subscriberPersistance;
    private MqttPublisher publisher;
    private MqttSubscriber subscriber;

    public MqttClientISW(String publisherTopic, int publisherQos, String publisherBroker, String publisherId,
                         String subscriberTopic, int subscriberQos, String subscriberBroker, String subscriberId)
    {
        this.publisherTopic = publisherTopic;
        this.publisherQos = publisherQos;
        this.publisherBroker= publisherBroker;
        this.publisherId = publisherId;
        this.publsiherPersistence = new MemoryPersistence();
        this.subscriberTopic = subscriberTopic;
        this.subscriberQos = subscriberQos;
        this.subscriberBroker = subscriberBroker;
        this.subscriberId = subscriberId;
        this.subscriberPersistance = new MemoryPersistence();

        this.publisher = new MqttPublisher(this.publisherTopic, this.publisherQos, this.publisherBroker, this.publisherId);
        this.subscriber = new MqttSubscriber(this.subscriberTopic, this.subscriberQos, this.subscriberBroker, this.subscriberId);
    }

    public void sendMessage(String message)
    {
        this.publisher.sendMessage(message);
    }

    public String getMessageFromList()
    {
        return this.subscriber.getOldestMessage();
    }
    public int getQueueLenght()
    {
        return this.subscriber.getQueueLenght();
    }
    public boolean messageAvailable()
    {
        return this.subscriber.messageAvailable();
    }
}
