package com.mobiledev.androidstudio.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mobiledev.androidstudio.utils.FileUtils;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Helper class for Gemini AI integration
 */
public class GeminiAiHelper {

    private static final String TAG = "GeminiAiHelper";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    
    private Context context;
    private String apiKey;
    private Handler mainHandler;

    /**
     * Constructor
     *
     * @param context Context
     */
    public GeminiAiHelper(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Set the API key
     *
     * @param apiKey API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Get code completion suggestions
     *
     * @param code Current code
     * @param position Cursor position
     * @param language Programming language
     * @param callback Callback for suggestions
     */
    public void getCodeCompletions(String code, int position, String language, CompletionCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not set");
            return;
        }
        
        // Create a substring of the code up to the cursor position
        String codePrefix = code.substring(0, position);
        
        // Create the prompt
        String prompt = "Complete the following " + language + " code. Only provide the completion, not the entire code:\n\n" + codePrefix;
        
        // Execute in background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create request payload
                String payload = "{"
                        + "\"contents\": [{"
                        + "  \"parts\": [{"
                        + "    \"text\": \"" + escapeJson(prompt) + "\""
                        + "  }]"
                        + "}],"
                        + "\"generationConfig\": {"
                        + "  \"temperature\": 0.2,"
                        + "  \"topP\": 0.8,"
                        + "  \"topK\": 10,"
                        + "  \"maxOutputTokens\": 200"
                        + "}}";
                
                // Make the API request
                String response = makeApiRequest(payload);
                
                // Parse the response
                String completion = parseResponse(response);
                
                // Return the result on the main thread
                mainHandler.post(() -> {
                    if (completion != null) {
                        callback.onCompleted(completion);
                    } else {
                        callback.onError("Failed to parse response");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error getting code completions", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Generate documentation for code
     *
     * @param code Code to document
     * @param language Programming language
     * @param callback Callback for documentation
     */
    public void generateDocumentation(String code, String language, CompletionCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not set");
            return;
        }
        
        // Create the prompt
        String prompt = "Generate documentation for the following " + language + " code. Include function descriptions, parameter explanations, and return value details:\n\n" + code;
        
        // Execute in background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create request payload
                String payload = "{"
                        + "\"contents\": [{"
                        + "  \"parts\": [{"
                        + "    \"text\": \"" + escapeJson(prompt) + "\""
                        + "  }]"
                        + "}],"
                        + "\"generationConfig\": {"
                        + "  \"temperature\": 0.2,"
                        + "  \"topP\": 0.8,"
                        + "  \"topK\": 10,"
                        + "  \"maxOutputTokens\": 1000"
                        + "}}";
                
                // Make the API request
                String response = makeApiRequest(payload);
                
                // Parse the response
                String documentation = parseResponse(response);
                
                // Return the result on the main thread
                mainHandler.post(() -> {
                    if (documentation != null) {
                        callback.onCompleted(documentation);
                    } else {
                        callback.onError("Failed to parse response");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error generating documentation", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Debug code and suggest fixes
     *
     * @param code Code to debug
     * @param error Error message
     * @param language Programming language
     * @param callback Callback for fixes
     */
    public void debugCode(String code, String error, String language, CompletionCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not set");
            return;
        }
        
        // Create the prompt
        String prompt = "Debug the following " + language + " code and suggest fixes. The error is: " + error + "\n\nCode:\n" + code;
        
        // Execute in background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create request payload
                String payload = "{"
                        + "\"contents\": [{"
                        + "  \"parts\": [{"
                        + "    \"text\": \"" + escapeJson(prompt) + "\""
                        + "  }]"
                        + "}],"
                        + "\"generationConfig\": {"
                        + "  \"temperature\": 0.2,"
                        + "  \"topP\": 0.8,"
                        + "  \"topK\": 10,"
                        + "  \"maxOutputTokens\": 1000"
                        + "}}";
                
                // Make the API request
                String response = makeApiRequest(payload);
                
                // Parse the response
                String fix = parseResponse(response);
                
                // Return the result on the main thread
                mainHandler.post(() -> {
                    if (fix != null) {
                        callback.onCompleted(fix);
                    } else {
                        callback.onError("Failed to parse response");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error debugging code", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Analyze a project and provide insights
     *
     * @param projectPath Path to the project
     * @param callback Callback for insights
     */
    public void analyzeProject(String projectPath, CompletionCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not set");
            return;
        }
        
        // Execute in background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Get a list of files in the project
                StringBuilder fileContents = new StringBuilder();
                
                File projectDir = new File(projectPath);
                File[] files = projectDir.listFiles();
                
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && isSupportedFile(file.getName())) {
                            fileContents.append("File: ").append(file.getName()).append("\n\n");
                            fileContents.append(FileUtils.readFile(file)).append("\n\n");
                            
                            // Limit the size to avoid exceeding API limits
                            if (fileContents.length() > 10000) {
                                fileContents.append("... (truncated due to size limitations)");
                                break;
                            }
                        }
                    }
                }
                
                // Create the prompt
                String prompt = "Analyze the following project files and provide insights on code quality, suggestions for improvements, and potential issues:\n\n" + fileContents.toString();
                
                // Create request payload
                String payload = "{"
                        + "\"contents\": [{"
                        + "  \"parts\": [{"
                        + "    \"text\": \"" + escapeJson(prompt) + "\""
                        + "  }]"
                        + "}],"
                        + "\"generationConfig\": {"
                        + "  \"temperature\": 0.2,"
                        + "  \"topP\": 0.8,"
                        + "  \"topK\": 10,"
                        + "  \"maxOutputTokens\": 2000"
                        + "}}";
                
                // Make the API request
                String response = makeApiRequest(payload);
                
                // Parse the response
                String insights = parseResponse(response);
                
                // Return the result on the main thread
                mainHandler.post(() -> {
                    if (insights != null) {
                        callback.onCompleted(insights);
                    } else {
                        callback.onError("Failed to parse response");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing project", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Check if a file is supported for analysis
     *
     * @param fileName File name
     * @return True if supported
     */
    private boolean isSupportedFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return extension.equals("java") || extension.equals("kt") || extension.equals("xml") || 
                extension.equals("gradle") || extension.equals("py") || extension.equals("js") || 
                extension.equals("html") || extension.equals("css") || extension.equals("json");
    }

    /**
     * Make an API request to the Gemini API
     *
     * @param payload Request payload
     * @return Response
     */
    private String makeApiRequest(String payload) {
        // TODO: Implement actual API request
        // For now, return a mock response
        return "{"
                + "\"candidates\": [{"
                + "  \"content\": {"
                + "    \"parts\": [{"
                + "      \"text\": \"// This is a sample completion\\npublic void sampleMethod() {\\n    // Your code here\\n}\""
                + "    }]"
                + "  }"
                + "}]"
                + "}";
    }

    /**
     * Parse the API response
     *
     * @param response API response
     * @return Parsed response
     */
    private String parseResponse(String response) {
        try {
            // Extract the text from the response
            int startIndex = response.indexOf("\"text\": \"") + 9;
            int endIndex = response.indexOf("\"", startIndex);
            
            if (startIndex >= 9 && endIndex > startIndex) {
                return response.substring(startIndex, endIndex).replace("\\n", "\n").replace("\\\"", "\"");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
        }
        
        return null;
    }

    /**
     * Escape JSON string
     *
     * @param input Input string
     * @return Escaped string
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Interface for completion callbacks
     */
    public interface CompletionCallback {
        void onCompleted(String result);
        void onError(String error);
    }
}