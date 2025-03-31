package com.mobiledev.androidstudio.build;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobiledev.androidstudio.MobileDevApplication;
import com.mobiledev.androidstudio.utils.PreRootManager;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for building APK files from projects
 */
public class ApkBuilderActivity extends AppCompatActivity {
    private static final String TAG = "ApkBuilderActivity";
    
    private EditText projectPathEditText;
    private RadioGroup projectTypeRadioGroup;
    private Button buildButton;
    private ProgressBar buildProgressBar;
    private TextView buildStatusTextView;
    private TextView buildLogTextView;
    
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This would normally be done with a layout resource
        // setContentView(R.layout.activity_apk_builder);
        
        // For simplicity, create views programmatically
        projectPathEditText = new EditText(this);
        projectTypeRadioGroup = new RadioGroup(this);
        buildButton = new Button(this);
        buildProgressBar = new ProgressBar(this);
        buildStatusTextView = new TextView(this);
        buildLogTextView = new TextView(this);
        
        // Create radio buttons for project type
        String[] projectTypes = {"Standard Android", "React Native", "Flutter", "Terminal App"};
        for (String type : projectTypes) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(type);
            projectTypeRadioGroup.addView(radioButton);
        }
        
        // Select first radio button by default
        ((RadioButton) projectTypeRadioGroup.getChildAt(0)).setChecked(true);
        
        // Set up build button
        buildButton.setText("Build APK");
        buildButton.setOnClickListener(v -> startBuild());
        
        // Create executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();
    }
    
    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }
    
    /**
     * Start the APK build process
     */
    private void startBuild() {
        // Get project path
        String projectPath = projectPathEditText.getText().toString().trim();
        
        // Validate project path
        if (projectPath.isEmpty()) {
            Toast.makeText(this, "Please enter a project path", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if directory exists
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            Toast.makeText(this, "Invalid project directory", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected project type
        int selectedId = projectTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String projectType = selectedRadioButton.getText().toString();
        
        // Show progress and disable build button
        buildProgressBar.setVisibility(View.VISIBLE);
        buildButton.setEnabled(false);
        buildStatusTextView.setText("Building " + projectType + " APK...");
        buildLogTextView.setText("");
        
        // Build APK in background
        executorService.execute(() -> {
            try {
                // Get PRoot manager
                PreRootManager prootManager = MobileDevApplication.getInstance().getPreRootManager();
                
                // Build command based on project type
                String buildCommand;
                switch (projectType) {
                    case "Flutter":
                        buildCommand = "cd " + projectPath + " && flutter build apk --release";
                        break;
                    case "React Native":
                        buildCommand = "cd " + projectPath + " && react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle --assets-dest android/app/src/main/res && cd android && ./gradlew assembleRelease";
                        break;
                    case "Terminal App":
                        buildCommand = "cd " + projectPath + " && aapt package -f -m -J . -M AndroidManifest.xml -S res -I android.jar && javac *.java && dx --dex --output=classes.dex *.class && aapt package -f -m -F app.apk -M AndroidManifest.xml -S res -I android.jar && aapt add app.apk classes.dex";
                        break;
                    case "Standard Android":
                    default:
                        buildCommand = "cd " + projectPath + " && ./gradlew assembleRelease";
                        break;
                }
                
                // Execute build command
                appendLog("Executing build command: " + buildCommand);
                int result = prootManager.executeCommand(buildCommand);
                
                // Check result
                if (result == 0) {
                    // Build successful
                    runOnUiThread(() -> {
                        buildStatusTextView.setText("Build completed successfully");
                        Toast.makeText(ApkBuilderActivity.this, "APK build completed", Toast.LENGTH_SHORT).show();
                    });
                    
                    // Find APK file
                    String findApkCommand;
                    switch (projectType) {
                        case "Flutter":
                            findApkCommand = "find " + projectPath + "/build/app/outputs/apk -name \"*.apk\"";
                            break;
                        case "React Native":
                            findApkCommand = "find " + projectPath + "/android/app/build/outputs/apk -name \"*.apk\"";
                            break;
                        case "Terminal App":
                            findApkCommand = "find " + projectPath + " -name \"app.apk\"";
                            break;
                        case "Standard Android":
                        default:
                            findApkCommand = "find " + projectPath + "/app/build/outputs/apk -name \"*.apk\"";
                            break;
                    }
                    
                    // Find APK file and display path
                    appendLog("Locating APK file...");
                    prootManager.executeCommand(findApkCommand + " > /tmp/apk_path.txt");
                    
                    // In a real implementation, we would read the output file and display the APK path
                    appendLog("APK file generated successfully");
                } else {
                    // Build failed
                    runOnUiThread(() -> {
                        buildStatusTextView.setText("Build failed");
                        Toast.makeText(ApkBuilderActivity.this, "APK build failed", Toast.LENGTH_SHORT).show();
                    });
                    
                    appendLog("Build failed with error code: " + result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during build", e);
                
                runOnUiThread(() -> {
                    buildStatusTextView.setText("Build error");
                    appendLog("Error: " + e.getMessage());
                    Toast.makeText(ApkBuilderActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                // Re-enable build button and hide progress
                runOnUiThread(() -> {
                    buildButton.setEnabled(true);
                    buildProgressBar.setVisibility(View.GONE);
                });
            }
        });
    }
    
    /**
     * Append text to the build log
     */
    private void appendLog(String text) {
        runOnUiThread(() -> {
            buildLogTextView.append(text + "\n");
        });
    }
}