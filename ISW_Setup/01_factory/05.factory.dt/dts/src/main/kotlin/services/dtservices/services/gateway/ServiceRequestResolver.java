package services.dtservices.services.gateway;

import services.dtservices.services.IBootable;
import services.dtservices.services.query.Callable;
import services.dtservices.services.query.Selectable;

import java.util.Optional;


public class ServiceRequestResolver {
    
    private final String replyQueue;
    private final String correlationId;
    private final IBootable service;

    public ServiceRequestResolver(String replyQueue, String correlationId, IBootable service) {
        this.replyQueue = replyQueue;
        this.correlationId = correlationId;
        this.service = service;
    }


    public Optional<Object> resolve(ServiceRequest request) {
        // this will resolve which method to call on the service
        if(request.getSelect().isPresent()) {
            return callInterfaceMethod(Selectable.class, "onSelect", request.getSelect().get());
        } else if(request.getInsert().isPresent()) {
            return callInterfaceMethod(Callable.class, "onInsert", request.getInsert().get());
        } else if(request.getCall().isPresent()) {
            return callInterfaceMethod(Callable.class, "onCall", request.getCall().get());
        } else if(request.getUpdate().isPresent()) {
            return callInterfaceMethod(Callable.class, "onUpdate", request.getUpdate().get());
        } else if(request.getDelete().isPresent()) {
            return callInterfaceMethod(Callable.class, "onDelete", request.getDelete().get());        
        }else {
            return Optional.empty();
        }
    }

    private Optional<Object> callInterfaceMethod(Class<?> interfaceClass, String methodName, Object arg) {
        // check if service has interface implemented
        if(service.getClass().getInterfaces().length > 0) {
            for(Class<?> clazz : service.getClass().getInterfaces()) {
                if(clazz.equals(interfaceClass)) {
                    // call the method
                    try {
                        return Optional.of(clazz.getMethod(methodName, arg.getClass()).invoke(service, arg));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }


}
