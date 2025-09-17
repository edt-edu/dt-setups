package de.unistuttgart.isw.dtservices.services.auth;

public class ServiceValue {
    final private ServiceValueType type;
    final private String value;

    public ServiceValue(ServiceValueType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Get the type of the value
     * @return
     */
    public ServiceValueType getType() {
        return type;
    }

    /**
     * Get the value
     * @return
     */
    public String getValue() {
        return value;
    }
}
