package de.unistuttgart.isw.query.executors;

import de.unistuttgart.isw.dtservices.services.GatewayService;
import de.unistuttgart.isw.dtservices.services.configuration.gateway.GatewayContext.Service;
import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.QueryContext;
import de.unistuttgart.isw.query.QueryError;
import de.unistuttgart.isw.query.QueryExecutor;
import de.unistuttgart.isw.query.responses.SelectResponse;
import dtsql._ast.ASTSelectCommand;

public class SelectExecutor implements QueryExecutor<ASTSelectCommand,SelectResponse> {


    private final QueryContext context;

    public SelectExecutor(QueryContext context){
        this.context = context;
    }

    @Override
    public ExecutorResponse<SelectResponse> executeQuery(GatewayService gateway, ASTSelectCommand queryvalue) {
        System.out.println("Executing Select Query");
        boolean hasService = hasService(gateway, queryvalue);
        if (!hasService) {
            throw new QueryError("Service not found");
        }
        return ExecutorResponse.selectResponse((SelectResponse.SelectResponseBuilder selectResponseBuilder) -> {
           return selectResponseBuilder;
        });
    }


    private Service findService(GatewayService gateway, ASTSelectCommand queryvalue){
        String name = queryvalue.getService().getName().getCHAR_LIST();
        return gateway.findService(name);
    }

    private boolean hasService(GatewayService gateway, ASTSelectCommand queryvalue){
        return findService(gateway, queryvalue) != null;
    }

    @Override
    public QueryContext getContext() {
        return context;
    }


    

}
