package JSON.Parsing;

import JSON.CommandsANDStatusrequests.*;
import JSON.EnumsAndParameters.*;
import JSON.Exceptions.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Class which is the basis for all objects that are being parsed.
 *
 * It defines some kind of common header structure for all json outputs.
 */
@JsonPropertyOrder({"topicName", "timestamp", "message"})
public class JSONOutput {

    private final String topicName; //as machine ID
    private final ZonedDateTime timestamp;
    private final JSONParsable message;

    /**
     * Constructor of a JSONOutput object, that is then going into serialization. Only used in JSONOutput-builder.
     *
     * Ensures there is a "header" for all output (topic name, timestamp)
     *
     * @param topicName
     * @param message
     */
    private JSONOutput(@JsonProperty("topicName") String topicName,
                      @JsonProperty("message") JSONParsable message) {
        this.topicName = topicName;
        this.timestamp = ZonedDateTime.now();
        this.message = message;
    }

    public String getTopicName() {
        return topicName;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public JSONParsable getMessage() {
        return message;
    }

    public String toString() {
        return "JSONOutput{" +
                "topicName=" + topicName +
                ", timestamp=" + timestamp +
                ", message=" + message +
                '}';
    }



    /**
     * Builder for JSONOutput class - Only way to create JSONOutput objects!!!
     */
    public static class JSONOutputBuilder {

        private final AllMachineCommandConfigs allMachineCommandConfigs;
        private final AllMachineStatusRequestConfigs allMachineStatusRequestConfigs;

        private String topicName = null;
        private JSONOutputType jsonType = null;
        private CommandType type = null;
        private int commandId = 0;
        private int requestId = 0;
        private CommandNames name = null;
        private List<RequestedParameter> requested = new LinkedList<>();
        private List<Parameter> params = new LinkedList<>();

        /**
         * Creation of the builder object with the given configs
         * to use them as knowledge to check the build input for conformance
         */
        public JSONOutputBuilder(String path1, String path2) throws IOException {
            this.allMachineCommandConfigs = new JSONConfigReader().readCommandConfig(path1);
            this.allMachineStatusRequestConfigs = new JSONConfigReader().readStatusRequestConfig(path2);
        }

        /**
         * Sets the command Type
         * The commandType specifies the type of machine the command is executed on, e.g. GRIPPER, WAREHOUSE
         * @param type
         * @return JSONOutputBuilder
         */
        public JSONOutputBuilder setType(CommandType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the json Type
         * The jsonType specifies, which kind of message this is, e.g. a COMMAND or a STATUSREQUEST
         * @param jsonType
         * @return
         */
        public JSONOutputBuilder setJsonType(JSONOutputType jsonType) {
            this.jsonType = jsonType;
            return this;
        }

        /**
         * sets the command name
         * The command name specifies the action, that is executed, e.g. move, pick, place, ...
         * @param name
         * @return
         */
        public JSONOutputBuilder setName(CommandNames name) {
            this.name = name;
            return this;
        }

        /**
         * sets the list of requested parameters
         * @param requested
         * @return
         */
        public JSONOutputBuilder setRequest(List<RequestedParameter> requested) {
            this.requested = requested;
            return this;
        }

        /**
         * sets the parameters that go with the command name - see config for details
         * @param params
         * @return
         */
        public JSONOutputBuilder setParams(List<Parameter> params) {
            this.params = params;
            return this;
        }

        /**
         * sets the topic name
         * topic names need to be individual to each machine
         * the revpis use them to map the commands to the correct machines; a topic name can be e.g. "2.6-Freeze" - see
         * machine name labels for correct names
         * @param topicName
         * @return
         */
        public JSONOutputBuilder setTopicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        /**
         * Reset function that deletes all currently safed builder values
         *
         * Should be called whenever a build is finished or an exception occurs
         */
        private void reset() {
            this.topicName = null;
            this.type = null;
            this.name = null;
            this.params = null;
            this.requested = null;
        }

        /**
         * performs the actual build process returning a JSONOutput object
         *
         * In the build process it uses all known configs which are passed in constructor to check the given input.
         * Throws all kinds of exceptions depending on the actual mistake in the input.
         *
         * Also, this method ensures individual ids for the commands/requests
         *
         * @return the JSONOutput object if the build was successful
         * @throws MissingBuildParameterException @see
         * @throws CommandNotSupportedOnThisMachineException @see
         * @throws WrongNumberOfParametersException @see
         * @throws RequestNotKnownOnThisMachineException @see
         */
        public JSONOutput build() throws JsonApiExcpetion {
            JSONOutput j = null;
            AMachineCommandConfig usedConfig = null;
            RequestedParameter usedRequestParameter = null;
            System.out.println("JSONOutputType " + this.jsonType);

            //check for missing parameters - part 1
            if(this.jsonType == null || this.type == null || this.topicName == null){
                this.reset();
                throw new MissingBuildParameterException();
            }

            //FALL COMMAND
            if(this.jsonType == JSONOutputType.COMMAND){
                //check for missing parameters - part 2
                if(this.name == null){
                    this.reset();
                    throw new MissingBuildParameterException();
                }
                //check, whether the given parameters match a command from the config file (together with if outside the loop)
                for(AnyMachineCommandConfig commandConfig: allMachineCommandConfigs.getAllConfigs()){
                    if(commandConfig.getType() == type){
                        for(AMachineCommandConfig config: commandConfig.getAllCommands()){
                            if(config.getName() == name){
                                usedConfig = config;
                                break;
                            }
                        }
                    }
                }
                if(usedConfig == null){
                    this.reset();
                    throw new CommandNotSupportedOnThisMachineException();
                    //check whether there are enough parameters
                } else if(params.size() != usedConfig.getNumOfParams().stream().mapToInt(Integer::intValue).sum()){
                    this.reset();
                    throw new WrongNumberOfParametersException();
                }
                //increase counter for individual id and call constructor for JSONOutput
                commandId++;
                j = new JSONOutput(topicName, new MachineCommand(jsonType, type, commandId, name, params));

                //FALL STATUSREQUEST
            } else if(this.jsonType == JSONOutputType.STATUSREQUEST){
                //check for missing parameters - part 2
                if(this.requested == null){
                    this.reset();
                    throw new MissingBuildParameterException();
                } else if(this.requested.size() == 0){
                    this.reset();
                    throw new MissingBuildParameterException();
                }
                //check, whether the given parameters match a request from the config file
                for(AnyMachineStatusRequestConfig requestConfig: allMachineStatusRequestConfigs.getAllConfigs()){
                    if(requestConfig.getType() == type){
                        for(RequestedParameter requestedParameter: this.requested){
                            if(requestConfig.getRequestables().contains(requestedParameter)){
                                if(requestedParameter == RequestedParameter.ALL){
                                    this.requested.clear();
                                    this.requested = requestConfig.getRequestables();
                                    this.requested.remove(RequestedParameter.ALL);
                                }
                            } else{
                                this.reset();
                                throw new RequestNotKnownOnThisMachineException();
                            }
                        }
                    }
                }
                //increase counter for individual id and call constructor for JSONOutput
                commandId++;
                j = new JSONOutput(topicName, new MachineStatusRequest(jsonType, type, commandId, requested));
            } else {
                //only COMMANDS and STATUSREQUESTS can be built, all other JSONOutputTypes throw errors
                throw new JsonApiExcpetion();
            }
            //reset internal parameters and return object
            this.reset();
            return j;
        }
    }

}
