package de.unistuttgart.isw.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExportServiceTypeField {
    public String name() default "";    
    public boolean isEnum() default false;
    public String[] onlyValues() default {};
    public Class<?> type();
}
