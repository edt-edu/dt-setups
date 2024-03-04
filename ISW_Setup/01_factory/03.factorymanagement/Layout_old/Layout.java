package Layout_old;

import java.util.LinkedList;
import java.util.List;


public abstract class Layout {

    private List<Machine> machines;
    private List<Machine> withoutPredecessor = new LinkedList<>();
    private List<Machine> withoutSuccessor = new LinkedList<>();
    private List<Machine> others = new LinkedList<>();
    private List<List<StationObject>> allCycles = new LinkedList<>();

    /**
     * The constructor for a layout object
     * A layout consists of Layout.Machine objects that are interconnected with each other
     * through their attributes specifying their successors and predecessors.
     * This leads to the construction of a directed graph of some kind.
     *
     * @param maschinen a list of all the machines that make up that layout - to call the constructor,
     *                  they do not need to be interconnected yet but can be;
     *                  for all other functions except those setting these attributes,
     *                  they have to be interconnected
     */
    public Layout(List<Machine> maschinen){
        for(Machine m: maschinen){
            if(m.getSuccessor().size() != 0 || m.getPredecessor().size() != 0){
                m.getPredecessor().clear();
                m.getSuccessor().clear();
                //oder Exception werfen und Abbruch
            }
        }
        this.machines = maschinen;
    }

    /**
     * Connects two machines together in the specified order,
     * using the machines attributes to hold the predecessor and successor
     *
     * @param fromMachine the machine where the connection starts from
     * @param toMachine the machine where the connection ends
     * @throws IllegalArgumentException thrown if one or both of the machines are not part of the layout
     */
    public void connectM(Machine fromMachine, Machine toMachine) throws IllegalArgumentException {
        //prüfe, ob Maschinen ein Teil des Layouts sind bzw. sich selbst referenzieren
        if (!(this.machines.contains(fromMachine) && this.machines.contains(toMachine))){
            throw new IllegalArgumentException("mindestens eine der Layout.Machine nicht Teil des Layouts");
        } else if (fromMachine == toMachine) {
            throw new IllegalArgumentException("Maschine kann nicht mit sich selbst verbunden werden");
        }
        //setze Attribute -> Verbindung herstellen
        fromMachine.setSuccessor(toMachine);
        toMachine.setPredecessor(fromMachine);
    }

    public void connectI(Input i, Machine m1) throws IllegalArgumentException {
        //prüfe ob Maschine Teil des Layouts
        if (!(this.machines.contains(m1))) {
            throw new IllegalArgumentException("Layout.Machine nicht Teil des Layouts");
        }
        //setze Attribute -> Verbindung herstellen
        m1.setPredecessor(i);
        i.setSuccessor(m1);
    }

    public void connectO(Machine m1, Output o) throws IllegalArgumentException {
        //prüfe ob Maschine Teil des Layouts
        if (!(this.machines.contains(m1))) {
            throw new IllegalArgumentException("Layout.Machine nicht Teil des Layouts");
        }
        //setze Attribute -> Verbindung herstellen
        m1.setSuccessor(o);
        o.setPredecessor(m1);
    }

    /**
     * Returns a List containing all machine objects that are part of this layout
     *
     * @return a list with all machines part of this layout
     */
    //TODO make return deepcopy
    public List<Machine> getMachines() {
        return machines;
    }
    //TODO make return deepcopy
    public List<Machine> getWithoutPredecessor() {
        return withoutPredecessor;
    }
    //TODO make return deepcopy
    public List<Machine> getWithoutSuccessor() {
        return withoutSuccessor;
    }
    //TODO make return deepcopy
    public List<Machine> getOthers() {
        return others;
    }

    /**
     * Generates three subsets of all machines based on their connections
     *
     * the list withoutPredecessor is filled with all Machines that have an Layout.Input-object as their predecessor
     * the list withoutSuccessor is filled with all Machines that have an Layout.Output-object as their successor
     * this list others is filled with all Machines that aren't part of the above lists plus those Machines that
     *  additionally have  Machines as thier predecessors/successors
     */
    public void findStartEnd(){ //TODO private
        withoutSuccessor.clear();
        withoutPredecessor.clear();
        others.clear();
        for(Machine m1: this.machines){ //betrachte jede Maschine
            if(m1.getPredecessor().size() == 0 && m1.getSuccessor().size() == 0){
                throw new RuntimeException("at least one machine isn't interconnected at all");
            }
            if(m1.getPredecessor().stream().anyMatch(m -> m instanceof Input)){
                withoutPredecessor.add(m1);
            }
            if(m1.getSuccessor().stream().anyMatch(m -> m instanceof Output)) {
                withoutSuccessor.add(m1);
            }
            //hat Vorgänger und Nachfolger, die nicht exklusiv Inputs bzw Outputs sind
            if(!((m1.getPredecessor().stream().anyMatch(m -> m instanceof Input) && m1.getPredecessor().size() <= 1) ||
                    (m1.getSuccessor().stream().anyMatch(m -> m instanceof Output) && m1.getSuccessor().size() <= 1))){
                others.add(m1);
            }
        }
    }

    public void movePackets(List<StationObject> currentPacketPos, List<StationObject> nextPacketPos){
        if(currentPacketPos.size() != nextPacketPos.size()){
            throw new IllegalArgumentException("list sizes are not allowed to be different");
        }
        for(int i = 0; i < currentPacketPos.size(); i++){
            currentPacketPos.get(i).passPacket(nextPacketPos.get(i));
        }
    }

    public void generateCommands(){
        //TODO API-abhängig
        ;//nop
    }

    public abstract List<StationObject> findExecutionStep();

    public List<StationObject> whereIsPacket(){
        List<StationObject> stationsWithPacket = new LinkedList<>();
        for(Machine m1: this.machines){
            for(StationObject m2: m1.getPredecessor()){
                if(m2.getClass() == Input.class){
                    if(m2.hasPacket() != null){
                        stationsWithPacket.add(m2);
                    }
                }
                //TODO same for output
                if(m1.hasPacket() != null){
                    stationsWithPacket.add(m1);
                }
            }
        }
        return stationsWithPacket;
    }

    public abstract boolean isValidLayout();

    /**
     * Checks for cycles in the graph using depth first search algorithm
     *
     * @return true if a cycle is found
     */
    /*
    public boolean containsCycle(){
        List<Layout.StationObject> visited = new LinkedList<>();
        List<Layout.StationObject> stack = new LinkedList<>();
        for(Layout.StationObject m: this.machines){
            if(isCyclic(m, visited, stack)){
                return true;
            }
        }
        return false;
    }

     */

    /**
     * Helper function for containsCycle()
     *
     * @param m
     * @param visited
     * @param stack
     * @return
     */
    /*
    private boolean isCyclic(Layout.StationObject m, List<Layout.StationObject> visited, List<Layout.StationObject> stack){
        if(stack.contains(m)){
            return true;
        }
        if(visited.contains(m)){
            return false;
        }
        visited.add(m);
        stack.add(m);
        List<Layout.StationObject> successors = m.getSuccessor();
        for(Layout.StationObject m1: successors){
            if (isCyclic(m1, visited, stack)) {
                return true;
            }
        }
        stack.remove(m);
        return false;
    }
    */
}