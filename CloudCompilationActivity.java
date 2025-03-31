package com.mobiledev.androidstudio.cloud;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.model.Project;

import java.io.File;

/**
 * Activity for cloud compilation
 */
public class CloudCompilationActivity extends AppCompatActivity {
    
    public static final String EXTRA_PROJECT = "project";
    
    private Project project;
    private CloudCompilationConfig config;
    private CloudCompilationService service;
    
    private Spinner buildTypeSpinner;
    private Spinner deploymentSpinner;
    private EditText apiKeyEditText;
    private CheckBox minifyCheckBox;
    private Button compileButton;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private Button installButton;
    
    private String apkPath;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_compilation);
        
        // Get project from intent
        project = getIntent().getParcelableExtra(EXTRA_PROJECT);
        
        if (project == null) {
            Toast.makeText(this, "No project provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set title
        setTitle("Cloud Compilation: " + project.getName());
        
        // Initialize service
        service = new CloudCompilationService(this);
        
        // Initialize config
        config = new CloudCompilationConfig();
        
        // Initialize views
        findViews();
        setupViews();
    }
    
    /**
     * Find views
     */
    private void findViews() {
        buildTypeSpinner = findViewById(R.id.build_type_spinner);
        deploymentSpinner = findViewById(R.id.deployment_spinner);
        apiKeyEditText = findViewById(R.id.api_key_edit_text);
        minifyCheckBox = findViewById(R.id.minify_checkbox);
        compileButton = findViewById(R.id.compile_button);
        progressBar = findViewById(R.id.progress_bar);
        statusTextView = findViewById(R.id.status_text_view);
        installButton = findViewById(R.id.install_button);
    }
    
    /**
     * Set up views
     */
    private void setupViews() {
        // Set up build type spinner
        ArrayAdapter<CharSequence> buildTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.build_types, android.R.layout.simple_spinner_item);
        buildTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildTypeSpinner.setAdapter(buildTypeAdapter);
        buildTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.setBuildType(CloudCompilationConfig.buildTypeFromIndex(position));
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Set up deployment spinner
        ArrayAdapter<CharSequence> deploymentAdapter = ArrayAdapter.createFromResource(
                this, R.array.deployment_options, android.R.layout.simple_spinner_item);
        deploymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deploymentSpinner.setAdapter(deploymentAdapter);
        deploymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.setDeploymentType(CloudCompilationConfig.deploymentTypeFromIndex(position));
                
                // Show/hide API key field based on deployment type
                boolean needsApiKey = position > 0;
                apiKeyEditText.setVisibility(needsApiKey ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // API key field initially hidden
        apiKeyEditText.setVisibility(View.GONE);
        
        // Set up minify checkbox
        minifyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                config.addExtraOption("minify", "true");
            } else {
                config.addExtraOption("minify", "false");
            }
        });
        
        // Set up compile button
        compileButton.setOnClickListener(v -> startCompilation());
        
        // Set up install button
        installButton.setOnClickListener(v -> installApk());
        installButton.setVisibility(View.GONE);
    }
    
    /**
     * Start compilation
     */
    private void startCompilation() {
        // Get API key if needed
        if (deploymentSpinner.getSelectedItemPosition() > 0) {
            String apiKey = apiKeyEditText.getText().toString().trim();
            
            if (apiKey.isEmpty()) {
                apiKeyEditText.setError("API key is required");
                return;
            }
            
            config.setApiKey(apiKey);
        }
        
        // Update UI
        compileButton.setEnabled(false);
        installButton.setVisibility(View.GONE);
        progressBar.setProgress(0);
        statusTextView.setText("Preparing for compilation...");
        
        // Start compilation
        service.compileProject(project, config, new CloudCompilationService.CompilationCallback() {
            @Override
            public void onStatusUpdate(int status, String message, int progress) {
                runOnUiThread(() -> {
                    statusTextView.setText(message);
                    progressBar.setProgress(progress);
                });
            }
            
            @Override
            public void onComplete(boolean success, String apkFile, String errorMessage) {
                runOnUiThread(() -> {
                    compileButton.setEnabled(true);
                    
                    if (success) {
                        Toast.makeText(CloudCompilationActivity.this,
                                "Compilation completed", Toast.LENGTH_SHORT).show();
                        
                        // Store APK path for install button
                        apkPath = apkFile;
                        installButton.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(CloudCompilationActivity.this,
                                "Compilation failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        
                        // Show error details
                        new AlertDialog.Builder(CloudCompilationActivity.this)
                                .setTitle("Compilation Failed")
                                .setMessage(errorMessage)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
            }
        });
    }
    
    /**
     * Install APK
     */
    private void installApk() {
        if (apkPath == null || apkPath.isEmpty()) {
            Toast.makeText(this, "No APK available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        File apkFile = new File(apkPath);
        
        if (!apkFile.exists()) {
            Toast.makeText(this, "APK file not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create URI for APK file
            Uri apkUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", apkFile);
            
            // Create intent to install APK
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to install APK: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}