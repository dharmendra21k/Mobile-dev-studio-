package com.mobiledev.androidstudio.editor;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Fragment for code editing
 */
public class CodeEditorFragment extends Fragment {

    public static final String ARG_FILE_PATH = "file_path";
    public static final String ARG_LANGUAGE = "language";

    private String filePath;
    private String language;
    private EditText codeEditText;
    private SyntaxHighlighter syntaxHighlighter;
    private boolean fileModified = false;
    private CodeEditorListener listener;

    /**
     * Interface for code editor events
     */
    public interface CodeEditorListener {
        void onFileModified(String filePath, boolean modified);
        void onRequestCodeCompletion(String code, int position);
    }

    /**
     * Create a new instance of CodeEditorFragment
     *
     * @param filePath Path to the file to edit
     * @param language Programming language of the file
     * @return A new instance of CodeEditorFragment
     */
    public static CodeEditorFragment newInstance(String filePath, String language) {
        CodeEditorFragment fragment = new CodeEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, filePath);
        args.putString(ARG_LANGUAGE, language);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        // Get arguments
        if (getArguments() != null) {
            filePath = getArguments().getString(ARG_FILE_PATH);
            language = getArguments().getString(ARG_LANGUAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_editor, container, false);
        
        // Initialize views
        codeEditText = view.findViewById(R.id.edit_code);
        
        // Set up the editor
        setupEditor();
        
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CodeEditorListener) {
            listener = (CodeEditorListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_save) {
            saveFile();
            return true;
        } else if (id == R.id.action_undo) {
            // Undo last edit (not fully implemented)
            return true;
        } else if (id == R.id.action_auto_complete) {
            requestCodeCompletion();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the code editor
     */
    private void setupEditor() {
        // Create syntax highlighter based on language
        syntaxHighlighter = new SyntaxHighlighter(getContext(), language);
        
        // Load file content
        loadFileContent();
        
        // Set up text change listener
        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Apply syntax highlighting
                syntaxHighlighter.highlight(s);
                
                // Mark file as modified
                if (!fileModified) {
                    fileModified = true;
                    if (listener != null) {
                        listener.onFileModified(filePath, true);
                    }
                }
            }
        });
    }

    /**
     * Load the file content
     */
    private void loadFileContent() {
        if (filePath == null) {
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            Toast.makeText(getContext(), "File not found: " + filePath, Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String content = FileUtils.readFile(file);
            codeEditText.setText(content);
            fileModified = false;
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save the file
     */
    public void saveFile() {
        if (filePath == null) {
            return;
        }
        
        File file = new File(filePath);
        try {
            String content = codeEditText.getText().toString();
            FileUtils.writeFile(file, content);
            fileModified = false;
            
            if (listener != null) {
                listener.onFileModified(filePath, false);
            }
            
            Toast.makeText(getContext(), "File saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if the file has been modified
     *
     * @return true if the file has been modified, false otherwise
     */
    public boolean isFileModified() {
        return fileModified;
    }

    /**
     * Request code completion
     */
    private void requestCodeCompletion() {
        if (listener != null) {
            int position = codeEditText.getSelectionStart();
            String code = codeEditText.getText().toString();
            listener.onRequestCodeCompletion(code, position);
        }
    }
}