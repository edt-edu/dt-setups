package Layout_old;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LayoutIncomingTTest {
    private Input i = new Input("start", 0);
    private Output o = new Output("end",9);
    private Machine m1 = new Machine(1, 1);
    private Machine m2 = new Machine(2, 1);
    private Machine m3 = new Machine(3, 1);
    private Machine m4 = new Machine(4, 1);
    private Machine m5 = new Machine(5, 1);
    private Machine m6 = new Machine(6, 1);
    private Machine m7 = new Machine(7, 1);
    private List<Machine> maschinen1 = new LinkedList<>();
    private List<Machine> maschinen2 = new LinkedList<>();
    private Yoghurt p1 = new Yoghurt("tttt");
    private Yoghurt p2 = new Yoghurt("ssss");
    LayoutIncomingT l;

    @Before
    public void setup(){
        this.maschinen1.add(m1);
        this.maschinen1.add(m2);
        this.maschinen1.add(m3);
        this.maschinen1.add(m4);
        this.maschinen2.add(m3);
        this.maschinen2.add(m5);
        m4.setSuccessor(m2);
        m2.setPredecessor(m4); //diese beiden Verbindungen sollen im Konstruktor gelöscht werden
        l = new LayoutIncomingT(this.maschinen1, this.maschinen2, i, o);
    }

    @Test
    public void testValidLayout() {
        System.out.println("test");
        //assertEquals(0, l.getMachines().get(1).getPredecessor().size());
    }
}
