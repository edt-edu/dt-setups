package services.dtservices.services.query.query.executors;


import dtsql._ast.ASTListCommand;
import dtsql._ast.ASTListType;
import dtsql._ast.ASTListTypeContext;
import dtsql._ast.ASTListTypeService;
import services.dtservices.services.GatewayService;
import services.dtservices.services.query.query.ExecutorResponse;
import services.dtservices.services.query.query.QueryContext;
import services.dtservices.services.query.query.QueryExecutor;
import services.dtservices.services.query.query.responses.ListResponse;

import java.util.List;

public class ListExecutor  implements QueryExecutor<ASTListCommand, ListResponse> {


    private QueryContext context;

    public ListExecutor(QueryContext context){
        this.context = context;
    }

    @Override
    public ExecutorResponse<ListResponse> executeQuery(GatewayService gateway, ASTListCommand queryvalue) {
        ASTListType type = queryvalue.getListType();
        if(type.isPresentListTypeContext()){
            return executeContextList(gateway, type.getListTypeContext());
        }else if(type.isPresentListTypeService()){
            return executeServiceList(gateway, type.getListTypeService());
        }
        return null;
    }

    private ExecutorResponse<ListResponse> executeServiceList(GatewayService gateway, ASTListTypeService type){
        final String currentContext = context.getServiceContext();
        final List<String> services = gateway.getServices(currentContext);
        return ExecutorResponse.listResponse((ListResponse.ListResponseBuilder builder) -> {
            return builder.setServices(services);
        });     
    }

    private ExecutorResponse<ListResponse> executeContextList(GatewayService gateway, ASTListTypeContext type){
        final List<String> contexts = gateway.getContexts();
        return ExecutorResponse.listResponse((ListResponse.ListResponseBuilder builder) -> {
            return builder.setContexts(contexts);
        });
    }
    
    @Override
    public QueryContext getContext() {
        return context;
    }

}
