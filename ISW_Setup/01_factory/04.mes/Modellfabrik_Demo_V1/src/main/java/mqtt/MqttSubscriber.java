package mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.LinkedBlockingQueue;

public class MqttSubscriber implements MqttCallback
{
    private String topic;
    private int qos;
    private String broker;
    private String clientId;
    private MemoryPersistence persistence;
    private MqttClient javaClient;
    private MqttConnectOptions connOpts;
    private LinkedBlockingQueue<String> inputBuffer = new LinkedBlockingQueue<>();

    public MqttSubscriber(String topic, int qos, String brokerIp, String clientId)
    {
        this.topic = topic;
        this.qos = qos;
        this.broker = brokerIp;
        this.clientId = clientId;
        this.persistence = new MemoryPersistence();

        try
        {
            javaClient = new MqttClient(this.broker, this.clientId, this.persistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setConnectionTimeout(60);
            connOpts.setKeepAliveInterval(60);
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            System.out.println("Connecting to broker: " + broker);
            javaClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Set callback...");
            javaClient.setCallback(this);
            System.out.println("Callback set!");
            System.out.println("Subscribe...");
            javaClient.subscribe(topic);
            System.out.println("Subscribed!");
        }
        catch (MqttException me)
        {
            System.out.println("Reason: " + me.getReasonCode());
            System.out.println("Message: " + me.getMessage());
            System.out.println("Local: " + me.getLocalizedMessage());
            System.out.println("Cause: " + me.getCause());
            System.out.println("Exception: " + me);
            me.printStackTrace();
        }
    }
    public String getOldestMessage()
    {
        if(messageAvailable())
        {
            return this.inputBuffer.remove();
        }
        else
        {
            System.out.println("Queue empty.");
            return null;
        }
    }
    public int getQueueLenght()
    {
        return this.inputBuffer.size();
    }
    public boolean messageAvailable()
    {
        return !this.inputBuffer.isEmpty();
    }
    @Override
    public void connectionLost(Throwable cause)
    {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        this.inputBuffer.add(message.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void close() throws MqttException
    {
        try
        {
            this.javaClient.disconnect(0);
        }
        finally
        {
            this.javaClient.close();
        }
    }
}