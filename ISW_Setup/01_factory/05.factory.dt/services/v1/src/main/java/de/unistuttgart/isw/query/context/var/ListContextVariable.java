package de.unistuttgart.isw.query.context.var;

import java.util.List;

import de.unistuttgart.isw.query.context.value.ContextValue;
import de.unistuttgart.isw.query.context.value.ListContextValue;
                                            
public class ListContextVariable<ListType> extends ContextVariable<List<ContextValue<ListType>>, ListContextValue<ListType>>  {

    public ListContextVariable(String name, ListContextValue<ListType> value) {
        super(name, value);
    } 
    
}
