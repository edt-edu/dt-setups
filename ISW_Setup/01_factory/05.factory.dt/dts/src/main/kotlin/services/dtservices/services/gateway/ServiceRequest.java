package services.dtservices.services.gateway;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import services.dtservices.services.gateway.operations.Call;
import services.dtservices.services.query.*;


/**
 * This request is sent by the gateway to the service to operate on the service.
 */
public class ServiceRequest {

    @JsonProperty("select")
    private SelectionOperation select;
    @JsonProperty("insert")
    private InsertableOperation insert;
    @JsonProperty("update")
    private UpdateOperation update;
    @JsonProperty("delete")
    private DeleteOperation delete;
    @JsonProperty("call")
    private Call call;

    

    public ServiceRequest() {
    }

    /*
     * gets the select operation
     */
    @JsonIgnore
    public Optional<SelectionOperation> getSelect() {
        return Optional.of(select);
    }

    /*
     * gets the insert operation
     */
    @JsonIgnore
    public Optional<InsertableOperation> getInsert() {
        return Optional.of(insert);
    }

    /*
     * gets the update operation
     */
    @JsonIgnore
    public Optional<UpdateOperation> getUpdate() {
        return Optional.of(update);
    }

    /*
     * gets the delete operation
     */
    @JsonIgnore
    public Optional<DeleteOperation> getDelete() {
        return Optional.of(delete);
    }

    /*
     * gets the call operation
     */
    @JsonIgnore
    public Optional<CallOperation> getCall() {
        return Optional.of((CallOperation) call);
    }
    
}
