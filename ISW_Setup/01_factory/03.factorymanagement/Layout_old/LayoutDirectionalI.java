package Layout_old;

import java.util.LinkedList;
import java.util.List;

public class LayoutDirectionalI extends Layout{

    private boolean validLayout = false;

    /**
     * The constructor for a layout object
     * A layout consists of Layout.Machine objects that are interconnected with each other
     * through their attributes specifying their successors and predecessors.
     * This leads to the construction of a directed graph of some kind.
     *
     * @param orderedMaschinen a list of all the machines that make up that layout - to call the constructor,
     *                  they do should not need to be interconnected yet (if they are, connections are being removed)
     */
    public LayoutDirectionalI(List<Machine> orderedMaschinen, Input input, Output output) {
        super(orderedMaschinen);
        if(orderedMaschinen.size() < 2){
            throw new IllegalArgumentException("one machine can't form an I-Layout");
        }
        //TODO use contructor to make layout not changeable at runtime
        //TODO check valid layout here and remove at findExecutionStep
        for(int i = 0; i < orderedMaschinen.size(); i++){
            if(i == 0){
                orderedMaschinen.get(i).setSuccessor(orderedMaschinen.get(i+1));
                orderedMaschinen.get(i).setPredecessor(input);
                input.setSuccessor(orderedMaschinen.get(i));
            } else if(i == orderedMaschinen.size()-1){
                orderedMaschinen.get(i).setSuccessor(output);
                orderedMaschinen.get(i).setPredecessor(orderedMaschinen.get(i-1));
                output.setPredecessor(orderedMaschinen.get(i));
            } else {
                orderedMaschinen.get(i).setSuccessor(orderedMaschinen.get(i+1));
                orderedMaschinen.get(i).setPredecessor(orderedMaschinen.get(i-1));

            }
        }
        if(!isValidLayout()){
            throw new RuntimeException("something went wrong, dunno");
        }
        validLayout = true;
    }

    //TODO make sure packet is at exactly one station!!! - is glaub fertig
    public List<StationObject> findExecutionStep() {
        //ifs so gesplittet, damit bei allererster ausführung direkt ergebnis vorliegend, falls layout korrekt
        if(validLayout) {
            //Liste aller Stationen mit Paket im aktuellen Anlagenzustand
            List<StationObject> stationsWithPacket = whereIsPacket();
            List<StationObject> stationsToReceivePacket = new LinkedList<>();
            //TODO siehe TODO in Maschine für isExecuting
            for(StationObject m1: stationsWithPacket){
                if(!m1.isExecuting()){
                    stationsToReceivePacket.add(m1.getSuccessor().get(0)); //alle Maschinen im Folgeschritt
                }
                //TODO Ausnahme für Layout.Output
                //(TODO make sure machine is free) Sonderfall wenn Maschine fertig, aber Teil noch nicht abgeholt (zb förderband mit Pick von Robot)
                //TODO möglicherweise abstand
            }
            movePackets(stationsWithPacket, stationsToReceivePacket);
            return stationsToReceivePacket;
        }
        return new LinkedList<>();
    }

    //make sure it actually is I-shaped
    //(jede maschine hat genau einen Vorgänger und Nachfolger)
    //fails, if machines are not connected or if they form a cycle
    @Override
    public boolean isValidLayout() {
        List<Machine> usedMachines = new LinkedList<>();
        for(Machine m: this.getMachines()){
            usedMachines.add(m);
            if(m.getPredecessor().size() != 1 || m.getSuccessor().size() != 1){
                return false;
            }
            //makes sure the same machine doesn't appear twice as successor (loops)
            if(usedMachines.contains(m.getSuccessor())){
                return false;
            }
        }
        return true;
    }
}
