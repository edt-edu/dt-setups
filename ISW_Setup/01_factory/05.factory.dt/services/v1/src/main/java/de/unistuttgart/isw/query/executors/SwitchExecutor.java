package de.unistuttgart.isw.query.executors;

import java.util.List;

import de.unistuttgart.isw.dtservices.services.GatewayService;
import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.QueryContext;
import de.unistuttgart.isw.query.QueryError;
import de.unistuttgart.isw.query.QueryExecutor;
import de.unistuttgart.isw.query.responses.SwitchResponse;
import dtsql._ast.ASTSwitchCommand;

public class SwitchExecutor implements QueryExecutor<ASTSwitchCommand, SwitchResponse>{
    
    private final QueryContext context;

    public SwitchExecutor(QueryContext context){
        this.context = context;
    }

    @Override
    public ExecutorResponse<SwitchResponse> executeQuery(GatewayService gateway, ASTSwitchCommand queryvalue) {
        final List<String> availableContexts = gateway.getContexts();
        final String currentContext = context.getServiceContext();
        final String newContext = queryvalue.getContext().getName().getCHAR_LIST();
        if(!availableContexts.contains(newContext)){
            throw new QueryError("Context not found");
        }
        this.context.setServiceContext(newContext);
        return ExecutorResponse.switchResponse(currentContext, newContext);
        
    }

    @Override
    public QueryContext getContext() {
        return context;
    }



}
