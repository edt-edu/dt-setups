package de.unistuttgart.isw.query.context.value;

import java.util.Optional;

import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.context.var.QueryContextVariable;
import dtsql._ast.ASTQuery;

public class QueryContextValue extends ContextValue<ExecutorResponse<?>> {


    private boolean executed = false;
    private Optional<ASTQuery> query = Optional.empty();


    public QueryContextValue(ExecutorResponse<?> value) {
        super(value);
        this.executed = true;
    }

    public QueryContextValue(){
        super(null);
        this.query = Optional.empty();
    }

    public QueryContextValue(ASTQuery query) {
        super(null);
        this.query = Optional.of(query);
    }

    /**
     * @return the query
     */
    public Optional<ASTQuery> getQuery() {
        return query;
    }


    /**
     * @return if query is already executed and the result get be retrieved
     */
    boolean isExecuted() {
        return executed;
    }

    @Override
    public ContextValueType getType() {
        return ContextValueType.QUERY;
    }

    @Override
    public ContextVariable<ExecutorResponse<?>, ? extends ContextValue<ExecutorResponse<?>>> toVariable(
            String varName) {
        return new QueryContextVariable(varName, this);
    }
    
}
