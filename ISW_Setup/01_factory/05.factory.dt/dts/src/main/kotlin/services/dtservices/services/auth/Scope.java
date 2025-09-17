package services.dtservices.services.auth;

public class Scope {
    // human readable name of the scope
    final private String name;
    // human readable description of the scope
    final private String description;
    // value of the scope
    final private String value;

    public Scope(String name, String description, String value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    /**
     * Get the name of the scope
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of the scope
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the value of the scope
     * @return
     */
    public String getValue() {
        return value;
    }
}
