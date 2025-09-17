package services.dtservices.services.query.query.responses;


import services.dtservices.services.query.query.context.value.ContextValue;

public class SigilResponse {
    
    private final ContextValue<?> oldValue;
    private final ContextValue<?> newValue;

    
    

    public SigilResponse(ContextValue<?> oldValue, ContextValue<?> newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * gets the old value of the variable
     */
    public ContextValue<?> getOldValue(){
        return oldValue;
    }

    /**
     * gets the new value of the variable
     */
    public ContextValue<?> getNewValue(){
        return newValue;
    }

}