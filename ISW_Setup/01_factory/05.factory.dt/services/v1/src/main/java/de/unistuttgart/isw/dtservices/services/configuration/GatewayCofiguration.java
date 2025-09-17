package de.unistuttgart.isw.dtservices.services.configuration;

import java.util.List;

import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext;
import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext.Service;

public interface GatewayCofiguration {
    

    List<GatewayContext> getGatewayContext();

    String getDefaultContext();

    Service findService(String serviceName);

    List<String> getServices(final String context);

    List<String> getContexts();



}
