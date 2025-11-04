package backend_sysml_service;

/**
 * Represents a transition in the SysML model.
 * Attributes:
 * - name: primary key (unique identifier for the transition)
 * - first: source state or event
 * - guardCondition: the guard condition for the transition
 * - then: target state or action
 * - parent: reference to the parent object (foreign key, e.g. State or Part)
 */
public class Transition {
    private String name;           // primary key
    private String first;
    private String guardCondition;
    private String then;
    private Parent parent;         // foreign key reference

    public Transition(String name, String first, String guardCondition, String then, Parent parent) {
        this.name = name;
        this.first = first;
        this.guardCondition = guardCondition;
        this.then = then;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public String getFirst() {
        return first;
    }

    public String getGuardCondition() {
        return guardCondition;
    }

    public String getThen() {
        return then;
    }

    public Parent getParent() {
        return parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public void setGuardCondition(String guardCondition) {
        this.guardCondition = guardCondition;
    }

    public void setThen(String then) {
        this.then = then;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
