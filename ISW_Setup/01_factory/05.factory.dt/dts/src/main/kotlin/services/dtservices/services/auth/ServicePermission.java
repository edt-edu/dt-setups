package services.dtservices.services.auth;

import java.util.List;

public class ServicePermission {

    private final ServiceValue service;
    private final List<ServiceOperation> operations;
    private final String path;

    public ServicePermission(ServiceValue service, List<ServiceOperation> operations, String path) {
        this.service = service;
        this.operations = operations;
        this.path = path;
    }

    /**
     * Get the service
     * @return
     */
    public ServiceValue getService() {
        return service;
    }

    /**
     * Get the operations
     * @return
     */
    public List<ServiceOperation> getOperations() {
        return operations;
    }

    /**
     * Get the path
     * @return
     */
    public String getPath() {
        return path;
    }
    
}
