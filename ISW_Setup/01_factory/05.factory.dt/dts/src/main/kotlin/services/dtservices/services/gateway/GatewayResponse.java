package services.dtservices.services.gateway;

import java.util.Map;

public class GatewayResponse {
    private final String serviceName;
    private final Map<String, ResponseType> response;

    public GatewayResponse(String serviceName, Map<String, ResponseType> response) {
        this.serviceName = serviceName;
        this.response = response;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Map<String, ResponseType> getResponse() {
        return response;
    }


    public static class ResponseType {
        private final String type;
        private final String value;

        public ResponseType(String type, Object value) {
            this.type = type;
            this.value = (String) value;
        }
        public String getType() {
            return type;
        }
        public Object getValue() {
            return value;
        }
    }

}