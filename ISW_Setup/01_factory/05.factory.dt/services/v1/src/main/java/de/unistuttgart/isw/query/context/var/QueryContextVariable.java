package de.unistuttgart.isw.query.context.var;

import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.context.value.QueryContextValue;



public class QueryContextVariable extends ContextVariable<ExecutorResponse<?>, QueryContextValue> {

    public QueryContextVariable(String name, QueryContextValue value) {
        super(name, value);
    }

    
}
