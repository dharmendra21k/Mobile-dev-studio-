package com.mobiledev.androidstudio.ai;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.tabs.TabLayout;
import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.utils.PerformanceOptimizer;

/**
 * Activity for interacting with the Gemini AI assistant.
 * Provides functionality for code generation, completion, explanation, fixing, and answering questions.
 */
public class GeminiAssistantActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "gemini_prefs";
    private static final String KEY_API_KEY = "api_key";
    
    // UI Elements
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private EditText promptInput;
    private EditText codeInput;
    private Spinner languageSpinner;
    private Button submitButton;
    private TextView resultText;
    private ProgressBar progressBar;
    private NestedScrollView scrollView;
    
    // Gemini AI Manager
    private GeminiAIManager aiManager;
    
    // Current tab selection
    private int currentTab = 0;
    
    // Tab titles
    private final String[] tabTitles = {
            "Generate Code",
            "Complete Code",
            "Explain Code",
            "Fix Code",
            "Ask Question"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_assistant);
        
        // Initialize views
        initializeViews();
        
        // Set up toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Gemini AI Assistant");
        }
        
        // Set up language spinner
        setupLanguageSpinner();
        
        // Initialize AI manager
        aiManager = GeminiAIManager.getInstance(this);
        
        // Check if API key is already set
        checkApiKey();
        
        // Set up tab layout
        setupTabLayout();
        
        // Set up submit button
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    /**
     * Initialize views from layout
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        promptInput = findViewById(R.id.prompt_input);
        codeInput = findViewById(R.id.code_input);
        languageSpinner = findViewById(R.id.language_spinner);
        submitButton = findViewById(R.id.submit_button);
        resultText = findViewById(R.id.result_text);
        progressBar = findViewById(R.id.progress_bar);
        scrollView = findViewById(R.id.scroll_view);
    }

    /**
     * Set up language spinner with programming languages
     */
    private void setupLanguageSpinner() {
        String[] languages = {
                "Java",
                "Kotlin",
                "XML",
                "C++",
                "Python",
                "JavaScript",
                "HTML",
                "CSS",
                "SQL",
                "Shell"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
    }

    /**
     * Check if API key is already set, if not, prompt the user
     */
    private void checkApiKey() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String apiKey = prefs.getString(KEY_API_KEY, "");
        
        if (!apiKey.isEmpty()) {
            // Initialize with saved API key
            boolean success = aiManager.initialize(apiKey);
            if (!success) {
                showApiKeyDialog("The saved API key appears to be invalid. Please enter a valid Gemini API key.");
            }
        } else {
            // Prompt for API key
            showApiKeyDialog("Please enter your Gemini API key to use the AI assistant.");
        }
    }

    /**
     * Show dialog to enter API key
     *
     * @param message Message to display
     */
    private void showApiKeyDialog(String message) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_api_key, null);
        EditText apiKeyInput = dialogView.findViewById(R.id.api_key_input);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Gemini API Key")
                .setMessage(message)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Save", (d, which) -> {
                    String apiKey = apiKeyInput.getText().toString().trim();
                    if (!apiKey.isEmpty()) {
                        // Save and initialize
                        saveApiKey(apiKey);
                        boolean success = aiManager.initialize(apiKey);
                        if (!success) {
                            Toast.makeText(GeminiAssistantActivity.this, 
                                    "Failed to initialize with provided API key", 
                                    Toast.LENGTH_SHORT).show();
                            // Try again
                            showApiKeyDialog("The API key appears to be invalid. Please enter a valid Gemini API key.");
                        }
                    } else {
                        Toast.makeText(GeminiAssistantActivity.this, 
                                "API key cannot be empty", 
                                Toast.LENGTH_SHORT).show();
                        // Try again
                        showApiKeyDialog("API key cannot be empty. Please enter a valid Gemini API key.");
                    }
                })
                .setNegativeButton("Cancel", (d, which) -> finish())
                .create();
        
        dialog.show();
    }

    /**
     * Save API key to preferences
     *
     * @param apiKey API key to save
     */
    private void saveApiKey(String apiKey) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_API_KEY, apiKey).apply();
    }

    /**
     * Set up tab layout with tab titles
     */
    private void setupTabLayout() {
        for (String title : tabTitles) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateUIForTab(currentTab);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
        
        // Set initial UI
        updateUIForTab(currentTab);
    }

    /**
     * Update UI based on selected tab
     *
     * @param tabPosition Tab position
     */
    private void updateUIForTab(int tabPosition) {
        // Clear previous results
        resultText.setText("");
        
        switch (tabPosition) {
            case 0: // Generate Code
                promptInput.setVisibility(View.VISIBLE);
                promptInput.setHint("Describe the code you want to generate...");
                codeInput.setVisibility(View.GONE);
                languageSpinner.setVisibility(View.VISIBLE);
                break;
                
            case 1: // Complete Code
                promptInput.setVisibility(View.GONE);
                codeInput.setVisibility(View.VISIBLE);
                codeInput.setHint("Enter the code you want to complete...");
                languageSpinner.setVisibility(View.VISIBLE);
                break;
                
            case 2: // Explain Code
                promptInput.setVisibility(View.GONE);
                codeInput.setVisibility(View.VISIBLE);
                codeInput.setHint("Enter the code you want explained...");
                languageSpinner.setVisibility(View.VISIBLE);
                break;
                
            case 3: // Fix Code
                promptInput.setVisibility(View.GONE);
                codeInput.setVisibility(View.VISIBLE);
                codeInput.setHint("Enter the code you want to fix...");
                languageSpinner.setVisibility(View.VISIBLE);
                break;
                
            case 4: // Ask Question
                promptInput.setVisibility(View.VISIBLE);
                promptInput.setHint("Enter your Android development question...");
                codeInput.setVisibility(View.GONE);
                languageSpinner.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Handle submit button click based on current tab
     */
    private void handleSubmit() {
        if (!aiManager.isInitialized()) {
            Toast.makeText(this, "AI not initialized. Please set API key first.", Toast.LENGTH_SHORT).show();
            checkApiKey();
            return;
        }
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        
        // Get selected language if applicable
        String language = "java";
        if (languageSpinner.getVisibility() == View.VISIBLE) {
            language = languageSpinner.getSelectedItem().toString().toLowerCase();
        }
        
        // Clear previous result
        resultText.setText("");
        
        // Handle based on current tab
        switch (currentTab) {
            case 0: // Generate Code
                String generatePrompt = promptInput.getText().toString().trim();
                if (generatePrompt.isEmpty()) {
                    showError("Please enter a description of the code you want to generate.");
                    return;
                }
                
                aiManager.generateCode(generatePrompt, language, new GeminiAIManager.AIResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(response);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        showError(errorMessage);
                    }
                });
                break;
                
            case 1: // Complete Code
                String codeToComplete = codeInput.getText().toString().trim();
                if (codeToComplete.isEmpty()) {
                    showError("Please enter code to complete.");
                    return;
                }
                
                aiManager.completeCode(codeToComplete, language, new GeminiAIManager.AIResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(response);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        showError(errorMessage);
                    }
                });
                break;
                
            case 2: // Explain Code
                String codeToExplain = codeInput.getText().toString().trim();
                if (codeToExplain.isEmpty()) {
                    showError("Please enter code to explain.");
                    return;
                }
                
                aiManager.explainCode(codeToExplain, language, new GeminiAIManager.AIResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(response);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        showError(errorMessage);
                    }
                });
                break;
                
            case 3: // Fix Code
                String codeToFix = codeInput.getText().toString().trim();
                if (codeToFix.isEmpty()) {
                    showError("Please enter code to fix.");
                    return;
                }
                
                aiManager.fixCode(codeToFix, language, null, new GeminiAIManager.AIResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(response);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        showError(errorMessage);
                    }
                });
                break;
                
            case 4: // Ask Question
                String question = promptInput.getText().toString().trim();
                if (question.isEmpty()) {
                    showError("Please enter a question.");
                    return;
                }
                
                aiManager.askCodingQuestion(question, new GeminiAIManager.AIResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(response);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        showError(errorMessage);
                    }
                });
                break;
        }
    }

    /**
     * Handle AI response
     *
     * @param response Response from AI
     */
    private void handleResponse(String response) {
        progressBar.setVisibility(View.GONE);
        submitButton.setEnabled(true);
        
        resultText.setText(response);
        
        // Scroll to see results
        PerformanceOptimizer.runOnMainThreadDelayed(() -> 
                scrollView.fullScroll(View.FOCUS_DOWN), 100);
    }

    /**
     * Show error message
     *
     * @param message Error message
     */
    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        submitButton.setEnabled(true);
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show dialog to change API key
     */
    public void changeApiKey(View view) {
        showApiKeyDialog("Enter a new Gemini API key:");
    }
}