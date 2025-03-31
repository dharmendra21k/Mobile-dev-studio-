package com.mobiledev.androidstudio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.adapters.ProjectAdapter;
import com.mobiledev.androidstudio.models.Project;
import com.mobiledev.androidstudio.projects.CreateProjectActivity;
import com.mobiledev.androidstudio.utils.ProjectManager;

import java.util.List;

/**
 * Fragment for displaying and managing projects
 */
public class ProjectsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProjectAdapter adapter;
    private ProjectManager projectManager;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        // Initialize components
        recyclerView = view.findViewById(R.id.recycler_projects);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        emptyView = view.findViewById(R.id.empty_view);
        FloatingActionButton fabAddProject = view.findViewById(R.id.fab_add_project);
        Button btnCreateFirst = view.findViewById(R.id.btn_create_first_project);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        projectManager = new ProjectManager(requireContext());
        
        adapter = new ProjectAdapter(getContext(), project -> {
            // Handle project click
            projectManager.openProject(project);
        });
        
        recyclerView.setAdapter(adapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadProjects);

        // Set up FAB for adding new projects
        fabAddProject.setOnClickListener(v -> openCreateProjectScreen());
        
        // Set up "Create First Project" button (shown when no projects exist)
        btnCreateFirst.setOnClickListener(v -> openCreateProjectScreen());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }

    /**
     * Load projects and update the UI
     */
    private void loadProjects() {
        // Show loading indicator
        swipeRefreshLayout.setRefreshing(true);

        // Get all projects
        List<Project> projects = projectManager.getAllProjects();
        
        // Update adapter
        adapter.setProjects(projects);
        
        // Show/hide empty view
        if (projects.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        
        // Hide loading indicator
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Open the create project screen
     */
    private void openCreateProjectScreen() {
        Intent intent = new Intent(getActivity(), CreateProjectActivity.class);
        startActivity(intent);
    }
}