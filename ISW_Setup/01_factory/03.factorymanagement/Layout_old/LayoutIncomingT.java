package Layout_old;

import java.util.LinkedList;
import java.util.List;

public class LayoutIncomingT extends Layout{


    /**
     * T Layout, the point where the two Directional I Layouts meet is orderedIncomingMaschinen[0],
     * orderedMachines is the path that goes through
     *
     * @param orderedMaschinen
     * @param orderedIncomingMaschinen
     * @param input
     * @param output
     */
    public LayoutIncomingT(List<Machine> orderedMaschinen, List<Machine> orderedIncomingMaschinen, Input input, Output output) {
        super(new LinkedList<Machine>() {{
            addAll(orderedMaschinen);
            addAll(orderedIncomingMaschinen);
        }});
        if(orderedMaschinen.size() < 2){
            throw new IllegalArgumentException("one machine can't form a T-Layout");
        }
        if(orderedIncomingMaschinen.size() < 1){
            throw new IllegalArgumentException("one machine can't form a T-Layout");
        }
        Machine merge = orderedIncomingMaschinen.get(0);
        if(!orderedMaschinen.contains(merge)){
            throw new IllegalArgumentException("there is no merging point");
        }
        //TODO build logic

    }

    //TODO implement me
    @Override
    public List<StationObject> findExecutionStep() {
        return null;
    }

    //TODO implement me
    @Override
    public boolean isValidLayout() {
        return false;
    }
}
