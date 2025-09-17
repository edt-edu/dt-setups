package org.example.database.fileParser.stepFileHandling;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.database.fileParser.FileContext;
import org.example.database.fileParser.QueryableProperty;

/**
 * Collection of property extractors for STEP files
 */
public class StepFileProperties {
    // Common patterns used by multiple property extractors
    private static final Pattern CARTESIAN_POINT_PATTERN = Pattern.compile(
            "CARTESIAN_POINT\\(\\s*'[^']*'\\s*,\\s*\\(\\s*([\\-0-9.]+)\\s*,\\s*([\\-0-9.]+)\\s*,\\s*([\\-0-9.]+)\\s*\\)\\s*\\)");
    
    private static final Pattern COLOUR_RGB_PATTERN = Pattern.compile(
            "COLOUR_RGB\\(\\s*'[^']*'\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*\\)");
    
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile(
            "PRODUCT_DEFINITION\\(\\s*'([^']*)'\\s*,");
    
    private static final Pattern PRODUCT_DEFINITION_PATTERN = Pattern.compile(
            "PRODUCT_DEFINITION\\([^,]*,[^,]*,[^,]*,([^,]*)\\)");
            
    private static final Pattern PRODUCT_NAME_PATTERN2 = Pattern.compile(
            "PRODUCT\\([^,]*,\\s*'([^']*)'\\s*,");
            
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "FILE_NAME\\([^,]*,\\s*'([^']*)'\\s*,");
    
    // Common content types for all STEP property extractors
    private static final String[] STEP_CONTENT_TYPES = new String[] {
            "application/step", "application/x-step", "model/step", "application/stp"
    };
    
    /**
     * Helper method to round a double value to 2 decimal places
     * @param value The value to round
     * @return The rounded value
     */
    private static double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    
    /**
     * Helper method to extract all points from a STEP file
     */
    private static List<double[]> extractPoints(FileContext context) throws IOException {
        List<double[]> points = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(context.getOriginalFile().toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = CARTESIAN_POINT_PATTERN.matcher(line);
                if (matcher.find()) {
                    double x = roundToTwoDecimalPlaces(Double.parseDouble(matcher.group(1)));
                    double y = roundToTwoDecimalPlaces(Double.parseDouble(matcher.group(2)));
                    double z = roundToTwoDecimalPlaces(Double.parseDouble(matcher.group(3)));
                    points.add(new double[]{x, y, z});
                }
            }
        }
        
        return points;
    }
    
    /**
     * Helper method to extract color from a STEP file
     */
    private static String extractColor(FileContext context) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(context.getOriginalFile().toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = COLOUR_RGB_PATTERN.matcher(line);
                if (matcher.find()) {
                    double r = roundToTwoDecimalPlaces(Double.parseDouble(matcher.group(1)));
                    double g = roundToTwoDecimalPlaces(Double.parseDouble(matcher.group(2)));
                    double b = roundToTwoDecimalPlaces(Double.parseDouble(matcher.group(3)));
                    
                    return String.format("#%02X%02X%02X", 
                            (int)(r*255), (int)(g*255), (int)(b*255));
                }
            }
        }
        
        // Default color if none found
        return "#CCCCCC";
    }
    
    /**
     * Helper method to extract part name from a STEP file
     */
    private static String extractPartName(FileContext context) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(context.getOriginalFile().toPath())) {
            String line;
            String partName = null;
            
            // First try to find the product name directly
            while ((line = reader.readLine()) != null) {
                // Try the PRODUCT_DEFINITION pattern
                Matcher matcher = PRODUCT_NAME_PATTERN.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1).trim();
                    if (!name.isEmpty()) {
                        return name;
                    }
                }
                
                // Try the PRODUCT pattern
                matcher = PRODUCT_NAME_PATTERN2.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1).trim();
                    if (!name.isEmpty()) {
                        return name;
                    }
                }
                
                // Try the FILE_NAME pattern as a fallback
                matcher = FILE_NAME_PATTERN.matcher(line);
                if (matcher.find() && partName == null) {
                    partName = matcher.group(1).trim();
                    // Don't return immediately, keep looking for a better name
                }
            }
            
            // If we found a file name but no product name, use the file name
            if (partName != null && !partName.isEmpty()) {
                return partName;
            }
            
            // If not found, try the product definition approach
            reader.close();
            try (BufferedReader reader2 = Files.newBufferedReader(context.getOriginalFile().toPath())) {
                while ((line = reader2.readLine()) != null) {
                    Matcher matcher = PRODUCT_DEFINITION_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String productRef = matcher.group(1).trim();
                        // Extract the ID number from the reference
                        if (productRef.startsWith("#")) {
                            String idStr = productRef.substring(1);
                            try {
                                int id = Integer.parseInt(idStr);
                                // Now look for this ID in the file
                                reader2.close();
                                try (BufferedReader reader3 = Files.newBufferedReader(context.getOriginalFile().toPath())) {
                                    while ((line = reader3.readLine()) != null) {
                                        if (line.startsWith("#" + id + "=")) {
                                            // Found the product definition, extract the name
                                            if (line.contains("''")) {
                                                int startIdx = line.indexOf("''") + 2;
                                                int endIdx = line.indexOf("''", startIdx);
                                                if (endIdx > startIdx) {
                                                    String name = line.substring(startIdx, endIdx).trim();
                                                    if (!name.isEmpty()) {
                                                        return name;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // Ignore and continue
                            }
                        }
                    }
                }
            }
        }
        
        // If no part name found, use the filename without extension
        String fileName = context.getOriginalFile().getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String nameWithoutExtension = fileName.substring(0, dotIndex).trim();
            if (!nameWithoutExtension.isEmpty()) {
                return nameWithoutExtension;
            }
        }
        
        // Last resort
        return "Unknown Part";
    }
    
    /**
     * Extracts the width property from STEP files
     */
    public static class WidthProperty implements QueryableProperty {
        @Override
        public Object extract(FileContext context) throws IOException {
            List<double[]> points = extractPoints(context);
            
            if (points.isEmpty()) {
                return 0.0;
            }
            
            // Find min and max Y values
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (double[] point : points) {
                minY = Math.min(minY, point[1]);
                maxY = Math.max(maxY, point[1]);
            }
            
            return roundToTwoDecimalPlaces(Math.max(maxY - minY, 1.0));
        }
        
        @Override
        public String getPropertyPath() {
            return "file.dimensions.width";
        }
        
        @Override
        public String[] getSupportedContentTypes() {
            return STEP_CONTENT_TYPES;
        }
    }
    
    /**
     * Extracts the length property from STEP files
     */
    public static class LengthProperty implements QueryableProperty {
        @Override
        public Object extract(FileContext context) throws IOException {
            List<double[]> points = extractPoints(context);
            
            if (points.isEmpty()) {
                return 0.0;
            }
            
            // Find min and max X values
            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            for (double[] point : points) {
                minX = Math.min(minX, point[0]);
                maxX = Math.max(maxX, point[0]);
            }
            
            return roundToTwoDecimalPlaces(Math.max(maxX - minX, 1.0));
        }
        
        @Override
        public String getPropertyPath() {
            return "file.dimensions.length";
        }
        
        @Override
        public String[] getSupportedContentTypes() {
            return STEP_CONTENT_TYPES;
        }
    }
    
    /**
     * Extracts the height property from STEP files
     */
    public static class HeightProperty implements QueryableProperty {
        @Override
        public Object extract(FileContext context) throws IOException {
            List<double[]> points = extractPoints(context);
            
            if (points.isEmpty()) {
                return 0.0;
            }
            
            // Find min and max Z values
            double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
            for (double[] point : points) {
                minZ = Math.min(minZ, point[2]);
                maxZ = Math.max(maxZ, point[2]);
            }
            
            return roundToTwoDecimalPlaces(Math.max(maxZ - minZ, 1.0));
        }
        
        @Override
        public String getPropertyPath() {
            return "file.dimensions.height";
        }
        
        @Override
        public String[] getSupportedContentTypes() {
            return STEP_CONTENT_TYPES;
        }
    }
    
    /**
     * Extracts the color property from STEP files
     */
    public static class ColorProperty implements QueryableProperty {
        @Override
        public Object extract(FileContext context) throws IOException {
            return extractColor(context);
        }
        
        @Override
        public String getPropertyPath() {
            return "file.color";
        }
        
        @Override
        public String[] getSupportedContentTypes() {
            return STEP_CONTENT_TYPES;
        }
    }
    
    /**
     * Extracts the part name property from STEP files
     */
    public static class PartNameProperty implements QueryableProperty {
        @Override
        public Object extract(FileContext context) throws IOException {
            return extractPartName(context);
        }
        
        @Override
        public String getPropertyPath() {
            return "file.partName";
        }
        
        @Override
        public String[] getSupportedContentTypes() {
            return STEP_CONTENT_TYPES;
        }
    }
}
