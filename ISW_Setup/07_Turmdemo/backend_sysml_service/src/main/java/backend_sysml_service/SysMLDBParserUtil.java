package backend_sysml_service;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility class for extracting and analyzing SysMLv2 unit definitions,
 * assignments, and transitions related to units p1-p9 from a SysML file.
 */
public class SysMLDBParserUtil {

    /** The SysML file to be examined. */
    private final File sysmlFile;

    /**
     * Constructs a new SysMLUnitFinder for the given SysML file.
     * 
     * @param sysmlFile The SysML file to exaamine.
     */
    public SysMLDBParserUtil(File sysmlFile) {
        this.sysmlFile = sysmlFile;
    }

    /**
     * Extracts all StorageUnits from the SysML file and returns them as a list of
     * StorageUnit objects.
     * Each StorageUnit contains its id, name, and position (which is empty in this
     * case).
     *
     * @return List of StorageUnit objects where only the name ist set.
     * @throws IOException if the file cannot be read.
     */
    public List<StorageUnit> extractStorageUnits() throws IOException {
        List<StorageUnit> units = new ArrayList<>();
        // Pattern auf StorageAttribute ändern!
        Pattern attrPattern = Pattern.compile("^\\s*attribute\\s+(p[1-9])\\s*:\\s*StorageAttribute\\s*\\{?");
        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = attrPattern.matcher(line);
                if (m.find()) {
                    String unitName = m.group(1);
                    units.add(new StorageUnit(0, unitName, null)); 
                }
            }
        }
        return units;
    }

    /**
     * Extracts all transitions from the SysML file and returns them as a list of
     * Transition objects.
     * Each Transition contains its name, first, guard condition, then, and a
     * reference to its parent (part or state).
     *
     * @return List of Transition objects.
     * @throws IOException if the file cannot be read.
     */
    public List<Transition> extractTransitions() throws IOException {
        List<Transition> transitions = new ArrayList<>();
        Pattern transitionPattern = Pattern.compile("^\\s*transition\\s+([a-zA-Z0-9_]+)");
        Pattern firstAcceptThenPattern = Pattern
                .compile("^\\s*first\\s+([a-zA-Z0-9_]+)\\s+accept\\s+(.+?)\\s+then\\s+([a-zA-Z0-9_]+);?");
        Pattern firstPattern = Pattern.compile("^\\s*first\\s+([a-zA-Z0-9_]+)");
        Pattern thenPattern = Pattern.compile("\\bthen\\s+([a-zA-Z0-9_]+);?");
        Pattern blockBeginPattern = Pattern.compile(
                "\\bpart\\s+def\\s+([a-zA-Z0-9_]+)\\s*\\{|\\bstate\\s+def\\s+([a-zA-Z0-9_]+)\\s*\\{");

        String name = null;
        String first = null;
        String guardCondition = null;
        String then = null;
        boolean inTransition = false;
        boolean collectingGuard = false;
        StringBuilder guardBuilder = new StringBuilder();
        Deque<Parent> parentStack = new ArrayDeque<>();
        Deque<Integer> blockDepthStack = new ArrayDeque<>();
        int currentBlockDepth = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int openBraces = countChar(line, '{');
                int closeBraces = countChar(line, '}');
                currentBlockDepth += openBraces - closeBraces;

                // Detect block begin (part def or state def)
                Matcher blockBeginMatcher = blockBeginPattern.matcher(line);
                if (blockBeginMatcher.find()) {
                    if (blockBeginMatcher.group(1) != null) {
                        parentStack.push(new Parent(blockBeginMatcher.group(1), "part"));
                    } else if (blockBeginMatcher.group(2) != null) {
                        parentStack.push(new Parent(blockBeginMatcher.group(2), "state"));
                    }
                    blockDepthStack.push(currentBlockDepth - openBraces + 1);
                }

                // Detect transition start
                Matcher transitionMatcher = transitionPattern.matcher(line);
                if (transitionMatcher.find()) {
                    // Save previous transition if still open
                    if (inTransition && name != null) {
                        Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                        transitions.add(new Transition(name, first, guardCondition, then, parent));
                    }
                    name = transitionMatcher.group(1);
                    first = null;
                    guardCondition = null;
                    then = null;
                    inTransition = true;
                    collectingGuard = false;
                    guardBuilder.setLength(0);
                    continue;
                }

                // --- NEU: first ... accept ... then ... auf einer Zeile ---
                if (inTransition) {
                    Matcher firstAcceptThenMatcher = firstAcceptThenPattern.matcher(line);
                    if (firstAcceptThenMatcher.find()) {
                        first = firstAcceptThenMatcher.group(1);
                        guardCondition = "accept " + firstAcceptThenMatcher.group(2).trim();
                        then = firstAcceptThenMatcher.group(3);
                        Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                        transitions.add(new Transition(name, first, guardCondition, then, parent));
                        // Reset
                        name = null;
                        first = null;
                        guardCondition = null;
                        then = null;
                        inTransition = false;
                        collectingGuard = false;
                        continue;
                    }
                }

                if (inTransition) {
                    Matcher firstMatcher = firstPattern.matcher(line);
                    if (firstMatcher.find()) {
                        first = firstMatcher.group(1);
                        collectingGuard = true;
                        guardBuilder.setLength(0);
                        // Prüfe, ob auf der gleichen Zeile schon "then" steht
                        Matcher thenMatcher = thenPattern.matcher(line);
                        if (thenMatcher.find()) {
                            then = thenMatcher.group(1);
                            guardCondition = guardBuilder.toString().trim();
                            Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                            transitions.add(new Transition(name, first, guardCondition, then, parent));
                            // Reset
                            name = null;
                            first = null;
                            guardCondition = null;
                            then = null;
                            inTransition = false;
                            collectingGuard = false;
                        }
                        continue;
                    }
                    if (collectingGuard) {
                        Matcher thenMatcher = thenPattern.matcher(line);
                        if (thenMatcher.find()) {
                            then = thenMatcher.group(1);
                            String beforeThen = line.substring(0, thenMatcher.start()).trim();
                            if (!beforeThen.isEmpty())
                                guardBuilder.append(beforeThen).append("\n");
                            guardCondition = guardBuilder.toString().trim();
                            Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                            transitions.add(new Transition(name, first, guardCondition, then, parent));
                            // Reset
                            name = null;
                            first = null;
                            guardCondition = null;
                            then = null;
                            inTransition = false;
                            collectingGuard = false;
                        } else {
                            guardBuilder.append(line.trim()).append("\n");
                        }
                        continue;
                    }
                }

                // Pop parent stack when leaving a block
                while (!blockDepthStack.isEmpty() && currentBlockDepth < blockDepthStack.peek()) {
                    blockDepthStack.pop();
                    if (!parentStack.isEmpty())
                        parentStack.pop();
                }
            }
            // Save last transition if still open
            if (inTransition && name != null) {
                Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                transitions.add(new Transition(name, first, guardCondition, then, parent));
            }
        }
        return transitions;
    }

    /**
     * Extracts all states from the SysML file and returns them as a list of State
     * objects.
     * Each State contains its name, action type, action name, action content, and a
     * reference to its parent (part or state).
     *
     * The method uses a stack to keep track of the current parent block (either a
     * part or a state definition).
     * For each state found, the current parent is assigned.
     *
     * @return List of State objects representing all states found in the SysML
     *         file.
     * @throws IOException if the file cannot be read.
     */
    public List<State> extractStates() throws IOException {
        List<State> states = new ArrayList<>();
        Pattern statePattern = Pattern.compile("^\\s*state\\s+([a-zA-Z0-9_]+)\\b");
        Pattern actionPattern = Pattern.compile("^\\s*(entry|do|exit)\\s+(action\\s+)?([a-zA-Z0-9_]+)\\s*(\\{)?");
        // Pattern for detecting the beginning of a part or state definition block
        Pattern blockBeginPattern = Pattern.compile(
                "\\bpart\\s+def\\s+([a-zA-Z0-9_]+)\\s*\\{|\\bstate\\s+def\\s+([a-zA-Z0-9_]+)\\s*\\{");

        String stateName = null;
        String actionType = null;
        String actionName = null;
        String actionContent = null;
        boolean inState = false;
        boolean inAction = false;
        boolean foundAction = false;
        StringBuilder actionContentBuilder = new StringBuilder();
        Deque<Parent> parentStack = new ArrayDeque<>();
        Deque<Integer> blockDepthStack = new ArrayDeque<>();
        int currentBlockDepth = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Count braces to track the current block depth
                int openBraces = countChar(line, '{');
                int closeBraces = countChar(line, '}');
                currentBlockDepth += openBraces - closeBraces;

                // Detect the beginning of a part or state definition block and push it onto the
                // stack
                Matcher blockBeginMatcher = blockBeginPattern.matcher(line);
                if (blockBeginMatcher.find()) {
                    if (blockBeginMatcher.group(1) != null) {
                        parentStack.push(new Parent(blockBeginMatcher.group(1), "part"));
                    } else if (blockBeginMatcher.group(2) != null) {
                        parentStack.push(new Parent(blockBeginMatcher.group(2), "state"));
                    }
                    blockDepthStack.push(currentBlockDepth - openBraces + 1);
                }

                // Detect normal states (but not state definitions)
                Matcher stateMatcher = statePattern.matcher(line);
                if (stateMatcher.find() && !line.trim().startsWith("state def")) {
                    String foundStateName = stateMatcher.group(1);
                    if (foundStateName.equals("def"))
                        continue;
                    // If a previous state was open but had no action, save it
                    if (inState && !foundAction && stateName != null) {
                        Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                        states.add(new State(stateName, null, null, null, parent));
                    }
                    stateName = foundStateName;
                    Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                    inState = true;
                    inAction = false;
                    foundAction = false;
                    actionType = null;
                    actionName = null;
                    actionContentBuilder.setLength(0);

                    // Check for inline state (opened and closed in the same line)
                    if (line.contains("{") && line.contains("}")) {
                        states.add(new State(stateName, null, null, null, parent));
                        inState = false;
                        stateName = null;
                    }
                    continue;
                }

                // Handle state actions or state block end
                if (inState && !inAction) {
                    Matcher actionMatcher = actionPattern.matcher(line);
                    if (actionMatcher.find()) {
                        actionType = actionMatcher.group(1);
                        actionName = actionMatcher.group(3);
                        boolean hasBlock = actionMatcher.group(4) != null;
                        actionContentBuilder.setLength(0);
                        inAction = hasBlock; // Nur wenn Block, dann Action-Content sammeln
                        foundAction = true;
                        if (!hasBlock) {
                            // Direkt speichern, falls kein Block folgt
                            Parent parent = getCorrectParent(parentStack);
                            states.add(new State(stateName, actionType, actionName, null, parent));
                            actionType = null;
                            actionName = null;
                        }
                        continue;
                    }
                    // End of a state block without action
                    if (line.trim().equals("}")) {
                        if (!foundAction && stateName != null) {
                            Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                            states.add(new State(stateName, null, null, null, parent));
                        }
                        inState = false;
                        stateName = null;
                    }
                }

                // Handle action content
                if (inAction) {
                    if (line.contains("}")) {
                        actionContent = actionContentBuilder.toString().trim();
                        Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                        states.add(new State(stateName, actionType, actionName, actionContent, parent));
                        inAction = false;
                        actionType = null;
                        actionName = null;
                        actionContentBuilder.setLength(0);
                    } else {
                        actionContentBuilder.append(line.trim()).append(" ");
                    }
                }

                // Pop the parent stack when leaving a block
                while (!blockDepthStack.isEmpty() && currentBlockDepth < blockDepthStack.peek()) {
                    blockDepthStack.pop();
                    if (!parentStack.isEmpty())
                        parentStack.pop();
                }
            }
            // If a state is still open at the end, save it
            if (inState && !foundAction && stateName != null) {
                Parent parent = parentStack.isEmpty() ? null : parentStack.peek();
                states.add(new State(stateName, null, null, null, parent));
            }
        }
        return states;
    }

    /**
     * Returns the correct parent from the parent stack, or null if the stack is
     * empty.
     * 
     * @param parentStack The stack of Parent objects.
     * @return The top Parent object or null.
     */
    private Parent getCorrectParent(Deque<Parent> parentStack) {
        return parentStack.isEmpty() ? null : parentStack.peek();
    }

    /**
     * Counts the number of occurrences of a character in a string.
     * 
     * @param line The string to search.
     * @param c    The character to count.
     * @return The number of occurrences.
     */
    private int countChar(String line, char c) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == c)
                count++;
        }
        return count;
    }

    /**
     * Extracts all containers from the SysML file and returns them as a list of
     * Container objects.
     * Each Container contains its id, name, unitId, itemType, itemQuantity,
     * itemWeightKg, itemDateIn, and statusUpdatedAt.
     *
     * @return List of Container objects.
     * @throws IOException if the file cannot be read.
     */
    public List<Container> extractContainers() throws IOException {
        
        Set<String> containerKeys = new HashSet<>();
        Pattern containerAttrPattern = Pattern.compile("^\\s*attribute\\s+(c[1-9])\\s*:\\s*ContainerAttribute\\s*\\{?");

        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = containerAttrPattern.matcher(line);
                if (m.find()) {
                    containerKeys.add(m.group(1));
                }
            }
        }

        Map<String, Map<String, String>> raw = new HashMap<>();
        Pattern idPattern = Pattern.compile("^\\s*assign\\s+(c[1-9])\\.containerId\\s*:=\\s*(\\d+)\\s*;");
        Pattern namePattern = Pattern.compile("^\\s*assign\\s+(c[1-9])\\.containerName\\s*:=\\s*\"([^\"]+)\"\\s*;");
        Pattern unitIdPattern = Pattern.compile("^\\s*assign\\s+(c[1-9])\\.unitId\\s*:=\\s*p([1-9])\\.unitId\\s*;");
        Pattern itemTypePattern = Pattern
                .compile("^\\s*assign\\s+(c[1-9])\\.itemType\\s*:=\\s*ItemType::([A-Z_]+)\\s*;");
        Pattern itemQtyPattern = Pattern.compile("^\\s*assign\\s+(c[1-9])\\.itemQuantity\\s*:=\\s*(\\d+)\\s*;");
        Pattern itemWeightPattern = Pattern.compile("^\\s*assign\\s+(c[1-9])\\.itemWeightKG\\s*:=\\s*([0-9.]+)\\s*;");
        Pattern itemDateInPattern = Pattern.compile("^\\s*assign\\s+(c[1-9])\\.itemDateIn\\s*:=\\s*([0-9\\-]+)\\s*;");
        Pattern statusUpdatedAtPattern = Pattern
                .compile("^\\s*assign\\s+(c[1-9])\\.statusUpdatedAt\\s*:=\\s*([0-9\\-T:.]+)\\s*;");

        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher mId = idPattern.matcher(line);
                Matcher mName = namePattern.matcher(line);
                Matcher mUnitId = unitIdPattern.matcher(line);
                Matcher mItemType = itemTypePattern.matcher(line);
                Matcher mItemQty = itemQtyPattern.matcher(line);
                Matcher mItemWeight = itemWeightPattern.matcher(line);
                Matcher mItemDateIn = itemDateInPattern.matcher(line);
                Matcher mStatusUpdatedAt = statusUpdatedAtPattern.matcher(line);

                if (mId.find()) {
                    String key = mId.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("id", mId.group(2));
                }
                if (mName.find()) {
                    String key = mName.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("containerName", mName.group(2));
                }
                if (mUnitId.find()) {
                    String key = mUnitId.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("unitId", mUnitId.group(2));
                }
                if (mItemType.find()) {
                    String key = mItemType.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("itemType", mItemType.group(2));
                }
                if (mItemQty.find()) {
                    String key = mItemQty.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("itemQty", mItemQty.group(2));
                }
                if (mItemWeight.find()) {
                    String key = mItemWeight.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("itemWeightKg", mItemWeight.group(2));
                }
                if (mItemDateIn.find()) {
                    String key = mItemDateIn.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("itemDateIn", mItemDateIn.group(2));
                }
                if (mStatusUpdatedAt.find()) {
                    String key = mStatusUpdatedAt.group(1);
                    raw.computeIfAbsent(key, k -> new HashMap<>()).put("statusUpdatedAt", mStatusUpdatedAt.group(2));
                }
            }
        }

        List<Container> containers = new ArrayList<>();
        for (String key : containerKeys) {
            Map<String, String> map = raw.getOrDefault(key, Collections.emptyMap());

            int id = Integer.parseInt(map.getOrDefault("id", "0"));
            String containerName = map.getOrDefault("containerName", key.toUpperCase()); // fallback: "C1" etc.
            int unitId = Integer.parseInt(map.getOrDefault("unitId", "0"));
            String itemType = map.getOrDefault("itemType", null);
            int itemQty = Integer.parseInt(map.getOrDefault("itemQty", "0"));
            double itemWeightKg = Double.parseDouble(map.getOrDefault("itemWeightKg", "0.0"));
            LocalDate itemDateIn = null;
            if (map.containsKey("itemDateIn")) {
                try {
                    itemDateIn = LocalDate.parse(map.get("itemDateIn"));
                } catch (Exception ignored) {
                }
            }
            LocalDateTime statusUpdatedAt = null;
            if (map.containsKey("statusUpdatedAt")) {
                try {
                    statusUpdatedAt = LocalDateTime.parse(map.get("statusUpdatedAt"));
                } catch (Exception ignored) {
                }
            }

            containers.add(new Container(id, containerName, unitId, itemType, itemQty, itemWeightKg, itemDateIn,
                    statusUpdatedAt));
        }
        return containers;
    }
    /**
     * Extracts all ArmOperation objects from the SysML file.
     * Each ArmOperation contains its armId, containerId, operationType, timestamp,
     * triggerSource, and relativePosition.
     *
     * @return List of ArmOperation objects.
     * @throws IOException if the file cannot be read.
     */
    public List<ArmOperation> extractArmOperations() throws IOException {
        Map<String, Integer> armIdMap = new HashMap<>();
        Pattern armIdPattern = Pattern.compile(
                "^\\s*assign\\s+(cantileverArmOperation|verticalArmOperation|horizontalArmOperation)\\.armId\\s*:=\\s*(\\d+)\\s*;");

        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = armIdPattern.matcher(line);
                if (m.find()) {
                    armIdMap.put(m.group(1), Integer.parseInt(m.group(2)));
                }
            }
        }
        Map<String, String> operationTypeMap = new HashMap<>();
        Map<String, String> triggerSourceMap = new HashMap<>();
        Map<String, int[]> relativePositionMap = new HashMap<>();
    
        int defaultContainerId = -1;
        LocalDateTime defaultTimestamp = null;

        boolean inSetup = false;
        Pattern opTypePattern = Pattern.compile(
                "^\\s*assign\\s+selfEngine\\.(cantileverArmOperation|verticalArmOperation|horizontalArmOperation)\\.operationType\\s*:=\\s*OperationType::([A-Z_]+)\\s*;");
        Pattern triggerPattern = Pattern.compile(
                "^\\s*assign\\s+selfEngine\\.(cantileverArmOperation|verticalArmOperation|horizontalArmOperation)\\.triggerSource\\s*:=\\s*\"([^\"]+)\"\\s*;");
        Pattern relPosPattern = Pattern.compile(
                "^\\s*assign\\s+selfEngine\\.(cantileverArmOperation|verticalArmOperation|horizontalArmOperation)\\.relativePosition\\s*:=\\s*WarehouseRelativePosition\\(([-\\d]+),\\s*([-\\d]+),\\s*([-\\d]+)\\)\\s*;");

        try (BufferedReader reader = new BufferedReader(new FileReader(sysmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("entry setup")) {
                    inSetup = true;
                    continue;
                }
                if (inSetup && line.contains("}"))
                    break;
                if (!inSetup)
                    continue;

                Matcher mOpType = opTypePattern.matcher(line);
                Matcher mTrigger = triggerPattern.matcher(line);
                Matcher mRelPos = relPosPattern.matcher(line);

                if (mOpType.find()) {
                    operationTypeMap.put(mOpType.group(1), mOpType.group(2));
                }
                if (mTrigger.find()) {
                    triggerSourceMap.put(mTrigger.group(1), mTrigger.group(2));
                }
                if (mRelPos.find()) {
                    int[] pos = new int[] {
                            Integer.parseInt(mRelPos.group(2)),
                            Integer.parseInt(mRelPos.group(3)),
                            Integer.parseInt(mRelPos.group(4))
                    };
                    relativePositionMap.put(mRelPos.group(1), pos);
                }
            }
        }

        List<ArmOperation> result = new ArrayList<>();
        String[] arms = { "cantileverArmOperation", "verticalArmOperation", "horizontalArmOperation" };
        for (String arm : arms) {
            int armId = armIdMap.getOrDefault(arm, -1);
            String opType = operationTypeMap.getOrDefault(arm, null);
            String trigger = triggerSourceMap.getOrDefault(arm, null);
            int[] relPos = relativePositionMap.getOrDefault(arm, null);

            result.add(new ArmOperation(
                    armId,
                    defaultContainerId,
                    opType,
                    defaultTimestamp,
                    trigger,
                    relPos));
        }
        return result;
    }

}