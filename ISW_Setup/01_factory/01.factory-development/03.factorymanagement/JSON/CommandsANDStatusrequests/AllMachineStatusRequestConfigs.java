package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.CommandType;
import JSON.Exceptions.ConfigException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * This class contains all possible requests for each machine type
 */
public class AllMachineStatusRequestConfigs {
    private final List<AnyMachineStatusRequestConfig> allConfigs;

    public AllMachineStatusRequestConfigs(@JsonProperty("allConfigs") List<AnyMachineStatusRequestConfig> allConfigs) throws ConfigException {
        this.allConfigs = allConfigs;
        List<CommandType> types = new LinkedList<>();
        for(AnyMachineStatusRequestConfig confs: allConfigs){
            if(!types.contains(confs.getType())){
                types.add(confs.getType());
            } else {
                throw new ConfigException("there are two configs for the same machine");
            }
        }
    }

    public List<AnyMachineStatusRequestConfig> getAllConfigs() {
        return allConfigs;
    }

    @Override
    public String toString() {
        return "AllMachineStatusRequestConfigs{" +
                "allConfigs=" + allConfigs +
                '}';
    }
}
