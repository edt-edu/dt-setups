package org.example.service.mapper;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.QualifierKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultQualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.example.database.model.LangString;
import org.example.database.model.SubmodelData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter for transforming MongoDB Submodel objects to BaSyx Submodel objects.
 * Handles complex nested structures including submodel elements, qualifiers, and references.
 */
@Component
public class MongoToSubmodelConverter {

    private final ReferenceConverter referenceConverter;

    @Autowired
    public MongoToSubmodelConverter(ReferenceConverter referenceConverter) {
        this.referenceConverter = referenceConverter;
    }

    /**
     * Converts a MongoDB SubmodelData to a BaSyx Submodel
     */
    public Submodel convert(SubmodelData submodelData) {
        if (submodelData == null) return null;

        DefaultSubmodel submodel = new DefaultSubmodel.Builder()
                .id(submodelData.getId())
                .idShort(submodelData.getIdShort())
                .build();

        // Set empty collections to match BaSyx format
        submodel.setEmbeddedDataSpecifications(new ArrayList<>());
        submodel.setExtensions(new ArrayList<>());
        submodel.setSupplementalSemanticIds(new ArrayList<>());
        submodel.setQualifiers(new ArrayList<>());
        submodel.setDisplayName(new ArrayList<>());

        // Convert descriptions if available
        if (submodelData.getDescription() != null) {
            submodel.setDescription(convertLangStrings(submodelData.getDescription()));
        }

        // Set kind if available
        if (submodelData.getKind() != null) {
            try {
                submodel.setKind(org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind.valueOf(submodelData.getKind()));
            } catch (IllegalArgumentException e) {
                // Default to INSTANCE if invalid
                submodel.setKind(org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind.INSTANCE);
            }
        }

        // Convert semanticId if available
        if (submodelData.getSemanticId() != null) {
            Reference semanticId = referenceConverter.convertMongoReferenceToBasyxReference(submodelData.getSemanticId());
            submodel.setSemanticId(semanticId);
        }
        
        // Convert supplementalSemanticIds if available
        if (submodelData.getSupplementalSemanticIds() != null && !submodelData.getSupplementalSemanticIds().isEmpty()) {
            List<Reference> supplementalSemanticIds = submodelData.getSupplementalSemanticIds().stream()
                .map(referenceConverter::convertMongoReferenceToBasyxReference)
                .collect(Collectors.toList());
            submodel.setSupplementalSemanticIds(supplementalSemanticIds);
        }

        // Convert administration if available
        if (submodelData.getAdministration() != null) {
            Map<String, String> adminMap = submodelData.getAdministration();
            DefaultAdministrativeInformation adminInfo = new DefaultAdministrativeInformation();
            
            if (adminMap.containsKey("version")) {
                adminInfo.setVersion(adminMap.get("version").toString());
            }
            
            if (adminMap.containsKey("revision")) {
                adminInfo.setRevision(adminMap.get("revision").toString());
            }
            
            // Set templateId if available
            if (adminMap.containsKey("templateId")) {
                try {
                    // Try to set templateId if the method exists
                    java.lang.reflect.Method setTemplateId = 
                        adminInfo.getClass().getMethod("setTemplateId", String.class);
                    setTemplateId.invoke(adminInfo, adminMap.get("templateId"));
                } catch (Exception e) {
                    // Skip setting templateId if method not available
                    System.out.println("Could not set templateId: " + e.getMessage());
                }
            } else {
                // Set default templateId to match BaSyx repository format
                try {
                    java.lang.reflect.Method setTemplateId = 
                        adminInfo.getClass().getMethod("setTemplateId", String.class);
                    setTemplateId.invoke(adminInfo, "https://admin-shell.io/IDTA 02006-3-0");
                } catch (Exception e) {
                    // Skip setting templateId if method not available
                }
            }
            
            // Set empty embeddedDataSpecifications to match BaSyx format
            adminInfo.setEmbeddedDataSpecifications(new ArrayList<>());
            
            submodel.setAdministration(adminInfo);
        }

        // Convert submodel elements if available
        if (submodelData.getSubmodelElements() != null) {
            List<SubmodelElement> submodelElements = convertSubmodelElements(submodelData.getSubmodelElements());
            submodel.setSubmodelElements(submodelElements);
        }

        return submodel;
    }

    /**
     * Helper method to convert language strings
     */
    private List<LangStringTextType> convertLangStrings(List<LangString> langStrings) {
        if (langStrings == null) return null;
        
        return langStrings.stream()
                .map(ls -> new DefaultLangStringTextType.Builder()
                        .language(ls.getLanguage())
                        .text(ls.getText())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to convert supplementalSemanticIds from a map to BaSyx References
     */
    private List<Reference> convertSupplementalSemanticIds(Map<String, Object> elementMap) {
        if (elementMap == null || !elementMap.containsKey("supplementalSemanticIds")) {
            return new ArrayList<>();
        }
        
        Object supplementalSemanticIdsObj = elementMap.get("supplementalSemanticIds");
        if (!(supplementalSemanticIdsObj instanceof List)) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Object> supplementalSemanticIdsList = (List<Object>) supplementalSemanticIdsObj;
        List<Reference> supplementalSemanticIds = new ArrayList<>();
        
        for (Object supplementalSemanticIdObj : supplementalSemanticIdsList) {
            if (supplementalSemanticIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> supplementalSemanticIdMap = (Map<String, Object>) supplementalSemanticIdObj;
                Reference supplementalSemanticId = referenceConverter.convertMapToReference(supplementalSemanticIdMap);
                if (supplementalSemanticId != null) {
                    supplementalSemanticIds.add(supplementalSemanticId);
                }
            }
        }
        
        return supplementalSemanticIds;
    }
    
    /**
     * Helper method to extract description from MongoDB data
     */
    private List<LangStringTextType> extractDescription(Map<String, Object> elementMap) {
        if (elementMap == null || !elementMap.containsKey("description")) {
            return new ArrayList<>();
        }
        
        Object descriptionObj = elementMap.get("description");
        if (!(descriptionObj instanceof List)) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, String>> descriptionList = (List<Map<String, String>>) descriptionObj;
        
        List<LangStringTextType> result = new ArrayList<>();
        for (Map<String, String> desc : descriptionList) {
            if (desc.containsKey("language") && desc.containsKey("text")) {
                result.add(new DefaultLangStringTextType.Builder()
                    .language(desc.get("language"))
                    .text(desc.get("text"))
                    .build());
            }
        }
        
        return result;
    }
    
    /**
     * Helper method to apply description and supplementalSemanticIds to any builder
     * This is a generic method that works with all submodel element builders
     */
    private void applyDescriptionAndSemanticIds(Object builder, Map<String, Object> elementMap) {
        // Handle description
        if (elementMap.containsKey("description")) {
            List<LangStringTextType> description = extractDescription(elementMap);
            try {
                java.lang.reflect.Method descriptionMethod = 
                    builder.getClass().getMethod("description", List.class);
                descriptionMethod.invoke(builder, description);
            } catch (Exception e) {
                System.out.println("Could not set description: " + e.getMessage());
            }
        }
        
        // Handle supplementalSemanticIds
        if (elementMap.containsKey("supplementalSemanticIds")) {
            List<Reference> supplementalSemanticIds = convertSupplementalSemanticIds(elementMap);
            try {
                java.lang.reflect.Method semanticIdsMethod = 
                    builder.getClass().getMethod("supplementalSemanticIds", List.class);
                semanticIdsMethod.invoke(builder, supplementalSemanticIds);
            } catch (Exception e) {
                System.out.println("Could not set supplementalSemanticIds: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize a builder with common properties including description and supplementalSemanticIds
     * This reduces duplicate code across converter methods
     */
    private <T> void initializeBuilder(T builder, Map<String, Object> sourceMap) {
        try {
            // Set empty collections
            java.lang.reflect.Method embeddedMethod = 
                builder.getClass().getMethod("embeddedDataSpecifications", List.class);
            embeddedMethod.invoke(builder, new ArrayList<>());
            
            java.lang.reflect.Method extensionsMethod = 
                builder.getClass().getMethod("extensions", List.class);
            extensionsMethod.invoke(builder, new ArrayList<>());
            
            java.lang.reflect.Method displayNameMethod = 
                builder.getClass().getMethod("displayName", List.class);
            displayNameMethod.invoke(builder, new ArrayList<>());
            
            // Apply description and supplementalSemanticIds
            applyDescriptionAndSemanticIds(builder, sourceMap);
        } catch (Exception e) {
            System.out.println("Error initializing builder: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert MongoDB submodel elements to BaSyx submodel elements
     */
    private List<SubmodelElement> convertSubmodelElements(List<Map<String, Object>> submodelElements) {
        if (submodelElements == null) return null;
        
        List<SubmodelElement> result = new ArrayList<>();
        
        for (Map<String, Object> element : submodelElements) {
            String modelType = (String) element.get("modelType");
            String type = (String) element.get("$type");
            
            // Use $type if available, otherwise fall back to modelType
            String elementType = type != null ? type : modelType;
            
            if (elementType == null) continue;
            
            SubmodelElement submodelElement = null;
            
            // Handle different types of submodel elements based on elementType
            switch (elementType) {
                case "Property":
                case "DefaultProperty":
                    submodelElement = convertProperty(element);
                    break;
                case "SubmodelElementCollection":
                case "DefaultSubmodelElementCollection":
                    submodelElement = convertSubmodelElementCollection(element);
                    break;
                case "MultiLanguageProperty":
                case "DefaultMultiLanguageProperty":
                    submodelElement = convertMultiLanguageProperty(element);
                    break;
                case "File":
                case "DefaultFile":
                    submodelElement = convertFile(element);
                    break;
                case "SubmodelElementList":
                case "DefaultSubmodelElementList":
                    submodelElement = convertSubmodelElementList(element);
                    break;
                default:
                    // For unknown types, try to convert as Property
                    submodelElement = convertProperty(element);
                    break;
            }
            
            if (submodelElement != null) {
                result.add(submodelElement);
            }
        }
        
        return result;
    }

    /**
     * Helper method to convert a property from Map to BaSyx Property
     */
    private Property convertProperty(Map<String, Object> propertyMap) {
        if (propertyMap == null) return null;
        
        String idShort = (String) propertyMap.get("idShort");
        String category = (String) propertyMap.get("category");
        Object value = propertyMap.get("value");
        String valueType = (String) propertyMap.get("valueType");
        
        DefaultProperty.Builder builder = new DefaultProperty.Builder()
                .idShort(idShort);
        
        // Initialize builder with common properties
        initializeBuilder(builder, propertyMap);
        
        if (category != null) {
            builder.category(category);
        }
        
        // Handle value and valueType
        if (value != null) {
            builder.value(value.toString());
        }
        
        if (valueType != null) {
            try {
                builder.valueType(DataTypeDefXsd.valueOf(valueType));
            } catch (IllegalArgumentException e) {
                // Default to STRING if invalid
                builder.valueType(DataTypeDefXsd.STRING);
            }
        }
        
        // Handle semantic ID if present
        if (propertyMap.containsKey("semanticId")) {
            Object semanticIdObj = propertyMap.get("semanticId");
            if (semanticIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                builder.semanticId(semanticId);
            }
        }
        
        // Handle qualifiers if present
        if (propertyMap.containsKey("qualifiers")) {
            Object qualifiersObj = propertyMap.get("qualifiers");
            if (qualifiersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> qualifiersList = (List<Map<String, Object>>) qualifiersObj;
                List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> basyxQualifiers = 
                    convertQualifiers(qualifiersList);
                if (basyxQualifiers != null && !basyxQualifiers.isEmpty()) {
                    builder.qualifiers(basyxQualifiers);
                }
            }
        } else {
            builder.qualifiers(new ArrayList<>());
        }
        
        Property property = builder.build();
        
        // Set $type field to match BaSyx format
        try {
            java.lang.reflect.Method setTypeMethod = 
                property.getClass().getMethod("set$type", String.class);
            setTypeMethod.invoke(property, "Property");
            
            // Set valueId to null to match BaSyx format
            java.lang.reflect.Method setValueId = 
                property.getClass().getMethod("setValueId", Reference.class);
            setValueId.invoke(property, (Reference)null);
        } catch (Exception e) {
            // If the method doesn't exist, we'll skip it
            System.out.println("Could not set $type or valueId for Property: " + e.getMessage());
        }
        
        return property;
    }

    /**
     * Converts a SubmodelElementCollection from Map to BaSyx SubmodelElementCollection
     */
    private org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection convertSubmodelElementCollection(Map<String, Object> collectionMap) {
        if (collectionMap == null) return null;
        
        String idShort = (String) collectionMap.get("idShort");
        String category = (String) collectionMap.get("category");
        
        DefaultSubmodelElementCollection.Builder builder = 
            new DefaultSubmodelElementCollection.Builder()
                .idShort(idShort);
        
        // Initialize builder with common properties
        initializeBuilder(builder, collectionMap);
        
        if (category != null) {
            builder.category(category);
        }
        
        // Handle semantic ID if present
        if (collectionMap.containsKey("semanticId")) {
            Object semanticIdObj = collectionMap.get("semanticId");
            if (semanticIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                builder.semanticId(semanticId);
            }
        }
        
        // Handle qualifiers if present
        if (collectionMap.containsKey("qualifiers")) {
            Object qualifiersObj = collectionMap.get("qualifiers");
            if (qualifiersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> qualifiersList = (List<Map<String, Object>>) qualifiersObj;
                List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> basyxQualifiers = 
                    convertQualifiers(qualifiersList);
                if (basyxQualifiers != null && !basyxQualifiers.isEmpty()) {
                    builder.qualifiers(basyxQualifiers);
                }
            }
        } else {
            builder.qualifiers(new ArrayList<>());
        }
        
        // Handle nested elements (value)
        if (collectionMap.containsKey("value")) {
            Object valueObj = collectionMap.get("value");
            if (valueObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> valueList = (List<Map<String, Object>>) valueObj;
                List<SubmodelElement> nestedElements = convertSubmodelElements(valueList);
                
                if (nestedElements != null && !nestedElements.isEmpty()) {
                    builder.value(nestedElements);
                }
            }
        }
        
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection collection = builder.build();
        
        // Set $type field to match BaSyx format
        try {
            java.lang.reflect.Method setTypeMethod = 
                collection.getClass().getMethod("set$type", String.class);
            setTypeMethod.invoke(collection, "SubmodelElementCollection");
        } catch (Exception e) {
            // If the method doesn't exist, we'll skip it
            System.out.println("Could not set $type for SubmodelElementCollection: " + e.getMessage());
        }
        
        return collection;
    }

    /**
     * Converts a MultiLanguageProperty from Map to BaSyx MultiLanguageProperty
     */
    private org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty convertMultiLanguageProperty(Map<String, Object> propertyMap) {
        if (propertyMap == null) return null;
        
        String idShort = (String) propertyMap.get("idShort");
        String category = (String) propertyMap.get("category");
        
        DefaultMultiLanguageProperty.Builder builder = 
            new DefaultMultiLanguageProperty.Builder()
                .idShort(idShort);
        
        // Initialize builder with common properties
        initializeBuilder(builder, propertyMap);
        
        if (category != null) {
            builder.category(category);
        }
        
        // Handle semantic ID if present
        if (propertyMap.containsKey("semanticId")) {
            Object semanticIdObj = propertyMap.get("semanticId");
            if (semanticIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                builder.semanticId(semanticId);
            }
        }
        
        // Handle qualifiers if present
        if (propertyMap.containsKey("qualifiers")) {
            Object qualifiersObj = propertyMap.get("qualifiers");
            if (qualifiersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> qualifiersList = (List<Map<String, Object>>) qualifiersObj;
                List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> basyxQualifiers = 
                    convertQualifiers(qualifiersList);
                if (basyxQualifiers != null && !basyxQualifiers.isEmpty()) {
                    builder.qualifiers(basyxQualifiers);
                }
            }
        } else {
            builder.qualifiers(new ArrayList<>());
        }
        
        // Handle value (language strings)
        if (propertyMap.containsKey("value")) {
            Object valueObj = propertyMap.get("value");
            if (valueObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> langStringList = (List<Map<String, String>>) valueObj;
                List<LangStringTextType> langStrings = new ArrayList<>();
                
                for (Map<String, String> langString : langStringList) {
                    String language = langString.get("language");
                    String text = langString.get("text");
                    if (language != null && text != null) {
                        langStrings.add(new DefaultLangStringTextType.Builder()
                                .language(language)
                                .text(text)
                                .build());
                    }
                }
                
                if (!langStrings.isEmpty()) {
                    builder.value(langStrings);
                }
            }
        }
        
        org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty property = builder.build();
        
        // Set $type field to match BaSyx format
        try {
            java.lang.reflect.Method setTypeMethod = 
                property.getClass().getMethod("set$type", String.class);
            setTypeMethod.invoke(property, "MultiLanguageProperty");
        } catch (Exception e) {
            // If the method doesn't exist, we'll skip it
            System.out.println("Could not set $type for MultiLanguageProperty: " + e.getMessage());
        }
        
        return property;
    }

    /**
     * Converts a File element from Map to BaSyx File
     */
    private org.eclipse.digitaltwin.aas4j.v3.model.File convertFile(Map<String, Object> fileMap) {
        if (fileMap == null) return null;
        
        String idShort = (String) fileMap.get("idShort");
        String category = (String) fileMap.get("category");
        String contentType = null;
        String value = null;
        
        // Check for nested 'file' object structure (BaSyx format)
        if (fileMap.containsKey("file") && fileMap.get("file") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileObj = (Map<String, Object>) fileMap.get("file");
            contentType = (String) fileObj.get("contentType");
            value = (String) fileObj.get("value");
        } else {
            // Try direct properties (fallback)
            contentType = (String) fileMap.get("contentType");
            value = (String) fileMap.get("value");
        }
        
        DefaultFile.Builder builder = 
            new DefaultFile.Builder()
                .idShort(idShort);
        
        // Initialize builder with common properties
        initializeBuilder(builder, fileMap);
        
        if (category != null) {
            builder.category(category);
        }
        
        if (contentType != null) {
            builder.contentType(contentType);
        } else {
            // Default to application/octet-stream only if no content type is found
            builder.contentType("application/octet-stream");
        }
        
        if (value != null) {
            builder.value(value);
        }
        
        // Handle semantic ID if present
        if (fileMap.containsKey("semanticId")) {
            Object semanticIdObj = fileMap.get("semanticId");
            if (semanticIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                builder.semanticId(semanticId);
            }
        }
        
        // Handle qualifiers if present
        if (fileMap.containsKey("qualifiers")) {
            Object qualifiersObj = fileMap.get("qualifiers");
            if (qualifiersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> qualifiersList = (List<Map<String, Object>>) qualifiersObj;
                List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> basyxQualifiers = 
                    convertQualifiers(qualifiersList);
                if (basyxQualifiers != null && !basyxQualifiers.isEmpty()) {
                    builder.qualifiers(basyxQualifiers);
                }
            }
        } else {
            builder.qualifiers(new ArrayList<>());
        }
        
        org.eclipse.digitaltwin.aas4j.v3.model.File file = builder.build();
        
        // Set $type field to match BaSyx format
        try {
            java.lang.reflect.Method setTypeMethod = 
                file.getClass().getMethod("set$type", String.class);
            setTypeMethod.invoke(file, "File");
            
            // Try to set valueId to null to match BaSyx format
            try {
                java.lang.reflect.Method setValueId = 
                    file.getClass().getMethod("setValueId", Reference.class);
                setValueId.invoke(file, (Reference)null);
            } catch (Exception valueIdEx) {
                // Skip if method not available
            }
        } catch (Exception e) {
            // If the method doesn't exist, we'll skip it
            System.out.println("Could not set $type for File: " + e.getMessage());
        }
        
        return file;
    }
    private org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList convertSubmodelElementList(Map<String, Object> listMap) {
        if (listMap == null) return null;
        
        String idShort = (String) listMap.get("idShort");
        String category = (String) listMap.get("category");
        Boolean orderRelevant = (Boolean) listMap.get("orderRelevant");
        String typeValueListElement = (String) listMap.get("typeValueListElement");
        
        DefaultSubmodelElementList.Builder builder = 
            new DefaultSubmodelElementList.Builder()
                .idShort(idShort);
        
        // Initialize builder with common properties
        initializeBuilder(builder, listMap);
        
        if (category != null) {
            builder.category(category);
        }
        
        if (orderRelevant != null) {
            builder.orderRelevant(orderRelevant);
        }
        
        if (typeValueListElement != null) {
            try {
                AasSubmodelElements enumValue = AasSubmodelElements.valueOf(typeValueListElement);
                builder.typeValueListElement(enumValue);
            } catch (IllegalArgumentException e) {
                // Log error or handle the case where the string doesn't match any enum value
                if (listMap.containsKey("semanticId")) {
                    Object semanticIdObj = listMap.get("semanticId");
                    if (semanticIdObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                        Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                        builder.semanticIdListElement(semanticId);
                    }
                }
            }
        }
        
        // Handle semanticId separately (in addition to semanticIdListElement)
        if (listMap.containsKey("semanticId")) {
            Object semanticIdObj = listMap.get("semanticId");
            if (semanticIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                builder.semanticId(semanticId);
            }
        }
        
        // Handle qualifiers if present
        if (listMap.containsKey("qualifiers")) {
            Object qualifiersObj = listMap.get("qualifiers");
            if (qualifiersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> qualifiersList = (List<Map<String, Object>>) qualifiersObj;
                List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> basyxQualifiers = 
                    convertQualifiers(qualifiersList);
                if (basyxQualifiers != null && !basyxQualifiers.isEmpty()) {
                    builder.qualifiers(basyxQualifiers);
                }
            }
        } else {
            builder.qualifiers(new ArrayList<>());
        }
        
        // Handle nested elements (value)
        if (listMap.containsKey("value")) {
            Object valueObj = listMap.get("value");
            if (valueObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> valueList = (List<Map<String, Object>>) valueObj;
                List<SubmodelElement> nestedElements = convertSubmodelElements(valueList);
                
                if (nestedElements != null && !nestedElements.isEmpty()) {
                    builder.value(nestedElements);
                }
            }
        }
        
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList list = builder.build();
        
        // Set $type field to match BaSyx format
        try {
            java.lang.reflect.Method setTypeMethod = 
                list.getClass().getMethod("set$type", String.class);
            setTypeMethod.invoke(list, "SubmodelElementList");
        } catch (Exception e) {
            // If the method doesn't exist, we'll skip it
            System.out.println("Could not set $type for SubmodelElementList: " + e.getMessage());
        }
        
        return list;
    }

    /**
     * Converts a list of qualifier maps to BaSyx Qualifiers
     */
    private List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> convertQualifiers(List<Map<String, Object>> qualifiersList) {
        if (qualifiersList == null || qualifiersList.isEmpty()) return Collections.emptyList();
        
        List<org.eclipse.digitaltwin.aas4j.v3.model.Qualifier> result = new ArrayList<>();
        
        for (Map<String, Object> qualifierMap : qualifiersList) {
            String kind = (String) qualifierMap.get("kind");
            String type = (String) qualifierMap.get("type");
            String valueType = (String) qualifierMap.get("valueType");
            Object valueObj = qualifierMap.get("value");
            String value = valueObj != null ? valueObj.toString() : null;
            
            if (type != null) {
                DefaultQualifier.Builder builder = 
                    new DefaultQualifier.Builder()
                        .type(type);
                
                if (kind != null) {
                    try {
                        QualifierKind qualifierKind = QualifierKind.valueOf(kind);
                        builder.kind(qualifierKind);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid kind
                        System.out.println("Invalid qualifier kind: " + kind);
                    }
                }
                
                if (valueType != null) {
                    try {
                        builder.valueType(DataTypeDefXsd.valueOf(valueType));
                    } catch (IllegalArgumentException e) {
                        // Default to STRING if invalid
                        builder.valueType(DataTypeDefXsd.STRING);
                    }
                }
                
                if (value != null) {
                    builder.value(value);
                }
                
                // Handle semantic ID if present
                if (qualifierMap.containsKey("semanticId")) {
                    Object semanticIdObj = qualifierMap.get("semanticId");
                    if (semanticIdObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> semanticIdMap = (Map<String, Object>) semanticIdObj;
                        Reference semanticId = referenceConverter.convertMapToReference(semanticIdMap);
                        builder.semanticId(semanticId);
                    }
                }
                
                // Set empty supplementalSemanticIds to match BaSyx format
                builder.supplementalSemanticIds(new ArrayList<>());
                
                result.add(builder.build());
            }
        }
        
        return result;
    }
}
