package com.mobiledev.androidstudio.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.models.Project;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying recent projects
 */
public class RecentProjectsAdapter extends RecyclerView.Adapter<RecentProjectsAdapter.ProjectViewHolder> {

    private List<Project> projects;
    private OnProjectClickListener clickListener;

    /**
     * Interface for project click listener
     */
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    /**
     * Create a new RecentProjectsAdapter
     *
     * @param projects List of projects
     * @param clickListener Click listener
     */
    public RecentProjectsAdapter(List<Project> projects, OnProjectClickListener clickListener) {
        this.projects = projects;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    /**
     * Update the list of projects
     *
     * @param projects New list of projects
     */
    public void updateProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for projects
     */
    class ProjectViewHolder extends RecyclerView.ViewHolder {
        
        private TextView nameTextView;
        private TextView packageTextView;
        private TextView lastModifiedTextView;
        private ImageButton openButton;
        
        /**
         * Create a new ProjectViewHolder
         *
         * @param itemView Item view
         */
        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            
            nameTextView = itemView.findViewById(R.id.text_project_name);
            packageTextView = itemView.findViewById(R.id.text_package_name);
            lastModifiedTextView = itemView.findViewById(R.id.text_last_modified);
            openButton = itemView.findViewById(R.id.btn_open_project);
        }
        
        /**
         * Bind a project to this ViewHolder
         *
         * @param project Project to bind
         */
        void bind(final Project project) {
            nameTextView.setText(project.getName());
            packageTextView.setText(project.getPackageName());
            
            // Format the last modified date
            long lastModified = project.getLastModified();
            String formattedDate;
            
            if (DateUtils.isToday(lastModified)) {
                formattedDate = "Today at " + new SimpleDateFormat("h:mm a", Locale.getDefault())
                        .format(new Date(lastModified));
            } else {
                formattedDate = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(new Date(lastModified));
            }
            
            lastModifiedTextView.setText(formattedDate);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProjectClick(project);
                }
            });
            
            openButton.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProjectClick(project);
                }
            });
        }
    }
}