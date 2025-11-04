package backend_sysml_service;

public class State {
    private String name;
    private String actionType;    // entry, do, exit
    private String actionName;    // z.B. stopMotor, move, ...
    private String actionContent;
    private Parent parent; // Referenz auf Parent-Objekt

    public State(String name, String actionType, String actionName, String actionContent, Parent parent) {
        this.name = name;
        this.actionType = actionType;
        this.actionName = actionName;
        this.actionContent = actionContent;
        this.parent = parent;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getActionContent() { return actionContent; }
    public void setActionContent(String actionContent) { this.actionContent = actionContent; }

    public Parent getParent() { return parent; }
    public void setParent(Parent parent) { this.parent = parent; }
}
