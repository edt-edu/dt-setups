package services.dtservices.services.query.query.responses;

import java.util.List;

public class ListResponse {

    protected List<String> services;
    protected List<String> contexts;


    private ListResponse() {
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public static ListResponseBuilder builder() {
        return new ListResponseBuilder();
    }

    public static class ListResponseBuilder {
        private List<String> services;
        private List<String> contexts;

        public ListResponseBuilder setServices(List<String> services) {
            this.services = services;
            return this;
        }

        public ListResponseBuilder setContexts(List<String> contexts) {
            this.contexts = contexts;
            return this;
        }

        public ListResponse build() {
            ListResponse lr =  new ListResponse();
            lr.services = services;
            lr.contexts = contexts;
            return lr;
        }
    }
    
}
