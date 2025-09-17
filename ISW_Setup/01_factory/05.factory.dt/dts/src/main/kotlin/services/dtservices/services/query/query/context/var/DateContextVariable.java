package services.dtservices.services.query.query.context.var;


import services.dtservices.services.query.query.context.value.DateContextValue;

import java.util.Date;


public class DateContextVariable extends ContextVariable<Date, DateContextValue> {

    public DateContextVariable(String name, DateContextValue value) {
        super(name, value);
    }
  
    
}
