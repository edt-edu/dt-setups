package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.*;
import JSON.Exceptions.ConfigException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.LinkedList;
import java.util.List;

/**
 * only for testing purposes
 */
public class JSONConfigParser {

    private final ObjectMapper mapper;

    public JSONConfigParser(){
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void configParserForCommands() throws JsonProcessingException, ConfigException {
        List<AMachineCommandConfig> conf = new LinkedList<>();
        List<Object> paramTypes = new LinkedList<>();
        List<Integer> numOfParams = new LinkedList<>();
        List<Integer> numOfParams2 = new LinkedList<>();
        JSONOutputType jsonType = JSONOutputType.COMMAND;
        CommandNames name = CommandNames.MOVE;
        paramTypes.add(PositionParameterThreeD.class);
        numOfParams.add(2);
        numOfParams2.add(1);
        AMachineCommandConfig m1 = new AMachineCommandConfig(jsonType, name, paramTypes, numOfParams, "test");
        conf.add(m1);
        AMachineCommandConfig m2 = new AMachineCommandConfig(jsonType, CommandNames.PICK, paramTypes, numOfParams2, "description");
        conf.add(m2);
        AnyMachineCommandConfig mm = new AnyMachineCommandConfig(CommandType.GRIPPER, conf);
        String str = mapper.writeValueAsString(mm);
        //System.out.println(str);

        List<AMachineCommandConfig> conf2 = new LinkedList<>();
        List<Object> paramTypes2 = new LinkedList<>();
        List<Integer> numOfParams3 = new LinkedList<>();
        List<Integer> numOfParams4 = new LinkedList<>();
        JSONOutputType jsonType2 = JSONOutputType.COMMAND;
        CommandNames name2 = CommandNames.MOVE;
        paramTypes2.add(PositionParameterThreeD.class);
        numOfParams3.add(2);
        numOfParams4.add(1);
        AMachineCommandConfig m3 = new AMachineCommandConfig(jsonType2, name2, paramTypes2, numOfParams3, "comment");
        conf2.add(m3);
        AMachineCommandConfig m4 = new AMachineCommandConfig(jsonType2, CommandNames.PICK, paramTypes2, numOfParams4, "string");
        conf2.add(m4);
        AnyMachineCommandConfig mm2 = new AnyMachineCommandConfig(CommandType.VACUUM, conf2);
        String str2 = mapper.writeValueAsString(mm2);
        //System.out.println(str2);

        List<AnyMachineCommandConfig> allConfs = new LinkedList<>();
        allConfs.add(mm);
        allConfs.add(mm2);

        AllMachineCommandConfigs alls;
        try {
            alls = new AllMachineCommandConfigs(allConfs);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        String str3 = mapper.writeValueAsString(alls);
        System.out.println(str3);

    }

    public void configParserForStatus() throws JsonProcessingException, ConfigException {
        JSONOutputType jsonType = JSONOutputType.STATUSREQUEST;
        List<RequestedParameter> requestedParameters = new LinkedList<>();
        requestedParameters.add(RequestedParameter.MOTORCONVEYORBELTFORWARD);
        requestedParameters.add(RequestedParameter.LIGHTBARRIERINSIDE);
        CommandType type = CommandType.GRIPPER;
        AnyMachineStatusRequestConfig m = new AnyMachineStatusRequestConfig(type, jsonType, requestedParameters);

        jsonType = JSONOutputType.STATUSREQUEST;
        requestedParameters = new LinkedList<>();
        requestedParameters.add(RequestedParameter.MOTORCONVEYORBELTFORWARD);
        requestedParameters.add(RequestedParameter.LIGHTBARRIERINSIDE);
        type = CommandType.VACUUM;
        AnyMachineStatusRequestConfig n = new AnyMachineStatusRequestConfig(type, jsonType, requestedParameters);

        List<AnyMachineStatusRequestConfig> allConfs = new LinkedList<>();
        allConfs.add(m);
        allConfs.add(n);

        AllMachineStatusRequestConfigs alls;
        try {
            alls = new AllMachineStatusRequestConfigs(allConfs);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        String str3 = mapper.writeValueAsString(alls);
        System.out.println(str3);

    }

    public static void main(String[] args){
        try {
            new JSONConfigParser().configParserForCommands();
            //new JSONConfigParser().configParserForStatus();
        } catch (JsonProcessingException | ConfigException e) {
            throw new RuntimeException(e);
        }
    }
}
