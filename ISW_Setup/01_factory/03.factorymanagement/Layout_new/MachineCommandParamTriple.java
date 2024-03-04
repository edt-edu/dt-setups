package Layout_new;

import JSON.EnumsAndParameters.CommandNames;
import JSON.EnumsAndParameters.Parameter;

import java.util.List;

/**
 * part of all configs of the executors
 */
public class MachineCommandParamTriple {

    private final Machine machine;
    private final CommandNames name;
    private final List<Parameter> parameterList;
    private final boolean executeParallelToNextMachine;

    /**
     * constructor sets all final parameters as explained below
     * @param machine a Machine object with topicName, commandType and a max object capacity (last currently unused)
     * @param name the name of the command
     * @param parameterList the paramters that go with the command
     * @param executeParallelToNextMachine true if the next command in the list of machineCommandParamTriples shall be executed parallel to this one
     *                                     (note: if this is also true in the next machineCommandParamTriple(s), these will also be executed)
     */
    public MachineCommandParamTriple(Machine machine, CommandNames name, List<Parameter> parameterList, boolean executeParallelToNextMachine) {
        this.machine = machine;
        this.name = name;
        this.parameterList = parameterList;
        this.executeParallelToNextMachine = executeParallelToNextMachine;
    }

    public Machine getMachine() {
        return machine;
    }

    public CommandNames getName() {
        return name;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public boolean getExecuteParallel(){
        return executeParallelToNextMachine;
    }
}
