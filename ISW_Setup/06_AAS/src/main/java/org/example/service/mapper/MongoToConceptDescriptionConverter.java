package org.example.service.mapper;

import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultDataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.database.model.LangString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter for transforming MongoDB ConceptDescription objects to BaSyx ConceptDescription objects.
 */
@Component
public class MongoToConceptDescriptionConverter {
    
    private static final Logger log = LoggerFactory.getLogger(MongoToConceptDescriptionConverter.class);

    private final ReferenceConverter referenceConverter;

    @Autowired
    public MongoToConceptDescriptionConverter(ReferenceConverter referenceConverter) {
        this.referenceConverter = referenceConverter;
    }

    /**
     * Converts a MongoDB ConceptDescription to a BaSyx ConceptDescription
     */
    public ConceptDescription convert(org.example.database.model.ConceptDescription mongoCD) {
        if (mongoCD == null) return null;

        DefaultConceptDescription cd = new DefaultConceptDescription.Builder()
                .id(mongoCD.getId())
                .idShort(mongoCD.getIdShort())
                .build();

        // Set empty collections to match BaSyx format
        cd.setExtensions(new ArrayList<>());
        // Note: setSupplementalSemanticIds and setQualifiers are not available in this version of BaSyx
        cd.setDisplayName(new ArrayList<>());
        
        // Add administration information if available in MongoDB
        if (mongoCD.getAdministration() != null && !mongoCD.getAdministration().isEmpty()) {
            Map<String, String> adminMap = mongoCD.getAdministration();
            DefaultAdministrativeInformation adminInfo = new DefaultAdministrativeInformation();
            
            if (adminMap.containsKey("version")) {
                adminInfo.setVersion(adminMap.get("version"));
            } else {
                adminInfo.setVersion("1");
            }
            
            if (adminMap.containsKey("revision")) {
                adminInfo.setRevision(adminMap.get("revision"));
            } else {
                adminInfo.setRevision("1");
            }
            
            if (adminMap.containsKey("templateId")) {
                adminInfo.setTemplateId(adminMap.get("templateId"));
            }
            
            adminInfo.setEmbeddedDataSpecifications(new ArrayList<>());
            cd.setAdministration(adminInfo);
            log.debug("Using existing administration data from MongoDB");
        } else {
            // Add default administration information if not available
            DefaultAdministrativeInformation adminInfo = new DefaultAdministrativeInformation();
            adminInfo.setVersion("1");
            adminInfo.setRevision("1");
            adminInfo.setEmbeddedDataSpecifications(new ArrayList<>());
            cd.setAdministration(adminInfo);
            log.debug("Using default administration data");
        }

        // Convert category if available
        if (mongoCD.getCategory() != null) {
            cd.setCategory(mongoCD.getCategory());
        }

        // Convert descriptions if available
        if (mongoCD.getDescriptions() != null) {
            cd.setDescription(convertLangStrings(mongoCD.getDescriptions()));
        }

        // Convert isCaseOf if available
        if (mongoCD.getIsCaseOf() != null && !mongoCD.getIsCaseOf().isEmpty()) {
            List<Reference> isCaseOf = mongoCD.getIsCaseOf().stream()
                    .map(referenceConverter::convertMongoReferenceToBasyxReference)
                    .collect(Collectors.toList());
            cd.setIsCaseOf(isCaseOf);
        }

        // Convert data specifications if available
        if (mongoCD.getDataSpecifications() != null && !mongoCD.getDataSpecifications().isEmpty()) {
            List<org.example.database.model.Reference> dataSpecs = mongoCD.getDataSpecifications();
            List<Reference> basyxReferences = new ArrayList<>();
            
            for (org.example.database.model.Reference dataSpec : dataSpecs) {
                Reference reference = referenceConverter.convertMongoReferenceToBasyxReference(dataSpec);
                if (reference != null) {
                    basyxReferences.add(reference);
                }
            }
            
            if (!basyxReferences.isEmpty()) {
                cd.setIsCaseOf(basyxReferences);
            }
        }
        
        // Handle embedded data specifications
        if (mongoCD.getEmbeddedDataSpecifications() != null && !mongoCD.getEmbeddedDataSpecifications().isEmpty()) {
            List<EmbeddedDataSpecification> basyxSpecs = new ArrayList<>();
            
            for (Object embeddedSpecObj : mongoCD.getEmbeddedDataSpecifications()) {
                try {
                    log.debug("Processing embedded data specification: {}", embeddedSpecObj);
                    
                    // Create a new builder
                    DefaultEmbeddedDataSpecification.Builder builder = new DefaultEmbeddedDataSpecification.Builder();
                    DefaultDataSpecificationIec61360 iec61360 = new DefaultDataSpecificationIec61360();
                    
                    // Handle different types of embedded specifications
                    if (embeddedSpecObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> embeddedSpec = (Map<String, Object>) embeddedSpecObj;
                        
                        // Handle data specification reference
                        if (embeddedSpec.containsKey("dataSpecification")) {
                            Object dataSpecObj = embeddedSpec.get("dataSpecification");
                            Reference reference = null;
                            
                            if (dataSpecObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> dataSpecMap = (Map<String, Object>) dataSpecObj;
                                reference = referenceConverter.convertMapToReference(dataSpecMap);
                            } else if (dataSpecObj instanceof org.example.database.model.Reference) {
                                reference = referenceConverter.convertMongoReferenceToBasyxReference(
                                        (org.example.database.model.Reference) dataSpecObj);
                            }
                            
                            if (reference != null) {
                                builder.dataSpecification(reference);
                                log.debug("Added data specification reference: {}", reference);
                            }
                        }
                        
                        // Handle data specification content
                        if (embeddedSpec.containsKey("dataSpecificationContent")) {
                            Object contentObj = embeddedSpec.get("dataSpecificationContent");
                            
                            if (contentObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> contentMap = (Map<String, Object>) contentObj;
                                log.debug("Processing data specification content: {}", contentMap);
                                
                                // Handle definition
                                if (contentMap.containsKey("definition")) {
                                    try {
                                        @SuppressWarnings("unchecked")
                                        List<Map<String, String>> definitionList = (List<Map<String, String>>) contentMap.get("definition");
                                        if (definitionList != null && !definitionList.isEmpty()) {
                                            List<LangStringDefinitionTypeIec61360> definitions = new ArrayList<>();
                                            for (Map<String, String> langMap : definitionList) {
                                                DefaultLangStringDefinitionTypeIec61360 langString = new DefaultLangStringDefinitionTypeIec61360.Builder()
                                                        .language(langMap.get("language"))
                                                        .text(langMap.get("text"))
                                                        .build();
                                                definitions.add(langString);
                                            }
                                            iec61360.setDefinition(definitions);
                                            log.debug("Added definition to IEC61360 data specification");
                                        }
                                    } catch (Exception e) {
                                        log.warn("Error processing definition: {}", e.getMessage());
                                    }
                                }
                                
                                // Handle preferred name
                                if (contentMap.containsKey("preferredName")) {
                                    try {
                                        @SuppressWarnings("unchecked")
                                        List<Map<String, String>> preferredNameList = (List<Map<String, String>>) contentMap.get("preferredName");
                                        if (preferredNameList != null && !preferredNameList.isEmpty()) {
                                            List<LangStringPreferredNameTypeIec61360> names = new ArrayList<>();
                                            for (Map<String, String> langMap : preferredNameList) {
                                                DefaultLangStringPreferredNameTypeIec61360 langString = new DefaultLangStringPreferredNameTypeIec61360.Builder()
                                                        .language(langMap.get("language"))
                                                        .text(langMap.get("text"))
                                                        .build();
                                                names.add(langString);
                                            }
                                            iec61360.setPreferredName(names);
                                            log.debug("Added preferred name to IEC61360 data specification");
                                        }
                                    } catch (Exception e) {
                                        log.warn("Error processing preferred name: {}", e.getMessage());
                                    }
                                }
                                
                                // Handle short name
                                if (contentMap.containsKey("shortName")) {
                                    try {
                                        @SuppressWarnings("unchecked")
                                        List<Map<String, String>> shortNameList = (List<Map<String, String>>) contentMap.get("shortName");
                                        if (shortNameList != null && !shortNameList.isEmpty()) {
                                            List<LangStringShortNameTypeIec61360> names = new ArrayList<>();
                                            for (Map<String, String> langMap : shortNameList) {
                                                DefaultLangStringShortNameTypeIec61360 langString = new DefaultLangStringShortNameTypeIec61360.Builder()
                                                        .language(langMap.get("language"))
                                                        .text(langMap.get("text"))
                                                        .build();
                                                names.add(langString);
                                            }
                                            iec61360.setShortName(names);
                                            log.debug("Added short name to IEC61360 data specification");
                                        }
                                    } catch (Exception e) {
                                        log.warn("Error processing short name: {}", e.getMessage());
                                    }
                                }
                            }
                        }
                    } else if (embeddedSpecObj instanceof org.example.database.model.Reference) {
                        // If the embedded spec is a direct Reference object
                        org.example.database.model.Reference mongoRef = (org.example.database.model.Reference) embeddedSpecObj;
                        Reference basyxRef = referenceConverter.convertMongoReferenceToBasyxReference(mongoRef);
                        
                        if (basyxRef != null) {
                            builder.dataSpecification(basyxRef);
                            log.debug("Added reference as data specification: {}", basyxRef);
                        }
                    }
                    
                    // Always set the content - even if it's just an empty IEC61360 container
                    builder.dataSpecificationContent(iec61360);
                    
                    // Add to the list
                    basyxSpecs.add(builder.build());
                } catch (Exception e) {
                    log.error("Error converting embedded data specification: " + e.getMessage(), e);
                }
            }
            
            // Set the specifications
            if (!basyxSpecs.isEmpty()) {
                cd.setEmbeddedDataSpecifications(basyxSpecs);
            }
        }
        
        return cd;
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
}
