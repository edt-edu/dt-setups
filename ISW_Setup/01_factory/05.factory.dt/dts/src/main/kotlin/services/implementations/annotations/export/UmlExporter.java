package services.implementations.annotations.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

public class UmlExporter implements Exporter {

    private String filePath;
    private List<ExportDataType> dataTypes = new LinkedList<>();
    private List<ExportServiceTypeDef> serviceTypes= new LinkedList<>();
    private List<UmlRelationship> relationships = new LinkedList<>();

    // output buffer
    private StringBuilder outputBuffer;

    public UmlExporter(final String filePath) {
        this.filePath = filePath;
        this.outputBuffer = new StringBuilder();
    }


    @Override
    public void exportType(ExportDataType type) {
        this.dataTypes.add(type);
    }

    @Override
    public void exportServiceType(ExportServiceTypeDef serviceType) {
        this.serviceTypes.add(serviceType);        
    }


    /**
     * 
     * create plant uml class diagramm based on exported data types
     * 
     */
    private String createUmlClass(ExportDataType type){
        String className = type.getName();
        String alias = type.getIdentifier();
        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("class \"").append(className).append("\" as ").append(alias).append(" {\n");
        for (ExportDataType.ExportDataTypeField field : type.getFields()) {
            classBuilder.append("    + ").append(field.getName()).append(": ").append(field.getTypeDisplayName()).append("\n");
        }
        classBuilder.append("}\n");
        return classBuilder.toString();
    }

    private String createUmlClass(ExportServiceTypeDef type){
        String className = type.getTypeName();
        String alias = type.getIdentifier();
        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("class \"").append(className).append("\" as ").append(alias).append(" {\n");
        // add functions
        for (ExportServiceTypeDef.ExportServiceFunctionDef function : type.getFunctions()) {
            // input types
            String inputType = function.getInputTypes().getName();
            String outputType = function.getOutputType().getName();
            classBuilder.append("    + ").append(function.getName()).append("(").append(inputType).append("): ").append(outputType).append("\n");            
        }
        classBuilder.append("}\n");
        return classBuilder.toString();
    }





    @Override
    public void finished() {        

        outputBuffer.append("@startuml\n");

        for (ExportDataType type : dataTypes) {
            outputBuffer.append(createUmlClass(type)).append("\n");
        }
      
        for (ExportServiceTypeDef serviceType : serviceTypes) {
            outputBuffer.append(createUmlClass(serviceType)).append("\n");
        }

        outputBuffer.append("\n");
        // end uml class diagramm
        outputBuffer.append("@enduml\n");

        // write to file
        File file = new File(filePath);
        if(file.exists()) {
            file.delete();
        }       
        try {
             // create file and write to it
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(outputBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("Error writing to file: " + filePath);
            e.printStackTrace();

        }
        
    }

    private static UmlRelationship createRelationship(String source, String target, String type) {
        return new UmlRelationship(source, target, type);
    }
    public static class UmlRelationship {
        private String source;
        private String target;
        private String type;

        public UmlRelationship(String source, String target, String type) {
            this.source = source;
            this.target = target;
            this.type = type;
        }

        @Override
        public String toString() {
            return "UmlRelationship{" +
                    "source='" + source + '\'' +
                    ", target='" + target + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
    
}
