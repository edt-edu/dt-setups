package Commands;

import enums.IndexedEnums;
import mqtt.*;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttException;

public class IndexedLineCommand extends Command
{
    private IndexedEnums indexedCommandEnum;
    public IndexedLineCommand(int islandNumber, int machineNumber, IndexedEnums indexedCommandEnum, String brokerIp)
    {
        super(islandNumber, machineNumber, brokerIp);
        this.machine = "indexedLine";
        this.subscriberTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/status";
        this.publisherTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/control";
        this.indexedCommandEnum = indexedCommandEnum;

        switch (indexedCommandEnum)
        {
            case ACTION_MILL:
                this.jsonCommand = "{\"action\": \"mill\"}";
                break;
            case ACTION_DRILL:
                this.jsonCommand = "{\"action\": \"drill\"}";
                break;
            case ACTION_TRANSFER_FEED_TO_MILL:
                this.jsonCommand = "{\"action\": \"transfer\", \"transfer_from_to\": \"feed_to_mill\"}";
                break;
            case ACTION_TRANSFER_MILL_TO_DRILL:
                this.jsonCommand = "{\"action\": \"transfer\", \"transfer_from_to\": \"mill_to_drill\"}";
                break;
            case ACTION_TRANSFER_DRILL_TO_END:
                this.jsonCommand = "{\"action\": \"transfer\", \"transfer_from_to\": \"drill_to_end\"}";
                break;
        }
    }

    public void send() throws MqttException
    {
        Gson gson = new Gson();

        MqttSubscriber subscriber = new MqttSubscriber(this.subscriberTopic, 1, brokerIp, this.getClass().getName() + "/subscriber");

        MqttPublisher publisher = new MqttPublisher(this.publisherTopic, 1, brokerIp, this.getClass().getName() + "/publisher");
        publisher.sendMessage(this.jsonCommand);

        while(loop)
        {
            if(subscriber.messageAvailable())
            {
                IndexedLineFeedback indexedLineFeedback = gson.fromJson(subscriber.getOldestMessage(), IndexedLineFeedback.class);

                if(indexedLineFeedback.getState().equals("ready"))
                {
                    loop = false;
                }
            }
        }

        subscriber.close();
        System.out.println("Finished command " + this.getClass().getName());
    }
}