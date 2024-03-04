package JSON.CommandsANDStatusrequests;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Class that implements mechanisms for reading the JSON-config files
 * The Reader interprets the given config files and fills the AllMachineCommandConfigs and
 * AllMachineStatusRequestConfigs with the information from the JSON-config files and wraps
 * them into the All-/Any-/A-MachineCommand/-StatusRequest-Config(s)
 */
public class JSONConfigReader {

    private final ObjectMapper mapper;

    /**
     * setup of the config reader to match the used json-scheme
     */
    public JSONConfigReader(){
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        //To avoid the first parsing process at runtime to take much longer than expected,
        //make mapper read example Json-Strings for each class
    }

    /**
     * Method to read config files associated with Machine Commands
     *
     * @param path the file path to the config file
     * @return the AllMachineCommandConfigs object containing all the information from the file
     * @throws IOException if the file can not be accessed
     */
    public AllMachineCommandConfigs readCommandConfig(String path) throws IOException {
        File file = new File(path);
        return mapper.readerFor(AllMachineCommandConfigs.class).readValue(file);
    }

    /**
     * Method to read config files associated with Status Requests
     *
     * @param path the file path to the config file
     * @return the AllMachineStatusRequestConfigs object containing all the information from the file
     * @throws IOException if the file can not be accessed
     */
    public AllMachineStatusRequestConfigs readStatusRequestConfig(String path) throws IOException {
        File file = new File(path);
        return mapper.readerFor(AllMachineStatusRequestConfigs.class).readValue(file);
    }

}

