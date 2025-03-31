package com.mobiledev.androidstudio.editor;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.ai.GeminiAiHelper;
import com.mobiledev.androidstudio.utils.FileUtils;

import java.io.File;

/**
 * Activity for the code editor
 */
public class EditorActivity extends AppCompatActivity implements CodeEditorFragment.CodeEditorListener {

    private String projectPath;
    private GeminiAiHelper aiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        // Get project path from intent
        projectPath = getIntent().getStringExtra("project_path");
        if (projectPath == null) {
            Toast.makeText(this, "Error: No project path provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize Gemini AI Helper if API key is available
        String apiKey = getResources().getString(R.string.gemini_api_key);
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY_HERE")) {
            aiHelper = new GeminiAiHelper(this, apiKey);
        }
        
        // Open the file explorer fragment
        openFileExplorer();
    }

    /**
     * Open the file explorer fragment
     */
    private void openFileExplorer() {
        FileExplorerFragment fragment = FileExplorerFragment.newInstance(projectPath);
        fragment.setFileClickListener(file -> openFile(file));
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        
        // Update title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Project Files");
        }
    }

    /**
     * Open a file in the editor
     *
     * @param file File to open
     */
    private void openFile(File file) {
        // Check if the file is a binary file
        if (FileUtils.isBinaryFile(file)) {
            Toast.makeText(this, "Cannot open binary file", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Determine the language for syntax highlighting
        String language = FileUtils.getLanguageFromExtension(file.getName());
        if (language == null) {
            language = "text";
        }
        
        // Create and show the editor fragment
        CodeEditorFragment fragment = CodeEditorFragment.newInstance(file.getAbsolutePath(), language);
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        
        // Update title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(file.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        
        if (currentFragment instanceof CodeEditorFragment) {
            CodeEditorFragment editorFragment = (CodeEditorFragment) currentFragment;
            
            if (editorFragment.isFileModified()) {
                // Ask if user wants to save before exiting
                saveBeforeExit(editorFragment);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Ask the user if they want to save before exiting
     *
     * @param editorFragment Editor fragment
     */
    private void saveBeforeExit(CodeEditorFragment editorFragment) {
        // In a real implementation, this would show a dialog
        // For now, just save and exit
        editorFragment.saveFile();
        super.onBackPressed();
    }

    // --- CodeEditorListener methods ---

    @Override
    public void onFileModified(String filePath, boolean modified) {
        // Update the title to show modified state
        if (getSupportActionBar() != null) {
            String title = getSupportActionBar().getTitle().toString();
            if (modified && !title.endsWith("*")) {
                getSupportActionBar().setTitle(title + "*");
            } else if (!modified && title.endsWith("*")) {
                getSupportActionBar().setTitle(title.substring(0, title.length() - 1));
            }
        }
    }

    @Override
    public void onRequestCodeCompletion(String code, int position) {
        if (aiHelper != null) {
            // Get the file extension to determine the language
            String filePath = getSupportActionBar().getTitle().toString();
            if (filePath.endsWith("*")) {
                filePath = filePath.substring(0, filePath.length() - 1);
            }
            
            String language = FileUtils.getLanguageFromExtension(filePath);
            if (language == null) {
                language = "text";
            }
            
            // Request code completion from Gemini AI
            aiHelper.getCodeCompletion(code, language, position, new GeminiAiHelper.CompletionCallback() {
                @Override
                public void onSuccess(String completion) {
                    // Find the current editor fragment and update it
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
                    
                    if (currentFragment instanceof CodeEditorFragment) {
                        CodeEditorFragment editorFragment = (CodeEditorFragment) currentFragment;
                        // In a real implementation, this would insert the completion
                        Toast.makeText(EditorActivity.this, "Completion received", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditorActivity.this, 
                            "Completion error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Gemini AI not configured", Toast.LENGTH_SHORT).show();
        }
    }
}