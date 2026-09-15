package Commands;

import enums.VacuumGripperEnums;
import mqtt.*;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttException;

public class VacuumGripperCommand extends Command
{
    private VacuumGripperEnums vacuumGripperCommandEnum;

    private int[] target;

    public VacuumGripperCommand(int islandNumber, int machineNumber, VacuumGripperEnums vacuumGripperCommandEnum, int[] target, String brokerIp)
    {
        super(islandNumber, machineNumber, brokerIp);

        this.machine = "vacuumGripper";
        this.subscriberTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/status";
        this.publisherTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/control";
        this.vacuumGripperCommandEnum = vacuumGripperCommandEnum;
        this.target = target;

        switch (vacuumGripperCommandEnum)
        {
            case PICK:
                this.jsonCommand = "{\"valve\": \"true\"}";
                break;
            case PLACE:
                this.jsonCommand = "{\"valve\": \"false\"}";;
                break;
            case MOVE:
                this.jsonCommand = "{\"arm\": \"to=" + this.target[0] + "\", \"rotate\": \"to=" + this.target[1] + "\", \"vertical\": \"to=" + this.target[2] + "\"}";
                break;
            case ACTIVATE_COMPRESSOR:
                this.jsonCommand = "{\"compressor\": \"true\"}";
                break;
            case DEACTIVATE_COMPRESSOR:
                this.jsonCommand = "{\"compressor\": \"false\"}";
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
                VacuumGripperFeedback vaccumGripperFeedback = gson.fromJson(subscriber.getOldestMessage(), VacuumGripperFeedback.class);

                System.out.println("Kompressor: " + vaccumGripperFeedback.getCompressor() + " Ventil: " + vaccumGripperFeedback.getValve());

                if(vacuumGripperCommandEnum.equals(VacuumGripperEnums.MOVE))
                {
                    if(vaccumGripperFeedback.getArmStatus().equals("stopped") && vaccumGripperFeedback.getRotorStatus().equals("stopped") && vaccumGripperFeedback.getVerticalStatus().equals("stopped"))
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