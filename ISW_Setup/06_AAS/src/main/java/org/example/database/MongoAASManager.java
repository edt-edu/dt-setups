package org.example.database;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.example.database.model.MongoAssetAdministrationShell;
import org.example.database.model.SubmodelData;
import org.example.database.model.Reference;
import org.example.database.model.Key;
import org.example.database.model.LangString;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MongoAASManager {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    // MongoDB Storage Methods
    public void saveToMongo(AssetAdministrationShell aas) {
        log.info("Saving AAS with idShort: {} to MongoDB", aas.getIdShort());
        MongoAssetAdministrationShell mongoAas = convertToMongoAAS(aas);
        mongoTemplate.save(mongoAas);
    }

    public List<MongoAssetAdministrationShell> getAllMongoAAS() {
        return mongoTemplate.findAll(MongoAssetAdministrationShell.class);
    }

    public MongoAssetAdministrationShell getMongoAASByIdShort(String idShort) {
        Query query = new Query(Criteria.where("idShort").is(idShort));
        return mongoTemplate.findOne(query, MongoAssetAdministrationShell.class);
    }

    public void saveToMongo(Submodel submodel) {
        log.info("Saving Submodel with idShort: {} to MongoDB", submodel.getIdShort());
        SubmodelData submodelData = convertToSubmodelData(submodel);
        mongoTemplate.save(submodelData);
    }

    public List<SubmodelData> getAllMongoSubmodels() {
        return mongoTemplate.findAll(SubmodelData.class);
    }

    public SubmodelData getMongoSubmodelByIdShort(String idShort) {
        Query query = new Query(Criteria.where("idShort").is(idShort));
        return mongoTemplate.findOne(query, SubmodelData.class);
    }

    public void deleteAllMongoData() {
        log.info("Clearing all MongoDB AAS and Submodel data");
        mongoTemplate.dropCollection(MongoAssetAdministrationShell.class);
        mongoTemplate.dropCollection(SubmodelData.class);
        mongoTemplate.dropCollection(org.example.database.model.ConceptDescription.class);
    }

  
    // Conversion methods
    private MongoAssetAdministrationShell convertToMongoAAS(AssetAdministrationShell aas) {
        MongoAssetAdministrationShell mongoAas = new MongoAssetAdministrationShell();
        
        // Basic fields
        mongoAas.setId(aas.getId());
        mongoAas.setIdShort(aas.getIdShort());
        mongoAas.setCategory(aas.getCategory());
        
        // Convert multi-language descriptions
        if (aas.getDescription() != null && !aas.getDescription().isEmpty()) {
            List<LangString> descriptions = new ArrayList<>();
            for (LangStringTextType desc : aas.getDescription()) {
                LangString langString = new LangString();
                langString.setLanguage(desc.getLanguage());
                langString.setText(desc.getText());
                descriptions.add(langString);
            }
            mongoAas.setDescriptions(descriptions);
        }
        
        // Convert Administration data
        if (aas.getAdministration() != null) {
            Map<String, String> administration = new HashMap<>();
            administration.put("version", aas.getAdministration().getVersion());
            administration.put("revision", aas.getAdministration().getRevision());
            if (aas.getAdministration().getTemplateId() != null) {
                administration.put("templateId", aas.getAdministration().getTemplateId());
            }
            mongoAas.setAdministration(administration);
        }
        
        // Convert Asset Reference
        if (aas.getAssetInformation() != null && aas.getAssetInformation().getGlobalAssetId() != null) {
            Reference assetRef = new Reference();
            assetRef.setType("GlobalReference");
            
            // Create a single key for the global asset ID
            List<Key> keys = new ArrayList<>();
            Key key = new Key();
            key.setType("GlobalReference");
            key.setValue(aas.getAssetInformation().getGlobalAssetId());
            keys.add(key);
            
            assetRef.setKeys(keys);
            mongoAas.setAssetRef(assetRef);
        }
        
        // Convert Submodel References
        if (aas.getSubmodels() != null) {
            List<Reference> submodelRefs = convertReferences(aas.getSubmodels());
            mongoAas.setSubmodelRefs(submodelRefs);
        }
        
        // Store source and timestamp
        mongoAas.setSourceFile("Synced from BaSyx");
        mongoAas.setImportedAt(LocalDateTime.now());
        
        return mongoAas;
    }

    private SubmodelData convertToSubmodelData(Submodel submodel) {
        SubmodelData submodelData = new SubmodelData();
        
        // Basic fields
        submodelData.setId(submodel.getId());
        submodelData.setIdShort(submodel.getIdShort());
        submodelData.setCategory(submodel.getCategory());
        
        // Convert description to multi-language format
        if (submodel.getDescription() != null && !submodel.getDescription().isEmpty()) {
            List<LangString> descriptions = new ArrayList<>();
            for (LangStringTextType desc : submodel.getDescription()) {
                LangString langString = new LangString();
                langString.setLanguage(desc.getLanguage());
                langString.setText(desc.getText());
                descriptions.add(langString);
            }
            submodelData.setDescription(descriptions);
        }
        
        // Set kind
        submodelData.setKind(submodel.getKind() != null ? submodel.getKind().toString() : null);
        
        // Convert Semantic ID
        if (submodel.getSemanticId() != null) {
            submodelData.setSemanticId(convertReference(submodel.getSemanticId()));
        }
        
        // Convert Supplemental Semantic IDs
        if (submodel.getSupplementalSemanticIds() != null && !submodel.getSupplementalSemanticIds().isEmpty()) {
            submodelData.setSupplementalSemanticIds(convertReferences(submodel.getSupplementalSemanticIds()));
        }
        
        // Convert Administration data
        if (submodel.getAdministration() != null) {
            Map<String, String> administration = new HashMap<>();
            administration.put("version", submodel.getAdministration().getVersion());
            administration.put("revision", submodel.getAdministration().getRevision());
            if (submodel.getAdministration().getTemplateId() != null) {
                administration.put("templateId", submodel.getAdministration().getTemplateId());
            }
            if (submodel.getKind() != null) {
                administration.put("kind", submodel.getKind().toString());
            }
            submodelData.setAdministration(administration);
        }
        
        // Convert Submodel Elements
        if (submodel.getSubmodelElements() != null) {
            List<Map<String, Object>> elements = new ArrayList<>();
            for (SubmodelElement element : submodel.getSubmodelElements()) {
                Map<String, Object> elementData = convertSubmodelElement(element);
                elements.add(elementData);
            }
            submodelData.setSubmodelElements(elements);
        }
        
        // Store source and timestamp
        submodelData.setSourceFile("Synced from BaSyx");
        submodelData.setImportedAt(LocalDateTime.now());
        
        return submodelData;
    }
    
    /**
     * Recursively converts a submodel element to a map representation
     */
    private Map<String, Object> convertSubmodelElement(SubmodelElement element) {
        Map<String, Object> elementData = new HashMap<>();
        
        // Basic fields
        elementData.put("idShort", element.getIdShort());
        elementData.put("modelType", element.getClass().getSimpleName());
        
        // Category
        if (element.getCategory() != null) {
            elementData.put("category", element.getCategory());
        }
        
        // Handle descriptions
        if (element.getDescription() != null && !element.getDescription().isEmpty()) {
            List<Map<String, String>> descriptions = new ArrayList<>();
            for (LangStringTextType desc : element.getDescription()) {
                Map<String, String> langString = new HashMap<>();
                langString.put("language", desc.getLanguage());
                langString.put("text", desc.getText());
                descriptions.add(langString);
            }
            elementData.put("description", descriptions);
        }
        
        // Handle semantic ID
        if (element.getSemanticId() != null) {
            Reference convertedRef = convertReference(element.getSemanticId());
            Map<String, Object> semanticId = new HashMap<>();
            semanticId.put("type", convertedRef.getType());
            semanticId.put("keys", convertedRef.getKeys());
            elementData.put("semanticId", semanticId);
        }
        
        // Handle supplemental semantic IDs
        if (element.getSupplementalSemanticIds() != null && !element.getSupplementalSemanticIds().isEmpty()) {
            List<Map<String, Object>> supplementalIds = new ArrayList<>();
            for (org.eclipse.digitaltwin.aas4j.v3.model.Reference ref : element.getSupplementalSemanticIds()) {
                Reference convertedRef = convertReference(ref);
                Map<String, Object> supplementalId = new HashMap<>();
                supplementalId.put("type", convertedRef.getType());
                supplementalId.put("keys", convertedRef.getKeys());
                supplementalIds.add(supplementalId);
            }
            elementData.put("supplementalSemanticIds", supplementalIds);
        }
        
        // Handle qualifiers
        if (element.getQualifiers() != null && !element.getQualifiers().isEmpty()) {
            List<Map<String, Object>> qualifiers = new ArrayList<>();
            for (Qualifier qualifier : element.getQualifiers()) {
                Map<String, Object> qualifierData = new HashMap<>();
                qualifierData.put("type", qualifier.getType());
                qualifierData.put("value", qualifier.getValue());
                qualifierData.put("valueType", qualifier.getValueType());
                qualifierData.put("kind", qualifier.getKind() != null ? qualifier.getKind().toString() : null);
                
                if (qualifier.getSemanticId() != null) {
                    Reference convertedRef = convertReference(qualifier.getSemanticId());
                    Map<String, Object> semanticId = new HashMap<>();
                    semanticId.put("type", convertedRef.getType());
                    semanticId.put("keys", convertedRef.getKeys());
                    qualifierData.put("semanticId", semanticId);
                }
                
                qualifiers.add(qualifierData);
            }
            elementData.put("qualifiers", qualifiers);
        }
        
        // Handle specific element types
        if (element instanceof Property) {
            Property prop = (Property) element;
            elementData.put("value", prop.getValue());
            elementData.put("valueType", prop.getValueType());
            
            if (prop.getValueId() != null) {
                elementData.put("valueId", convertReference(prop.getValueId()));
            }
        } 
        else if (element instanceof MultiLanguageProperty) {
            MultiLanguageProperty mlProp = (MultiLanguageProperty) element;
            if (mlProp.getValue() != null) {
                List<Map<String, String>> langStrings = new ArrayList<>();
                for (LangStringTextType ls : mlProp.getValue()) {
                    Map<String, String> langString = new HashMap<>();
                    langString.put("language", ls.getLanguage());
                    langString.put("text", ls.getText());
                    langStrings.add(langString);
                }
                elementData.put("value", langStrings);
            }
            if (mlProp.getValueId() != null) {
                elementData.put("valueId", convertReference(mlProp.getValueId()));
            }
        }
        else if (element instanceof Range) {
            Range range = (Range) element;
            Map<String, Object> rangeData = new HashMap<>();
            rangeData.put("min", range.getMin());
            rangeData.put("max", range.getMax());
            rangeData.put("valueType", range.getValueType());
            elementData.put("range", rangeData);
        }
        else if (element instanceof ReferenceElement) {
            ReferenceElement refElement = (ReferenceElement) element;
            if (refElement.getValue() != null) {
                elementData.put("value", convertReference(refElement.getValue()));
            }
        }
        else if (element instanceof File) {
            File file = (File) element;
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("value", file.getValue());
            fileData.put("contentType", file.getContentType());
            elementData.put("file", fileData);
        }
        else if (element instanceof Blob) {
            Blob blob = (Blob) element;
            Map<String, Object> blobData = new HashMap<>();
            blobData.put("value", blob.getValue());
            blobData.put("contentType", blob.getContentType());
            elementData.put("blob", blobData);
        }
        else if (element instanceof RelationshipElement) {
            RelationshipElement rel = (RelationshipElement) element;
            Map<String, Object> relData = new HashMap<>();
            if (rel.getFirst() != null) {
                relData.put("first", convertReference(rel.getFirst()));
            }
            if (rel.getSecond() != null) {
                relData.put("second", convertReference(rel.getSecond()));
            }
            elementData.put("relationship", relData);
        }
        else if (element instanceof AnnotatedRelationshipElement) {
            AnnotatedRelationshipElement arel = (AnnotatedRelationshipElement) element;
            Map<String, Object> arelData = new HashMap<>();
            if (arel.getFirst() != null) {
                arelData.put("first", convertReference(arel.getFirst()));
            }
            if (arel.getSecond() != null) {
                arelData.put("second", convertReference(arel.getSecond()));
            }
            
            // Handle annotations
            if (arel.getAnnotations() != null && !arel.getAnnotations().isEmpty()) {
                List<Map<String, Object>> annotations = new ArrayList<>();
                for (SubmodelElement annotation : arel.getAnnotations()) {
                    annotations.add(convertSubmodelElement(annotation));
                }
                arelData.put("annotations", annotations);
            }
            
            elementData.put("annotatedRelationship", arelData);
        }
        else if (element instanceof SubmodelElementCollection) {
            SubmodelElementCollection collection = (SubmodelElementCollection) element;
            
            // Recursively process all child elements
            if (collection.getValue() != null && !collection.getValue().isEmpty()) {
                List<Map<String, Object>> childElements = new ArrayList<>();
                for (SubmodelElement childElement : collection.getValue()) {
                    childElements.add(convertSubmodelElement(childElement));
                }
                elementData.put("value", childElements);
            }
        }
        else if (element instanceof SubmodelElementList) {
            SubmodelElementList list = (SubmodelElementList) element;
            
            // Store type name
            if (list.getTypeValueListElement() != null) {
                elementData.put("typeValueListElement", list.getTypeValueListElement());
            }
            
            // Recursively process all child elements
            if (list.getValue() != null && !list.getValue().isEmpty()) {
                List<Map<String, Object>> childElements = new ArrayList<>();
                for (SubmodelElement childElement : list.getValue()) {
                    childElements.add(convertSubmodelElement(childElement));
                }
                elementData.put("value", childElements);
            }
        }
        
        return elementData;
    }

    // Helper method to convert BaSyx Keys to MongoDB Keys
    private List<Key> convertKeys(List<org.eclipse.digitaltwin.aas4j.v3.model.Key> basyxKeys) {
        if (basyxKeys == null) return null;
        return basyxKeys.stream()
            .map(basyxKey -> {
                Key key = new Key();
                key.setType(basyxKey.getType().toString());
                key.setValue(basyxKey.getValue());
                return key;
            })
            .collect(Collectors.toList());
    }

    // Helper method to convert BaSyx Reference to MongoDB Reference
    private Reference convertReference(org.eclipse.digitaltwin.aas4j.v3.model.Reference basyxRef) {
        if (basyxRef == null) return null;
        Reference ref = new Reference();
        ref.setType(basyxRef.getType().toString());
        ref.setKeys(convertKeys(basyxRef.getKeys()));
        return ref;
    }

    // Helper method to convert list of BaSyx References to MongoDB References
    private List<Reference> convertReferences(List<org.eclipse.digitaltwin.aas4j.v3.model.Reference> basyxRefs) {
        if (basyxRefs == null) return null;
        return basyxRefs.stream()
            .map(this::convertReference)
            .collect(Collectors.toList());
    }

    public void saveToMongo(ConceptDescription cd) {
        log.info("Saving ConceptDescription with idShort: {} to MongoDB", cd.getIdShort());
        org.example.database.model.ConceptDescription mongoCD = convertToMongoConceptDescription(cd);
        mongoTemplate.save(mongoCD);
    }

    public List<org.example.database.model.ConceptDescription> getAllMongoConceptDescriptions() {
        return mongoTemplate.findAll(org.example.database.model.ConceptDescription.class);
    }
    
    public org.example.database.model.ConceptDescription getMongoConceptDescriptionByIdShort(String idShort) {
        Query query = new Query(Criteria.where("id_short").is(idShort));
        return mongoTemplate.findOne(query, org.example.database.model.ConceptDescription.class);
    }
    
    public void deleteConceptDescription(String conceptDescriptionId) {
        log.info("Deleting ConceptDescription with ID: {} from MongoDB", conceptDescriptionId);
        Query query = new Query(Criteria.where("id").is(conceptDescriptionId));
        mongoTemplate.remove(query, org.example.database.model.ConceptDescription.class);
    }
  
    private org.example.database.model.ConceptDescription convertToMongoConceptDescription(ConceptDescription cd) {
        org.example.database.model.ConceptDescription mongoCD = new org.example.database.model.ConceptDescription();
        
        // Basic fields
        mongoCD.setId(cd.getId());
        mongoCD.setIdShort(cd.getIdShort());
        mongoCD.setCategory(cd.getCategory());
        
        // Convert Administration data
        if (cd.getAdministration() != null) {
            Map<String, String> administration = new HashMap<>();
            
            // Preserve version
            if (cd.getAdministration().getVersion() != null && !cd.getAdministration().getVersion().isEmpty()) {
                administration.put("version", cd.getAdministration().getVersion());
                log.debug("Using version from BaSyx: {}", cd.getAdministration().getVersion());
            } else {
                administration.put("version", "1");
                log.debug("Using default version: 1");
            }
            
            // Preserve revision
            if (cd.getAdministration().getRevision() != null && !cd.getAdministration().getRevision().isEmpty()) {
                administration.put("revision", cd.getAdministration().getRevision());
                log.debug("Using revision from BaSyx: {}", cd.getAdministration().getRevision());
            } else {
                administration.put("revision", "1");
                log.debug("Using default revision: 1");
            }
            
            // Preserve templateId if available
            if (cd.getAdministration().getTemplateId() != null && !cd.getAdministration().getTemplateId().isEmpty()) {
                administration.put("templateId", cd.getAdministration().getTemplateId());
                log.debug("Using templateId from BaSyx: {}", cd.getAdministration().getTemplateId());
            }
            
            mongoCD.setAdministration(administration);
        } else {
            // Check if we already have this concept description in MongoDB to preserve existing admin data
            org.example.database.model.ConceptDescription existingCD = null;
            try {
                Query query = new Query(Criteria.where("id").is(cd.getId()));
                existingCD = mongoTemplate.findOne(query, org.example.database.model.ConceptDescription.class);
            } catch (Exception e) {
                log.warn("Error looking up existing ConceptDescription: {}", e.getMessage());
            }
            
            if (existingCD != null && existingCD.getAdministration() != null && !existingCD.getAdministration().isEmpty()) {
                // Use existing administration data
                mongoCD.setAdministration(existingCD.getAdministration());
                log.debug("Preserving existing administration data from MongoDB");
            } else {
                // Add default administration if not present
                Map<String, String> administration = new HashMap<>();
                administration.put("version", "1");
                administration.put("revision", "1");
                mongoCD.setAdministration(administration);
                log.debug("Using default administration data");
            }
        }
        
        // Convert multi-language descriptions
        if (cd.getDescription() != null && !cd.getDescription().isEmpty()) {
            List<LangString> descriptions = new ArrayList<>();
            for (LangStringTextType desc : cd.getDescription()) {
                LangString langString = new LangString();
                langString.setLanguage(desc.getLanguage());
                langString.setText(desc.getText());
                descriptions.add(langString);
            }
            mongoCD.setDescriptions(descriptions);
        }
        
        // IsCaseOf references
        if (cd.getIsCaseOf() != null) {
            List<Reference> isCaseOfRefs = convertReferences(cd.getIsCaseOf());
            mongoCD.setIsCaseOf(isCaseOfRefs);
        }
        
        // Data specifications
        if (cd.getEmbeddedDataSpecifications() != null) {
            List<Map<String, Object>> embeddedSpecs = new ArrayList<>();
            Map<String, Object> iec61360Data = new HashMap<>();
            
            for (EmbeddedDataSpecification spec : cd.getEmbeddedDataSpecifications()) {
                Map<String, Object> specData = new HashMap<>();
                
                // Convert data specification reference
                if (spec.getDataSpecification() != null) {
                    specData.put("dataSpecification", convertReference(spec.getDataSpecification()));
                }
                
                // Convert content with detailed extraction
                if (spec.getDataSpecificationContent() != null) {
                    Map<String, Object> contentData = new HashMap<>();
                    
                    // Handle IEC61360 data specification
                    if (spec.getDataSpecificationContent() instanceof DataSpecificationIec61360) {
                        DataSpecificationIec61360 iec61360 = (DataSpecificationIec61360) spec.getDataSpecificationContent();
                        
                        // Extract preferred name
                        if (iec61360.getPreferredName() != null && !iec61360.getPreferredName().isEmpty()) {
                            List<Map<String, String>> preferredNames = new ArrayList<>();
                            for (LangStringPreferredNameTypeIec61360 name : iec61360.getPreferredName()) {
                                Map<String, String> langString = new HashMap<>();
                                langString.put("language", name.getLanguage());
                                langString.put("text", name.getText());
                                preferredNames.add(langString);
                            }
                            contentData.put("preferredName", preferredNames);
                            iec61360Data.put("preferredName", preferredNames);
                        }
                        
                        // Extract short name
                        if (iec61360.getShortName() != null && !iec61360.getShortName().isEmpty()) {
                            List<Map<String, String>> shortNames = new ArrayList<>();
                            for (LangStringShortNameTypeIec61360 name : iec61360.getShortName()) {
                                Map<String, String> langString = new HashMap<>();
                                langString.put("language", name.getLanguage());
                                langString.put("text", name.getText());
                                shortNames.add(langString);
                            }
                            contentData.put("shortName", shortNames);
                            iec61360Data.put("shortName", shortNames);
                        }
                        
                        // Extract definition
                        if (iec61360.getDefinition() != null && !iec61360.getDefinition().isEmpty()) {
                            List<Map<String, String>> definitions = new ArrayList<>();
                            for (LangStringDefinitionTypeIec61360 def : iec61360.getDefinition()) {
                                Map<String, String> langString = new HashMap<>();
                                langString.put("language", def.getLanguage());
                                langString.put("text", def.getText());
                                definitions.add(langString);
                            }
                            contentData.put("definition", definitions);
                            iec61360Data.put("definition", definitions);
                        }
                        
                        // Extract data type
                        if (iec61360.getDataType() != null) {
                            contentData.put("dataType", iec61360.getDataType().toString());
                            iec61360Data.put("dataType", iec61360.getDataType().toString());
                        }
                        
                        // Extract unit
                        if (iec61360.getUnit() != null) {
                            contentData.put("unit", iec61360.getUnit());
                            iec61360Data.put("unit", iec61360.getUnit());
                        }
                        
                        // Extract value format
                        if (iec61360.getValueFormat() != null) {
                            contentData.put("valueFormat", iec61360.getValueFormat());
                            iec61360Data.put("valueFormat", iec61360.getValueFormat());
                        }
                    }
                    
                    // Add basic type info
                    contentData.put("type", spec.getDataSpecificationContent().getClass().getSimpleName());
                    specData.put("dataSpecificationContent", contentData);
                }
                
                embeddedSpecs.add(specData);
            }
            
            mongoCD.setEmbeddedDataSpecifications(embeddedSpecs);
            
           
        }
        
        // Metadata
        mongoCD.setSourceFile("Synced from BaSyx");
        mongoCD.setImportedAt(LocalDateTime.now());
        
        return mongoCD;
    }
}
