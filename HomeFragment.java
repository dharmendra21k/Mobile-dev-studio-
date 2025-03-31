package com.mobiledev.androidstudio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.adapters.RecentProjectsAdapter;
import com.mobiledev.androidstudio.models.Project;
import com.mobiledev.androidstudio.projects.CreateProjectActivity;
import com.mobiledev.androidstudio.terminal.TerminalActivity;
import com.mobiledev.androidstudio.utils.ProjectManager;

import java.util.List;

/**
 * Home screen fragment
 */
public class HomeFragment extends Fragment {

    private RecyclerView recentProjectsRecyclerView;
    private TextView noProjectsTextView;
    private ProjectManager projectManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize ProjectManager
        projectManager = new ProjectManager(requireContext());
        
        // Find views
        recentProjectsRecyclerView = view.findViewById(R.id.recycler_recent_projects);
        noProjectsTextView = view.findViewById(R.id.text_no_projects);
        
        // Set up button handlers
        Button createProjectButton = view.findViewById(R.id.btn_create_project);
        Button openTerminalButton = view.findViewById(R.id.btn_open_terminal);
        Button packageManagerButton = view.findViewById(R.id.btn_package_manager);
        Button cloudBuildButton = view.findViewById(R.id.btn_cloud_build);
        
        createProjectButton.setOnClickListener(v -> openCreateProject());
        openTerminalButton.setOnClickListener(v -> openTerminal());
        packageManagerButton.setOnClickListener(v -> openPackageManager());
        cloudBuildButton.setOnClickListener(v -> openCloudBuild());
        
        // Set up recent projects recycler view
        recentProjectsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentProjects();
    }

    /**
     * Load recent projects
     */
    private void loadRecentProjects() {
        List<Project> recentProjects = projectManager.getRecentProjects();
        
        if (recentProjects.isEmpty()) {
            noProjectsTextView.setVisibility(View.VISIBLE);
            recentProjectsRecyclerView.setVisibility(View.GONE);
        } else {
            noProjectsTextView.setVisibility(View.GONE);
            recentProjectsRecyclerView.setVisibility(View.VISIBLE);
            
            RecentProjectsAdapter adapter = new RecentProjectsAdapter(
                    recentProjects,
                    project -> projectManager.openProject(project)
            );
            recentProjectsRecyclerView.setAdapter(adapter);
        }
    }

    /**
     * Open the create project activity
     */
    private void openCreateProject() {
        Intent intent = new Intent(requireContext(), CreateProjectActivity.class);
        startActivity(intent);
    }

    /**
     * Open the terminal activity
     */
    private void openTerminal() {
        Intent intent = new Intent(requireContext(), TerminalActivity.class);
        startActivity(intent);
    }

    /**
     * Open the package manager (not implemented yet)
     */
    private void openPackageManager() {
        // This would be implemented to open the package manager
    }

    /**
     * Open the cloud build screen (not implemented yet)
     */
    private void openCloudBuild() {
        // This would be implemented to open the cloud build screen
    }
}