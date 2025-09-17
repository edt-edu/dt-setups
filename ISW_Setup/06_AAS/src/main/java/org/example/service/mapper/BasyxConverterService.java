package org.example.service.mapper;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.example.database.model.MongoAssetAdministrationShell;
import org.example.database.model.SubmodelData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that coordinates the conversion between MongoDB models and BaSyx models.
 * This service delegates to specialized converters for each model type.
 */
@Service
public class BasyxConverterService {

    private final MongoToAASConverter aasConverter;
    private final MongoToSubmodelConverter submodelConverter;
    private final MongoToConceptDescriptionConverter conceptDescriptionConverter;

    @Autowired
    public BasyxConverterService(
            MongoToAASConverter aasConverter,
            MongoToSubmodelConverter submodelConverter,
            MongoToConceptDescriptionConverter conceptDescriptionConverter) {
        this.aasConverter = aasConverter;
        this.submodelConverter = submodelConverter;
        this.conceptDescriptionConverter = conceptDescriptionConverter;
    }

    /**
     * Converts a MongoDB AAS to a BaSyx AAS
     */
    public AssetAdministrationShell convertToBasyxAAS(MongoAssetAdministrationShell mongoAAS) {
        return aasConverter.convert(mongoAAS);
    }

    /**
     * Converts a MongoDB SubmodelData to a BaSyx Submodel
     */
    public Submodel convertToBasyxSubmodel(SubmodelData submodelData) {
        return submodelConverter.convert(submodelData);
    }

    /**
     * Converts a MongoDB ConceptDescription to a BaSyx ConceptDescription
     */
    public ConceptDescription convertToBasyxConceptDescription(org.example.database.model.ConceptDescription mongoCD) {
        return conceptDescriptionConverter.convert(mongoCD);
    }
}
