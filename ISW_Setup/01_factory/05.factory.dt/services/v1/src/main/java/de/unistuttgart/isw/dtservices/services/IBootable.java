package de.unistuttgart.isw.dtservices.services;

import de.unistuttgart.isw.dtservices.configs.Config;

public interface IBootable {
    public void start();

    public void setConfig(Config config);

    public void setGatewayOutboundConnection(GatewayOutboundConnection gatewayOutboundConnection); 
}
