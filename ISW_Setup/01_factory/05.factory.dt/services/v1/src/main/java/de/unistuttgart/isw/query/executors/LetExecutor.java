package de.unistuttgart.isw.query.executors;

import de.unistuttgart.isw.dtservices.services.GatewayService;
import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.QueryContext;
import de.unistuttgart.isw.query.QueryExecutor;
import de.unistuttgart.isw.query.context.value.ContextValue;
import de.unistuttgart.isw.query.context.value.NumberContextValue;
import de.unistuttgart.isw.query.context.value.QueryContextValue;
import de.unistuttgart.isw.query.context.value.StringContextValue;
import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.responses.LetResponse;
import dtsql._ast.ASTLetCommand;
import dtsql._ast.ASTValue;
import dtsql._ast.ASTVarDefinitionValue;

public class LetExecutor implements QueryExecutor<ASTLetCommand, LetResponse>{


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
