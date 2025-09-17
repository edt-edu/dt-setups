import EnumsAndParameters.*;
import mqtt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Gson-Builder erstellen mit Einstellung, das Strings formatiert werden

        ThreeDGripper gripper22 = new ThreeDGripper("2.2-Grip"); // Erstellen eines Greifer-Objekts mit ID entsprechend der Beschriftung auf Modellfabrik
        gripper22.setCommand(Command.MOVE); // Command aus Enum auswählen
        gripper22.setPickPosition(Isle2Positions.GRIP21IO27START.getPosition()); // Position aus Enum auswählen. (Sind bisher nur ein paar Testweise vorhanden)
        gripper22.setPlacePosition(Isle2Positions.GRIP21CONV23CENTEREND.getPosition());
        Message test = new Message(MessageType.COMMAND, gripper22); // Gesamte Message erstellen

        String stringTest = gson.toJson(test);// JSON-String mittels Gson erstellen
        System.out.println(stringTest);



        //MqttClientISW mqttTest = new MqttClientISW("subscribe/isle2", 1, "tcp://192.168.1.104:6000", "127.0.0.1", "publish/isle2", 1, "tcp://192.168.1.104:6000", "127.0.0.2");

        //mqttTest.sendMessage(stringTest); // JSON-String mittels MQTT senden

        /*while(true) // Zum empfangen der Feedback-Strings
        {
            if(mqttTest.messageAvailable())
            {
                System.out.println(mqttTest.getMessageFromList());
            }
        }*/
    }
}

//in revpimain inputbufferitem wird abgearbeitet, wird in decoderFunctions definiert