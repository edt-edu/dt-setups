package services.dtservices.services.query.query.responses;

public class SwitchResponse {
    
    private final String newContext;
    private final String oldContext;

    public SwitchResponse(String oldContext, String newContext) {
        this.newContext = newContext;
        this.oldContext = oldContext;
    }

    public String getNewContext() {
        return newContext;
    }

    public String getOldContext() {
        return oldContext;
    }

}
