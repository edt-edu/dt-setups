package Layout_new;

import JSON.EnumsAndParameters.CommandType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Machine class holds information of machines, that can be accessed from the stepChainExecutor
 * TODO unused methods: manche können einen Mehrwert bieten(zb object on machine für Warehouse-Platzmanagement, ...)
 */
public class Machine {

    private final String topicName;
    private final CommandType commandType;
    private List<Processable> objectsOnMachine;
    private final int maxObjectsOnMachine;

    public Machine(String topicName, CommandType commandType, int maxObjectsOnMachine) {
        this.topicName = topicName;
        this.commandType = commandType;
        this.objectsOnMachine = new LinkedList<>();
        this.maxObjectsOnMachine = maxObjectsOnMachine;
    }

    public String getTopicName() {
        return topicName;
    }

    public CommandType getCommandType(){
        return this.commandType;
    }

    public List<Processable> getObjectsOnMachine() {
        return objectsOnMachine;
    }

    public boolean available(){
        return this.objectsOnMachine.size() < this.maxObjectsOnMachine;
    }

    public boolean setObjectOnMachine(Processable processable){
        if(this.objectsOnMachine.size() < this.maxObjectsOnMachine){
            this.objectsOnMachine.add(processable);
            return true;
        } else {
            return false;
        }
    }

    public Processable removeSpecificObjectFromMachine(Processable processable) {
        boolean t = this.objectsOnMachine.remove(processable);
        return processable;

    }

    public Processable removeAnyObjectFromMachine(){
        return this.objectsOnMachine.remove(0);
    }

    public int getMaxObjectsOnMachine() {
        return maxObjectsOnMachine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machine)) return false;
        Machine machine = (Machine) o;
        return maxObjectsOnMachine == machine.maxObjectsOnMachine && topicName.equals(machine.topicName) && commandType == machine.commandType && Objects.equals(objectsOnMachine, machine.objectsOnMachine);
    }
}
