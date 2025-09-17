package services.util.serviceRequest;

import java.util.List;

/**
 * This class represents the data structure send from gateway to the service
 */
public class ServiceRequest {

    final String method;
    final List<MethodArg> args;


    public ServiceRequest(String method, List<MethodArg> args){
        this.method = method;
        this.args = args;
    }

    /**
     * Returns the method of the service
     */
    public String getMethod(){
        return method;
    }

    /**
     * Returns the arguments of the method
     */
    public List<MethodArg> getArgs(){
        return args;
    }


    public static class MethodArg{
        private final String value;
        private final String type;

        public MethodArg(final String value, final String type){
            this.value = value;
            this.type = type;
        }

        /**
         * Returns the value of the argument
         */
        public String getValue(){
            return this.value;
        }

        /**
         * Returns the type of the argument
         */
        public String getType(){
            return this.type;
        }


    }
    
}
