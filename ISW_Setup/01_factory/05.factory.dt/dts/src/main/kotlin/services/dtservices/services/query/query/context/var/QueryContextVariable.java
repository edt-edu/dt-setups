package services.dtservices.services.query.query.context.var;


import services.dtservices.services.query.query.ExecutorResponse;
import services.dtservices.services.query.query.context.value.QueryContextValue;

public class QueryContextVariable extends ContextVariable<ExecutorResponse<?>, QueryContextValue> {

    public QueryContextVariable(String name, QueryContextValue value) {
        super(name, value);
    }

    
}
