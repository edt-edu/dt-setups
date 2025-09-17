package services.dtservices.services.query.query.context.var;


import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.value.ListContextValue;

import java.util.List;

public class ListContextVariable<ListType> extends ContextVariable<List<ContextValue<ListType>>, ListContextValue<ListType>> {

    public ListContextVariable(String name, ListContextValue<ListType> value) {
        super(name, value);
    } 
    
}
