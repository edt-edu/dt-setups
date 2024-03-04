package Layout_new.StationExecutor;

import JSON.FeedbackANDStatusANDOrders.CommandFeedback;
import JSON.Parsing.JSONInput;
import JSON.Parsing.JSONOutput;
import JSON.Parsing.JSONParser;
import JSON.Parsing.JSONReadable;
import JSON.Server.Communication;
import Layout_new.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * class that is used for executor logics of each station
 */
public abstract class StationExecutorLogic implements Runnable{
    private int objectsOnLine;
    private final InputOutputStation IOStation;
    private final int maxObjectsOnLine = 1;
    private final Communication communication;
    private final JSONParser parser;
    private StepChainExecutor stepChainExecutor;
    private boolean stepChainExecutorFinished;

    Processable inputObject;
    JSONOutput.JSONOutputBuilder builder;
    String path1 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineCommands.json";
    String path2 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineStatusRequests.json";

    //TODO JSONOutputBuilder für echt individuelle output ids außerhalb dieser Klasse auf oberster ebene init und als param übergeben
    /**
     * constructor, sets up communication and JSONOutputBuilder
     * @param IOStation
     * @param revPiNumber
     */
    public StationExecutorLogic(InputOutputStation IOStation, RevPiNumber revPiNumber) {
        stepChainExecutorFinished = true;
        this.objectsOnLine = 0;
        this.IOStation = IOStation;
        this.inputObject = null;
        try {
            this.builder = new JSONOutput.JSONOutputBuilder(path1, path2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.communication = new Communication(revPiNumber);
        this.parser = new JSONParser();
    }

    public InputOutputStation getIOStation(){
        return this.IOStation;
    }

    /**
     * Executes the whole process on this station
     *
     * checks for feedback by acessing the communication object, thereby decides upon the next steps and
     * sets up the commands for sending to the revpis via the communication object
     *
     * @param inputObject
     * @param processList
     * @throws ExecutorException
     */
    public void process(Processable inputObject, List<MachineCommandParamTriple> processList) throws ExecutorException {
        //TODO mit dieser Logik nur ein Objekt pro Linie möglich; für mehrere Objekte stepchainexecutor in Liste halten und parallel aufrufen
        Optional<CommandFeedback> feedback = Optional.empty();
        if(this.communication.receivedAvailable()){
            try {
                JSONInput input = this.parser.readJSONInput(this.communication.removeFromReceiveList());
                JSONReadable readable = input.getMessage();
                if(readable instanceof CommandFeedback){
                    feedback = Optional.of((CommandFeedback) readable);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        this.inputObject = inputObject;
        Optional<JSONOutput> json = Optional.empty();
        //TODO exception für abbruch werfen, wenn process list geändert
        if(this.stepChainExecutorFinished){
            if(this.inputObject != null) {
                if(processList.isEmpty()){
                    throw new ExecutorException("leere config liste");
                }
                stepChainExecutor = new StepChainExecutor(processList);
            }
        }
        if(stepChainExecutor != null) {
            json = stepChainExecutor.execute(feedback);
            this.stepChainExecutorFinished = stepChainExecutor.isEOL();
        }
        if(json.isPresent()) {
            try {
                System.out.println("tried sending command");
                System.out.println(json.get());
                this.communication.addToSendList(this.parser.parse(json.get()));
                //TODO delete last 3 lines
                //List<RequestedParameter> rqs = new LinkedList<>();
                //rqs.add(RequestedParameter.ALL);
                //this.communication.addToSendList(this.parser.parse(builder.setJsonType(JSONOutputType.STATUSREQUEST).setType(CommandType.MULTIPROCESSING).setTopicName("2.6-Freeze").setRequest(rqs).build()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            //} catch (JsonApiExcpetion e) {
              //  throw new RuntimeException(e);
            }
        }
    }

    /**
     * checks, whether an other item is allowed on line
     * @return
     */
    public boolean acceptsInput() {
        return this.objectsOnLine < this.maxObjectsOnLine;
    }

    /**
     * Returns an InputObject when the line is able to accept one and the input station has one, else null
     * @return
     * @throws IllegalActionException
     */
    protected Processable getInputObject() throws IllegalActionException {
        //solange weitere items auf die Linie dürfen, input, falls vorhanden, akzeptieren
        if(this.objectsOnLine < this.maxObjectsOnLine){ //if acceptsInput
            if(IOStation.hasInputObjectAvailable()){
                return IOStation.getInputObject();
            }
        }
        return null;
    }

    /**
     * returns true when the full execution cycle on this station has been finished
     * @return
     */
    public boolean getStepChainExecutorFinished() {
        return stepChainExecutorFinished;
    }

    /**
     * run method of every executor, can be called after correct construction as all preconditions are then fulfilled
     * -- see demo for example usage
     */
    public abstract void run();
}

