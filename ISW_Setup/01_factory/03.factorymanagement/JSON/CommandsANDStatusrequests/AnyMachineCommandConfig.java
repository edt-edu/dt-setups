package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.CommandNames;
import JSON.EnumsAndParameters.CommandType;
import JSON.Exceptions.ConfigException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds all allowed commands for one machine type
 */
public class AnyMachineCommandConfig {
    private final CommandType type;
    private final List<AMachineCommandConfig> allCommands;

    public AnyMachineCommandConfig(@JsonProperty("type") CommandType type, @JsonProperty("allCommands") List<AMachineCommandConfig> allCommands) throws ConfigException {
        this.type = type;
        this.allCommands = allCommands;
        List<CommandNames> commands = new LinkedList<>();
        for(AMachineCommandConfig comms: allCommands){
            if(!commands.contains(comms.getName())){
                commands.add(comms.getName());
            } else {
                throw new ConfigException("there are two configs for the same command within one machine");
            }
        }
    }

    public CommandType getType() {
        return type;
    }

    public List<AMachineCommandConfig> getAllCommands() {
        return allCommands;
    }

    @Override
    public String toString() {
        return "\n \n AnyMachineCommandConfig{" +
                "type=" + type +
                " allCommands=\n" + allCommands +
                "}";
    }
}
