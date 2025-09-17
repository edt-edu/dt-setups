package de.unistuttgart.isw.query.responses;

import java.util.List;
import java.util.function.Function;

import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext.Service;

public class DescribeResponse {

    private ServiceDescription service;
    private ContextDescription context;

    private DescribeResponse() {
    }

    public ServiceDescription getService() {
        return service;
    }

    public ContextDescription getContext() {
        return context;
    }

    public static Builder builder() {
        return new Builder();
    }

    


    /**
     * Creates a DescribeResponse object with a ServiceDescription built using the provided function.
     *
     * @param service a function that takes a ServiceDescription.Builder and returns a modified ServiceDescription.Builder
     * @return a DescribeResponse object with the built ServiceDescription, or null if the service function is null
     */
    public static DescribeResponse service(Function<ServiceDescription.Builder, ServiceDescription.Builder> service) {
        if(service != null ){
            ServiceDescription sd = service.apply(ServiceDescription.builder()).build();
            DescribeResponse dr = new DescribeResponse();
            dr.service = sd;
            return dr;
        }
        return null;
    }

    /**
     * Creates a DescribeResponse object with a ContextDescription built using the provided function.
     *
     * @param context a function that takes a ContextDescription.Builder and returns a modified ContextDescription.Builder
     * @return a DescribeResponse object with the built ContextDescription, or null if the context function is null
     */
    public static DescribeResponse context(Function<ContextDescription.Builder, ContextDescription.Builder> context) {
        if(context != null ){
            ContextDescription cd = context.apply(ContextDescription.builder()).build();
            DescribeResponse dr = new DescribeResponse();
            dr.context = cd;
            return dr;
        }
        return null;
    }

    public static class Builder {
        private ServiceDescription service;
        private ContextDescription context;

        public DescribeResponse service(Function<ServiceDescription.Builder, ServiceDescription.Builder> service) {
            this.service = service.apply(ServiceDescription.builder()).build();
            return this.build();
        }

        public DescribeResponse context(Function<ContextDescription.Builder, ContextDescription.Builder> context) {
            this.context = context.apply(ContextDescription.builder()).build();
            return this.build();
        }

        public DescribeResponse build() {
            DescribeResponse dr = new DescribeResponse();
            dr.service = service;
            dr.context = context;
            return dr;
        }
    }



    public static class ServiceDescription {
        private String name;
        private String description;
        private String version;
        private String endpoint;
        private List<String> fields;
        private String type;

        public ServiceDescription(String name, String description, String version, String endpoint, List<String> fields, String type) {
            this.name = name;
            this.description = description;
            this.version = version;
            this.endpoint = endpoint;
            this.fields = fields;
            this.type = type;
        }

        private ServiceDescription() {
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getVersion() {
            return version;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public List<String> getFields() {
            return fields;
        }

        public String getType() {
            return type;
        }


        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private String description;
            private String version;
            private String endpoint;
            private List<String> fields;
            private String type;

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setDescription(String description) {
                this.description = description;
                return this;
            }

            public Builder setVersion(String version) {
                this.version = version;
                return this;
            }

            public Builder setEndpoint(String endpoint) {
                this.endpoint = endpoint;
                return this;
            }

            public Builder setFields(List<String> fields) {
                this.fields = fields;
                return this;
            }

            public Builder setType(String type) {
                this.type = type;
                return this;
            }

            public ServiceDescription build() {
                ServiceDescription sd = new ServiceDescription();
                sd.name = name;
                sd.description = description;
                sd.version = version;
                sd.endpoint = endpoint;
                sd.fields = fields;
                sd.type = type;
                return sd;
            }
        }     
    }

    public static class ContextDescription {
        private List<Service> services;
        private String name;

        public ContextDescription(List<Service> services, String name) {
            this.services = services;
            this.name = name;
        }

        private ContextDescription() {
        }


        public List<Service> getServices() {
            return services;
        }

        public String getName() {
            return name;
        }


        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private List<Service> services;
            private String name;

            public Builder setServices(List<Service> services) {
                this.services = services;
                return this;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public ContextDescription build() {
                ContextDescription cd = new ContextDescription();
                cd.services = services;
                cd.name = name;
                return cd;
            }
        }

    }

    
}
