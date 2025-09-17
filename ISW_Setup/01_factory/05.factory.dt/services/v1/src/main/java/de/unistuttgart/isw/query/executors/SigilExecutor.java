package de.unistuttgart.isw.query.executors;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.unistuttgart.isw.dtservices.services.GatewayService;
import de.unistuttgart.isw.query.ExecutorResponse;
import de.unistuttgart.isw.query.QueryContext;
import de.unistuttgart.isw.query.QueryExecutor;
import de.unistuttgart.isw.query.context.value.BooleanContextValue;
import de.unistuttgart.isw.query.context.value.ContextValue;
import de.unistuttgart.isw.query.context.value.DateContextValue;
import de.unistuttgart.isw.query.context.value.ListContextValue;
import de.unistuttgart.isw.query.context.value.MapContextValue;
import de.unistuttgart.isw.query.context.value.NumberContextValue;
import de.unistuttgart.isw.query.context.value.QueryContextValue;
import de.unistuttgart.isw.query.context.value.StringContextValue;
import de.unistuttgart.isw.query.context.value.ValueCaster;
import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.responses.SigilResponse;
import dtsql._ast.ASTSigilCompute;
import dtsql._ast.ASTValue;

public class SigilExecutor implements QueryExecutor<ASTSigilCompute,SigilResponse> {


    private final QueryContext context;

    public SigilExecutor(QueryContext context){
        this.context = context;
    }

    @Override
    public ExecutorResponse<SigilResponse> executeQuery(GatewayService gateway, ASTSigilCompute queryvalue) {       
   
        final String sigil = queryvalue.getSigilName().getSIGIL();
        final ASTValue value =  queryvalue.getValue();
        SigilResponse sr = handle(sigil, value);
        return ExecutorResponse.sigilResponse(sr);
    }

    protected boolean isVariable(ASTValue value){
        return value.isPresentCHAR_LIST();
    }

    protected boolean isNumericValue (ASTValue value){
        return value.isPresentNumber();
    }

    protected SigilResponse handle(String sigil, ASTValue value){
        if(isVariable(value)){
            return handleVariable(sigil, value.getCHAR_LIST());
        }else if(isNumericValue(value)){
            return handleNumberValue(sigil, value.getNumber().getNUM_VALUE());
        }
        else {
            return handleStringValue(sigil, value.getStringString().getSTRING_VALUE());
        }
    }

    protected SigilResponse handleVariable(String sigil, String variableName){
        ContextVariable<?,?> cv = context.getContextVariable(variableName);     
        if(cv == null){
            throw new UnsupportedOperationException("Variable not found");
        }
        return handleContextValue(sigil, cv.getValue());        
    }

    protected SigilResponse handleNumberValue(String sigil, String value){       
        ContextValue<?> vw = new NumberContextValue(value);
        return handleContextValue(sigil, vw);
    }

    protected SigilResponse handleStringValue(String sigil, String value){
        // the parser keeps the quotes in the value
        boolean isInQuotes = value.startsWith("\"") && value.endsWith("\"");
        if(isInQuotes){
            value = value.substring(1, value.length()-1);
        }
        ContextValue<?> vw = new StringContextValue(value);
        return handleContextValue(sigil, vw);
    }

    protected SigilResponse handleContextValue(String sigil, ContextValue<?> cv){       
        ValueCaster<ContextValue<?>, ContextValue<?>> caster = new ValueCaster<>(cv, getTargetType(sigil));
        final ContextValue<?> newValue = caster.cast();
        return new SigilResponse(cv, newValue);
    }

    private ContextValue<?> getTargetType(String sigil) {
        switch (sigil.toLowerCase()) {
            case "~s" -> {
                return new StringContextValue("");
            }
            case "~d" -> {
                return new DateContextValue(new Date());
            }
            case "~n" -> {
                return new NumberContextValue(0.0);
            }
            case "~b" -> {
                return new BooleanContextValue(false);
            }
            case "~q" -> {
                return new QueryContextValue();
            }
            case "~l" -> {
                return new ListContextValue<>(new ArrayList<>());
            }
            case "~m" -> {
                return new MapContextValue(new HashMap<>());
            }
            default -> throw new UnsupportedOperationException("Sigil not supported: " + sigil);
        }
    }



    @Override
    public QueryContext getContext() {
        return context;
    }
    
}
