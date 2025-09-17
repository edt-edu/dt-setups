package services.util.routing;

public class ServiceRoute {
    private String serviceName;
    private String serviceUri;

    public ServiceRoute(String serviceName, String serviceUri) {
        this.serviceName = serviceName;
        this.serviceUri = serviceUri;
    }

    /**
     * Get the name of the service
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the URI of the service
     * @return
     */
    public String getServiceUri() {
        return serviceUri;
    }
}
