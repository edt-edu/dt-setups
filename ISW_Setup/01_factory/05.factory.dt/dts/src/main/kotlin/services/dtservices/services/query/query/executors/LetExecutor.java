package services.dtservices.services.query.query.executors;


import dtsql._ast.ASTLetCommand;
import dtsql._ast.ASTValue;
import dtsql._ast.ASTVarDefinitionValue;
import services.dtservices.services.GatewayService;
import services.dtservices.services.query.query.ExecutorResponse;
import services.dtservices.services.query.query.QueryContext;
import services.dtservices.services.query.query.QueryExecutor;
import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.value.NumberContextValue;
import services.dtservices.services.query.query.context.value.QueryContextValue;
import services.dtservices.services.query.query.context.value.StringContextValue;
import services.dtservices.services.query.query.context.var.ContextVariable;
import services.dtservices.services.query.query.responses.LetResponse;

public class LetExecutor implements QueryExecutor<ASTLetCommand, LetResponse> {


    private QueryContext context;

    public LetExecutor(QueryContext context){
        this.context = context;
    }

    @Override
    public ExecutorResponse<LetResponse> executeQuery(GatewayService gateway, ASTLetCommand queryvalue) {
        
        String varName = queryvalue.getVar().getName().getCHAR_LIST();
        ASTVarDefinitionValue val = queryvalue.getVarDefinitionValue();
        return declareVariable(varName, val);
    }

    private ExecutorResponse<LetResponse> declareVariable(String varName, ASTVarDefinitionValue val){
       ContextValue<?> value = createValue(val);
       ContextVariable<?,?> contextVariable = value.toVariable(varName);
       context.setContextVariable(contextVariable);
       return ExecutorResponse.simpleResponse(new LetResponse(contextVariable));
    }

    private ContextValue<?> createValue(ASTVarDefinitionValue val){
        if(val.isPresentValue()){
            ASTValue value = val.getValue();
            if(value.isPresentNumber()){
                return new NumberContextValue(value.getNumber().getNUM_VALUE());
            }else if(value.isPresentStringString()){
                String strVal = value.getStringString().getSTRING_VALUE();            
                return new StringContextValue(strVal, true);
            }else if(value.isPresentCHAR_LIST()){
                String name = value.getCHAR_LIST();
                return context.getContextVariable(name).getValue().deepCopy();
            }            
        }
        if(val.isPresentQuery()){
            return new QueryContextValue(val.getQuery());
        }
        throw new IllegalArgumentException("Unknown value type");
    }

    @Override
    public QueryContext getContext() {
        return this.context;
    }
    
}
