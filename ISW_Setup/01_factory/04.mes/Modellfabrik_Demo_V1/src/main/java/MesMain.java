import Commands.*;
import enums.*;
import mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Time;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class MesMain
{
    private Queue<ShopOrder> shopOrders;
    private Queue<Command> currentCommandChain;

    public MesMain() throws MqttException
    {

    }

    public static void main(String[] args) throws InterruptedException, MqttException {
        MesMain testMain = new MesMain();
        MqttSubscriber subscriber = new MqttSubscriber("test", 1, "tcp://localhost:1883", "MES");

        while(true)
        {
            if(subscriber.messageAvailable())
            {
                subscriber.getOldestMessage();
                testMain.startDemoProcessingOrder();
                System.out.println("Nächster Befehl");
            }
        }
    }
    private void startDemoProcessingOrder() throws MqttException, InterruptedException {
        Queue<Command> testQueue = new LinkedList<>();

        // Von Verladestation auf Förderband vor SortingLine
        GripperCommand demoGrip11;
        GripperCommand demoGripE1;
        GripperCommand demoGrip12;
        GripperCommand demoGrip13;
        GripperCommand demoGripE2;
        GripperCommand demoGrip14;
        GripperCommand demoGrip15;
        GripperCommand demoGrip16;
        GripperCommand demoGrip17;
        GripperCommand demoGrip18;
        GripperCommand demoGrip19;
        testQueue.add(demoGrip11 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{0, 2365, 2400},"tcp://localhost:1883"));
        testQueue.add(demoGripE1 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{10, 2365, 2400},"tcp://localhost:1883"));
        testQueue.add(demoGrip12 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{10, 2365, 2850},"tcp://localhost:1883"));
        testQueue.add(demoGrip13 = new GripperCommand(1,2, GripperEnums.PICK, null,"tcp://localhost:1883"));
        testQueue.add(demoGrip14 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{10, 2365, 1500},"tcp://localhost:1883"));
        testQueue.add(demoGripE2 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{10, 1700, 1500},"tcp://localhost:1883"));
        testQueue.add(demoGrip15 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{81, 1700, 1500},"tcp://localhost:1883"));
        testQueue.add(demoGrip16 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{81, 1700, 2100},"tcp://localhost:1883"));
        testQueue.add(demoGrip17 = new GripperCommand(1,2, GripperEnums.PLACE, null,"tcp://localhost:1883"));
        testQueue.add(demoGrip18 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{81, 1700, 1500},"tcp://localhost:1883"));
        testQueue.add(demoGrip19 = new GripperCommand(1,2, GripperEnums.MOVE, new int[]{0, 0, 0},"tcp://localhost:1883"));

        //Von Förderband durch Sortingline auf Rot
        ConveyorCommand demoConv1;
        SortingLineCommand demoSort1;
        SortingLineCommand demoSort2;
        testQueue.add(demoConv1 = new ConveyorCommand(1, 1, ConveyorEnums.MOTOR_LEFT_TO, 25, "tcp://localhost:1883"));
        testQueue.add(demoSort1 = new SortingLineCommand(1, 4, SortingLineEnums.ACTION_MOVE_TO_EJECTORS, "tcp://localhost:1883"));
        testQueue.add(demoSort2 = new SortingLineCommand(1, 4, SortingLineEnums.ACTION_SORT_COLOR_RED, "tcp://localhost:1883"));

        //Von SortingLine Rot zu IndexedLine
        VacuumGripperCommand demoVac11;
        VacuumGripperCommand demoVac12;
        VacuumGripperCommand demoVac13;
        VacuumGripperCommand demoVac14;
        VacuumGripperCommand demoVac15;
        VacuumGripperCommand demoVac16;
        VacuumGripperCommand demoVac17;
        VacuumGripperCommand demoVac18;
        VacuumGripperCommand demoVac19;
        VacuumGripperCommand demoVac110;
        VacuumGripperCommand demoVac111;
        testQueue.add(demoVac11 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{80,2700,1200}, "tcp://localhost:1883"));
        testQueue.add(demoVac12 = new VacuumGripperCommand(1,6,VacuumGripperEnums.ACTIVATE_COMPRESSOR, null, "tcp://localhost:1883"));
        testQueue.add(demoVac13 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{80,2700,1550}, "tcp://localhost:1883"));
        testQueue.add(demoVac14 = new VacuumGripperCommand(1,6,VacuumGripperEnums.PICK, null, "tcp://localhost:1883"));
        testQueue.add(demoVac15 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{80,2700,800}, "tcp://localhost:1883"));
        testQueue.add(demoVac16 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{1450,2125,800}, "tcp://localhost:1883"));
        testQueue.add(demoVac17 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{1400,2125,1200}, "tcp://localhost:1883"));
        testQueue.add(demoVac18 = new VacuumGripperCommand(1,6,VacuumGripperEnums.PLACE, null, "tcp://localhost:1883"));
        testQueue.add(demoVac19 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{900,2125,800}, "tcp://localhost:1883"));
        testQueue.add(demoVac110 = new VacuumGripperCommand(1,6,VacuumGripperEnums.DEACTIVATE_COMPRESSOR, null, "tcp://localhost:1883"));
        testQueue.add(demoVac111 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{0,0,0}, "tcp://localhost:1883"));

        //Durch die IndexedLine
        IndexedLineCommand demoIndex1;
        IndexedLineCommand demoIndex2;
        IndexedLineCommand demoIndex3;
        IndexedLineCommand demoIndex4;
        IndexedLineCommand demoIndex5;
        testQueue.add(demoIndex1 = new IndexedLineCommand(1, 7, IndexedEnums.ACTION_TRANSFER_FEED_TO_MILL, "tcp://localhost:1883"));
        testQueue.add(demoIndex2 = new IndexedLineCommand(1, 7, IndexedEnums.ACTION_MILL,"tcp://localhost:1883"));
        testQueue.add(demoIndex3 = new IndexedLineCommand(1, 7, IndexedEnums.ACTION_TRANSFER_MILL_TO_DRILL,"tcp://localhost:1883"));
        testQueue.add(demoIndex4 = new IndexedLineCommand(1, 7, IndexedEnums.ACTION_DRILL,"tcp://localhost:1883"));
        testQueue.add(demoIndex5 = new IndexedLineCommand(1, 7, IndexedEnums.ACTION_TRANSFER_DRILL_TO_END,"tcp://localhost:1883"));

        //Von IndexedLine auf Zwischenablage
        GripperCommand demoGrip21;
        GripperCommand demoGrip22;
        GripperCommand demoGrip23;
        GripperCommand demoGrip24;
        GripperCommand demoGrip25;
        GripperCommand demoGrip26;
        GripperCommand demoGrip27;
        GripperCommand demoGrip28;
        GripperCommand demoGrip29;
        testQueue.add(demoGrip21 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{0, 2200, 1800},"tcp://localhost:1883"));
        testQueue.add(demoGrip22 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{0, 2200, 2300},"tcp://localhost:1883"));
        testQueue.add(demoGrip23 = new GripperCommand(1,8, GripperEnums.PICK, null,"tcp://localhost:1883"));
        testQueue.add(demoGrip24 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{0, 2200, 2100},"tcp://localhost:1883"));
        testQueue.add(demoGrip25 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{0, 3500, 2100},"tcp://localhost:1883"));
        testQueue.add(demoGrip26 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{3, 3500, 2700},"tcp://localhost:1883"));
        testQueue.add(demoGrip27 = new GripperCommand(1,8, GripperEnums.PLACE, null,"tcp://localhost:1883"));
        testQueue.add(demoGrip28 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{3, 3500, 1200},"tcp://localhost:1883"));
        testQueue.add(demoGrip29 = new GripperCommand(1,8, GripperEnums.MOVE, new int[]{0, 0, 0},"tcp://localhost:1883"));

        //Von Zwischenablage in Warenlager
        VacuumGripperCommand demoVac21;
        VacuumGripperCommand demoVacE1;
        VacuumGripperCommand demoVac22;
        VacuumGripperCommand demoVac23;
        VacuumGripperCommand demoVac24;
        VacuumGripperCommand demoVac25;
        VacuumGripperCommand demoVac26;
        VacuumGripperCommand demoVac28;
        VacuumGripperCommand demoVac29;
        VacuumGripperCommand demoVac210;
        VacuumGripperCommand demoVac211;
        testQueue.add(demoVac21 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{0,1775,1500}, "tcp://localhost:1883"));
        testQueue.add(demoVacE1 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{1450,1775,1500}, "tcp://localhost:1883"));
        testQueue.add(demoVac22 = new VacuumGripperCommand(1,6,VacuumGripperEnums.ACTIVATE_COMPRESSOR, null, "tcp://localhost:1883"));
        testQueue.add(demoVac23 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{1450,1775,1650}, "tcp://localhost:1883"));
        testQueue.add(demoVac24 = new VacuumGripperCommand(1,6,VacuumGripperEnums.PICK, null, "tcp://localhost:1883"));
        testQueue.add(demoVac25 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{1450,1775,0}, "tcp://localhost:1883"));
        testQueue.add(demoVac26 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{1450,1430,0}, "tcp://localhost:1883"));
        testQueue.add(demoVac28 = new VacuumGripperCommand(1,6,VacuumGripperEnums.PLACE, null, "tcp://localhost:1883"));
        testQueue.add(demoVac29 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{0,1430,0}, "tcp://localhost:1883"));
        testQueue.add(demoVac210 = new VacuumGripperCommand(1,6,VacuumGripperEnums.DEACTIVATE_COMPRESSOR, null, "tcp://localhost:1883"));
        testQueue.add(demoVac211 = new VacuumGripperCommand(1,6,VacuumGripperEnums.MOVE, new int[]{0,0,0}, "tcp://localhost:1883"));

        //In Warenlager einlagern
        WarehouseCommand demoWare;
        testQueue.add(demoWare = new WarehouseCommand(1,5,WarehouseEnums.ACTION_STORE,1,1,"tcp://localhost:1883"));

        /*while (!testQueue.isEmpty())
        {
            testQueue.poll().send();
        }*/

        // Von Verladestation auf Förderband vor SortingLine
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        TimeUnit.SECONDS.sleep(1);
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        TimeUnit.SECONDS.sleep(1);
        testQueue.poll().send();
        testQueue.poll().send();

        //Von Förderband durch Sortingline auf Rot
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();

        //Von SortingLine Rot zu IndexedLine
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();

        //Durch die IndexedLine

        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();

        //Von IndexedLine auf Zwischenablage
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        TimeUnit.SECONDS.sleep(1);
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        TimeUnit.SECONDS.sleep(1);
        testQueue.poll().send();
        testQueue.poll().send();

        //Von Zwischenablage in Warenlager
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();
        testQueue.poll().send();

        //In Warenlager einlagern
        testQueue.poll().send();
    }
}