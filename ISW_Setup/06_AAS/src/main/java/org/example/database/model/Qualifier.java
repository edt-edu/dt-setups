package org.example.database.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class Qualifier {
    @Field("type")
    private String type;
    
    @Field("value")
    private Object value;
    
    @Field("value_type")
    private String valueType;
    
    @Field("semantic_id")
    private Reference semanticId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Reference getSemanticId() {
        return semanticId;
    }

    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }
}
