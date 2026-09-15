package Commands;

import enums.GripperEnums;
import mqtt.*;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class GripperCommand extends Command
{
    private GripperEnums gripperCommandEnum;
    private int[] target;

    public GripperCommand(int islandNumber, int machineNumber, GripperEnums gripperCommandEnum, int[] target, String brokerIp)
    {
        super(islandNumber, machineNumber, brokerIp);

        this.machine = "clawGripper";
        this.subscriberTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/status";
        this.publisherTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/control";
        this.gripperCommandEnum = gripperCommandEnum;
        this.target = target;

        switch (gripperCommandEnum)
        {
            case PICK:
                this.jsonCommand = "{\"claw\": \"close\"}";
                break;
            case PLACE:
                this.jsonCommand = "{\"claw\": \"open\"}";;
                break;
            case MOVE:
                this.jsonCommand = "{\"arm\": \"to=" + this.target[0] + "\", \"rotate\": \"to=" + this.target[1] + "\", \"vertical\": \"to=" + this.target[2] + "\"}";
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
                GripperFeedback gripperFeedback = gson.fromJson(subscriber.getOldestMessage(), GripperFeedback.class);

                System.out.println("Greifer status: " + gripperFeedback.getClawStatus());

                if(this.gripperCommandEnum.equals(GripperEnums.MOVE))
                {
                    if(gripperFeedback.getArmStatus().equals("stopped") && gripperFeedback.getRotorStatus().equals("stopped") && gripperFeedback.getVerticalStatus().equals("stopped"))
                    {
                        loop = false;
                    }
                }
                else
                {
                    loop = false;
                }

            }
        }

        subscriber.close();
        System.out.println("Finished command " + this.getClass().getName());
    }
}
