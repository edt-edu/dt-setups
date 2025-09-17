package de.unistuttgart.isw.query.context.var;

import java.util.Date;

import de.unistuttgart.isw.query.context.value.DateContextValue;


public class DateContextVariable extends ContextVariable<Date, DateContextValue> {

    public DateContextVariable(String name, DateContextValue value) {
        super(name, value);
    }
  
    
}
