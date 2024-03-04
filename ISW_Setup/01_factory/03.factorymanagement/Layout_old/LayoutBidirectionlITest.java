package Layout_old;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class LayoutBidirectionlITest {

    private Input i = new Input("start", 0);
    private Output o = new Output("end", 9);
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
    LayoutBidirectionalI l;

    @Before
    public void setup() {
        this.maschinen.add(m1);
        this.maschinen.add(m3);
        this.maschinen.add(m2);
        this.maschinen.add(m4);
        l = new LayoutBidirectionalI(this.maschinen, this.i, this.o);
    }

    @Test
    public void testLayoutIIsValid() {
        assertTrue(l.isValidLayout());
        assertEquals(m4, l.getRetPoint());
        l.connectM(m4, m2);
        assertFalse(l.isValidLayout());
    }

}