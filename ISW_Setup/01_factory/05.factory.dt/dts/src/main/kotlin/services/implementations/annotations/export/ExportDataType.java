package services.implementations.annotations.export;

import java.util.List;

public class ExportDataType {

    private String name;
    private String identifier;
    private List<ExportDataTypeField> fields;

    public ExportDataType(String name, String identifier, List<ExportDataTypeField> fields) {
        this.name = name;
        this.identifier = identifier;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<ExportDataTypeField> getFields() {
        return fields;
    }     

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExportDataType{");
        sb.append("name='").append(name).append('\'');
        sb.append(", identifier='").append(identifier).append('\'');
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }

    public static class ExportDataTypeField {
        private String name;
        private boolean isEnum;
        private String[] onlyValues;
        private String type;
        private String typeDisplayName;

        public ExportDataTypeField(String name, boolean isEnum, String[] onlyValues, String type, String typeDisplayName) {
            this.name = name;
            this.isEnum = isEnum;
            this.onlyValues = onlyValues;
            this.type = type;
            this.typeDisplayName = typeDisplayName;
        }

        public String getName() {
            return name;
        }

        public boolean isEnum() {
            return isEnum;
        }

        public String[] getOnlyValues() {
            return onlyValues;
        }

        public String getType() {
            return type;
        }

        public String getTypeDisplayName() {
            return typeDisplayName;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ExportDataTypeField{");
            sb.append("name='").append(name).append('\'');
            sb.append(", isEnum=").append(isEnum);
            sb.append(", onlyValues=").append(String.join(", ", onlyValues));
            sb.append(", type='").append(type).append('\'');
            sb.append(", typeDisplayName='").append(typeDisplayName).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
