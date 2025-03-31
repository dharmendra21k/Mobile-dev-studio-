package com.mobiledev.androidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.adapters.TemplateAdapter;
import com.mobiledev.androidstudio.models.Project;
import com.mobiledev.androidstudio.models.Template;
import com.mobiledev.androidstudio.utils.ProjectManager;
import com.mobiledev.androidstudio.utils.TemplateManager;

import java.io.IOException;
import java.util.List;

public class ProjectActivity extends AppCompatActivity implements TemplateAdapter.OnTemplateSelectedListener {

    private static final String DEFAULT_PACKAGE_PREFIX = "com.example.";
    
    private EditText etProjectName;
    private EditText etPackageName;
    private RecyclerView rvTemplates;
    private Button btnCreate;
    private Toolbar toolbar;
    
    private TemplateAdapter templateAdapter;
    private TemplateManager templateManager;
    private ProjectManager projectManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        
        // Initialize views
        etProjectName = findViewById(R.id.etProjectName);
        etPackageName = findViewById(R.id.etPackageName);
        rvTemplates = findViewById(R.id.rvTemplates);
        btnCreate = findViewById(R.id.btnCreate);
        toolbar = findViewById(R.id.toolbar);
        
        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create New Project");
        
        // Initialize managers
        templateManager = new TemplateManager(this);
        projectManager = new ProjectManager(this);
        
        // Set up templates recycler view
        rvTemplates.setLayoutManager(new LinearLayoutManager(this));
        List<Template> templates = templateManager.getAvailableTemplates();
        templateAdapter = new TemplateAdapter(this, templates, this);
        rvTemplates.setAdapter(templateAdapter);
        
        // Auto-generate package name from project name
        etProjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!etPackageName.getText().toString().startsWith(DEFAULT_PACKAGE_PREFIX)) {
                    // Only auto-generate if user hasn't customized the package name
                    String packageSuffix = s.toString().toLowerCase().replaceAll("[^a-z0-9]", "");
                    etPackageName.setText(DEFAULT_PACKAGE_PREFIX + packageSuffix);
                }
            }
        });
        
        // Set up create button
        btnCreate.setOnClickListener(v -> createProject());
    }
    
    private void createProject() {
        String projectName = etProjectName.getText().toString().trim();
        String packageName = etPackageName.getText().toString().trim();
        Template selectedTemplate = templateAdapter.getSelectedTemplate();
        
        // Validate inputs
        if (projectName.isEmpty()) {
            etProjectName.setError("Project name cannot be empty");
            return;
        }
        
        if (packageName.isEmpty()) {
            etPackageName.setError("Package name cannot be empty");
            return;
        }
        
        if (!isValidPackageName(packageName)) {
            etPackageName.setError("Invalid package name format");
            return;
        }
        
        if (selectedTemplate == null) {
            Toast.makeText(this, "Please select a template", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create project
        try {
            Project project = projectManager.createProject(projectName, packageName, selectedTemplate);
            Toast.makeText(this, "Project created successfully", Toast.LENGTH_SHORT).show();
            
            // Open the project in the editor
            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("projectName", project.getName());
            intent.putExtra("projectPath", project.getPath());
            intent.putExtra("packageName", project.getPackageName());
            startActivity(intent);
            
            // Set result and finish
            setResult(RESULT_OK);
            finish();
        } catch (IOException e) {
            Toast.makeText(this, "Error creating project: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private boolean isValidPackageName(String packageName) {
        if (packageName.isEmpty()) {
            return false;
        }
        
        // Package name must have at least two segments
        String[] segments = packageName.split("\\.");
        if (segments.length < 2) {
            return false;
        }
        
        // Each segment must be a valid Java identifier
        for (String segment : segments) {
            if (segment.isEmpty()) {
                return false;
            }
            
            // First character must be a letter or underscore
            char firstChar = segment.charAt(0);
            if (!Character.isLetter(firstChar) && firstChar != '_') {
                return false;
            }
            
            // Remaining characters must be letters, digits, or underscores
            for (int i = 1; i < segment.length(); i++) {
                char c = segment.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '_') {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public void onTemplateSelected(Template template) {
        // Do nothing here, template will be retrieved when creating project
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