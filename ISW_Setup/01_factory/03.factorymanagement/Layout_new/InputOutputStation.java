package Layout_new;

import java.util.LinkedList;
import java.util.List;

/**
 * a class for the IOStation, holds the objects from the outside and provides them for the executors
 */
public class InputOutputStation {

    private final int spaces;
    private int availableSpaces;
    private int inputObjectsNumber;
    private int outputObjectsNumber;
    private List<Processable> storedObjects;

    public InputOutputStation(int spaces){
        this.inputObjectsNumber = 0;
        this.outputObjectsNumber = 0;
        this.spaces = spaces;
        this.availableSpaces = spaces;
        this.storedObjects = new LinkedList<>();
    }

    //TODO Unterscheidung möglicher Input Objecte zwischen Yoghurt und RawMaterial -> Unterschiedliche Behandlung
    //vmtl inzwischen bereits ausreichend in der Station Executor logik berücksichtigt

    public boolean hasSpaceAvailable(){
        return this.availableSpaces > 0;
    }

    public boolean hasInputObjectAvailable(){
        return this.inputObjectsNumber > 0;
    }

    public boolean hasOutputObjectAvailable(){
        return this.outputObjectsNumber > 0;
    }

    public void putInputObject(Processable processable) throws IllegalActionException {
        if(!hasSpaceAvailable()){
            throw new IllegalActionException();
        }
        this.storedObjects.add(processable);
        this.availableSpaces -= 1;
        this.inputObjectsNumber += 1;
    }

    public Processable getInputObject() throws IllegalActionException {
        if(!hasInputObjectAvailable()){
            throw new IllegalActionException();
        }
        //intentionally do not make space available so that the finished product can always be placed there
        this.inputObjectsNumber -= 1;
        return this.storedObjects.get(0);
    }

    public void putOutputObject(Processable processable) throws IllegalActionException {
        if(!hasSpaceAvailable()){
            throw new IllegalActionException();
        }
        this.storedObjects.add(processable);
        //as space is reserved, this does not affect the number of available spaces
        this.outputObjectsNumber += 1;
    }
}
