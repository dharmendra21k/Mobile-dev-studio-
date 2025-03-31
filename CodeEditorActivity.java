package com.mobiledev.androidstudio.editor;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.model.Project;

/**
 * Activity for code editor
 */
public class CodeEditorActivity extends AppCompatActivity {
    
    public static final String EXTRA_PROJECT = "project";
    
    private Project project;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        
        // Get project from intent
        project = getIntent().getParcelableExtra(EXTRA_PROJECT);
        
        if (project == null) {
            Toast.makeText(this, "No project provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set title
        setTitle("Editor: " + project.getName());
        
        // TODO: Initialize code editor
        // This is a placeholder for now
        Toast.makeText(this, "Code editor not implemented yet", Toast.LENGTH_SHORT).show();
    }
}