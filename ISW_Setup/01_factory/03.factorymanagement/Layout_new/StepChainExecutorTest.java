package Layout_new;

import JSON.EnumsAndParameters.*;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.FeedbackANDStatusANDOrders.CommandFeedback;
import JSON.Parsing.JSONOutput;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class StepChainExecutorTest {

    private StepChainExecutor stepChainExecutor1;
    private List<MachineCommandParamTriple> machineCommandParamTripleList1;

    private MachineCommandParamTriple t1;
    private MachineCommandParamTriple t2;
    private MachineCommandParamTriple t3;
    private MachineCommandParamTriple t4;
    private MachineCommandParamTriple t5;
    private Machine m1;
    private Machine m2;
    private Machine m3;
    private Machine m4;
    private Machine m5;

    private Processable processable1;
    private CommandFeedback feedback1;
    private CommandFeedback feedback2;


    @Before
    public void setup(){
        machineCommandParamTripleList1 = new LinkedList<>();



    }

    @Test
    public void testTwoSimpleMachines(){
        machineCommandParamTripleList1.clear();

        m1 = new Machine("Robot1Topping", CommandType.GRIPPER, 1);
        Parameter p1 = new Parameter(new PositionParameterThreeD(PositionMeaning.START, 0, 1, 3));
        Parameter p2 = new Parameter(new PositionParameterThreeD(PositionMeaning.END, 0, 1, 3));
        List<Parameter> ps1 = new LinkedList<>();
        ps1.add(p1);
        ps1.add(p2);
        t1 = new MachineCommandParamTriple(m1, CommandNames.MOVE, ps1, false);
        machineCommandParamTripleList1.add(t1);


        m2 = new Machine("Conveyor1Topping", CommandType.CONVEYOR, 1);
        Parameter p3 = new Parameter(Direction.FORWARD);
        List<Parameter> ps2 = new LinkedList<>();
        ps2.add(p3);
        t2 = new MachineCommandParamTriple(m2, CommandNames.MOVE, ps2, false);
        machineCommandParamTripleList1.add(t2);


        processable1 = new RawMaterial();
        try {
            feedback1 = new CommandFeedback(JSONOutputType.FEEDBACK, 1, ExecutionStatus.FINISHED, "");
            feedback2 = new CommandFeedback(JSONOutputType.FEEDBACK, 2, ExecutionStatus.FINISHED, "");
        } catch (JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }

        stepChainExecutor1 = new StepChainExecutor(machineCommandParamTripleList1);
        Optional<JSONOutput> j = Optional.empty();

        j = stepChainExecutor1.execute(Optional.empty());
        //j = stepChainExecutor1.execute(processable1, Optional.empty());
        assertTrue(j.isPresent());
        assertEquals(j.get().getMessage().getOutputId(), 1);
        assertEquals(j.get().getMessage().getType(), CommandType.GRIPPER);
        //TODO
        //assertEquals(m1.getObjectsOnMachine().size(), 1); //not working because not currently used in execute logic
        assertEquals(m1, stepChainExecutor1.currentlyOnMachine());

        j = stepChainExecutor1.execute(Optional.empty());
        //j = stepChainExecutor1.execute(processable1, Optional.empty());
        assertFalse(j.isPresent());

        j = stepChainExecutor1.execute(Optional.of(feedback1));
        //j = stepChainExecutor1.execute(processable1, Optional.of(feedback1));
        assertTrue(j.isPresent());
        assertEquals(j.get().getMessage().getOutputId(), 2);
        assertEquals(j.get().getMessage().getType(), CommandType.CONVEYOR);

        j = stepChainExecutor1.execute(Optional.empty());
        //j = stepChainExecutor1.execute(processable1, Optional.empty());
        assertFalse(j.isPresent());
        assertFalse(stepChainExecutor1.isEOL());

        //"falsches" Feedback
        j = stepChainExecutor1.execute(Optional.of(feedback1));
        //j = stepChainExecutor1.execute(processable1, Optional.of(feedback1));
        assertFalse(j.isPresent());
        assertFalse(stepChainExecutor1.isEOL());
        assertEquals(m2, stepChainExecutor1.currentlyOnMachine());

        j = stepChainExecutor1.execute(Optional.of(feedback2));
        //j = stepChainExecutor1.execute(processable1, Optional.of(feedback2));
        assertFalse(j.isPresent());
        assertTrue(stepChainExecutor1.isEOL());
        assertNull(stepChainExecutor1.currentlyOnMachine());
    }

    @Test
    public void testParallelExecution() {
        m1 = new Machine("Robot1Topping", CommandType.GRIPPER, 1);
        Parameter p1 = new Parameter(new PositionParameterThreeD(PositionMeaning.START, 0, 1, 3));
        Parameter p2 = new Parameter(new PositionParameterThreeD(PositionMeaning.END, 0, 1, 3));
        List<Parameter> ps1 = new LinkedList<>();
        ps1.add(p1);
        ps1.add(p2);
        t1 = new MachineCommandParamTriple(m1, CommandNames.MOVE, ps1, false);
        machineCommandParamTripleList1.add(t1);


        m2 = new Machine("Conveyor1Topping", CommandType.CONVEYOR, 1);
        Parameter p3 = new Parameter(Direction.FORWARD);
        List<Parameter> ps2 = new LinkedList<>();
        ps2.add(p3);
        t2 = new MachineCommandParamTriple(m2, CommandNames.MOVE, ps2, true);
        machineCommandParamTripleList1.add(t2);

        m3 = new Machine("Conveyor2Topping", CommandType.CONVEYOR, 1);
        Parameter p4 = new Parameter(Direction.FORWARD);
        List<Parameter> ps3 = new LinkedList<>();
        ps3.add(p4);
        t3 = new MachineCommandParamTriple(m3, CommandNames.MOVE, ps3, false);
        machineCommandParamTripleList1.add(t3);

        processable1 = new RawMaterial();
        try {
            feedback1 = new CommandFeedback(JSONOutputType.FEEDBACK, 1, ExecutionStatus.FINISHED, "");
            feedback2 = new CommandFeedback(JSONOutputType.FEEDBACK, 2, ExecutionStatus.FINISHED, "");
        } catch (JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }


        stepChainExecutor1 = new StepChainExecutor(machineCommandParamTripleList1);
        Optional<JSONOutput> j = Optional.empty();

        j = stepChainExecutor1.execute(Optional.empty());
        //j = stepChainExecutor1.execute(processable1, Optional.empty());
        assertTrue(j.isPresent());
        assertEquals(j.get().getMessage().getOutputId(), 1);
        assertEquals(j.get().getMessage().getType(), CommandType.GRIPPER);
        //assertEquals(m1.getObjectsOnMachine().size(), 1);
        assertEquals(m1, stepChainExecutor1.currentlyOnMachine());

        j = stepChainExecutor1.execute(Optional.empty());
        // j = stepChainExecutor1.execute(processable1, Optional.empty());
        assertFalse(j.isPresent());

        j = stepChainExecutor1.execute(Optional.of(feedback1));
        //j = stepChainExecutor1.execute(processable1, Optional.of(feedback1));
        assertTrue(j.isPresent());
        assertEquals(j.get().getMessage().getOutputId(), 2);
        assertEquals(j.get().getMessage().getType(), CommandType.CONVEYOR);

        j = stepChainExecutor1.execute(Optional.empty());
        //j = stepChainExecutor1.execute(processable1, Optional.empty());
        assertTrue(j.isPresent());
        assertEquals(j.get().getMessage().getOutputId(), 3);
        assertEquals(j.get().getMessage().getType(), CommandType.CONVEYOR);
        assertFalse(stepChainExecutor1.isEOL());

        //"falsches" Feedback
        j = stepChainExecutor1.execute(Optional.of(feedback1));
        //j = stepChainExecutor1.execute(processable1, Optional.of(feedback1));
        assertFalse(j.isPresent());
        assertFalse(stepChainExecutor1.isEOL());
        assertEquals(m3, stepChainExecutor1.currentlyOnMachine());

        j = stepChainExecutor1.execute(Optional.of(feedback2));
        //j = stepChainExecutor1.execute(processable1, Optional.of(feedback2));
        assertFalse(j.isPresent());
        //assertTrue(stepChainExecutor1.isEOL());
        //assertNull(stepChainExecutor1.currentlyOnMachine());
    }
}
