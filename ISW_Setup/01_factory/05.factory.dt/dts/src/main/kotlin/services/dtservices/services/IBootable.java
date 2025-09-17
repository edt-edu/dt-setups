package services.dtservices.services;

import services.dtservices.configs.Config;

public interface IBootable {
    public void start();

    public void setConfig(Config config);

    public void setGatewayOutboundConnection(GatewayOutboundConnection gatewayOutboundConnection);
}
