package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.*;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONParsable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

/**
 * all json commands are parsed from this class
 */
@JsonTypeName("COMMAND")
@JsonPropertyOrder({"jsonType", "type", "commandId", "name", "parameters"})
public class MachineCommand implements JSONParsable {

    private final JSONOutputType jsonType;
    private final CommandType type;
    private final int commandId;
    private final CommandNames name;
    private final List<Parameter> parameters;

    /**
     * Creates a command, that contains all necessary information for execution on a machine
     *
     * @param jsonType the JSONOutputType, COMMAND in this case
     * @param type the machine type the command is for
     * @param commandId an individual number
     * @param name name of the command
     * @param parameters parameters the command uses (see config file)
     */
    public MachineCommand(@JsonProperty("jsonType") JSONOutputType jsonType,
                          @JsonProperty("type") CommandType type,
                          @JsonProperty("commandId") int commandId,
                          @JsonProperty("name") CommandNames name,
                          @JsonProperty("parameters") List<Parameter> parameters) throws JsonApiExcpetion {
        if(jsonType != JSONOutputType.COMMAND){
            throw new JsonApiExcpetion(); //TODO change to something more clear
        }
        this.jsonType = jsonType;
        this.type = type;
        this.commandId = commandId;
        this.name = name;
        this.parameters = parameters;
    }

    public CommandType getType() {
        return type;
    }

    public int getOutputId() {
        return commandId;
    }

    public JSONOutputType getJsonType() {
        return jsonType;
    }

    public CommandNames getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineCommand)) return false;
        MachineCommand that = (MachineCommand) o;
        return commandId == that.commandId && Objects.equals(jsonType, that.jsonType) && type == that.type && Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters);
    }

    @Override
    public String toString() {
        return "MachineCommand{" +
                "jsonType=" + jsonType +
                ", type=" + type +
                ", commandId=" + commandId +
                ", name=" + name +
                ", parameters=" + parameters +
                '}';
    }
}