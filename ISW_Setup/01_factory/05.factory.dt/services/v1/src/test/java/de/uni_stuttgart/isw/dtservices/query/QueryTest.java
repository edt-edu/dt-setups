package de.uni_stuttgart.isw.dtservices.query;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.unistuttgart.isw.dtservices.configs.Config;
import de.unistuttgart.isw.dtservices.impl.DefaultGateway;
import de.unistuttgart.isw.dtservices.services.GatewayService;
import de.unistuttgart.isw.dtservices.services.configuration.JsonGatewayConfiguration;
import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.QueryError;
import de.unistuttgart.isw.query.QueryHandler;
import de.unistuttgart.isw.query.context.value.ContextValueType;
import de.unistuttgart.isw.query.responses.LetResponse;
import de.unistuttgart.isw.query.responses.ListResponse;
import de.unistuttgart.isw.query.responses.SigilResponse;
import de.unistuttgart.isw.query.responses.SwitchResponse;

public class QueryTest {

    @Test
    public void selectQuery(){


          // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        String query = "SELECT (a, b) FROM (maintance_prep) WHERE (a > 10).";

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
      

        QueryHandler handler = new QueryHandler(gateway);
        handler.handleQuery(query);
    }


    @Test
    public void listContextsQuery(){

        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        String query = "LIST CONTEXTS.";

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
      

        QueryHandler handler = new QueryHandler(gateway);
        List<ExecutorResponse<?>> responses =  handler.handleQuery(query);

        assert responses.size() == 1;


        ExecutorResponse<ListResponse> res = (ExecutorResponse<ListResponse>) responses.get(0);
        assert res.getResponse().getContexts() != null;
        assert res.getResponse().getServices() == null;
        assert res.getResponse().getContexts().size() == 1;
        assert res.getResponse().getContexts().get(0).equals("defaultContext");

    }

    @Test
    public void listServicesQuery(){

        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        String query = "LIST SERVICES.";

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
      

        QueryHandler handler = new QueryHandler(gateway);
        List<ExecutorResponse<?>> responses =  handler.handleQuery(query);

        ExecutorResponse<ListResponse> res = (ExecutorResponse<ListResponse>) responses.get(0);
        assert res.getResponse().getContexts() == null;
        assert res.getResponse().getServices() != null;
        assert res.getResponse().getServices().size() == 2;
        assert res.getResponse().getServices().contains("auth");
        assert res.getResponse().getServices().contains("maintance_prep");

    }

    @Test
    public void decribeContextQuery(){

        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        String query = "DESCRIBE CONTEXT defaultContext.";

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
      

        QueryHandler handler = new QueryHandler(gateway);
        List<ExecutorResponse<?>> responses =  handler.handleQuery(query);

        assert responses.size() == 1;

    }

    @Test
    public void switchContextQueryNotFound(){

        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        String query = "SWITCH secondContext.";

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
      

        QueryHandler handler = new QueryHandler(gateway);
        try{            
            List<ExecutorResponse<?>> responses =  handler.handleQuery(query);
            assert false;
        }catch(QueryError e){            
            assert e.getMessage().equals("Context not found");
        }

    }

    @Test
    public void switchContextQuery(){

        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_two_context_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        String query = "SWITCH secondContext.";

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
      

        QueryHandler handler = new QueryHandler(gateway);
        try{            
            List<ExecutorResponse<?>> responses =  handler.handleQuery(query);
            assert responses.size() == 1;
            ExecutorResponse<SwitchResponse>  res = (ExecutorResponse<SwitchResponse>) responses.get(0);
            assert res.getResponse().getOldContext().equals("defaultContext");
            assert res.getResponse().getNewContext().equals("secondContext");
            assert handler.getContext().getServiceContext().equals("secondContext");
        }catch(QueryError e){
            assert false;
        }     

    }

    @Test
    public void letQueryTest(){

        QueryHandler handler = getDefaultHandler();

        String query = "LET a = 10. LET s = \"Hello World\". LET b = 20. ";
        try {
            handler.getContext().getContextVariable("a");
            assert false;
        }catch(QueryError e){
            assert true;
        }
        try {
            handler.getContext().getContextVariable("s");
            assert false;
        }catch(QueryError e){
            assert true;
        }
        try {
            handler.getContext().getContextVariable("b");
            assert false;
        }catch(QueryError e){
            assert true;
        }
        
        List<ExecutorResponse<?>> responses =  handler.handleQuery(query);
        assert responses.size() == 3;
        ExecutorResponse<LetResponse> lr1 = (ExecutorResponse<LetResponse>) responses.get(0);
        ExecutorResponse<LetResponse> lr2 =  (ExecutorResponse<LetResponse>) responses.get(1);
        ExecutorResponse<LetResponse> lr3 =  (ExecutorResponse<LetResponse>) responses.get(2);
        assert lr1.getResponse().getType().equals(ContextValueType.NUMBER.getTypeAsString());
        assert lr1.getResponse().getValue().getValue().equals(10L);
        assert lr2.getResponse().getType().equals(ContextValueType.STRING.getTypeAsString());
        assert lr2.getResponse().getValue().getValue().equals("Hello World");
        assert lr3.getResponse().getType().equals(ContextValueType.NUMBER.getTypeAsString());
        assert lr3.getResponse().getValue().getValue().equals(20L);

        assert handler.getContext().getContextVariable("a") != null;
        assert handler.getContext().getContextVariable("s") != null;
        assert handler.getContext().getContextVariable("b") != null;

        assert handler.getContext().getContextVariable("a").getValue().getValue().equals(10L);
        assert handler.getContext().getContextVariable("s").getValue().getValue().equals("Hello World");
        assert handler.getContext().getContextVariable("b").getValue().getValue().equals(20L);
        
    }

    

    @Test
    public void SigialQueryTest(){  
 
        // string to date
        String query = "~d(\"2025-01-01\").";
        QueryHandler handler = getDefaultHandler();
        try{            
            List<ExecutorResponse<?>> responses =  handler.handleQuery(query);
            assert responses.size() == 1;
            ExecutorResponse<SigilResponse>  res = (ExecutorResponse<SigilResponse>) responses.get(0);
            assert res.getResponse().getOldValue().getType().equals(ContextValueType.STRING);
            assert res.getResponse().getNewValue().getType().equals(ContextValueType.DATE);
            assert res.getResponse().getOldValue().getValue().equals("2025-01-01");
            Date expected = new GregorianCalendar(2025, 0, 1).getTime();
            assert res.getResponse().getNewValue().getValue().equals(expected);
        }catch(QueryError e){
            assert false;
        }     

        // string to integer
        query = "~n(\"10\").";
        handler = getDefaultHandler();
        try{            
            List<ExecutorResponse<?>> responses =  handler.handleQuery(query);
            assert responses.size() == 1;
            ExecutorResponse<SigilResponse>  res = (ExecutorResponse<SigilResponse>) responses.get(0);
            assert res.getResponse().getOldValue().getType().equals(ContextValueType.STRING);
            assert res.getResponse().getNewValue().getType().equals(ContextValueType.NUMBER);
            assert res.getResponse().getOldValue().getValue().equals("10");
            Number expected = 10L;
            assert res.getResponse().getNewValue().getValue().equals(expected);
        }catch(QueryError e){
            assert false;
        }     

        // integer to string
        query = "~s(10).";
        handler = getDefaultHandler();
        try{            
            List<ExecutorResponse<?>> responses =  handler.handleQuery(query);
            assert responses.size() == 1;
            ExecutorResponse<SigilResponse>  res = (ExecutorResponse<SigilResponse>) responses.get(0);
            assert res.getResponse().getOldValue().getType().equals(ContextValueType.NUMBER);
            assert res.getResponse().getNewValue().getType().equals(ContextValueType.STRING);
            assert res.getResponse().getOldValue().getValue().equals(10L);
            String expected = "10";
            assert res.getResponse().getNewValue().getValue().equals(expected);
        }catch(QueryError e){
            assert false;
        }   
       

    
    }



    private QueryHandler getDefaultHandler(){
        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_two_context_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);       

        GatewayService gateway = new DefaultGateway(new Config(), jsonGatewayConfiguration);
 
        QueryHandler handler = new QueryHandler(gateway);
        return handler;
    }
    
}
