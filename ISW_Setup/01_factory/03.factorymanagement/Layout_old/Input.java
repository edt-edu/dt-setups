package Layout_old;

import java.util.LinkedList;
import java.util.List;

public class Input implements StationObject {

    private final String id;
    private final int num;
    private final List<StationObject> successor;
    private Yoghurt p;
    private final boolean isExecuting = false;

    public Input(String id, int num){
        this.id = id;
        this.num = num;
        this.successor = new LinkedList<>();
    }

    //TODO add capacity
    public boolean acceptPacket(Yoghurt p){
        this.p = p;
        return true;
    }

    public void ejectPacket(){
        this.p = null;
    }

    @Override
    public List<StationObject> getSuccessor() {
        return this.successor;
    }

    @Override
    public List<StationObject> getPredecessor() {
        return null;
    }

    //TODO make optional
    public Yoghurt hasPacket(){
        if(this.p != null){
            return new Yoghurt(this.p.getName());
        }
        return null;
    }

    public void setSuccessor(Machine m1) {
        this.successor.add(m1);
    }

    @Override
    public boolean passPacket(StationObject sink) {
        boolean t = sink.acceptPacket(new Yoghurt(this.p.getName()));
        this.p = null;
        return t;
    }

    public boolean isExecuting(){
        return this.isExecuting;
    }

    @Override
    public int getNum() {
        return this.num;
    }

    @Override
    public int compareTo(StationObject o) {
        return 0;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
