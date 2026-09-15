package JSON.TestServer;

import JSON.EnumsAndParameters.*;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONInput;
import JSON.Parsing.JSONOutput;
import JSON.Parsing.JSONParser;
import JSON.Parsing.JSONReadable;
import JSON.Server.Communication;
import JSON.Server.RevPiNumber;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JSONBuilderTest {


    public JSONBuilderTest() {

    }

    /*public String testMethode() throws IOException, JsonApiExcpetion {
        JSONOutput.JSONOutputBuilder builder;
        List<Parameter> params = new LinkedList<>();
        params.add(new Parameter(Direction.FORWARD));
        builder = new JSONOutput.JSONOutputBuilder(path1, path2);
        //builder.reset();
        test = parser.parse(builder.setType(CommandType.CONVEYOR).setJsonType(JSONOutputType.COMMAND).setTopicName("1.1-Conv").setName(CommandNames.MOVELB).setParams(params).build());

        System.out.println(test);
        return test;
    }*/

    public static void main(String[] args) throws IOException, JsonApiExcpetion, InterruptedException {

        JSONParser parser = new JSONParser();
        String path1 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineCommands.json";
        String path2 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineStatusRequests.json";
        JSONOutput.JSONOutputBuilder builder;
        List<Parameter> params = new LinkedList<>();
        List<Parameter> paramsEmpty = new LinkedList<>();
        paramsEmpty.add(new Parameter(new NumberNatural(3)));
        params.add(new Parameter(Direction.FORWARD));
        builder = new JSONOutput.JSONOutputBuilder(path1, path2);
        LinkedList<RequestedParameter> requestp = new LinkedList<>();
        requestp.add(RequestedParameter.MOTORCONVEYORBELTFORWARD);
        //builder.reset();
        String test = parser.parse(builder.setType(CommandType.CONVEYOR).setJsonType(JSONOutputType.COMMAND).setTopicName("2.3-Conv").setName(CommandNames.MOVELB).setParams(params).build());
        String test2 = parser.parse(builder.setType(CommandType.CONVEYOR).setJsonType(JSONOutputType.STATUSREQUEST).setTopicName("2.3-Conv").setRequest(requestp).build());
        Communication communication = new Communication(RevPiNumber.CORE2);


        //Optional<CommandFeedback> feedback = Optional.empty();
        //communication.addToSendList(test);
        communication.addToSendList(test);
        while(true){
            TimeUnit.MILLISECONDS.sleep(100);
            communication.refreshData();
            if(communication.receivedAvailable()) {
                JSONInput input = parser.readJSONInput(communication.removeFromReceiveList());
                JSONReadable readable = input.getMessage();
                System.out.println(readable);
            }
            //TimeUnit.MILLISECONDS.sleep(1000);

        }

    }
}
