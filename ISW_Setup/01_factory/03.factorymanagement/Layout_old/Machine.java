package Layout_old;

import java.util.LinkedList;
import java.util.List;

public class Machine implements StationObject {

    //TODO möglichst viel final machen und Builder für default werte nutzen
    private String id = "";
    private int num = 0;
    //TODO make possibility for more than one successor/predecessor
    private final List<StationObject> successor;
    private final List<StationObject> predecessor;
    private int capacity = 0;
    private Machine cooperating = null;
    private String useFor;
    //TODO List für capa größer 1
    private Yoghurt p;
    private boolean isExecuting = false;

    public Machine(int id, int capacity){
        this.id = "hallo, test";
        this.num = id;
        this.successor = new LinkedList<>();
        this.predecessor = new LinkedList<>();
        this.capacity = capacity;
        this.cooperating = null;
        this.useFor = "all";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Machine getCooperating() {
        return cooperating;
    }

    public void setCooperating(Machine cooperating) {
        this.cooperating = cooperating;
    }

    public String getUseFor() {
        return useFor;
    }

    public void setUseFor(String useFor) {
        this.useFor = useFor;
    }

    public List<StationObject> getSuccessor() {
        return successor;
    }

    public void setSuccessor(StationObject successor) {
        this.successor.add(successor);
    }

    public List<StationObject> getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(StationObject predecessor) {
        this.predecessor.add(predecessor);
    }

    //TODO aus maschinenfeedback auslesen/abspeichern, wie auch immer
    public boolean isExecuting() {
        return this.isExecuting;
    }

    public void setIsExecuting(boolean isExecuting) {
        this.isExecuting = isExecuting;
    }

    //TODO make return optional
    @Override
    public Yoghurt hasPacket() {
        return this.p;
    }

    //TODO check with capacity
    @Override
    public boolean acceptPacket(Yoghurt p) {
        this.p = p;
        return true;
    }

    @Override
    public boolean passPacket(StationObject sink) {
        boolean t = sink.acceptPacket(new Yoghurt(this.p.getName()));
        this.p = null;
        return t;
    }

    //TODO es gibt sinnvolleres als nur die id zu nutzen, zu tetszwecken aktuell ausreichend
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return num == machine.num;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString(){
        return Integer.toString(this.num);
    }

    @Override
    public int compareTo(Object o) {
        Machine k = (Machine) o;
        return this.num - k.getNum();
    }

    @Override
    public int compareTo(StationObject o) {
        Machine k = (Machine) o;
        return this.num - k.getNum();
    }
}