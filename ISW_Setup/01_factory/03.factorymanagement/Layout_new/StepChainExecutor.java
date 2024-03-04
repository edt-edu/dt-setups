package Layout_new;

import JSON.EnumsAndParameters.ExecutionStatus;
import JSON.EnumsAndParameters.JSONOutputType;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.FeedbackANDStatusANDOrders.CommandFeedback;
import JSON.Parsing.JSONOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * the underlying logic to the stationExecutors, goes through the list of machineCommandParamTriples, evaluates feedback and creates commands
 */
public class StepChainExecutor {

    private final List<MachineCommandParamTriple> orderedCommandParamTripleList;
    private int pc;
    private boolean waitingOnFinished = false;
    private HashMap<Integer, Boolean> waitingOnFinishedMap = new HashMap<>();
    private List<Integer> currentOutputId = new LinkedList<>();
    private boolean eol; //end of machineCommandParamTripleslist
    private boolean parallel;

    JSONOutput.JSONOutputBuilder builder;
    String path1 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineCommands.json";
    String path2 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineStatusRequests.json";

    public StepChainExecutor(List<MachineCommandParamTriple> orderedCommandParamTripleList) {
        this.orderedCommandParamTripleList = orderedCommandParamTripleList;
        this.pc = 0;
        this.eol = false;
        this.parallel = false;
        try {
            this.builder = new JSONOutput.JSONOutputBuilder(path1, path2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO find option to run machines in parallel, eg two conveyors (probably add attribute to MachineCommandParamTriple or find info by machine type comparison)

    public Optional<JSONOutput> execute(/*Processable processable, */Optional<CommandFeedback> feedback) {
        if(waitingOnFinishedMap.containsValue(true)) {
        //if(waitingOnFinished){ //any in map true
            if(feedback.isPresent()){
                if(feedback.get().getStatus() == ExecutionStatus.FINISHED && currentOutputId.contains(feedback.get().getCommandId())){
                    currentOutputId.remove((Integer) feedback.get().getCommandId());
                    //use map to set specific one to false
                    waitingOnFinishedMap.remove(feedback.get().getCommandId());
                    //waitingOnFinished = false;
                }
            }
        }
        if(!waitingOnFinishedMap.containsValue(true) || parallel){
            parallel = false;
            if(this.pc < this.orderedCommandParamTripleList.size()){
                this.eol = false;
                MachineCommandParamTriple machineCommandParamTriple = this.orderedCommandParamTripleList.get(this.pc);
                this.pc++;
                //check in loop, whether the next machine executes parallel
                if(machineCommandParamTriple.getMachine().available()) {
                    if(machineCommandParamTriple.getExecuteParallel()) {
                        parallel = true;
                    }
                    //waitingOnFinished = true;
                    try {
                        JSONOutput p =  this.builder.setJsonType(JSONOutputType.COMMAND).
                                setTopicName(machineCommandParamTriple.getMachine().getTopicName()).
                                setType(machineCommandParamTriple.getMachine().getCommandType()).
                                setName(machineCommandParamTriple.getName()).
                                setParams(machineCommandParamTriple.getParameterList()).
                                build();
                        //machineCommandParamTriple.getMachine().setObjectOnMachine(processable);
                        currentOutputId.add(p.getMessage().getOutputId());
                        waitingOnFinishedMap.put(p.getMessage().getOutputId(), true);
                        return Optional.of(p);
                    } catch (JsonApiExcpetion e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (!parallel){
                this.eol = true;
            }
        }
        return Optional.empty();
    }

    /**
     * is at end of machineCommandParamTriplesList - no further commands to execute
     * @return
     */
    public boolean isEOL() {
        return this.eol;
    }

    /**
     * TODO glaub net gescheit implementiert
     * @return
     */
    public Machine currentlyOnMachine(){
        if(!this.eol){
            return this.orderedCommandParamTripleList.get(this.pc - 1).getMachine();
        } else {
            return null;
        }
    }
}
