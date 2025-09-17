package services.dtservices.services.query.query.context.value;


import services.dtservices.services.query.query.context.var.ContextVariable;

public abstract class ContextValue<VarType> {

    private final VarType value;

    public ContextValue(final VarType value) {
        this.value = value;
    }

    public static ContextValue<?> parse(String type, String type1) {
        return new ContextValue<Object>(type) {
            @Override
            public ContextValueType getType() {
                return ContextValueType.STRING;
            }

            @Override
            public ContextVariable<Object, ? extends ContextValue<Object>> toVariable(String varName) {
                return null;
            }
        };
    }


    public VarType getValue() {
        return value;
    }

    
    public final ContextValue<VarType> deepCopy() {
        // cast to itself will create a new instance of the same type
        return new ValueCaster<>(this, this).cast();
    }

    /**
     * cast the value to the given type
     */
    public final <ToType extends ContextValue<?>> ToType cast(final ToType toValue) {
        final ValueCaster<ContextValue<?>, ToType> caster = new ValueCaster<>(this, toValue);
        return caster.cast();
    }
    

    public abstract ContextValueType getType();

    @Override
    public String toString() {
        return "ContextVariable [value=" + value + ", type=" + getType().toString() + "]";
    }

    /**
     * create a new variable with the given name and the value of this context value
     */
    public abstract ContextVariable<VarType, ? extends ContextValue<VarType>> toVariable(final String varName);
    
}
