package de.unistuttgart.isw.models;

import java.util.function.Function;

/**
 * This interface handles the events send to the DT engine to update and create new models or fetch a model
 */
public interface DtEngineConnector {

    /**
     * This method is used to create a new model in the DT engine
     * @param dtModel the model to be created
     * @param callback the callback function to be called after the model is created
     */
    void createModel(DtModel dtModel, Function<DtModel, DtModel> callback);

    /**
     * This method is used to update an existing model in the DT engine
     * @param dtModel the model to be updated
     * @param callback the callback function to be called after the model is updated
     */
    void updateModel(DtModel dtModel, Function<DtModel, DtModel> callback);

    /**
     * This method is used to fetch a model from the DT engine
     * @param serialNumber the serial number of the model to be fetched
     * @param callback the callback function to be called after the model is fetched
     * @return the model with the given serial number
     */
    void fetchModelBySerialNumber(String serialNumber, Function<DtModel, DtModel> callback);

    /**
     * This method is used to fetch a model from the DT engine
     * @param uri the uri of the model to be fetched
     * @param callback the callback function to be called after the model is fetched
     * @return the model with the given uri
     */
    void fetchModelByUri(String uri, Function<DtModel, DtModel> callback);



    
}
