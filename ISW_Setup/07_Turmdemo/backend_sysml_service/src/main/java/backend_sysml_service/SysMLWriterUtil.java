package backend_sysml_service;

import freemarker.template.*;
import java.io.*;
import java.util.*;

/**
 * Utility for generating a filtered SysMLv2 model based on active StorageUnits.
 */
public class SysMLWriterUtil {
    /**
     * Writes a SysML model to a file using Freemarker template.
     *
     * @param dataModel      The data model containing SysML objects to be written.
     * @param outputFilePath The path where the SysML model will be saved.
     * @throws IOException        If an I/O error occurs during writing.
     * @throws TemplateException If there is an error processing the Freemarker template.
     */
    public void writeSysMLModelFreemarker(
            Map<String, Object> dataModel,
            String outputFilePath
    ) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");

        Template modelTemplate = cfg.getTemplate("model.ftl");

        // Stelle sicher, dass das output-Verzeichnis existiert
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Schreibe die Datei ins output-Verzeichnis
        File outFile = new File(outputDir, outputFilePath);
        try (Writer out = new FileWriter(outFile)) {
            modelTemplate.process(dataModel, out);
        }
    }


}