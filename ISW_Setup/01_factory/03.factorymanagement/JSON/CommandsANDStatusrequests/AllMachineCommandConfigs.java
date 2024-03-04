package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.CommandType;
import JSON.Exceptions.ConfigException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * This class contains all possible commands for each machine type
 */
public class AllMachineCommandConfigs {
    private final List<AnyMachineCommandConfig> allConfigs;

    public AllMachineCommandConfigs(@JsonProperty("allConfigs") List<AnyMachineCommandConfig> allConfigs) throws ConfigException {
        this.allConfigs = allConfigs;
        List<CommandType> types = new LinkedList<>();
        for(AnyMachineCommandConfig confs: allConfigs){
            if(!types.contains(confs.getType())){
                types.add(confs.getType());
            } else {
                throw new ConfigException("there are two configs for the same machine");
            }
        }
    }

    public List<AnyMachineCommandConfig> getAllConfigs() {
        return allConfigs;
    }

    @Override
    public String toString() {
        return "AllMachineCommandConfigs{" +
                "allConfigs=\n" + allConfigs +
                '}';
    }
}
