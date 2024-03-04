package Layout_old;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class LayoutTest{

    private Input i = new Input("start", 0);
    private Output o = new Output("end",9);
    private Machine m1 = new Machine(1, 1);
    private Machine m2 = new Machine(2, 1);
    private Machine m3 = new Machine(3, 1);
    private Machine m4 = new Machine(4, 1);
    private Machine m5 = new Machine(5, 1);
    private Machine m6 = new Machine(6, 1);
    private Machine m7 = new Machine(7, 1);
    private List<Machine> maschinen = new LinkedList<>();
    private Yoghurt p1 = new Yoghurt("tttt");
    private Yoghurt p2 = new Yoghurt("ssss");
    Layout l;

    @Before
    public void setup(){
        this.maschinen.add(m1);
        this.maschinen.add(m3);
        this.maschinen.add(m2);
        this.maschinen.add(m4);
        l = new Layout(this.maschinen) {
            @Override
            public List<StationObject> findExecutionStep() {
                return null;
            }

            @Override
            public boolean isValidLayout() {
                return false;
            }
        };
    }

    @Test
    public void testGraphCreation(){
        l.connectI(i, m1);
        l.connectM(m1,m2);
        l.connectM(m2,m3);
        assertTrue(m1.getPredecessor().contains(i));
        assertTrue(m1.getSuccessor().contains(m2));
        assertTrue(m2.getPredecessor().contains(m1));
        assertTrue(m2.getSuccessor().contains(m3));
        assertTrue(m3.getPredecessor().contains(m2));
        assertEquals(0, m3.getSuccessor().size());
        assertThrows(IllegalArgumentException.class, ()-> l.connectM(m1,m5));
    }


    @Test
    public void testStartEndFinding(){
        assertThrows(RuntimeException.class, () -> l.findStartEnd());
        l.connectI(i, m1);
        l.connectM(m1,m2);
        l.connectM(m2,m3);
        assertThrows(RuntimeException.class, () -> l.findStartEnd());
        l.connectM(m3,m4);
        l.connectO(m4, o);
        l.findStartEnd();
        assertEquals(1, l.getWithoutPredecessor().size());
        assertEquals(m1, l.getWithoutPredecessor().get(0));
        assertEquals(m4, l.getWithoutSuccessor().get(0));
        assertEquals(2,l.getOthers().size());
        assertTrue(l.getOthers().contains(m3));
        assertTrue(l.getOthers().contains(m2));
    }

    @Test
    public void testFindPacket(){
        l.connectI(i, m1);
        l.connectM(m1,m2);
        l.connectM(m2,m3);
        l.connectM(m3,m4);
        l.findStartEnd();
        i.acceptPacket(p1);
        List<StationObject> temp = l.whereIsPacket();
        assertEquals(1, temp.size());
        assertTrue(temp.contains(i));
        m2.acceptPacket(p2);
        temp = l.whereIsPacket();
        assertEquals(2, temp.size());
        assertTrue(temp.contains(i));
        assertTrue(temp.contains(m2));
    }

}