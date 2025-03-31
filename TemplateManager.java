package com.mobiledev.androidstudio.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager for project templates
 */
public class TemplateManager {
    private static final String TAG = "TemplateManager";
    private static final String TEMPLATES_DIR = "templates";
    
    private final Context context;
    private final Map<String, TemplateInfo> templates;
    
    /**
     * Constructor for TemplateManager
     * 
     * @param context Application context
     */
    public TemplateManager(Context context) {
        this.context = context;
        this.templates = new HashMap<>();
        
        // Load templates
        loadTemplates();
    }
    
    /**
     * Load available templates from assets
     */
    private void loadTemplates() {
        try {
            String[] templateDirs = context.getAssets().list(TEMPLATES_DIR);
            if (templateDirs != null) {
                for (String templateName : templateDirs) {
                    loadTemplateInfo(templateName);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading templates: " + e.getMessage());
        }
    }
    
    /**
     * Load template information
     * 
     * @param templateName Name of the template
     */
    private void loadTemplateInfo(String templateName) {
        try {
            String infoPath = TEMPLATES_DIR + "/" + templateName + "/info.txt";
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(infoPath)));
            
            String name = reader.readLine();
            String description = reader.readLine();
            
            templates.put(templateName, new TemplateInfo(name, description));
            
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error loading template info for " + templateName + ": " + e.getMessage());
        }
    }
    
    /**
     * Get a list of available template names
     * 
     * @return List of template names
     */
    public List<String> getTemplateNames() {
        return new ArrayList<>(templates.keySet());
    }
    
    /**
     * Get template info by name
     * 
     * @param templateName Name of the template
     * @return TemplateInfo or null if not found
     */
    public TemplateInfo getTemplateInfo(String templateName) {
        return templates.get(templateName);
    }
    
    /**
     * Apply a template to a project directory
     * 
     * @param templateName Name of the template
     * @param projectDir Project directory
     * @param packageName Package name for the project
     * @return true if successful
     */
    public boolean applyTemplate(String templateName, File projectDir, String packageName) {
        if (!templates.containsKey(templateName)) {
            Log.e(TAG, "Template not found: " + templateName);
            return false;
        }
        
        try {
            // List all files in the template
            List<String> templateFiles = listTemplateFiles(templateName);
            
            // Copy and process each file
            for (String relativePath : templateFiles) {
                processTemplateFile(templateName, relativePath, projectDir, packageName);
            }
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error applying template: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * List all files in a template recursively
     * 
     * @param templateName Name of the template
     * @return List of relative file paths
     * @throws IOException If an I/O error occurs
     */
    private List<String> listTemplateFiles(String templateName) throws IOException {
        List<String> result = new ArrayList<>();
        String basePath = TEMPLATES_DIR + "/" + templateName;
        
        listTemplateFilesRecursive(basePath, "", result);
        
        return result;
    }
    
    /**
     * Recursively list template files
     * 
     * @param basePath Base path in assets
     * @param relativePath Current relative path
     * @param result List to add files to
     * @throws IOException If an I/O error occurs
     */
    private void listTemplateFilesRecursive(String basePath, String relativePath, List<String> result) 
            throws IOException {
        String fullPath = basePath + (relativePath.isEmpty() ? "" : "/" + relativePath);
        String[] entries = context.getAssets().list(fullPath);
        
        if (entries != null && entries.length > 0) {
            // Directory
            for (String entry : entries) {
                String newRelativePath = relativePath.isEmpty() ? entry : relativePath + "/" + entry;
                listTemplateFilesRecursive(basePath, newRelativePath, result);
            }
        } else {
            // File
            // Skip info.txt which is used for template metadata
            if (!relativePath.equals("info.txt")) {
                result.add(relativePath);
            }
        }
    }
    
    /**
     * Process a template file (copy and replace placeholders)
     * 
     * @param templateName Name of the template
     * @param relativePath Relative path of the file
     * @param projectDir Project directory
     * @param packageName Package name for the project
     * @throws IOException If an I/O error occurs
     */
    private void processTemplateFile(String templateName, String relativePath, 
                                    File projectDir, String packageName) throws IOException {
        String templatePath = TEMPLATES_DIR + "/" + templateName + "/" + relativePath;
        
        // Process path placeholders (e.g., __PACKAGE_DIR__ -> com/example/app)
        String processedPath = processPathPlaceholders(relativePath, packageName);
        File destFile = new File(projectDir, processedPath);
        
        // Create parent directories if needed
        if (!destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directories: " + destFile.getParentFile());
            }
        }
        
        // Check if this is a text file that needs content processing
        if (isTextFile(relativePath)) {
            // Read content and process placeholders
            String content = readTemplateFile(templatePath);
            String processedContent = processContentPlaceholders(content, packageName);
            
            // Write processed content
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(processedContent.getBytes());
            }
        } else {
            // Binary file, just copy as-is
            try (InputStream in = context.getAssets().open(templatePath);
                OutputStream out = new FileOutputStream(destFile)) {
                
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }
    }
    
    /**
     * Process path placeholders in a file path
     * 
     * @param path File path
     * @param packageName Package name for the project
     * @return Processed path
     */
    private String processPathPlaceholders(String path, String packageName) {
        // Replace __PACKAGE_DIR__ with package directory structure (com/example/app)
        String packageDir = packageName.replace('.', '/');
        return path.replace("__PACKAGE_DIR__", packageDir);
    }
    
    /**
     * Process content placeholders in a file
     * 
     * @param content File content
     * @param packageName Package name for the project
     * @return Processed content
     */
    private String processContentPlaceholders(String content, String packageName) {
        // Replace __PACKAGE_NAME__ with actual package name
        return content.replace("__PACKAGE_NAME__", packageName);
    }
    
    /**
     * Check if a file is a text file based on extension
     * 
     * @param path File path
     * @return true if it's a text file
     */
    private boolean isTextFile(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.endsWith(".java") || lowerPath.endsWith(".xml") || 
               lowerPath.endsWith(".gradle") || lowerPath.endsWith(".properties") ||
               lowerPath.endsWith(".txt") || lowerPath.endsWith(".md") || 
               lowerPath.endsWith(".json") || lowerPath.endsWith(".kt");
    }
    
    /**
     * Read a template file as text
     * 
     * @param path Path to the file in assets
     * @return File content as string
     * @throws IOException If an I/O error occurs
     */
    private String readTemplateFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(path)))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Class to hold template information
     */
    public static class TemplateInfo {
        private final String name;
        private final String description;
        
        public TemplateInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
}