package org.example.database.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public class Reference {
    @Field("type")
    private String type;
    
    @Field("keys")
    private List<Key> keys;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }
}
