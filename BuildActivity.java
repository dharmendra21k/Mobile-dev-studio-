package com.mobiledev.androidstudio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.mobiledev.androidstudio.models.Project;
import com.mobiledev.androidstudio.utils.GradleWrapper;

import java.io.File;

public class BuildActivity extends AppCompatActivity implements GradleWrapper.OnGradleTaskListener {

    private Toolbar toolbar;
    private TextView tvBuildOutput;
    private ProgressBar progressBuild;
    private Button btnInstall;
    private Button btnRebuild;
    
    private Project project;
    private GradleWrapper gradleWrapper;
    private boolean buildSuccessful = false;
    private File apkFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tvBuildOutput = findViewById(R.id.tvBuildOutput);
        progressBuild = findViewById(R.id.progressBuild);
        btnInstall = findViewById(R.id.btnInstall);
        btnRebuild = findViewById(R.id.btnRebuild);
        
        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Build Project");
        
        // Set up scrolling for build output
        tvBuildOutput.setMovementMethod(new ScrollingMovementMethod());
        
        // Get project from intent
        String projectName = getIntent().getStringExtra("projectName");
        String projectPath = getIntent().getStringExtra("projectPath");
        String packageName = getIntent().getStringExtra("packageName");
        
        if (projectName == null || projectPath == null || packageName == null) {
            Toast.makeText(this, "Invalid project data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize project
        project = new Project(projectName, projectPath, packageName);
        
        // Initialize gradle wrapper
        gradleWrapper = new GradleWrapper(this);
        gradleWrapper.setProjectPath(project.getPath());
        gradleWrapper.setOnGradleTaskListener(this);
        
        // Set up button click listeners
        btnInstall.setOnClickListener(v -> installApk());
        btnRebuild.setOnClickListener(v -> buildProject());
        
        // Initially disable install button
        btnInstall.setEnabled(false);
        
        // Start build
        buildProject();
    }
    
    private void buildProject() {
        // Reset state
        buildSuccessful = false;
        btnInstall.setEnabled(false);
        tvBuildOutput.setText("");
        
        // Show progress
        progressBuild.setVisibility(View.VISIBLE);
        
        // Start build task
        gradleWrapper.assembleDebug();
    }
    
    private void installApk() {
        if (!buildSuccessful || apkFile == null || !apkFile.exists()) {
            Toast.makeText(this, "Build failed or APK not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get URI for APK file using FileProvider
        Uri apkUri = FileProvider.getUriForFile(
                this,
                project.getPackageName() + ".fileprovider",
                apkFile);
        
        // Create install intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        // Start install activity
        startActivity(intent);
    }
    
    @Override
    public void onTaskStarted() {
        runOnUiThread(() -> {
            tvBuildOutput.append("Build started...\n");
        });
    }
    
    @Override
    public void onTaskProgress(String message) {
        runOnUiThread(() -> {
            tvBuildOutput.append(message + "\n");
            
            // Scroll to bottom
            final int scrollAmount = tvBuildOutput.getLayout().getLineTop(tvBuildOutput.getLineCount()) - tvBuildOutput.getHeight();
            if (scrollAmount > 0) {
                tvBuildOutput.scrollTo(0, scrollAmount);
            } else {
                tvBuildOutput.scrollTo(0, 0);
            }
        });
    }
    
    @Override
    public void onTaskCompleted(boolean success, String output) {
        runOnUiThread(() -> {
            progressBuild.setVisibility(View.GONE);
            
            buildSuccessful = success;
            if (success) {
                tvBuildOutput.append("\nBuild successful!\n");
                
                // Find APK file
                File outputDir = new File(project.getPath() + "/app/build/outputs/apk/debug");
                File[] files = outputDir.listFiles((dir, name) -> name.endsWith(".apk"));
                
                if (files != null && files.length > 0) {
                    apkFile = files[0];
                    btnInstall.setEnabled(true);
                    tvBuildOutput.append("APK created: " + apkFile.getName() + "\n");
                } else {
                    tvBuildOutput.append("APK not found in output directory\n");
                }
            } else {
                tvBuildOutput.append("\nBuild failed!\n");
                tvBuildOutput.append("See above for error details\n");
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}