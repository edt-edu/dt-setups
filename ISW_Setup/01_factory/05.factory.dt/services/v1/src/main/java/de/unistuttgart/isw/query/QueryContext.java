package de.unistuttgart.isw.query;

import java.util.HashMap;
import java.util.Map;

import de.unistuttgart.isw.dtservices.events.DtEvent;
import de.unistuttgart.isw.dtservices.events.EventRegistry;
import de.unistuttgart.isw.query.context.var.ContextVariable;

public class QueryContext {
    
    private String serviceContext;

    private final Map<String, ContextVariable<?,?>> contextVariables = new HashMap<>();
    private final EventRegistry<DtEvent> eventHandler;

    public QueryContext(String serviceContext, EventRegistry<DtEvent> eventHandler) {
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
    public EventRegistry<DtEvent> getEventHandler() {
        return eventHandler;
    }

    /**
     * Set the service context
     */
    public void setServiceContext(String serviceContext) {
        this.serviceContext = serviceContext;
    }

}
