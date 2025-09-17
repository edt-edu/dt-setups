package de.unistuttgart.isw.query.context.value.cast;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import de.unistuttgart.isw.query.context.value.ContextValue;
import de.unistuttgart.isw.query.context.value.NumberContextValue;

public class NumberCaster <ToType extends ContextValue<?>> {

    @SuppressWarnings("unchecked")
    public ToType cast(NumberContextValue value, ToType toValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
       switch (toValue.getType()) {
           case STRING -> {
                return ((ToType)(toValue
                .getClass()
                .getConstructor(String.class)
                .newInstance(value.getValue().toString())));
           }
           case NUMBER -> {
                Number num = value.getValue();
                return ((ToType)(toValue
                .getClass()
                .getConstructor(Number.class)
                .newInstance(num)));
           }
            case BOOLEAN -> {
                boolean bool = value.getValue().longValue() == 1L;
                return ((ToType)(toValue
                .getClass()
                .getConstructor(Boolean.class)
                .newInstance(bool)));
            }
            case DATE -> {
                Date date = new Date(value.getValue().longValue() * 1000);              
                return ((ToType)(toValue
                .getClass()
                .getConstructor(Date.class)
                .newInstance(date)));
            }            
           default ->
                throw new UnsupportedOperationException("Cast from String to " + toValue.getType() + " not yet implemented");
       }
    }
    
}
