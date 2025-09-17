package services.dtservices.services.query.query;


import dtengine.eventsystem.abstractevents.DTEvent;
import services.dtservices.events.EventRegistry;
import services.dtservices.services.query.query.context.var.ContextVariable;

import java.util.HashMap;
import java.util.Map;

public class QueryContext {
    
    private String serviceContext;

    private final Map<String, ContextVariable<?,?>> contextVariables = new HashMap<>();
    private final EventRegistry<DTEvent> eventHandler;

    public QueryContext(String serviceContext, EventRegistry<DTEvent> eventHandler) {
        this.serviceContext = serviceContext;
        this.eventHandler = eventHandler;
    }

    /**
     * Set a context variable
     * @param contextVariable
     */
    public void setContextVariable(ContextVariable<?,?> contextVariable) {
        contextVariables.put(contextVariable.getName(), contextVariable);
    }

    /**
     * Get a context variable by name
     * @throws QueryError if the context variable is not found
     * @param name the name of the context variable
     * @return
     */
    public ContextVariable<?,?> getContextVariable(String name) {
        if (!contextVariables.containsKey(name)) {
            throw new QueryError("Context variable " + name + " not found");            
        }
        return contextVariables.get(name);
    }

    /**
     * Get the service context
     * @return
     */
    public String getServiceContext() {
        return serviceContext;
    }

    /**
     * Get the event handler
     * @return
     */
    public EventRegistry<DTEvent> getEventHandler() {
        return eventHandler;
    }

    /**
     * Set the service context
     */
    public void setServiceContext(String serviceContext) {
        this.serviceContext = serviceContext;
    }

}
