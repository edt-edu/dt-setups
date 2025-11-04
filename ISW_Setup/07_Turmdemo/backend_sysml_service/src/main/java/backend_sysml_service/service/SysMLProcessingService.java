package backend_sysml_service.service;

import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import backend_sysml_service.CollectorUtil;
import backend_sysml_service.State;
import backend_sysml_service.StorageUnit;
import backend_sysml_service.SysMLDeactivationUtil;
import backend_sysml_service.SysMLWriterUtil;
import backend_sysml_service.Transition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
// ...existing imports...
import java.util.*;

@Service
public class SysMLProcessingService {

    public String processSysmlModel(HashMap<String, Boolean> units)
            throws IOException, TemplateException {
        // Load the SysML model file from resources (immer im Ressourcenordner)
        InputStream sysmlStream = getClass().getClassLoader().getResourceAsStream("WarehouseV3.2_FULL_NOT_PARSEABLE.sysml");
        if (sysmlStream == null) {
            throw new FileNotFoundException("Resource not found!");
        }
        // Schreibe den InputStream in eine temporäre Datei
        File tempFile = File.createTempFile("sysml", ".sysml");
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
            sysmlStream.transferTo(out);
        }
        // Load the deactivated Units of the POST body to Set
        Set<String> deactivatedUnits = new HashSet<>(); 
        for (Map.Entry<String, Boolean> entry : units.entrySet()) {
            Boolean isActive = entry.getValue();
            if(isActive == false) {
                deactivatedUnits.add(entry.getKey());
            }
        }
        // Initialize writer, and deactivation utility
        SysMLWriterUtil writer = new SysMLWriterUtil();
        SysMLDeactivationUtil deactivationUtil = new SysMLDeactivationUtil();

        // Use CollectorUtil to extract all SysML objects from the temp file
        Object[][] allObjects = CollectorUtil.collectAllSysMLObjects(tempFile);

        // Assign the extracted objects to the corresponding lists
        @SuppressWarnings("unchecked")
        List<StorageUnit> storageUnits = (List<StorageUnit>) (List<?>) java.util.Arrays.asList(allObjects[0]);
        @SuppressWarnings("unchecked")
        List<State> states = (List<State>) (List<?>) java.util.Arrays.asList(allObjects[3]);
        @SuppressWarnings("unchecked")
        List<Transition> transitions = (List<Transition>) (List<?>) java.util.Arrays.asList(allObjects[4]);

        // Run the deactivation workflow to filter states and transitions
        SysMLDeactivationUtil.DeactivationResult deactivationResult = deactivationUtil
                .handleDeactivationWorkflow(states, transitions, storageUnits, deactivatedUnits);

        // Prepare data model for Freemarker template rendering
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("relevantStates", deactivationResult.states);
        dataModel.put("relevantTransitionsToCmdReceived", deactivationResult.relevantTransitionsToCmdReceived);
        dataModel.put("relevantTransitionsFixedGuardToStates", deactivationResult.relevantTransitionsFixedGuardToStates);
        dataModel.put("fixedSetupTransitions", deactivationResult.fixedSetupTransitions);
        dataModel.put("activeStorageUnits", deactivationResult.activeStorageUnits);

        // Generate a filtered SysML model using Freemarker templates
        writer.writeSysMLModelFreemarker(dataModel, "filtered_model.sysml");
        // Nutze den absoluten Pfad im Container
        runPythonWithModel("output/filtered_model.sysml", "/app/src/main/resources/SysOnBackendScript.py");
        return "Filtered SysML model written to filtered_model.sysml and SysOn Updated"; 
    }

    private void runPythonWithModel(String modelPath, String pythonScriptPath) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", pythonScriptPath);
            pb.redirectErrorStream(true); // Kombiniere stdout und stderr
            process = pb.start();

            // Stream the model file directly to Python
            try (BufferedReader fileReader = new BufferedReader(new FileReader(modelPath));
                 BufferedWriter pythonWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                String fileLine;
                while ((fileLine = fileReader.readLine()) != null) {
                    pythonWriter.write(fileLine);
                    pythonWriter.newLine();
                }
                pythonWriter.flush();
            } catch (IOException ioEx) {
                System.err.println("Fehler beim Schreiben in den Python-Stream: " + ioEx.getMessage());
                ioEx.printStackTrace();
            }

            // Lies die kombinierte Ausgabe von Python (stdout + stderr)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python Output: " + line);
                }
            } catch (IOException ioEx) {
                System.err.println("Fehler beim Lesen der Python-Ausgabe: " + ioEx.getMessage());
                ioEx.printStackTrace();
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Fehler beim Starten des Python-Prozesses: " + e.getMessage());
            e.printStackTrace();
            if (process != null) {
                try {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Python Error: " + line);
                    }
                } catch (IOException ioEx) {
                    System.err.println("Fehler beim Lesen des Python-Fehlerstreams: " + ioEx.getMessage());
                }
            }
        }
    }
}