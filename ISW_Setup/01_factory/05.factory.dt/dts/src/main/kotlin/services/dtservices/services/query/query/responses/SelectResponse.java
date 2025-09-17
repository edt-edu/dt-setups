package services.dtservices.services.query.query.responses;

import java.util.HashMap;
import java.util.Map;

public class SelectResponse {
    private Map<String, SekectResponseField> fields = new HashMap<>();


    private SelectResponse() {
    }

    public Map<String, SekectResponseField> getFields() {
        return fields;
    }

    public static SelectResponseBuilder builder() {
        return new SelectResponseBuilder();
    }


    public static class SelectResponseBuilder {
        private Map<String, SekectResponseField> fields = new HashMap<>();

        public SelectResponseBuilder addField(String key, SekectResponseField field) {
            fields.put(key, field);
            return this;
        }

        public SelectResponse build() {
            SelectResponse sr = new SelectResponse();
            sr.fields = fields;
            return sr;
        }
    }



    public static class SekectResponseField {   
        private String type;
        private String value;

        public SekectResponseField(String type, String value) {
       
            this.type = type;
            this.value = value;
        }


        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}
