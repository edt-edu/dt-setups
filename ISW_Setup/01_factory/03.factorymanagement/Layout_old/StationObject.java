package Layout_old;

import java.util.List;

public interface StationObject extends Comparable {
    List<StationObject> getSuccessor();
    List<StationObject> getPredecessor();
    Yoghurt hasPacket();
    boolean acceptPacket(Yoghurt p);
    boolean passPacket(StationObject sink);
    boolean isExecuting();
    int getNum();

    int compareTo(StationObject o);
}
