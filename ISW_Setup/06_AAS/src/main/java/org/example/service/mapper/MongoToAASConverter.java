package org.example.service.mapper;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.example.database.model.LangString;
import org.example.database.model.MongoAssetAdministrationShell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter for transforming MongoDB AAS objects to BaSyx AAS objects.
 */
@Component
public class MongoToAASConverter {

    private final ReferenceConverter referenceConverter;

    @Autowired
    public MongoToAASConverter(ReferenceConverter referenceConverter) {
        this.referenceConverter = referenceConverter;
    }

    /**
     * Converts a MongoDB AAS to a BaSyx AAS
     */
    public AssetAdministrationShell convert(MongoAssetAdministrationShell mongoAAS) {
        if (mongoAAS == null) return null;

        DefaultAssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id(mongoAAS.getId())
                .idShort(mongoAAS.getIdShort())
                .build();

        // Set empty collections to match BaSyx format
        aas.setEmbeddedDataSpecifications(Collections.emptyList());
        aas.setExtensions(Collections.emptyList());
        
        // These methods may not be available in your version of BaSyx
        // If they cause errors, you can comment them out
        try {
            java.lang.reflect.Method setSupplementalSemanticIds = 
                aas.getClass().getMethod("setSupplementalSemanticIds", List.class);
            setSupplementalSemanticIds.invoke(aas, new ArrayList<>());
            
            java.lang.reflect.Method setQualifiers = 
                aas.getClass().getMethod("setQualifiers", List.class);
            setQualifiers.invoke(aas, new ArrayList<>());
        } catch (Exception e) {
            // Methods not available in this version of BaSyx, ignore
            System.out.println("Some methods not available in this BaSyx version: " + e.getMessage());
        }
        
        aas.setDisplayName(Collections.emptyList());

        // Convert descriptions if available
        if (mongoAAS.getDescriptions() != null) {
            aas.setDescription(convertLangStrings(mongoAAS.getDescriptions()));
        }

        // Convert assetRef if available
        if (mongoAAS.getAssetRef() != null) {
            aas.setAssetInformation(convertAssetRef(mongoAAS.getAssetRef()));
        }

        // Convert submodelRefs if available
        if (mongoAAS.getSubmodelRefs() != null) {
            List<Reference> submodelRefs = convertMongoReferencesToBasyxReferences(mongoAAS.getSubmodelRefs());
            aas.setSubmodels(submodelRefs);
        }

        // Add administration if available
        if (mongoAAS.getAdministration() != null) {
            Map<String, String> adminMap = mongoAAS.getAdministration();
            DefaultAdministrativeInformation adminInfo = new DefaultAdministrativeInformation();
            
            if (adminMap.containsKey("version")) {
                adminInfo.setVersion(adminMap.get("version").toString());
            }
            
            if (adminMap.containsKey("revision")) {
                adminInfo.setRevision(adminMap.get("revision").toString());
            }
            
            // Set empty embeddedDataSpecifications to match BaSyx format
            adminInfo.setEmbeddedDataSpecifications(Collections.emptyList());
            
            aas.setAdministration(adminInfo);
        }

        return aas;
    }

    /**
     * Helper method to convert language strings
     */
    private List<LangStringTextType> convertLangStrings(List<LangString> langStrings) {
        if (langStrings == null) return Collections.emptyList();
        
        return langStrings.stream()
                .map(ls -> new DefaultLangStringTextType.Builder()
                        .language(ls.getLanguage())
                        .text(ls.getText())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert asset reference to asset information
     */
    private AssetInformation convertAssetRef(org.example.database.model.Reference assetRef) {
        if (assetRef == null) return null;

        DefaultAssetInformation assetInfo = new DefaultAssetInformation.Builder()
                .assetKind(AssetKind.TYPE) // Use TYPE to match BaSyx repository format
                .build();
        
        // Set the globalAssetId - handle both string and reference formats
        if (assetRef.getKeys() != null && !assetRef.getKeys().isEmpty()) {
            // First try to extract a string value for older BaSyx versions
            String globalAssetIdStr = assetRef.getKeys().get(0).getValue();
            if (globalAssetIdStr != null) {
                try {
                    // Try to set as string first (for older BaSyx versions)
                    java.lang.reflect.Method setGlobalAssetIdStr = 
                        assetInfo.getClass().getMethod("setGlobalAssetId", String.class);
                    setGlobalAssetIdStr.invoke(assetInfo, globalAssetIdStr);
                } catch (Exception e) {
                    // If string method not available, try with Reference object
                    try {
                        Reference globalAssetIdRef = referenceConverter.convertMongoReferenceToBasyxReference(assetRef);
                        java.lang.reflect.Method setGlobalAssetIdRef = 
                            assetInfo.getClass().getMethod("setGlobalAssetId", Reference.class);
                        setGlobalAssetIdRef.invoke(assetInfo, globalAssetIdRef);
                    } catch (Exception e2) {
                        System.out.println("Could not set globalAssetId: " + e2.getMessage());
                    }
                }
            }
            
            // Set assetType to "Type" to match BaSyx repository format
            assetInfo.setAssetType("Type");
            
            // Extract globalAssetId from reference if available
            String value = assetRef.getKeys().get(0).getValue();
            if (value != null) {
                // This is already handled above, but we ensure it's set correctly
                try {
                    assetInfo.setGlobalAssetId(value);
                } catch (Exception e) {
                    System.out.println("Could not set globalAssetId as string: " + e.getMessage());
                }
            }
        }
        
        return assetInfo;
    }
    
    /**
     * Helper method to convert submodel references
     */
    private List<Reference> convertMongoReferencesToBasyxReferences(List<org.example.database.model.Reference> submodelRefs) {
        if (submodelRefs == null) return Collections.emptyList();
        
        return submodelRefs.stream()
                .map(referenceConverter::convertMongoReferenceToBasyxReference)
                .collect(Collectors.toList());
    }
    
    // These methods have been moved to the ReferenceConverter class
}
