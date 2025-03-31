package com.mobiledev.androidstudio.ai;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.mobiledev.androidstudio.utils.PerformanceOptimizer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manager class for Google Gemini AI integration.
 * Handles interactions with the Gemini API for code generation, completion, and assistance.
 */
public class GeminiAIManager {

    private static final String TAG = "GeminiAIManager";
    
    // Default model to use for general coding tasks
    private static final String DEFAULT_MODEL = "gemini-1.5-pro";
    
    // Executor for background processing
    private static final Executor executor = Executors.newCachedThreadPool();
    
    // Singleton instance
    private static GeminiAIManager instance;
    
    // Context reference
    private final Context context;
    
    // API key for Gemini
    private String apiKey;
    
    // Generative model instance
    private GenerativeModelFutures model;
    
    // Callback interface for AI responses
    public interface AIResponseCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    /**
     * Private constructor for singleton pattern
     *
     * @param context Application context
     */
    private GeminiAIManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Get the singleton instance
     *
     * @param context Application context
     * @return GeminiAIManager instance
     */
    public static synchronized GeminiAIManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new GeminiAIManager(context);
        }
        return instance;
    }

    /**
     * Initialize the manager with an API key
     *
     * @param apiKey Gemini API key
     * @return true if initialization was successful
     */
    public boolean initialize(@NonNull String apiKey) {
        this.apiKey = apiKey;
        try {
            // Create configuration for text generation
            GenerationConfig config = new GenerationConfig.Builder()
                    .temperature(0.4f)  // Lower temperature for more deterministic responses
                    .topK(40)
                    .topP(0.95f)
                    .maxOutputTokens(8192)  // Support long responses for code generation
                    .build();
            
            // Create generative model
            GenerativeModel generativeModel = new GenerativeModel(
                    DEFAULT_MODEL,
                    apiKey,
                    config
            );
            
            this.model = GenerativeModelFutures.from(generativeModel);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Gemini AI", e);
            return false;
        }
    }

    /**
     * Check if the manager is initialized
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return model != null && apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Generate code based on a prompt
     *
     * @param prompt Description of the code to generate
     * @param language Programming language (java, kotlin, etc.)
     * @param callback Callback for the response
     */
    public void generateCode(@NonNull String prompt, @NonNull String language, @NonNull AIResponseCallback callback) {
        if (!isInitialized()) {
            callback.onError("AI Manager not initialized. Please set API key first.");
            return;
        }
        
        // Enhance prompt with language specification
        String enhancedPrompt = "Generate " + language + " code for the following: " + prompt 
                + "\n\nProvide only the code without explanations. Use best practices and optimize for performance.";
        
        generateContent(enhancedPrompt, callback);
    }

    /**
     * Provide code completion suggestions
     *
     * @param codeContext Current code context
     * @param language Programming language
     * @param callback Callback for the response
     */
    public void completeCode(@NonNull String codeContext, @NonNull String language, @NonNull AIResponseCallback callback) {
        if (!isInitialized()) {
            callback.onError("AI Manager not initialized. Please set API key first.");
            return;
        }
        
        String prompt = "Complete the following " + language + " code:\n\n" + codeContext 
                + "\n\nProvide only the completion code, no explanations.";
        
        generateContent(prompt, callback);
    }

    /**
     * Get an explanation of a code snippet
     *
     * @param code Code to explain
     * @param language Programming language
     * @param callback Callback for the response
     */
    public void explainCode(@NonNull String code, @NonNull String language, @NonNull AIResponseCallback callback) {
        if (!isInitialized()) {
            callback.onError("AI Manager not initialized. Please set API key first.");
            return;
        }
        
        String prompt = "Explain this " + language + " code in detail, highlighting key concepts and best practices:\n\n" + code;
        
        generateContent(prompt, callback);
    }

    /**
     * Fix issues in a code snippet
     *
     * @param code Code with issues
     * @param language Programming language
     * @param errorMessage Error message if available
     * @param callback Callback for the response
     */
    public void fixCode(@NonNull String code, @NonNull String language, @Nullable String errorMessage, @NonNull AIResponseCallback callback) {
        if (!isInitialized()) {
            callback.onError("AI Manager not initialized. Please set API key first.");
            return;
        }
        
        String prompt = "Fix issues in this " + language + " code:";
        
        if (errorMessage != null && !errorMessage.isEmpty()) {
            prompt += "\n\nError message: " + errorMessage;
        }
        
        prompt += "\n\nCode:\n" + code + "\n\nProvide the corrected code.";
        
        generateContent(prompt, callback);
    }

    /**
     * Ask a general coding question
     *
     * @param question Question about coding
     * @param callback Callback for the response
     */
    public void askCodingQuestion(@NonNull String question, @NonNull AIResponseCallback callback) {
        if (!isInitialized()) {
            callback.onError("AI Manager not initialized. Please set API key first.");
            return;
        }
        
        String prompt = "Answer this Android development question: " + question 
                + "\n\nProvide a detailed and helpful response.";
        
        generateContent(prompt, callback);
    }

    /**
     * Generate content from Gemini AI based on a prompt
     *
     * @param prompt Prompt to send to the model
     * @param callback Callback for the response
     */
    private void generateContent(@NonNull String prompt, @NonNull AIResponseCallback callback) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();
        
        ListenableFuture<GenerateContentResponse> future = model.generateContent(content);
        
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String response = result.getText();
                PerformanceOptimizer.runOnMainThread(() -> callback.onResponse(response));
            }
            
            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error generating content from Gemini", t);
                String errorMessage = "Error: " + t.getMessage();
                PerformanceOptimizer.runOnMainThread(() -> callback.onError(errorMessage));
            }
        }, MoreExecutors.directExecutor());
    }
}