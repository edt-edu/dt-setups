package backend_sysml_service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for collecting all relevant SysML objects from a SysML file.
 * Provides a method to extract and aggregate StorageUnits, Containers, ArmOperations,
 * States, Transitions, and Parents into a single array structure for further processing.
 */
public class CollectorUtil {

    /**
     * Extracts all relevant SysML objects from the given SysML file and returns them as an array of arrays.
     * The order of returned arrays is:
     *   StorageUnit[],
     *   Container[],
     *   ArmOperation[],
     *   State[],
     *   Transition[],
     *   Parent[]
     * @param sysmlFile The SysML file to be parsed.
     * @return An Object[][] where each sub-array contains objects of a specific SysML type.
     * @throws IOException If the file cannot be read or parsed.
     */
    public static Object[][] collectAllSysMLObjects(File sysmlFile) throws IOException {
        SysMLDBParserUtil parser = new SysMLDBParserUtil(sysmlFile);

        List<StorageUnit> storageUnits = parser.extractStorageUnits();
        List<State> states = parser.extractStates();
        List<Transition> transitions = parser.extractTransitions();
        List<Container> containers = parser.extractContainers();
        List<ArmOperation> armOperations = parser.extractArmOperations();

        // Extract unique parents from states and transitions
        List<Parent> parents = new java.util.ArrayList<>();
        for (State s : states) {
            if (s.getParent() != null && !parents.contains(s.getParent())) {
                parents.add(s.getParent());
            }
        }
        for (Transition t : transitions) {
            if (t.getParent() != null && !parents.contains(t.getParent())) {
                parents.add(t.getParent());
            }
        }

        return new Object[][] {
            storageUnits.toArray(new StorageUnit[0]),
            containers.toArray(new Container[0]),
            armOperations.toArray(new ArmOperation[0]),
            states.toArray(new State[0]),
            transitions.toArray(new Transition[0]),
            parents.toArray(new Parent[0])
        };
    }
}
