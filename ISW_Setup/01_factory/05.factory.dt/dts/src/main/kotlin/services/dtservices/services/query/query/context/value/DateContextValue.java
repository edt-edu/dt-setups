package services.dtservices.services.query.query.context.value;


import services.dtservices.services.query.query.context.var.ContextVariable;
import services.dtservices.services.query.query.context.var.DateContextVariable;

import java.util.Date;

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
