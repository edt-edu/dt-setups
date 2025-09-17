package de.unistuttgart.isw.annotations;

import java.util.LinkedList;
import java.util.List;

import de.unistuttgart.isw.annotations.export.ExportDataType;
import de.unistuttgart.isw.annotations.export.ExportServiceTypeDef;
import de.unistuttgart.isw.annotations.export.Exporter;
import de.unistuttgart.isw.annotations.export.TxtExporter;
import de.unistuttgart.isw.annotations.export.UmlExporter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;



public class ExportGenerator {

    // TODO: add support to validate with external schema to emit a warning if the schema is not valid or incompatible

    private String outputDir;
    
    public void generateServiceExport(String packageName, String outputDir) {

        this.outputDir = outputDir;
 
        // create directory if it does not exist
        java.io.File dir = new java.io.File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        System.out.println("Export Directory: " + outputDir);

        System.out.println("Start Building Export Files");
        
        // search all classes with "ExportService" annotation using spring        
        List<Class<?>> classesServices = findServices(packageName);
        List<Class<?>> classesTypes = findTypes(packageName);
        List<ExportDataType> exportDataTypes = classesTypes.stream()
                .map(this::exportType)
                .toList();
        List<ExportServiceTypeDef> exportServiceTypeDefs = classesServices.stream()
                .map(this::exportService)
                .toList();        

        try {
            runExport(exportDataTypes, exportServiceTypeDefs);  
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error generating export files: " + e.getMessage());
        }
              
    }

    private void runExport(List<ExportDataType> exportDataTypes, List<ExportServiceTypeDef> exportServiceTypeDefs){
        Exporter txtExporter = new TxtExporter(outputDir + "/export.txt");
        Exporter umlExporter = new UmlExporter(outputDir + "/export.plantuml");
        exportDataTypes.forEach(txtExporter::exportType);
        exportServiceTypeDefs.forEach(txtExporter::exportServiceType);
        txtExporter.finished();
        exportDataTypes.forEach(umlExporter::exportType);
        exportServiceTypeDefs.forEach(umlExporter::exportServiceType);
        umlExporter.finished();
    }

    private List<Class<?>> findTypes(String packageName){
        List<Class<?>> classes = new LinkedList<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(packageName) // your base package
                .scan()) {

            scanResult.getClassesWithAnnotation(ExportServiceType.class.getName())
                      .forEach(classInfo -> {
                          System.out.println("Found: " + classInfo.getName());
                            try {
                                classes.add(Class.forName(classInfo.getName()));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                      });
        }         
        return classes;
    }

    private List<Class<?>> findServices(String packageName){
        List<Class<?>> classes = new LinkedList<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(packageName) // your base package
                .scan()) {

            scanResult.getClassesWithAnnotation(ExportService.class.getName())
                      .forEach(classInfo -> {
                          System.out.println("Found: " + classInfo.getName());
                            try {
                                classes.add(Class.forName(classInfo.getName()));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                      });
        }         
        return classes;
    }

    private ExportDataType exportType(Class<?> typeClass){
        // get annotation
        ExportServiceType exportServiceType = typeClass.getAnnotation(ExportServiceType.class);
        String name = exportServiceType.typeName();
        String identifier = exportServiceType.identifier();

        // find all fields that have the ExportServiceTypeField annotation
        List<ExportDataType.ExportDataTypeField> fields = new LinkedList<>();

        for (var field : typeClass.getDeclaredFields()) {
            ExportServiceTypeField exportServiceTypeField = field.getAnnotation(ExportServiceTypeField.class);
            if (exportServiceTypeField != null) {
                String fieldName = exportServiceTypeField.name();
                boolean isEnum = exportServiceTypeField.isEnum();
                String[] onlyValues = exportServiceTypeField.onlyValues();
                Class<?> fieldTypeClass = exportServiceTypeField.type();
                ExportServiceType fieldType = fieldTypeClass.getAnnotation(ExportServiceType.class);     
                if(fieldType == null){
                    // check if is a primitive type or native java type
                    if(fieldTypeClass.isPrimitive() || fieldTypeClass.getPackageName().equals("java.lang")){
                        final String shortName = fieldTypeClass.getSimpleName().split("[.]")[0];
                        fields.add(new ExportDataType.ExportDataTypeField(fieldName, isEnum, onlyValues, fieldTypeClass.getName(), shortName));
                    }else{
                        throw new RuntimeException("Field type " + fieldTypeClass.getName() + " is not a valid export type. It must be annotated with @ExportServiceType or be a primitive type or a native java type.");
                    }
                }else{
                    fields.add(new ExportDataType.ExportDataTypeField(fieldName, isEnum, onlyValues, fieldType.identifier(), fieldType.typeName()));
                }          
                
            }
        }

        return new ExportDataType(name, identifier, fields);
    }

    private ExportServiceTypeDef exportService(Class<?> serviceClass){
        // get annotation
        ExportService exportService = serviceClass.getAnnotation(ExportService.class);

        String name = exportService.serviceName();
        String identifier = exportService.identifier();
        List<ExportServiceTypeDef.ExportServiceFunctionDef> functions = new LinkedList<>();

        return new ExportServiceTypeDef(name, identifier, functions);

    }
}
