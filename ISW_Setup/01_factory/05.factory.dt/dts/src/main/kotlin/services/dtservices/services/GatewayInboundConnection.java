package services.dtservices.services;


import de.unistuttgart.isw.dts.services.TServiceRequest;
import services.dtservices.configs.Config;

public interface GatewayInboundConnection {
        /*
     * This method is called when a message is received
     */
    public String onMessage(String input);

    /*
     * This method is called when a message is received and parsed
     */
    public TServiceRequest onParsedMessage(TServiceRequest serviceRequest);

    void setConfig(Config config);

}
