package org.example.database.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "concept_descriptions")
public class ConceptDescription {
    @Id
    private String id;

    @Field("id_short")
    private String idShort;

    @Field("category")
    private String category;

    @Field("descriptions")
    private List<LangString> descriptions;

    @Field("is_case_of")
    private List<Reference> isCaseOf;

    @Field("data_specifications")
    private List<Reference> dataSpecifications;

    @Field("embedded_data_specifications")
    private List<Map<String, Object>> embeddedDataSpecifications;
    
    @Field("administration")
    private Map<String, String> administration;

    // @Field("iec61360")
    // private Map<String, Object> iec61360Data;
    @Field("imported_at")
    private LocalDateTime importedAt;

    @Field("source_file")
    private String sourceFile;
}
