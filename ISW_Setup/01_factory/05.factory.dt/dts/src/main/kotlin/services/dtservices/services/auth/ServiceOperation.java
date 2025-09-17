package services.dtservices.services.auth;

public enum ServiceOperation {
    READ("read"), 
    WRITE("write"),
    DELETE("delete"), 
    UPDATE("update");

    final private String value;

    ServiceOperation(String value) {
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
