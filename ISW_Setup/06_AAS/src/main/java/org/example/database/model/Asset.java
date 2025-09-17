package org.example.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "assets")
public class Asset {
    @Id
    private String id;

    @Field("id_short")
    private String idShort;

    @Field("category")
    private String category;

    @Field("description")
    private String description;

    @Field("kind")
    private String kind;

    @Field("data_specifications")
    private List<Reference> dataSpecifications;

    @Field("embedded_data_specifications")
    private List<Map<String, Object>> embeddedDataSpecifications;

    @Field("global_asset_id")
    private String globalAssetId;

    @Field("thumbnail")
    private String thumbnail;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("imported_at")
    private LocalDateTime importedAt;

    @Field("source_file")
    private String sourceFile;

    // Getters and setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<Reference> getDataSpecifications() {
        return dataSpecifications;
    }

    public void setDataSpecifications(List<Reference> dataSpecifications) {
        this.dataSpecifications = dataSpecifications;
    }

    public List<Map<String, Object>> getEmbeddedDataSpecifications() {
        return embeddedDataSpecifications;
    }

    public void setEmbeddedDataSpecifications(List<Map<String, Object>> embeddedDataSpecifications) {
        this.embeddedDataSpecifications = embeddedDataSpecifications;
    }

    public String getGlobalAssetId() {
        return globalAssetId;
    }

    public void setGlobalAssetId(String globalAssetId) {
        this.globalAssetId = globalAssetId;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
}
