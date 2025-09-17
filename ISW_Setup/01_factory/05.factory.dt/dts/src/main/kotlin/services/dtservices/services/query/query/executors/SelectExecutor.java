package services.dtservices.services.query.query.executors;


import dtsql._ast.ASTSelectCommand;
import services.dtservices.services.GatewayService;
import services.dtservices.services.configuration.gateway.GatewayContext;
import services.dtservices.services.query.query.ExecutorResponse;
import services.dtservices.services.query.query.QueryContext;
import services.dtservices.services.query.query.QueryError;
import services.dtservices.services.query.query.QueryExecutor;
import services.dtservices.services.query.query.responses.SelectResponse;


public class SelectExecutor implements QueryExecutor<ASTSelectCommand, SelectResponse> {


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


    private GatewayContext.Service findService(GatewayService gateway, ASTSelectCommand queryvalue){
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
