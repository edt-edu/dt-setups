package de.unistuttgart.isw.query.context.value;

import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.context.var.StringContextVariable;

public class StringContextValue extends ContextValue<String> {


    public StringContextValue(String value, boolean unquoate){        
        super(prepareValue(value, unquoate));
    }
    
    public StringContextValue(String value) {
        this(value, false);
    }

    /*
     * Prepare the value by removing quotes if unquote is true
     */
    private static String prepareValue(String strVal, boolean unquote) {
        if(unquote){
            return unquote(strVal);
        }
        return strVal;
    }

    /*
     * Remove quotes from the string
     */
    private static String unquote(String strVal) {
        boolean hasQuotes = strVal.startsWith("\"") && strVal.endsWith("\"");
        if(hasQuotes){
            return strVal.substring(1, strVal.length()-1);
        }
        return strVal;
    }


    @Override
    public ContextValueType getType() {
        return ContextValueType.STRING;
    }

    @Override
    public ContextVariable<String, ? extends ContextValue<String>> toVariable(String varName) {
        return new StringContextVariable(varName, this);
    }

}
