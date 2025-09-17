package org.example.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "asset_administration_shells")
public class MongoAssetAdministrationShell {
    @Id
    private String id;

    @Field("id_short")
    private String idShort;

    @Field("category")
    private String category;

    @Field("descriptions")
    private List<LangString> descriptions;

    @Field("asset_ref")
    private org.example.database.model.Reference assetRef;

    @Field("submodel_refs")
    private List<org.example.database.model.Reference> submodelRefs;

    @Field("source_file")
    private String sourceFile;

    @Field("imported_at")
    private LocalDateTime importedAt;
    
    @Field("administration")
    private Map<String, String> administration;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdShort() {
        return idShort;
    }

    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LangString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<LangString> descriptions) {
        this.descriptions = descriptions;
    }

    public org.example.database.model.Reference getAssetRef() {
        return assetRef;
    }

    public void setAssetRef(org.example.database.model.Reference assetRef) {
        this.assetRef = assetRef;
    }

    public List<org.example.database.model.Reference> getSubmodelRefs() {
        return submodelRefs;
    }

    public void setSubmodelRefs(List<org.example.database.model.Reference> submodelRefs) {
        this.submodelRefs = submodelRefs;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }
    
    public Map<String, String> getAdministration() {
        return administration;
    }

    public void setAdministration(Map<String, String> administration) {
        this.administration = administration;
    }
}
