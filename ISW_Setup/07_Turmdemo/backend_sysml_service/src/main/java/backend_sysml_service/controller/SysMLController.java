package backend_sysml_service.controller;

import backend_sysml_service.service.SysMLProcessingService;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sysml")
public class SysMLController {

    @Autowired
    private SysMLProcessingService sysmlProcessingService;

    @PostMapping("/process")
    public ResponseEntity<String> processModel(@RequestBody HashMap<String, Boolean> units) {
        try {
            String result = sysmlProcessingService.processSysmlModel(units);
            return ResponseEntity.ok(result);
        } catch (IOException | TemplateException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/model")
    public ResponseEntity<String> getCurrentSysMLModel() {
        try {
            // Lies die Datei direkt aus /output/filtered_model.sysml
            File file = new File("output/filtered_model.sysml");
            System.out.println("Versuche Modell zu lesen von: " + file.getAbsolutePath());
            if (!file.exists()) {
                return ResponseEntity.status(404).body("Model file not found at: " + file.getAbsolutePath());
            }
            String content = java.nio.file.Files.readString(file.toPath());
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error reading model: " + e.getMessage());
        }
    }
}
