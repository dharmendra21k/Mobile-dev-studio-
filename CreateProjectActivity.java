package com.mobiledev.androidstudio.projects;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.models.Project;
import com.mobiledev.androidstudio.utils.ProjectManager;

/**
 * Activity for creating new Android projects
 */
public class CreateProjectActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText packageEditText;
    private Spinner templateSpinner;
    private TextView locationTextView;
    private Button createButton;
    private ProjectManager projectManager;
    private TextView nameErrorTextView;
    private TextView packageErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.project_create_new);

        // Initialize views
        nameEditText = findViewById(R.id.edit_project_name);
        packageEditText = findViewById(R.id.edit_package_name);
        templateSpinner = findViewById(R.id.spinner_template);
        locationTextView = findViewById(R.id.text_location);
        createButton = findViewById(R.id.btn_create_project);
        nameErrorTextView = findViewById(R.id.text_name_error);
        packageErrorTextView = findViewById(R.id.text_package_error);
        
        // Initialize ProjectManager
        projectManager = new ProjectManager(this);
        
        // Set default project location
        locationTextView.setText(projectManager.getProjectsDirectory().getAbsolutePath());
        
        // Set up template spinner
        setupTemplateSpinner();
        
        // Set up text change listeners for validation
        setupTextChangeListeners();
        
        // Set up create button
        createButton.setOnClickListener(v -> createProject());
    }

    /**
     * Set up the template spinner with available project templates
     */
    private void setupTemplateSpinner() {
        String[] templates = {
            "Empty Activity",
            "Basic Activity",
            "Bottom Navigation Activity",
            "Navigation Drawer Activity",
            "Tabbed Activity",
            "Settings Activity",
            "Login Activity",
            "Scrolling Activity",
            "Master Detail Flow",
            "Google Maps Activity"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, templates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        templateSpinner.setAdapter(adapter);
    }

    /**
     * Set up text change listeners for input validation
     */
    private void setupTextChangeListeners() {
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateProjectName(s.toString());
            }
        });

        packageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validatePackageName(s.toString());
            }
        });
    }

    /**
     * Validate the project name input
     *
     * @param name Project name to validate
     * @return true if valid, false otherwise
     */
    private boolean validateProjectName(String name) {
        if (name == null || name.trim().isEmpty()) {
            nameErrorTextView.setText(R.string.project_empty_name_error);
            nameErrorTextView.setVisibility(View.VISIBLE);
            return false;
        }
        
        nameErrorTextView.setVisibility(View.GONE);
        return true;
    }

    /**
     * Validate the package name input
     *
     * @param packageName Package name to validate
     * @return true if valid, false otherwise
     */
    private boolean validatePackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            packageErrorTextView.setText(R.string.project_invalid_package_error);
            packageErrorTextView.setVisibility(View.VISIBLE);
            return false;
        }
        
        // Use ProjectManager to validate package name
        if (!isValidPackageName(packageName)) {
            packageErrorTextView.setText(R.string.project_invalid_package_error);
            packageErrorTextView.setVisibility(View.VISIBLE);
            return false;
        }
        
        packageErrorTextView.setVisibility(View.GONE);
        return true;
    }

    /**
     * Create a new Android project
     */
    private void createProject() {
        String name = nameEditText.getText().toString().trim();
        String packageName = packageEditText.getText().toString().trim();
        String template = templateSpinner.getSelectedItem().toString();
        
        // Validate inputs
        if (!validateProjectName(name) || !validatePackageName(packageName)) {
            return;
        }
        
        // Create the project
        Project project = projectManager.createProject(name, packageName, template);
        
        if (project != null) {
            // Show success message
            Toast.makeText(this, "Project created successfully", Toast.LENGTH_SHORT).show();
            
            // Open the project
            projectManager.openProject(project);
            
            // Close this activity
            finish();
        } else {
            // Show error message
            Toast.makeText(this, "Failed to create project", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Validate a package name
     *
     * @param packageName Package name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        
        // Must have at least one dot (e.g., "com.example")
        if (!packageName.contains(".")) {
            return false;
        }
        
        // Check each package segment
        String[] segments = packageName.split("\\.");
        for (String segment : segments) {
            // Must not be empty
            if (segment.isEmpty()) {
                return false;
            }
            
            // First character must be a letter
            if (!Character.isLetter(segment.charAt(0))) {
                return false;
            }
            
            // Other characters must be letters, numbers, or underscores
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}