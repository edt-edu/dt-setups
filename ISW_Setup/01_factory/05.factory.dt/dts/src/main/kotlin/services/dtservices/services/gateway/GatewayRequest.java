package services.dtservices.services.gateway;



public class GatewayRequest {
    final String serviceName;
    final ServiceRequest request;

    public GatewayRequest(String serviceName, ServiceRequest request) {
        this.serviceName = serviceName;
        this.request = request;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceRequest getRequest() {
        return request;
    }
}