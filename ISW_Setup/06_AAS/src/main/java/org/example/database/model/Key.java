package org.example.database.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class Key {
    @Field("type")
    private String type;
    
    @Field("value")
    private String value;
    
    @Field("id_type")
    private String idType;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }
}
