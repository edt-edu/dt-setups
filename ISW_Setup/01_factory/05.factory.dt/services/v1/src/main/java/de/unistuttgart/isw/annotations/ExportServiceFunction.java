package de.unistuttgart.isw.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExportServiceFunction {
    public Class<?> inputType();
    public Class<?> outputType() default Void.class;
    public String serviceNameString() default "";
    public String identifier();
}
