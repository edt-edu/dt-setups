package mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublisher
{
    private String topic;
    private int qos;
    private String broker;
    private String clientId;
    private MemoryPersistence persistence;
    private MqttClient javaClient;
    private MqttConnectOptions connOpts;

    public MqttPublisher(String topic, int qos, String brokerIp, String clientId)
    {
        this.topic = topic;
        this.qos = qos;
        this.broker = brokerIp;
        this. clientId = clientId;
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

    public void sendMessage(String message)
    {
        try
        {
            System.out.println("Publishing message: " + message);
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(this.qos);
            javaClient.publish(this.topic, mqttMessage);
            System.out.println("Message published");
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
}
