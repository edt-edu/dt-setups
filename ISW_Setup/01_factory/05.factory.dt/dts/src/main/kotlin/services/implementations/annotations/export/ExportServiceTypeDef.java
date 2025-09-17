package services.implementations.annotations.export;

import java.util.List;

public class ExportServiceTypeDef {
    private String typeName;
    private String identifier;
    private List<ExportServiceFunctionDef> functions;

    public ExportServiceTypeDef(String typeName, String identifier, List<ExportServiceFunctionDef> functions) {
        this.typeName = typeName;
        this.identifier = identifier;
        this.functions = functions;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<ExportServiceFunctionDef> getFunctions() {
        return functions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExportServiceTypeDef{");
        sb.append("typeName='").append(typeName).append('\'');
        sb.append(", identifier='").append(identifier).append('\'');
        sb.append(", functions=").append(functions);
        sb.append('}');
        return sb.toString();
    }

    public static class ExportServiceFunctionDef {
        private String name;
        private String identifier;
        private ExportDataType inputTypes;
        private ExportDataType outputType;

        public ExportServiceFunctionDef(String name, String identifier, ExportDataType inputTypes, ExportDataType outputType) {
            this.name = name;
            this.identifier = identifier;
            this.inputTypes = inputTypes;
            this.outputType = outputType;
        }

        public String getName() {
            return name;
        }

        public String getIdentifier() {
            return identifier;
        }

        public ExportDataType getInputTypes() {
            return inputTypes;
        }

        public ExportDataType getOutputType() {
            return outputType;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ExportServiceFunctionDef{");
            sb.append("name='").append(name).append('\'');
            sb.append(", identifier='").append(identifier).append('\'');
            sb.append(", inputTypes=").append(inputTypes);
            sb.append(", outputType=").append(outputType);
            sb.append('}');
            return sb.toString();
        }
    }
    
}
