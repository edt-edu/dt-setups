package services.dtservices.services;

import java.lang.reflect.InvocationTargetException;

/**
 * ServiceLoader
 * It handles the loading process of the services. Crud service, computation service, etc.
 */
public class ServiceLoader {
    
    public CrudService loadCrudService(final String className)
    throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException{
        return (CrudService) Class
            .forName(className)
            .getConstructor()
            .newInstance();       
    }
    
}
