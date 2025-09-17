package org.example.database.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class SubmodelElement {
    @Field("id_short")
    private String idShort;
    
    @Field("semantic_id")
    private Reference semanticId;
    
    @Field("value")
    private Object value;
    
    @Field("value_type")
    private String valueType;
    
    @Field("category")
    private String category;
    
    @Field("description")
    private String description;
}
