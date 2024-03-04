package Layout_old;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class LayoutDirectionalITest {

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
    LayoutDirectionalI l;

    @Before
    public void setup(){
        this.maschinen.add(m1);
        this.maschinen.add(m2);
        this.maschinen.add(m3);
        this.maschinen.add(m4);
        m4.setSuccessor(m2);
        m2.setPredecessor(m4); //diese beiden Verbindungen sollen im Konstruktor gelöscht werden
        l = new LayoutDirectionalI(this.maschinen, i, o);
    }


    @Test
    public void testLayoutIIsValid(){
        assertTrue(l.isValidLayout());
        l.connectM(m3,m2);
        assertFalse(l.isValidLayout());
    }



    @Test
    public void testFindExecutionStep(){
        l.findStartEnd();
        i.acceptPacket(p1);
        m2.acceptPacket(p2);
        List<StationObject> temp = l.findExecutionStep();
        assertEquals(2, temp.size());
        assertTrue(temp.contains(m1));
        assertTrue(temp.contains(m3));
    }

    @Test
    public void testMovePacket(){
        l.findStartEnd();
        i.acceptPacket(p1);
        m2.acceptPacket(p2);
        List<StationObject> temp = l.whereIsPacket();
        Assert.assertEquals(p1.getName(), temp.get(0).hasPacket().getName());
        Assert.assertEquals(p2.getName(), temp.get(1).hasPacket().getName());
        List<StationObject> temp2 = l.findExecutionStep(); //move packet integrated in findExecutionStep
        assertNull(temp.get(0).hasPacket());
        assertNull(temp.get(1).hasPacket());
        Assert.assertEquals(p1.getName(), m1.hasPacket().getName());
        Assert.assertEquals(p2.getName(), m3.hasPacket().getName());
        temp = l.whereIsPacket();
        temp2 = l.findExecutionStep(); //move packet integrated in findExecutionStep
        assertNull(temp.get(0).hasPacket());
        assertNull(temp.get(1).hasPacket());
        Assert.assertEquals(p1.getName(), m2.hasPacket().getName());
        Assert.assertEquals(p2.getName(), m4.hasPacket().getName());
    }

}