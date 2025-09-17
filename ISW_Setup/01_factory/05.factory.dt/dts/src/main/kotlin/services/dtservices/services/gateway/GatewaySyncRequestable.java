package services.dtservices.services.gateway;


public interface GatewaySyncRequestable {
    /**
     * Sends a request to the gateway and waits for a response.
     *
     * @param request the request to send
     * @return the response from the gateway
     */
    GatewayResponse sendRequest(GatewayRequest request);

    /**
     * Sends a query to the gateway and waits for a response. It accepts the query language as input
     *
     * @param Query the query to send
     * @return the response from the gateway
     */
    GatewayResponse sendRequest(final String Query);
}