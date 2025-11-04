package backend_sysml_service;

public class Parent {
    private String name;
    private String type; // "part" oder "state"

    public Parent(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }
}
