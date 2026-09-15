package Commands;

import enums.ConveyorEnums;
import mqtt.*;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttException;

import static enums.ConveyorEnums.*;

public class ConveyorCommand extends Command
{
    private ConveyorEnums conveyorCommandEnum;
    private int steps;
    public ConveyorCommand(int islandNumber, int machineNumber, ConveyorEnums conveyorCommandEnum, int steps, String brokerIp)
    {
        super(islandNumber, machineNumber, brokerIp);

        this.machine = "conveyor";
        this.subscriberTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/status";
        this.publisherTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/control";
        this.conveyorCommandEnum = conveyorCommandEnum;
        this.steps = steps;

        switch (conveyorCommandEnum)
        {
            case MOTOR_RIGHT:
                this.jsonCommand = "{\"motor\": \"right\"}";
                break;
            case MOTOR_LEFT:
                this.jsonCommand = "{\"motor\": \"left\"}";
                break;
            case MOTOR_RIGHT_TO:
                this.jsonCommand = "{\"motor\": \"right-to=" + steps + "\"}";
                break;
            case MOTOR_LEFT_TO:
                this.jsonCommand = "{\"motor\": \"left-to=" + steps + "\"}";
                break;
            case MOTOR_STOP:
                this.jsonCommand = "{\"motor\": \"stop\"}";
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
                ConveyorFeedback conveyorFeedback = gson.fromJson(subscriber.getOldestMessage(), ConveyorFeedback.class);

                System.out.println(conveyorFeedback.getState());

                if(this.conveyorCommandEnum.equals(MOTOR_RIGHT) || this.conveyorCommandEnum.equals(MOTOR_LEFT))
                {
                    loop = false;
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
