package Commands;

import com.google.gson.Gson;
import enums.WarehouseEnums;
import mqtt.MqttPublisher;
import mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttException;

public class WarehouseCommand extends Command
{
    private WarehouseEnums warehouseCommandEnum;
    private int row;
    private int column;
    public WarehouseCommand(int islandNumber, int machineNumber, WarehouseEnums warehouseCommandEnum, int row, int column, String brokerIp)
    {
        super(islandNumber, machineNumber, brokerIp);

        this.machine = "warehouse";
        this.subscriberTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/status";
        this.publisherTopic = islandNumber + "-" + machineNumber + "-" + this.machine + "/control";
        this.warehouseCommandEnum = warehouseCommandEnum;
        this.row = row;
        this.column = column;

        switch(warehouseCommandEnum)
        {
            case ACTION_STORE:
                this.jsonCommand = "{\"action\": \"store\", \"row\": " + this.row + ", \"column\": " + this.column + "}";
                break;
            case ACTION_RETRIEVE:
                this.jsonCommand = "{\"action\": \"retrieve\", \"row\": " + this.row + ", \"column\": " + this.column + "}";
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
                WarehouseFeedback warehouseFeedback = gson.fromJson(subscriber.getOldestMessage(), WarehouseFeedback.class);

                if(warehouseFeedback.getState().equals("ready"))
                {
                    loop = false;
                }
            }
        }

        subscriber.close();
        System.out.println("Finished command " + this.getClass().getName());
    }
}
