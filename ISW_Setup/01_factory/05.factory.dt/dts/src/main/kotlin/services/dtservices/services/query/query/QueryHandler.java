package services.dtservices.services.query.query;


import dtsql._ast.ASTCommand;
import dtsql._parser.DTSQLParser;
import services.dtservices.services.GatewayService;
import services.dtservices.services.query.query.executors.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class QueryHandler {

    private final GatewayService gateway;
    private final QueryContext context;

    public QueryHandler(final GatewayService gateway){
        this.gateway = gateway;
        this.context = gateway.newQueryContext();
    }


    public QueryContext getContext(){
        return this.context;
    }


  
    public List<ExecutorResponse<?>> handleQuery(String query){
        DTSQLParser parser = new DTSQLParser();

       
        List<ExecutorResponse<?>> responses = new LinkedList<>();
        try {
            Optional<dtsql._ast.ASTScript> optAst = parser.parse_String(query);
      
            if (parser.hasErrors() || !optAst.isPresent()) {
              throw new QueryError("Error parsing query");
            }
           
            for(ASTCommand command : optAst.get().getCommandList()){
                ExecutorResponse<?> response = runExecutor(command);
                responses.add(response);
            }  
          
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new QueryError("Error parsing query");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new QueryError("Error parsing query");
        } catch(Exception e) {
            e.printStackTrace();
            throw new QueryError("Error parsing query");
        }

        return responses;
    }

    private ExecutorResponse<?> runExecutor(ASTCommand command){        
     
        if(command.isPresentSelectCommand()){
            SelectExecutor ex = new SelectExecutor(context);
            return ex.executeQuery(this.gateway, command.getSelectCommand());
        }
        if(command.isPresentListCommand()){
            ListExecutor ex = new ListExecutor(context);
            return ex.executeQuery(this.gateway, command.getListCommand());
        }
        if(command.isPresentSwitchCommand()) {
            SwitchExecutor ex = new SwitchExecutor(context);
            return ex.executeQuery(this.gateway, command.getSwitchCommand());
        } if(command.isPresentDescribeCommand()) {
            return new DescribeExecutor(context).executeQuery(this.gateway, command.getDescribeCommand());
        } if (command.isPresentSigilCompute()) {
            return new SigilExecutor(context).executeQuery(this.gateway, command.getSigilCompute());
        } if (command.isPresentLetCommand()) {
            return new LetExecutor(context).executeQuery(this.gateway, command.getLetCommand());
        }
        return null;
    }
}
