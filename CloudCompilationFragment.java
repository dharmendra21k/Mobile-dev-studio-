package com.mobiledev.androidstudio.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.cloud.CloudCompilationConfig;
import com.mobiledev.androidstudio.cloud.CloudCompilationService;
import com.mobiledev.androidstudio.model.Project;
import com.mobiledev.androidstudio.utils.PreferenceManager;

/**
 * Fragment for cloud compilation
 */
public class CloudCompilationFragment extends Fragment {
    
    private Project project;
    private CloudCompilationService compilationService;
    private PreferenceManager preferenceManager;
    
    // UI components
    private Spinner buildTypeSpinner;
    private Spinner deploymentOptionSpinner;
    private CheckBox generateApkCheckBox;
    private CheckBox generateAabCheckBox;
    private CheckBox useProguardCheckBox;
    private EditText versionNameEditText;
    private EditText versionCodeEditText;
    private EditText apiKeyEditText;
    private Button compileButton;
    private ProgressBar progressBar;
    private TextView progressTextView;
    
    /**
     * Create a new instance of the fragment
     * 
     * @param project Project to compile
     * @return Fragment instance
     */
    public static CloudCompilationFragment newInstance(Project project) {
        CloudCompilationFragment fragment = new CloudCompilationFragment();
        Bundle args = new Bundle();
        args.putParcelable("project", project);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            project = getArguments().getParcelable("project");
        }
        
        compilationService = new CloudCompilationService(requireContext());
        preferenceManager = new PreferenceManager(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_compilation, container, false);
        
        // Initialize UI components
        buildTypeSpinner = view.findViewById(R.id.build_type_spinner);
        deploymentOptionSpinner = view.findViewById(R.id.deployment_option_spinner);
        generateApkCheckBox = view.findViewById(R.id.generate_apk_checkbox);
        generateAabCheckBox = view.findViewById(R.id.generate_aab_checkbox);
        useProguardCheckBox = view.findViewById(R.id.use_proguard_checkbox);
        versionNameEditText = view.findViewById(R.id.version_name_edittext);
        versionCodeEditText = view.findViewById(R.id.version_code_edittext);
        apiKeyEditText = view.findViewById(R.id.api_key_edittext);
        compileButton = view.findViewById(R.id.compile_button);
        progressBar = view.findViewById(R.id.progress_bar);
        progressTextView = view.findViewById(R.id.progress_text);
        
        // Setup spinners
        setupBuildTypeSpinner();
        setupDeploymentOptionSpinner();
        
        // Set initial values
        String savedApiKey = preferenceManager.getString("cloud_compilation_api_key", "");
        apiKeyEditText.setText(savedApiKey);
        
        // Set up button click listener
        compileButton.setOnClickListener(v -> startCompilation());
        
        return view;
    }
    
    /**
     * Setup the build type spinner
     */
    private void setupBuildTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.build_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildTypeSpinner.setAdapter(adapter);
        
        buildTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUIBasedOnBuildType(position);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    /**
     * Setup the deployment option spinner
     */
    private void setupDeploymentOptionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.deployment_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deploymentOptionSpinner.setAdapter(adapter);
    }
    
    /**
     * Update UI based on build type
     * 
     * @param position Position of selected build type
     */
    private void updateUIBasedOnBuildType(int position) {
        if (position == 1) { // Release
            generateAabCheckBox.setChecked(true);
            useProguardCheckBox.setChecked(true);
        } else { // Debug
            generateAabCheckBox.setChecked(false);
            useProguardCheckBox.setChecked(false);
        }
    }
    
    /**
     * Get the selected build type
     * 
     * @return Build type
     */
    private CloudCompilationConfig.BuildType getSelectedBuildType() {
        return buildTypeSpinner.getSelectedItemPosition() == 0 ?
                CloudCompilationConfig.BuildType.DEBUG :
                CloudCompilationConfig.BuildType.RELEASE;
    }
    
    /**
     * Get the selected deployment option
     * 
     * @return Deployment option
     */
    private CloudCompilationConfig.DeploymentOption getSelectedDeploymentOption() {
        int position = deploymentOptionSpinner.getSelectedItemPosition();
        switch (position) {
            case 1:
                return CloudCompilationConfig.DeploymentOption.STORE;
            case 2:
                return CloudCompilationConfig.DeploymentOption.FIREBASE;
            case 3:
                return CloudCompilationConfig.DeploymentOption.CUSTOM;
            default:
                return CloudCompilationConfig.DeploymentOption.NONE;
        }
    }
    
    /**
     * Get the custom version name
     * 
     * @return Custom version name or null if empty
     */
    private String getCustomVersionName() {
        String versionName = versionNameEditText.getText().toString().trim();
        return versionName.isEmpty() ? null : versionName;
    }
    
    /**
     * Get the custom version code
     * 
     * @return Custom version code or null if empty
     */
    private Integer getCustomVersionCode() {
        String versionCodeStr = versionCodeEditText.getText().toString().trim();
        if (versionCodeStr.isEmpty()) {
            return null;
        }
        
        try {
            return Integer.parseInt(versionCodeStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Create a compilation configuration from UI values
     * 
     * @return Compilation configuration
     */
    private CloudCompilationConfig createConfigFromUI() {
        CloudCompilationConfig config = new CloudCompilationConfig();
        
        config.setBuildType(getSelectedBuildType())
                .setDeploymentOption(getSelectedDeploymentOption())
                .setGenerateApk(generateApkCheckBox.isChecked())
                .setGenerateAab(generateAabCheckBox.isChecked())
                .setUseProguard(useProguardCheckBox.isChecked())
                .setCustomVersionName(getCustomVersionName())
                .setCustomVersionCode(getCustomVersionCode());
        
        return config;
    }
    
    /**
     * Start compilation with the current configuration
     */
    private void startCompilation() {
        // Get API key
        String apiKey = apiKeyEditText.getText().toString().trim();
        if (apiKey.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an API key", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save API key for future use
        preferenceManager.putString("cloud_compilation_api_key", apiKey);
        
        // Create configuration
        CloudCompilationConfig config = createConfigFromUI();
        
        // Update UI state
        setUIEnabled(false);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        progressTextView.setVisibility(View.VISIBLE);
        progressTextView.setText("Preparing compilation...");
        
        // Start compilation
        compilationService.compileProject(project, apiKey, config, new CloudCompilationService.CloudCompilationCallback() {
            @Override
            public void onProgressUpdate(int progress, String message) {
                progressBar.setProgress(progress);
                progressTextView.setText(message);
            }
            
            @Override
            public void onCompilationComplete(boolean success, String message) {
                progressBar.setProgress(100);
                progressTextView.setText(message);
                
                if (success) {
                    Toast.makeText(requireContext(), "Compilation successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Compilation failed: " + message, Toast.LENGTH_LONG).show();
                }
                
                setUIEnabled(true);
            }
        });
    }
    
    /**
     * Set the enabled state of UI components
     * 
     * @param enabled Whether UI components should be enabled
     */
    private void setUIEnabled(boolean enabled) {
        buildTypeSpinner.setEnabled(enabled);
        deploymentOptionSpinner.setEnabled(enabled);
        generateApkCheckBox.setEnabled(enabled);
        generateAabCheckBox.setEnabled(enabled);
        useProguardCheckBox.setEnabled(enabled);
        versionNameEditText.setEnabled(enabled);
        versionCodeEditText.setEnabled(enabled);
        apiKeyEditText.setEnabled(enabled);
        compileButton.setEnabled(enabled);
    }
}