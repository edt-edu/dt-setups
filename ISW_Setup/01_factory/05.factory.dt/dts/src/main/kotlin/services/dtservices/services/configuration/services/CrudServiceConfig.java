package services.dtservices.services.configuration.services;

import java.util.List;

public class CrudServiceConfig {
   
    private List<Operations> operations;

    private boolean provideUI;

    public List<Operations> getOperations() {
        return operations;
    }

    public boolean isProvideUI() {
        return provideUI;
    }

    public static class Operations {

        /**
         * the model to use
         */
        private String model;

         // get fields    
        private List<String> read;

        private List<String> write;
        //
        private List<String> delete;
        public String getModel() {
            return model;
        }
        public List<String> getRead() {
            return read;
        }
        public List<String> getWrite() {
            return write;
        }
        public List<String> getDelete() {
            return delete;
        } 

        
    }

}


