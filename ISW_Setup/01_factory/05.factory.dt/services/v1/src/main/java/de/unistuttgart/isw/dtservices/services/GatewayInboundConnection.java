package de.unistuttgart.isw.dtservices.services;

import de.unistuttgart.isw.dts.services.TServiceRequest;

public interface GatewayInboundConnection {
        /*
     * This method is called when a message is received
     */
    public void onMessage(String input);

    /*
     * This method is called when a message is received and parsed
     */
    public void onParsedMessage(TServiceRequest serviceRequest);
}
