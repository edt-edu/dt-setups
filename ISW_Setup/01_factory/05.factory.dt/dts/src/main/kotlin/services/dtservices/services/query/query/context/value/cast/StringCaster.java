package services.dtservices.services.query.query.context.value.cast;


import services.dtservices.services.query.query.context.value.ContextValue;
import services.dtservices.services.query.query.context.value.StringContextValue;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class StringCaster<ToType extends ContextValue<?>> {

    @SuppressWarnings("unchecked")
    public ToType cast(StringContextValue value, ToType toValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
       switch (toValue.getType()) {
           case STRING -> {
                return ((ToType)(toValue
                .getClass()
                .getConstructor(String.class)
                .newInstance(value.getValue())));
           }
           case NUMBER -> {
                Number num = this.castNumber(value.getValue());
                return ((ToType)(toValue
                .getClass()
                .getConstructor(Number.class)
                .newInstance(num)));
           }
            case BOOLEAN -> {
                return ((ToType)(toValue
                .getClass()
                .getConstructor(Boolean.class)
                .newInstance(Boolean.valueOf(value.getValue()))));
            }
            case DATE -> {
                Date date = getDateFromString(value);                
                return ((ToType)(toValue
                .getClass()
                .getConstructor(Date.class)
                .newInstance(date)));
            }            
           default ->
                throw new UnsupportedOperationException("Cast from String to " + toValue.getType() + " not yet implemented");
       }
    }

    private Number castNumber(String value){
        // contains "." -> double
        if(value.contains(".")){
            return Double.valueOf(value);
        }
        // else -> long
        return Long.valueOf(value);
    }

    private Date getDateFromString(StringContextValue value) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDateTime = LocalDate.parse(value.getValue(), format);            
        // convert LocalDate to Date
        Date date = java.sql.Date.valueOf(localDateTime);
        return date;
    }
    
}
