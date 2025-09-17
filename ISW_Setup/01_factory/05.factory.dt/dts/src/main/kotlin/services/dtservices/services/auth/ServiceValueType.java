package services.dtservices.services.auth;

public enum ServiceValueType {
    SERVICE_ID("id"),
    SERVICE_NAME("name");

    final private String value;

    ServiceValueType(String value) {
        this.value = value;
    }

    /**
     * Get the value of the operation
     * @return
     */
    public String getValue() {
        return value;
    }
}
