package services.dtservices.services.query.query.context.value;


import services.dtservices.services.query.query.context.value.cast.NumberCaster;
import services.dtservices.services.query.query.context.value.cast.StringCaster;

import java.lang.reflect.InvocationTargetException;

public class ValueCaster<FromType extends ContextValue<?>, ToType extends ContextValue<?>> {

    private final FromType value;
    private final ToType toValue;

    public ValueCaster(final FromType value, final ToType toValue) {
        this.value = value;
        this.toValue = toValue;
    }

    public ToType cast() {
        try {      
            switch (value.getType()) {
                case STRING -> {
                    return castFromString((StringContextValue) value, toValue);
                }
                case NUMBER -> {
                    return castFromNumber((NumberContextValue) value, toValue);
                }
                case BOOLEAN -> {
                    return castFromBoolean((BooleanContextValue) value, toValue);
                }
                case QUERY -> {
                    return castFromQuery((QueryContextValue) value, toValue);
                }case LIST -> {
                    return castFromList((ListContextValue<?>) value, toValue);
                }
                case MAP -> {
                    return castFromMap((MapContextValue) value, toValue);
                }
                default -> throw new IllegalArgumentException("Unsupported type: " + value.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Cast from " + value.getType() + " to " + toValue.getType() + " failed", e);
        }
    }

    private ToType castFromNumber(final NumberContextValue fromValue, ToType toValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        NumberCaster<ToType> numberCaster = new NumberCaster<>();
        return numberCaster.cast(fromValue, toValue); 
    }

    private ToType castFromString(final StringContextValue fromValue, ToType toValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        StringCaster<ToType> stringCaster = new StringCaster<>();
        return stringCaster.cast(fromValue, toValue);
    }

    private ToType castFromBoolean(final BooleanContextValue fromValue, ToType toValue) {
        throw new UnsupportedOperationException("Cast from boolean not yet implemented");
    }

    private ToType castFromQuery(final QueryContextValue fromValue, ToType toValue) {
        throw new UnsupportedOperationException("Cast from query not yet implemented");
    }

    private ToType castFromList(final ListContextValue fromValue, ToType toValue) {
        throw new UnsupportedOperationException("Cast from list not yet implemented");
    }

    private ToType castFromMap(final MapContextValue fromValue, ToType toValue) {
        throw new UnsupportedOperationException("Cast from map not yet implemented");
    }


    
}
