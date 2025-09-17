package de.unistuttgart.isw.models;

import java.util.function.Function;

public class DTModelManager implements DtEngineConnector {

    @Override
    public void createModel(DtModel dtModel, Function<DtModel, DtModel> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateModel(DtModel dtModel, Function<DtModel, DtModel> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fetchModelBySerialNumber(String serialNumber, Function<DtModel, DtModel> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fetchModelByUri(String uri, Function<DtModel, DtModel> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
