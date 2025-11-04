package backend_sysml_service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling the deactivation of storage units in a SysML model.
 * Provides methods to clean and filter states and transitions based on a set of deactivated units.
 * This includes removing or updating state and transition names, guard conditions, and then-states
 * that reference deactivated units, as well as extracting only the relevant and active elements.
 *
 *
 */
public class SysMLDeactivationUtil {
    /**
     * Cleans the given transitions by removing all parts of the name and then-state
     * that reference deactivated states, and removes duplicates.
     *
     * @param transitions      List of transitions to clean.
     * @param deactivatedUnits Set of deactivated unit names.
     * @return List of cleaned transitions (all names updated to only reference active units).
     */
    public List<Transition> getUpdatedTransitions(List<Transition> transitions, Set<String> deactivatedUnits) {
        Pattern partPattern = Pattern.compile("^(P[1-9])(Full|Empt)$");

        // Hilfsfunktion für State-Namen-Bereinigung
        java.util.function.Function<String, String> cleanStateName = name -> {
            if (name == null)
                return null;
            String[] parts = name.split("_");
            List<String> keptParts = new ArrayList<>();
            for (String part : parts) {
                Matcher matcher = partPattern.matcher(part);
                if (matcher.matches()) {
                    String px = matcher.group(1);
                    String status = matcher.group(2);
                    if (!deactivatedUnits.contains(px)) {
                        keptParts.add(px + status);
                    }
                }
            }
            if (keptParts.isEmpty())
                return null;
            return String.join("_", keptParts);
        };
        
        Set<String> seen = new HashSet<>();
        List<Transition> result = new ArrayList<>();

        for (Transition tr : transitions) {
            String origName = tr.getName();
            String origFirst = tr.getFirst();
            String origThen = tr.getThen();

            String newName = origName;
            if (origName != null && origName.contains("_To_")) {
                int idx = origName.indexOf("_To_");
                String before = origName.substring(0, idx);
                String after = origName.substring(idx + 4);
                // Passe auch den Teil vor _To_ an!
                String cleanedBefore = cleanStateName.apply(before);
                String cleanedAfter = cleanStateName.apply(after);
                if (cleanedBefore != null && cleanedAfter != null) {
                    newName = cleanedBefore + "_To_" + cleanedAfter;
                } else if (cleanedAfter != null) {
                    newName = before + "_To_" + cleanedAfter;
                } else if (cleanedBefore != null) {
                    newName = cleanedBefore + "_To_" + after;
                }
            }

            // first anpassen
            String newFirst = origFirst;
            if (origFirst != null) {
                String cleanedFirst = cleanStateName.apply(origFirst);
                if (cleanedFirst != null) {
                    newFirst = cleanedFirst;
                }
            }

            // then anpassen
            String newThen = origThen;
            if (origThen != null) {
                String cleanedThen = cleanStateName.apply(origThen);
                if (cleanedThen != null) {
                    newThen = cleanedThen;
                }
            }

            // Filter: Wenn einer der State-Namen null/leer ist, überspringen!
            if (newName == null || newName.isBlank() ||
                    newFirst == null || newFirst.isBlank() ||
                    newThen == null || newThen.isBlank()) {
                continue;
            }

            Transition cleaned = new Transition(
                    newName,
                    newFirst,
                    tr.getGuardCondition(),
                    newThen,
                    tr.getParent());

            // *** Nur nach name, first, then deduplizieren ***
            String uniqueKey = cleaned.getName() + "|" + cleaned.getFirst() + "|" + cleaned.getThen();
            if (!seen.contains(uniqueKey)) {
                seen.add(uniqueKey);
                result.add(cleaned);
            }
        }
        return result;
    }

    /**
     * Removes all guard condition lines that reference deactivated units.
     * Also cleans up trailing logical operators and empty parentheses.
     *
     * @param guard            The original guard condition as a string (may be
     *                         multiline).
     * @param deactivatedUnits Set of deactivated unit names (e.g. "P1", "P2", ...).
     * @return The cleaned guard condition, or "if (false)" if nothing remains.
     */
    public String removeDeactivatedUnitFromGuard(String guard, Set<String> deactivatedUnits) {
        if (guard == null || guard.isBlank())
            return guard;

        String[] lines = guard.split("\\n");
        List<String> filtered = new ArrayList<>();
        for (String line : lines) {
            boolean containsDeactivated = false;
            for (String unit : deactivatedUnits) {
                if (line.matches(".*\\bUnitName::" + unit + "\\b.*") ||
                        line.matches(".*\\bselfEngine\\." + unit.toLowerCase() + "\\..*") ||
                        line.matches(".*\\b" + unit.toLowerCase() + "\\.occupied.*")) {
                    containsDeactivated = true;
                    break;
                }
            }
            if (!containsDeactivated)
                filtered.add(line);
        }

        // Entferne leere Klammerblöcke und überflüssige "and"/"or"
        String result = String.join("\n", filtered)
                .replaceAll("^(and|or)\\s*", "")
                .replaceAll("\\s*(and|or)$", "")
                .replaceAll("\\(\\s*\\)", "")
                .replaceAll("\\(\\s*\\)", "");

        result = result.replaceAll("(or|and)\\s*\\)$", ")");

        result = fixParentheses(result);

        // In case after filtering there is nothing left returns "if (false)"
        if (result.replaceAll("[\\s\\n\\(\\)]", "").isEmpty() || result.equals("if ()")) {
            return "if (false)";
        }
        return result;
    }

    /**
     * Filters the given transitions and returns only those whose guard condition
     * references at least one of the specified units (e.g. P1–P9).
     *
     * @param transitions List of transitions to filter.
     * @param units       Set of unit names to look for in the guard condition.
     * @return List of transitions whose guard condition references one of the units
     *         (that means irrelevant transitions, which do not reference any active
     *         unit, are excluded).
     */
    public List<Transition> filterTransitionsByUnitsInGuard(List<Transition> transitions, Set<String> units) {
        List<Transition> result = new ArrayList<>();
        for (Transition tr : transitions) {
            String guard = tr.getGuardCondition();
            if (guard == null)
                continue;
            for (String unit : units) {
                // Suche nach selfEngine.pX. (case-insensitive) oder UnitName::PX
                if (guard.toLowerCase().contains("selfengine." + unit.toLowerCase() + ".")
                        || guard.contains("UnitName::" + unit)) {
                    result.add(tr);
                    break;
                }
            }
        }
        return result;
    }
    /**
     * Fixes the parentheses in the guard condition string by ensuring that all
     * opening parentheses have a matching closing parenthesis at the end.
     * Also removes any trailing closing parentheses that do not match an opening
     * parenthesis.
     *
     * @param guard The guard condition string to fix.
     * @return The fixed guard condition string with balanced parentheses.
     */
    private String fixParentheses(String guard) {
        if (guard == null)
            return null;
        int open = 0, close = 0;
        for (char c : guard.toCharArray()) {
            if (c == '(')
                open++;
            if (c == ')')
                close++;
        }
        StringBuilder sb = new StringBuilder(guard.trim());
        // Füge fehlende schließende Klammern am Ende an
        while (close < open) {
            sb.append(")");
            close++;
        }
        // Optional: Entferne überzählige schließende Klammern am Ende
        while (close > open && sb.length() > 0 && sb.charAt(sb.length() - 1) == ')') {
            sb.deleteCharAt(sb.length() - 1);
            close--;
        }
        return sb.toString();
    }

    /**
     * Cleans the given transitions by removing all guard condition lines that
     * reference
     * deactivated units and removing transitions whose guard condition becomes
     * empty or "if (false)".
     *
     * @param states           (Unused, can be removed) List of states (not used in
     *                         this method).
     * @param transitions      List of transitions to clean.
     * @param deactivatedUnits Set of deactivated unit names.
     * @return List of cleaned transitions (that means all guard conditions have
     *         been updated to the only active).
     */
    public List<Transition> getCleanedGuardConditionTransitions(
            List<Transition> transitions,
            Set<String> deactivatedUnits) {
        List<Transition> cleanedTransitions = new ArrayList<>();
        boolean noDeactivation = deactivatedUnits == null || deactivatedUnits.isEmpty();

        for (Transition tr : transitions) {
            String originalGuard = tr.getGuardCondition();
            String newGuard = removeDeactivatedUnitFromGuard(originalGuard, deactivatedUnits);

            // Wenn keine Deaktivierung, dann einfach alle originalen Transitionen übernehmen
            if (noDeactivation) {
                cleanedTransitions.add(tr);
                continue;
            }

            // deletes transition if it is empty or only contains "if (false)"
            if (newGuard == null || newGuard.isBlank() || newGuard.trim().equals("if (false)")
                    || newGuard.trim().equals("if ()")) {
                continue;
            }
            // Nur hinzufügen, wenn sich die GuardCondition wirklich geändert hat
            if (!newGuard.equals(originalGuard)) {
                Transition cleaned = new Transition(
                    tr.getName(),
                    tr.getFirst(),
                    newGuard,
                    tr.getThen(),
                    tr.getParent()
                );
                cleanedTransitions.add(cleaned);
            }
        }
        return cleanedTransitions;
    }

    /**
     * Cleans the given states by removing all parts of the name that reference
     * deactivated units, and removes duplicates.
     *
     * @param states           List of states to clean.
     * @param deactivatedUnits Set of deactivated unit names (e.g. "P1", "P2", ...).
     * @return List of cleaned states (all names updated to only reference active
     *         units).
     */
    public List<State> getUpdatedStates(List<State> states, Set<String> deactivatedUnits) {
        Map<String, State> uniqueStates = new LinkedHashMap<>();
        Pattern partPattern = Pattern.compile("^(P[1-9])(Full|Empt)$");

        for (State state : states) {
            String name = state.getName();
            if (name == null)
                continue;

            String[] parts = name.split("_");
            List<String> keptParts = new ArrayList<>();
            for (String part : parts) {
                Matcher matcher = partPattern.matcher(part);
                if (matcher.matches()) {
                    String px = matcher.group(1); // z.B. "P1"
                    String status = matcher.group(2); // "Full" oder "Empt"
                    if (!deactivatedUnits.contains(px)) {
                        keptParts.add(px + status);
                    }
                }
            }
            if (keptParts.isEmpty())
                continue;
            String newName = String.join("_", keptParts);

            State newState = newName.equals(name)
                    ? state
                    : new State(newName, state.getActionType(), state.getActionName(), state.getActionContent(),
                            state.getParent());

            uniqueStates.put(newName, newState);
        }
        return new ArrayList<>(uniqueStates.values());
    }
    /**
     * Result object holding all relevant lists after the deactivation workflow.
     */
    public static class DeactivationResult {
        public final List<State> states;
        public final List<Transition> relevantTransitionsToCmdReceived;
        public final List<Transition> relevantTransitionsFixedGuardToStates;
        public final List<Transition> fixedSetupTransitions;
        public final List<StorageUnit> activeStorageUnits;
        /**
         * Constructs a DeactivationResult with all relevant lists.
         *
         * @param states                         Cleaned states.
         * @param transitions                    Transitions to cmdReceived.
         * @param updatedGuardTransition         Transitions with cleaned guard conditions.
         * @param fixedSetupTransitions          Setup transitions with cleaned then-states.
         * @param activeStorageUnits             List of active storage units.
         */
        public DeactivationResult(List<State> states, List<Transition> transitions,
                List<Transition> updatedGuardTransition, List<Transition> fixedSetupTransitions, List<StorageUnit> activeStorageUnits) {
            this.states = states;
            this.relevantTransitionsToCmdReceived = transitions;
            this.relevantTransitionsFixedGuardToStates = updatedGuardTransition;
            this.fixedSetupTransitions = fixedSetupTransitions;
            this.activeStorageUnits = activeStorageUnits; 
        }
    }

    /**
     * Handles the full deactivation workflow: cleans states and transitions,
     * removes duplicates, and returns only relevant transitions and states.
     *
     * @param allStates        All original states.
     * @param allTransitions   All original transitions.
     * @param allStorageUnits  All original storage units.
     * @param deactivatedUnits Set of deactivated unit names (e.g. "P1", "P4").
     * @return DeactivationResult with cleaned states and relevant transitions.
     */
    public DeactivationResult handleDeactivationWorkflow(
            List<State> allStates,
            List<Transition> allTransitions,
            List<StorageUnit> allStorageUnits,
            Set<String> deactivatedUnits) {

        // Step 1: Clean state names by removing references to deactivated units.
        List<State> cleanedStates = getUpdatedStates(allStates, deactivatedUnits);

        // Step 2: Clean transition names, first, and then-states by removing references to deactivated units.
        List<Transition> dedupedTransitions = getUpdatedTransitions(allTransitions, deactivatedUnits);

        // Step 3: Determine which units are still active after deactivation.
        Set<String> allUnits = Set.of("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9");
        Set<String> activeUnits = new HashSet<>(allUnits);
        activeUnits.removeAll(deactivatedUnits);

        // Step 4: Filter transitions to only those referencing active units in their guard conditions.
        List<Transition> filteredTransitionsByGuardCondition = filterTransitionsByUnitsInGuard(dedupedTransitions, activeUnits);

        // Step 5: Clean guard conditions in transitions by removing references to deactivated units.
        List<Transition> relevantTransitionsFixedGuardToStates = getCleanedGuardConditionTransitions(filteredTransitionsByGuardCondition, deactivatedUnits);

        // Step 6: Find all transitions leading to cmdReceived states.
        List<Transition> relevantTransitionsToCmdReceived = findTransitionsToCmdReceived(dedupedTransitions);

        // Step 7: Find all setup transitions and clean their then-states.
        List<Transition> setupTransitions = findSetupTransitions(dedupedTransitions);
        List<Transition> fixedSetupTransitions = fixThenStates(setupTransitions, deactivatedUnits);

        // Step 8: Get the list of currently active storage units.
        List<StorageUnit> activeStorageUnits = getActiveStorageUnits(allStorageUnits, deactivatedUnits);

        // Special case: If P1, P4, and P7 are all deactivated, return empty transition lists (automaton not needed).
        Set<String> deactUpper = new HashSet<>();
        for (String d : deactivatedUnits) deactUpper.add(d.toUpperCase());
        if (deactUpper.contains("P1") && deactUpper.contains("P4") && deactUpper.contains("P7")) {
            return new DeactivationResult(
                cleanedStates,
                List.of(),
                List.of(),
                List.of(),
                activeStorageUnits
            );
        }

        // Return the result object containing all cleaned and filtered lists.
        return new DeactivationResult(
            cleanedStates,
            relevantTransitionsToCmdReceived,
            relevantTransitionsFixedGuardToStates,
            fixedSetupTransitions,
            activeStorageUnits
        );
    }
    /**
     * Finds all transitions whose name contains "To_cmdReceived".
     *
     * @param transitions List of transitions to search.
     * @return List of transitions to cmdReceived.
     */
    public List<Transition> findTransitionsToCmdReceived(List<Transition> transitions) {
        List<Transition> result = new ArrayList<>();
        for (Transition tr : transitions) {
            if (tr.getName() != null && tr.getName().contains("To_cmdReceived")) {
                result.add(tr);
            }
        }
        return result;
    }
    /**
     * Finds all setup transitions whose name starts with "setup_To_".
     *
     * @param transitions List of transitions to search.
     * @return List of setup transitions.
     */
    public List<Transition> findSetupTransitions(List<Transition> transitions) {
        List<Transition> result = new ArrayList<>();
        for (Transition tr : transitions) {
            if (tr.getName() != null && tr.getName().startsWith("setup_To_")) {
                result.add(tr);
            }
        }
        return result;
    }

    /**
     * Cleans the then-state of setup transitions by removing references to deactivated units.
     *
     * @param transitions      List of setup transitions.
     * @param deactivatedUnits Set of deactivated unit names.
     * @return List of setup transitions with cleaned then-states.
     */
    public List<Transition> fixThenStates(List<Transition> transitions, Set<String> deactivatedUnits) {
        Pattern partPattern = Pattern.compile("^(P[1-9])(Full|Empt)$");
        java.util.function.Function<String, String> cleanStateName = name -> {
            if (name == null) return null;
            String[] parts = name.split("_");
            List<String> keptParts = new ArrayList<>();
            for (String part : parts) {
                Matcher matcher = partPattern.matcher(part);
                if (matcher.matches()) {
                    String px = matcher.group(1);
                    String status = matcher.group(2);
                    if (!deactivatedUnits.contains(px)) {
                        keptParts.add(px + status);
                    }
                }
            }
            if (keptParts.isEmpty()) return null;
            return String.join("_", keptParts);
        };

        List<Transition> result = new ArrayList<>();
        for (Transition tr : transitions) {
            String origThen = tr.getThen();
            String newThen = origThen;
            if (origThen != null) {
                String cleanedThen = cleanStateName.apply(origThen);
                if (cleanedThen != null) {
                    newThen = cleanedThen;
                }
            }
            // Nur behalten, wenn das then nicht null/leer ist
            if (newThen != null && !newThen.isBlank()) {
                // Erzeuge eine neue Transition mit angepasstem then
                Transition fixed = new Transition(
                    tr.getName(),
                    tr.getFirst(),
                    tr.getGuardCondition(),
                    newThen,
                    tr.getParent()
                );
                result.add(fixed);
            }
        }
        return result;
    }
    /**
     * Returns a list of active storage units, excluding those that are deactivated.
     * The deactivated units are compared case-insensitively.
     *
     * @param allUnits         List of all storage units.
     * @param deactivatedUnits Set of deactivated unit names (case-insensitive).
     * @return List of active storage units.
     */
    public List<StorageUnit> getActiveStorageUnits(List<StorageUnit> allUnits, Set<String> deactivatedUnits) {
        List<StorageUnit> active = new ArrayList<>();
        // Alle deaktivierten Namen in Kleinbuchstaben
        Set<String> deactivatedLower = new HashSet<>();
        for (String d : deactivatedUnits) {
            deactivatedLower.add(d.toLowerCase());
        }
        for (StorageUnit unit : allUnits) {
            String unitName = unit.getUnitName();
            if (unitName != null && !deactivatedLower.contains(unitName.toLowerCase())) {
                active.add(unit);
            }
        }
        return active;
    }
}
