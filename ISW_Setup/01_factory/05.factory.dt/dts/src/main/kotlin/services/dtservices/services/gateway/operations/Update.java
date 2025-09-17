package services.dtservices.services.gateway.operations;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import services.dtservices.services.query.UpdateOperation;
import services.dtservices.services.query.query.context.value.ContextValue;


public class Update implements UpdateOperation {

    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("values")
    private Map<String, UpdateValue> values;
    @JsonProperty("whereValues")
    private Map<String, UpdateValue> whereValues;


    @Override
    @JsonIgnore
    public String getServiceName() {
        return serviceName;
    }

    @Override
    @JsonIgnore
    public Map<String, ContextValue<?>> getValues() {
        Map<String, ContextValue<?>> result = new java.util.HashMap<>();
        for (Map.Entry<String, UpdateValue> entry : values.entrySet()) {
            String key = entry.getKey();
            UpdateValue value = entry.getValue();
            ContextValue<?> contextValue = ContextValue.parse(value.type, value.type);
            result.put(key, contextValue);
        }
        return result;        
    }

    @Override
    @JsonIgnore
    public Map<String, ContextValue<?>> getWhereValues() {
        Map<String, ContextValue<?>> result = new java.util.HashMap<>();
        for (Map.Entry<String, UpdateValue> entry : whereValues.entrySet()) {
            String key = entry.getKey();
            UpdateValue value = entry.getValue();
            ContextValue<?> contextValue = ContextValue.parse(value.type, value.type);
            result.put(key, contextValue);
        }
        return result;
    }

    public static class UpdateValue {
        private String type;
        private String value;

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

      
    }
    
}
