package Commands;

import enums.SortingLineEnums;
import mqtt.*;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SortingLineCommand extends Command
{
    private SortingLineEnums sortingLineCommandEnums;
    public SortingLineCommand(int islandNumber, int machineNumber, SortingLineEnums sortingLineCommandEnums, String brokerIp)
    {
        super(islandNumber, machineNumber, brokerIp);

        this.machine = "sortingLine";
        this.subscriberTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/status";
        this.publisherTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/control";
        this.sortingLineCommandEnums = sortingLineCommandEnums;

        switch (sortingLineCommandEnums)
        {
            case ACTION_MOVE_TO_EJECTORS:
                this.jsonCommand = "{\"action\": \"move to ejectors\"}";
                break;
            case ACTION_RESOLVE_FAILURE:
                this.jsonCommand = "{\"action\": \"resolve failure\"}";
                break;
            case ACTION_SORT_COLOR_WHITE:
                this.jsonCommand = "{\"action\": \"sort\", \"color\": \"white\"}";
                break;
            case ACTION_SORT_COLOR_RED:
                this.jsonCommand = "{\"action\": \"sort\", \"color\": \"red\"}";
                break;
            case ACTION_SORT_COLOR_BLUE:
                this.jsonCommand = "{\"action\": \"sort\", \"color\": \"blue\"}";
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
                SortingLineFeedback sortingLineFeedback = gson.fromJson(subscriber.getOldestMessage(), SortingLineFeedback.class);

                System.out.println(sortingLineFeedback.getState());

                if(sortingLineFeedback.getState().equals("package waiting at ejectors") || sortingLineFeedback.getState().equals("ready") )
                {
                    loop = false;
                }
            }
        }

        subscriber.close();
        System.out.println("Finished command " + this.getClass().getName());
    }
}
