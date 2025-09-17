package services.dtservices.services.query.query.executors;


import dtsql._ast.ASTSwitchCommand;
import services.dtservices.services.GatewayService;
import services.dtservices.services.query.query.ExecutorResponse;
import services.dtservices.services.query.query.QueryContext;
import services.dtservices.services.query.query.QueryError;
import services.dtservices.services.query.query.QueryExecutor;
import services.dtservices.services.query.query.responses.SwitchResponse;

import java.util.List;

public class SwitchExecutor implements QueryExecutor<ASTSwitchCommand, SwitchResponse> {
    
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
