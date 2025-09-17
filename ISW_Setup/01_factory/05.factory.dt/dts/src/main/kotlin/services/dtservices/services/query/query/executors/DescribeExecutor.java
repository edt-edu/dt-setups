package services.dtservices.services.query.query.executors;


import dtsql._ast.ASTDescribeCommand;
import dtsql._ast.ASTDescribeTypes;
import services.dtservices.services.GatewayService;
import services.dtservices.services.configuration.gateway.GatewayContext;
import services.dtservices.services.query.query.ExecutorResponse;
import services.dtservices.services.query.query.QueryContext;
import services.dtservices.services.query.query.QueryExecutor;
import services.dtservices.services.query.query.responses.DescribeResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DescribeExecutor  implements QueryExecutor<ASTDescribeCommand, DescribeResponse> {


    private QueryContext context;

    public DescribeExecutor(QueryContext context){
        this.context = context;
    }

    @Override
    public ExecutorResponse<DescribeResponse> executeQuery(GatewayService gateway, ASTDescribeCommand queryvalue) {
        ASTDescribeTypes type = queryvalue.getDescribeTypes();
        
        String serviceName = queryvalue.getName().getCHAR_LIST();
        if(type.isPresentDescribeTypesContext()){
            return executeContext(gateway, serviceName);
        }else if(type.isPresentDescribeTypesService()){
            return executeService(gateway, serviceName);
        }
        return null;
    }

    private ExecutorResponse<DescribeResponse> executeService(GatewayService gateway, String serviceName){
        final String currentContext = context.getServiceContext();
        final GatewayContext.Service service = gateway.findService(serviceName);
        return ExecutorResponse.describeResponse((DescribeResponse.Builder builder) -> {
            return builder.service((DescribeResponse.ServiceDescription.Builder b) -> {
                return b
                .setType("unknown")
                .setName(serviceName);                
            });
        });     
    }

    private ExecutorResponse<DescribeResponse> executeContext(GatewayService gateway, String context){
        final List<GatewayContext.Service> services = gateway.getServices(context).stream().map(name -> gateway.findService(name)).collect(Collectors.toList());
        return ExecutorResponse.describeResponse((DescribeResponse.Builder builder) -> {
            return builder.context((DescribeResponse.ContextDescription.Builder b) -> {
                return b
                .setName(context)
                .setServices(services);
            });
        });
    }

    @Override
    public QueryContext getContext() {
        return context;
    }
    
}
