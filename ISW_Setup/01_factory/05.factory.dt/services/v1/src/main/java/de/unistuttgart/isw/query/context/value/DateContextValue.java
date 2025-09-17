package de.unistuttgart.isw.query.context.value;

import java.util.Date;

import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.context.var.DateContextVariable;

public class DateContextValue extends ContextValue<Date> {

    public DateContextValue(Date value) {
        super(value);
    }

    @Override
    public ContextValueType getType() {
        return ContextValueType.DATE;
    }

    @Override
    public ContextVariable<Date, ? extends ContextValue<Date>> toVariable(String varName) {    
        return new DateContextVariable(varName, this);
    }

   

    
    
    
}
