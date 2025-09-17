package org.example.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "submodels")
public class SubmodelData {
    @Id
    @Field("_id")
    private String id;

    @Field("id_short")
    private String idShort;

    @Field("source_file")
    private String sourceFile;

    @Field("semantic_id")
    private Reference semanticId;

    @Field("supplemental_semantic_ids")
    private List<Reference> supplementalSemanticIds;

    @Field("submodel_id")
    private String submodelId;

    @Field("category")
    private String category;

    @Field("description")
    private List<LangString> description;

    @Field("administration")
    private Map<String, String> administration;

    @Field("kind")
    private String kind;

    @Field("imported_at")
    private LocalDateTime importedAt;

    @Field("submodel_elements")
    private List<Map<String, Object>> submodelElements;

    @Field("aas_ref")
    private Reference aasRef;

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

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Reference getSemanticId() {
        return semanticId;
    }

    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }

    public List<Reference> getSupplementalSemanticIds() {
        return supplementalSemanticIds;
    }

    public void setSupplementalSemanticIds(List<Reference> supplementalSemanticIds) {
        this.supplementalSemanticIds = supplementalSemanticIds;
    }

    public String getSubmodelId() {
        return submodelId;
    }

    public void setSubmodelId(String submodelId) {
        this.submodelId = submodelId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LangString> getDescription() {
        return description;
    }

    public void setDescription(List<LangString> description) {
        this.description = description;
    }

    public Map<String, String> getAdministration() {
        return administration;
    }

    public void setAdministration(Map<String, String> administration) {
        this.administration = administration;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public List<Map<String, Object>> getSubmodelElements() {
        return submodelElements;
    }

    public void setSubmodelElements(List<Map<String, Object>> submodelElements) {
        this.submodelElements = submodelElements;
    }

    public Reference getAasRef() {
        return aasRef;
    }

    public void setAasRef(Reference aasRef) {
        this.aasRef = aasRef;
    }
}
