package org.example.aasManager;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.ConceptDescriptionRepository;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.MissingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.example.database.MongoAASManager;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AASManager {


    private final ConnectedAasRepository aasRepository;
    private final ConnectedSubmodelRepository submodelRepository;
    private final ConceptDescriptionRepository conceptDescriptionRepository;
    private final MongoAASManager mongoAASManager;


    @Autowired
    public AASManager(ConnectedAasRepository aasRepository, ConnectedSubmodelRepository submodelRepository, 
                     ConceptDescriptionRepository conceptDescriptionRepository, MongoAASManager mongoAASManager) {
        this.aasRepository = aasRepository;
        this.submodelRepository = submodelRepository;
        this.conceptDescriptionRepository = conceptDescriptionRepository;
        this.mongoAASManager = mongoAASManager;
    }

    //Create an AAS in the BaSyx repository
    public void createAAS(AssetAdministrationShell aas){
        validateAAS(aas);
        validateSubmodelReference(aas);
        log.info("Creating AAS with Id: {}", aas.getIdShort());
        try {
            aasRepository.createAas(aas);
        }catch(CollidingIdentifierException e){
            String msg = String.format("AAS with ID %s already exists", aas.getId());
            log.error(msg);
            throw new AASOperationException(msg, e);
        }catch (Exception e){
            String msg = String.format("Failed to create AAS %s", aas.getIdShort());
            log.error(msg, e);
            throw new AASOperationException(e.getMessage(), e);
        }
    }

    //Get an AAS from the BaSyx repository
    public AssetAdministrationShell getAAS(String aasId){
        log.info("Getting AAS with Id: {}", aasId);
        try {
            return aasRepository.getAas(aasId);
        }catch (ElementDoesNotExistException e){
            log.warn("AAS with Id: {} does not exist", aasId);
            return null;
        }
    }

    //Get all AAS from repository
    public CursorResult<List<AssetAdministrationShell>> getAllAAS(PaginationInfo paginationInfo){
        log.info("Getting all AAS");
        return aasRepository.getAllAas(paginationInfo);
    }

    //Delete an AAS from the BaSyx repository
    public void deleteAAS(String aasId){
        log.info("Deleting AAS with Id: {}", aasId);
        try {
            aasRepository.deleteAas(aasId);
        }catch (Exception e){
            String msg = String.format("Failed to delete AAS %s", aasId);
            log.error(msg, e);
            throw new AASOperationException(msg, e);
        }
    }

    //Update an AAS
    public void updateAAS(String aasId, AssetAdministrationShell aas){
        validateAAS(aas);
        log.info("Updating AAS with Id: {}", aas.getIdShort());
        try {
            aasRepository.updateAas(aasId, aas);
        }catch (Exception e){
            String msg = String.format("Failed to update AAS %s", aasId);
            log.error(msg, e);
            throw new AASOperationException(msg, e);
        }
    }


    ////////Submodels methods////////////////////////

    //Create a submodel
    public void createSubmodel(Submodel submodel){
        validateSubmodel(submodel);
        log.info("Creating submodel with Id {}", submodel.getIdShort());
        try{
            submodelRepository.createSubmodel(submodel);
        }catch (CollidingIdentifierException e){
            throw new AASOperationException(String.format("Submodel %s already exists", submodel.getIdShort()), e);
        }catch (MissingIdentifierException e){
            throw new AASOperationException("Submodel needs a valid identifier", e);
        }
    }

    //Delete a submodel
    public void deleteSubmodel(String submodelId){
        log.info("Deleting Submodel with Id: {}", submodelId);
        try{
            submodelRepository.deleteSubmodel(submodelId);
        }catch (ElementDoesNotExistException e){
            throw new AASOperationException(String.format("Submodel %s does not exist", submodelId), e);
        }
    }

    //Add a submodel to an AAS
    public void linkSubmodelToAAS(String submodelId, String aasId){
        log.info("Linking submodel {} to AAS with Id {}", submodelId, aasId);
        try{
            AssetAdministrationShell aas = aasRepository.getAas(aasId);
            addSubmodelReference(aas, submodelId);
            aasRepository.updateAas(aasId,aas);
        }catch (Exception e){
            String msg = String.format("Failed to link submodel %s to AAS %s", submodelId, aasId);
            log.error(msg, e);
            throw new AASOperationException(msg, e);
        }
    }

    //Remove a submodel from an AAS
    public void unlinkSubmodelFromAAS(String submodelId, String aasId){
        log.info("Unlinking submodel {} from AAS with Id {}", submodelId, aasId);
        try{
            AssetAdministrationShell aas = aasRepository.getAas(aasId);
            removeSubmodelReference(aas, submodelId);
            aasRepository.updateAas(aasId,aas);
        }catch (Exception e){
            String msg = String.format("Failed to unlink submodel %s to AAS %s", submodelId, aasId);
            log.error(msg, e);
            throw new AASOperationException(msg, e);
        }
    }

    //Get a Submodel
    public Submodel getSubmodel(String submodelId){
        log.info("Getting submodel with Id {}", submodelId);
        try{
            return submodelRepository.getSubmodel(submodelId);
        }catch (ElementDoesNotExistException e){
            return null;
        }
    }

    //Get all submodels
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo paginationInfo){
        log.info("Getting all submodels");
        return submodelRepository.getAllSubmodels(paginationInfo);
    }

    //Update a submodel
    public void updateSubmodel(String submodelId, Submodel submodel){
        validateSubmodel(submodel);
        log.info("Updating submodel with Id {}", submodel.getIdShort());
        try{
            submodelRepository.updateSubmodel(submodelId, submodel);
        }catch (Exception e){
            String msg = String.format("Failed to update submodel %s", submodelId);
            log.error(msg, e);
            throw new AASOperationException(msg, e);
        }
    }


    ////////////////Helpers///////////////////////////
    private void validateAAS(AssetAdministrationShell aas){
        if(!StringUtils.hasText(aas.getId())){
            throw new IllegalArgumentException("AAS must have a valid id");
        }

        if(!StringUtils.hasText(aas.getIdShort())){
            throw new IllegalArgumentException("AAS must have an idShort");
        }
    }

    private void validateSubmodelReference(AssetAdministrationShell aas){
        getSubmodelIds(aas).forEach(submodelId -> {
            if(!submodelExists(submodelId)){
                throw new AASOperationException("Submodel with id " + submodelId + " does not exist");
            }
        });
    }


    private List<String> getSubmodelIds(AssetAdministrationShell aas){
        return aas.getSubmodels().stream()
                .map(ref -> ref.getKeys().getFirst().getValue())
                .collect(Collectors.toList());
    }

    private boolean submodelExists(String submodelId){
        try{
            submodelRepository.getSubmodel(submodelId);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private void validateSubmodel(Submodel submodel){
        if(!StringUtils.hasText(submodel.getIdShort())){
            throw new IllegalArgumentException("Submodel must have a valid id");
        }
    }

    //Add a submodel reference to an AAS
    private void addSubmodelReference(AssetAdministrationShell aas, String submodelId){
        List<Reference> references = aas.getSubmodels() == null ?
                new ArrayList<>() :
                new ArrayList<>(aas.getSubmodels());

        references.add(createReference(submodelId));
        aas.setSubmodels(references);
    }


    //Create a reference for the submodel
    private Reference createReference(String submodelId){
        Key key = new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value(submodelId).build();

        return new DefaultReference.Builder().keys(List.of(key)).build();
    }

    //remove a submodel reference from an AAS
    private void removeSubmodelReference(AssetAdministrationShell aas, String submodelId){
        List<Reference> references = aas.getSubmodels();
        if(references.isEmpty()) {
            return;
        }
        references.remove(createReference(submodelId));
        aas.setSubmodels(references);
    }


    //////Concept Description Methods///////////////////
    public void createConceptDescription(ConceptDescription conceptDescription){
        try {
            conceptDescriptionRepository.createConceptDescription(conceptDescription);
            mongoAASManager.saveToMongo(conceptDescription);
        }catch(CollidingIdentifierException e){
            throw new AASOperationException("Concept description already exists", e);
        }catch(MissingIdentifierException e){
            throw new AASOperationException("Error creating concept description", e);
        }
    }


    public void updateConceptDescription(String conceptDescriptionId, ConceptDescription conceptDescription){
        try {
            conceptDescriptionRepository.updateConceptDescription(conceptDescriptionId, conceptDescription);
        }catch(ElementDoesNotExistException e){
            throw new AASOperationException("Concept description doesn't exist", e);
        }
    }

    public ConceptDescription getConceptDescription(String conceptDescriptionId) {
        try {
            return conceptDescriptionRepository.getConceptDescription(conceptDescriptionId);
        } catch (ElementDoesNotExistException e) {
            throw new AASOperationException("Concept description doesn't exist", e);
        }
    }

    public CursorResult<List<ConceptDescription>> getAllConceptDescriptions(PaginationInfo paginationInfo){
        return conceptDescriptionRepository.getAllConceptDescriptions(paginationInfo);
    }



}
