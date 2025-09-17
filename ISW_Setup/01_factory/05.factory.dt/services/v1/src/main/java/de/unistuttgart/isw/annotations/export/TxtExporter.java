package de.unistuttgart.isw.annotations.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

public class TxtExporter implements Exporter {

    private String filePath;
    private List<ExportDataType> dataTypes = new LinkedList<>();
    private List<ExportServiceTypeDef> serviceTypes= new LinkedList<>();

    // output buffer
    private StringBuilder outputBuffer;

    public TxtExporter(final String filePath) {
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

    @Override
    public void finished() {

        for (ExportDataType type : dataTypes) {
            outputBuffer.append(type.toString()).append("\n");
        }
      
        for (ExportServiceTypeDef serviceType : serviceTypes) {
            outputBuffer.append(serviceType.toString()).append("\n");
        }

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
    
}
