package services.dtservices.services.query.query.context.var;


import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.value.ContextValueType;

/**
 * Represents a variable in the context of a query. A variable is immutable
 */
public abstract class ContextVariable<VarType, ValueType extends ContextValue<VarType>>  {

    private final String name;
    private final ValueType value;

    public ContextVariable(final String name, final ValueType value) {
        this.value = value;
        this.name = name;        
    }

    public final String getName() {
        return name;
    }  

    public final ValueType getValue() {
        return value;
    }

    public final ContextValueType getType() {
        return value.getType();
    }

    
    

    @Override
    public String toString() {
        return "ContextVariable [name=" + name + ", value=" + getValue() + ", type=" + getType().toString() + "]";
    }
    
}
