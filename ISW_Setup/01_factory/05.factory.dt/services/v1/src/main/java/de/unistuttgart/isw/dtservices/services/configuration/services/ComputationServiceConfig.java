package de.unistuttgart.isw.dtservices.services.configuration.services;

import java.util.List;
import java.util.Map;

public class ComputationServiceConfig {


    /**
     * each computation service can have individual arguments. This way a service can be reused for different purposes.
     */
    private Map<String, String> arguments;

    /**
     * this are the operations that are activated. The implementation and metadata is provided by the service.
     */
    private List<String> computations;

    /**
     * the operations that can retrieves data. Only those reads are allowed that are in the list.
     */
    private List<String> reads;


    
    public Map<String, String> getArguments() {
        return arguments;
    }

    public List<String> getComputations() {
        return computations;
    }

    public List<String> getReads() {
        return reads;
    }


    
    
}
