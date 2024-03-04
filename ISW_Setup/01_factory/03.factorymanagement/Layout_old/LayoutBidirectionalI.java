package Layout_old;

import java.util.LinkedList;
import java.util.List;

public class LayoutBidirectionalI extends Layout {

    private boolean validLayout = false;
    private Machine retPoint = null;

    //machine at index 0 in list is coupled with input and output
    public LayoutBidirectionalI(List<Machine> orderedMaschinen, Input input, Output output) {
        super(orderedMaschinen);
        if(orderedMaschinen.size() < 2){
            throw new IllegalArgumentException("one machine can't form an BiDirectional I-Layout");
        }
        for(int i = 0; i < orderedMaschinen.size(); i++){
            if(i == 0){
                orderedMaschinen.get(i).setSuccessor(orderedMaschinen.get(i+1));
                orderedMaschinen.get(i).setSuccessor(output);
                orderedMaschinen.get(i).setPredecessor(input);
                orderedMaschinen.get(i).setPredecessor(orderedMaschinen.get(i+1));
                input.setSuccessor(orderedMaschinen.get(i));
                output.setPredecessor(orderedMaschinen.get(i));
            } else if(i == orderedMaschinen.size()-1){ //Maschine am return point
                orderedMaschinen.get(i).setPredecessor(orderedMaschinen.get(i-1));
                orderedMaschinen.get(i).setSuccessor(orderedMaschinen.get(i-1));
            } else {
                orderedMaschinen.get(i).setSuccessor(orderedMaschinen.get(i+1));
                orderedMaschinen.get(i).setPredecessor(orderedMaschinen.get(i-1));
                orderedMaschinen.get(i).setSuccessor(orderedMaschinen.get(i-1));
                orderedMaschinen.get(i).setPredecessor(orderedMaschinen.get(i+1));

            }
        }
    }

    //maybe restructure into I layout without the option to have several packets on line

    //TODO implement me
    //same as two coupled I layouts, make sure that only one workpiece at a time on the line
    @Override
    public List<StationObject> findExecutionStep() {
        return null;
    }

    //makes sure layout is actually bidirectional I
    @Override
    public boolean isValidLayout() {
        List<Machine> hin = new LinkedList<>();
        int count = 0;
        for(Machine m: this.getMachines()){
            if(m.getPredecessor().size() != 2 || m.getSuccessor().size() != 2) {
                if (!(m.getPredecessor().size() != 1 || m.getSuccessor().size() != 1) && count == 0) {
                    count++;
                    retPoint = m;
                } else {
                    return false;
                }
            }
            //es gilt für alle maschinen: nachfolger und Vorgänger identisch außer für Input/Output-Objekte
            if(!m.getSuccessor().containsAll(m.getPredecessor())){ //ignore poor performance warning as there are only 2 items in the lists
                if(m.getPredecessor().stream().noneMatch((n -> n instanceof Input)) || m.getSuccessor().stream().noneMatch((n -> n instanceof Output))){
                    return false;
                }
            }
        }
        return true;
    }

    public Machine getRetPoint() {
        return retPoint;
    }
}
