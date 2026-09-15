package Commands;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Command
{
    protected String jsonCommand;
    protected  String machine;
    protected String subscriberTopic;
    protected String publisherTopic;
    protected String brokerIp;
    protected Boolean loop;

    public Command(int islandNumber, int machineNumber, String brokerIp)
    {
        this.brokerIp = brokerIp;
        this.loop = true;
    }

    public void send() throws MqttException
    {

    }
}