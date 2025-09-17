package services.dtservices.services.query.query.context.value;

public enum ContextValueType {
    STRING("string"), 
    NUMBER("number"),
    BOOLEAN("boolean"),
    MAP("map"),
    LIST("list"),
    QUERY("query"),
    DATE("date");


    private final String typeAsString;

    private ContextValueType(final String typeAsString) {
        this.typeAsString = typeAsString;
    }

    public String getTypeAsString() {
        return typeAsString;
    }

}
