package de.unistuttgart.isw.query.context.value;

import java.util.LinkedList;
import java.util.List;

import de.unistuttgart.isw.query.context.var.ContextVariable;
import de.unistuttgart.isw.query.context.var.ListContextVariable;

public class ListContextValue<ListType> extends ContextValue<List<ContextValue<ListType>>> {


    public ListContextValue(List<ContextValue<ListType>> value) {
        super(value);
    }

    public ListContextValue(Class<ListType> type) {
        super(new LinkedList<>());
    }    

    @Override
    public ContextValueType getType() {
        return ContextValueType.LIST;
    }

    @Override
    public ContextVariable<List<ContextValue<ListType>>, ? extends ContextValue<List<ContextValue<ListType>>>> toVariable(
            String varName) {
        return new ListContextVariable<>(varName, this);
    }
    
}
