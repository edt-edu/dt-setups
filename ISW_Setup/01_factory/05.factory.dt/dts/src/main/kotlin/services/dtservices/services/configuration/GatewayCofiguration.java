package services.dtservices.services.configuration;


import services.dtservices.services.configuration.gateway.GatewayContext;

import java.util.List;

public interface GatewayCofiguration {
    

    List<GatewayContext> getGatewayContext();

    String getDefaultContext();

    GatewayContext.Service findService(String serviceName);

    List<String> getServices(final String context);

    List<String> getContexts();



}
