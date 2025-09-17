package services.dtservices.services.configuration.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;



public class GatewayContext {


    private Gateway gateway;

    private DtConfig digitalTwin;

    private Auth auth;

    private Map<String, Service> services;

    private String name;



    public GatewayContext(String name, Gateway gateway, Auth auth, Map<String, Service> services){
        this(name, gateway, auth, services, null);
    }
    public GatewayContext(String name, Gateway gateway, Auth auth, Map<String, Service> services, DtConfig digitalTwin) {
        this.gateway = gateway;
        this.auth = auth;
        this.services = services;
        this.name = name;
        this.digitalTwin = digitalTwin;
    }

    private GatewayContext() {
    }

    public Gateway getGateway() {
        return gateway;
    }

    public Auth getAuth() {
        return auth;
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public String getName() {
        return name;
    }

    public DtConfig getDigitalTwin() {
        return digitalTwin;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Gateway gateway;
        private Auth auth;
        private Map<String, Service> services;
        private String name;
        private DtConfig digitalTwin;

        public Builder setGateway(Gateway gateway) {
            this.gateway = gateway;
            return this;
        }

        public Builder setAuth(Auth auth) {
            this.auth = auth;
            return this;
        }

        public Builder setDigitalTwin(DtConfig digitalTwin) {
            this.digitalTwin = digitalTwin;
            return this;
        }

        public Builder setServices(Map<String, Service> services) {
            this.services = services;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public GatewayContext build() {
            return new GatewayContext(name, gateway, auth, services, digitalTwin);
        }
    }

   
    public static class DtConfig{
        private String host;
        private int port;
        private boolean useHttps;
        private String rabbitMQConnection;
        // used to consume events for the services
        private String inboundQueue;
        // used to publish events to the dt engine
        private String outboundQueue;
        private boolean allowEvents;

        public DtConfig(String host, int port, boolean useHttps, String rabbitMQConnection, String inboundQueue, String outboundQueue, boolean allowEvents) {
            this.host = host;
            this.port = port;
            this.useHttps = useHttps;
            this.rabbitMQConnection = rabbitMQConnection;
            this.inboundQueue = inboundQueue;
            this.outboundQueue = outboundQueue;
            this.allowEvents = allowEvents;
        }

        public DtConfig(){
            this.useHttps = false;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isUseHttps() {
            return useHttps;
        }

        public String getRabbitMQConnection() {
            return rabbitMQConnection;
        }

        public String getInboundQueue() {
            return inboundQueue;
        }

        public String getOutboundQueue() {
            return outboundQueue;
        }

        public boolean isAllowEvents() {
            return allowEvents;
        }
    }

    public static class Gateway {
        private String[] activate;

        private Gateway() {
        }

        public Gateway(String[] activate) {
            this.activate = activate;
        }

        public String[] getActivate() {
            return activate;
        }
    }

    public static class Auth {
        private Map<String, Issuer> issuer; 
        private Rules rules;


        private Auth() {
        }

        public Auth(Map<String, Issuer> issuer, Rules rules) {
            this.issuer = issuer;
            this.rules = rules;
        }

        public Map<String, Issuer> getIssuer() {
            return issuer;
        }

        public Rules getRules() {
            return rules;
        }
    }

    public static class Issuer {
        private String service;

        private Issuer() {
        }

        public Issuer(String service) {
            this.service = service;
        }

        public String getService() {
            return this.service;
        }
    }

    public static class Rules {
        private final String[] files;

        @JsonProperty("static")
        private final Map<String, String[]> staticRecords;

        private Rules() {
            this.files = null;
            this.staticRecords = null;
        }

        public Rules(String[] files, Map<String, String[]> staticRecords) {
            this.files = files;
            this.staticRecords = staticRecords;
        }

        public String[] getFiles() {
            return files;
        }

        public Map<String, String[]> getStatic() {
            return staticRecords;
        }
    }

    public static class Service{

        private String host;
        private int port;
        private boolean useHttps;

      

        public Service(String host, int port, boolean useHttps) {
            this.host = host;
            this.port = port;
            this.useHttps = useHttps;
        }

        public Service(){
            this.useHttps = false;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isUseHttps() {
            return useHttps;
        }

        protected void setHost(String host) {
            this.host = host;
        }

        protected void setPort(int port) {
            this.port = port;
        }

        protected void setUseHttps(boolean useHttps) {
            this.useHttps = useHttps;
        }

    }
        
    
}

