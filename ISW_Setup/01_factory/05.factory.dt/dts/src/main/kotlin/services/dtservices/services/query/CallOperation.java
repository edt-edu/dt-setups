package services.dtservices.services.query;

import java.util.Map;

public interface CallOperation {
   
    public String getFunction();
    public String getServiceName();
    public Map<String, String> getParameters();
}
