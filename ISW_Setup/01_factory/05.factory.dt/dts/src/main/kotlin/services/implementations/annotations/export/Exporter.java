package services.implementations.annotations.export;

public interface Exporter {
    
    /**
     * called when a data type is passed to the exporter
     * @param type
     */
    void exportType(ExportDataType type);

    /**
     * called when a service type is passed to the exporter
     * @param serviceType
     */
    void exportServiceType(ExportServiceTypeDef serviceType);

    /**
     * called when all types are passed to the exporter
     */
    void finished();
}
