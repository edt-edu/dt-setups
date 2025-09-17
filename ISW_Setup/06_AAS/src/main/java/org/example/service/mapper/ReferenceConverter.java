package org.example.service.mapper;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.example.database.model.Key;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting between MongoDB references and BaSyx references.
 */
@Component
public class ReferenceConverter {

    /**
     * Converts a MongoDB Reference to a BaSyx Reference
     */
    public Reference convertMongoReferenceToBasyxReference(org.example.database.model.Reference mongoReference) {
        if (mongoReference == null) return null;

        List<org.eclipse.digitaltwin.aas4j.v3.model.Key> basyxKeys = new ArrayList<>();
        
        if (mongoReference.getKeys() != null) {
            for (Key mongoKey : mongoReference.getKeys()) {
                KeyTypes keyType;
                try {
                    keyType = KeyTypes.valueOf(mongoKey.getType());
                } catch (IllegalArgumentException e) {
                    // Default to GLOBAL_REFERENCE if the type is not recognized
                    keyType = KeyTypes.GLOBAL_REFERENCE;
                }
                
                DefaultKey basyxKey = new DefaultKey.Builder()
                        .type(keyType)
                        .value(mongoKey.getValue())
                        .build();
                
                if (mongoKey.getIdType() != null) {
                    try {
                        // Try to set idType if the method exists
                        java.lang.reflect.Method setIdType = 
                            basyxKey.getClass().getMethod("setIdType", String.class);
                        setIdType.invoke(basyxKey, mongoKey.getIdType());
                    } catch (Exception e) {
                        // Skip setting idType if method not available
                    }
                }
                
                basyxKeys.add(basyxKey);
            }
        }
        
        ReferenceTypes type = ReferenceTypes.MODEL_REFERENCE;
        if (mongoReference.getType() != null) {
            try {
                type = ReferenceTypes.valueOf(mongoReference.getType());
            } catch (IllegalArgumentException e) {
                // Default to MODEL_REFERENCE if the type is not recognized
            }
        }
        
        return new DefaultReference.Builder()
                .type(type)
                .keys(basyxKeys)
                .build();
    }

    /**
     * Converts a Map representation of a reference to a BaSyx Reference
     */
    public Reference convertMapToReference(Map<String, Object> referenceMap) {
        if (referenceMap == null) return null;

        // Extract the reference type
        String typeStr = (String) referenceMap.get("type");
        ReferenceTypes type = ReferenceTypes.MODEL_REFERENCE; // Default
        if (typeStr != null) {
            try {
                type = ReferenceTypes.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                // Use default if invalid
            }
        }
        
        // Extract the keys
        List<org.eclipse.digitaltwin.aas4j.v3.model.Key> keys = new ArrayList<>();
        Object keysObj = referenceMap.get("keys");
        if (keysObj instanceof List) {
            List<?> keysList = (List<?>) keysObj;
            
            for (Object keyObj : keysList) {
                // Handle different types of key objects
                if (keyObj instanceof Map) {
                    // Handle Map representation of keys
                    @SuppressWarnings("unchecked")
                    Map<String, Object> keyMap = (Map<String, Object>) keyObj;
                    String keyTypeStr = (String) keyMap.get("type");
                    String keyValue = (String) keyMap.get("value");
                    String idTypeStr = (String) keyMap.get("idType");
                    
                    if (keyTypeStr != null && keyValue != null) {
                        KeyTypes keyType;
                        try {
                            keyType = KeyTypes.valueOf(keyTypeStr);
                        } catch (IllegalArgumentException e) {
                            // Default to GLOBAL_REFERENCE if the type is not recognized
                            keyType = KeyTypes.GLOBAL_REFERENCE;
                        }
                        
                        DefaultKey basyxKey = new DefaultKey.Builder()
                                .type(keyType)
                                .value(keyValue)
                                .build();
                        
                        if (idTypeStr != null) {
                            try {
                                // Try to set idType if the method exists
                                java.lang.reflect.Method setIdType = 
                                    basyxKey.getClass().getMethod("setIdType", String.class);
                                setIdType.invoke(basyxKey, idTypeStr);
                            } catch (Exception e) {
                                // Skip setting idType if method not available
                            }
                        }
                        
                        keys.add(basyxKey);
                    }
                } else if (keyObj instanceof org.example.database.model.Key) {
                    // Handle direct Key objects
                    org.example.database.model.Key mongoKey = (org.example.database.model.Key) keyObj;
                    String keyTypeStr = mongoKey.getType();
                    String keyValue = mongoKey.getValue();
                    String idTypeStr = mongoKey.getIdType();
                    
                    if (keyTypeStr != null && keyValue != null) {
                        KeyTypes keyType;
                        try {
                            keyType = KeyTypes.valueOf(keyTypeStr);
                        } catch (IllegalArgumentException e) {
                            // Default to GLOBAL_REFERENCE if the type is not recognized
                            keyType = KeyTypes.GLOBAL_REFERENCE;
                        }
                        
                        DefaultKey basyxKey = new DefaultKey.Builder()
                                .type(keyType)
                                .value(keyValue)
                                .build();
                        
                        if (idTypeStr != null) {
                            try {
                                // Try to set idType if the method exists
                                java.lang.reflect.Method setIdType = 
                                    basyxKey.getClass().getMethod("setIdType", String.class);
                                setIdType.invoke(basyxKey, idTypeStr);
                            } catch (Exception e) {
                                // Skip setting idType if method not available
                            }
                        }
                        
                        keys.add(basyxKey);
                    }
                }
            }
        }
        
        DefaultReference reference = new DefaultReference.Builder()
            .type(type)
            .keys(keys)
            .build();
            
        // Set referredSemanticId to null to match BaSyx format
        try {
            java.lang.reflect.Method setReferredSemanticId = 
                reference.getClass().getMethod("setReferredSemanticId", Reference.class);
            setReferredSemanticId.invoke(reference, (Reference)null);
        } catch (Exception e) {
            // Skip if method not available
        }
        
        return reference;
    }
    
    /**
     * Converts a MongoDB Reference to a Map representation
     * This is useful for cases where the BaSyx API expects a Map
     */
    public Map<String, Object> convertReferenceToMap(org.example.database.model.Reference reference) {
        if (reference == null) return Collections.emptyMap();
        
        Map<String, Object> result = new java.util.HashMap<>();
        
        if (reference.getType() != null) {
            result.put("type", reference.getType());
        }
        
        if (reference.getKeys() != null && !reference.getKeys().isEmpty()) {
            List<Map<String, Object>> keysList = new ArrayList<>();
            
            for (Key key : reference.getKeys()) {
                Map<String, Object> keyMap = new java.util.HashMap<>();
                keyMap.put("type", key.getType());
                keyMap.put("value", key.getValue());
                
                if (key.getIdType() != null) {
                    keyMap.put("idType", key.getIdType());
                }
                
                keysList.add(keyMap);
            }
            
            result.put("keys", keysList);
        }
        
        return result;
    }
}
