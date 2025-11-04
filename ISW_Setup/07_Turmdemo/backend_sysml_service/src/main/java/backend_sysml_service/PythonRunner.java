package backend_sysml_service;
import java.io.*;

public class PythonRunner { 
    public static void main(String[] args) {
        try {
            // Adjust the path to the Python script if necessary
            ProcessBuilder pb = new ProcessBuilder("python3", "src/main/resources/SysOnScript.py");
            Process process = pb.start();

            // Lies das aktuelle Model aus output/filtered_model.sysml
            StringBuilder sysMLv2Model = new StringBuilder();
            try (BufferedReader fileReader = new BufferedReader(new FileReader("output/filtered_model.sysml"))) {
                String fileLine;
                while ((fileLine = fileReader.readLine()) != null) {
                    sysMLv2Model.append(fileLine).append(System.lineSeparator());
                }
            }

            // Sende das Model an das Python-Skript
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
            writer.write(sysMLv2Model.toString());
            writer.flush();
            writer.close();

            // Read output from Python, if desired. Otherwise, this part can be omitted.
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Python Output: " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
