package com.mobiledev.androidstudio.build;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobiledev.androidstudio.R;

import java.util.List;

/**
 * Adapter for the project spinner
 */
public class ProjectAdapter extends ArrayAdapter<ProjectItem> {

    private LayoutInflater inflater;

    /**
     * Constructor
     *
     * @param context Context
     * @param projects List of project items
     */
    public ProjectAdapter(Context context, List<ProjectItem> projects) {
        super(context, R.layout.item_project_spinner, projects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_project_spinner, parent, false);
        }
        
        ProjectItem projectItem = getItem(position);
        if (projectItem != null) {
            TextView nameTextView = view.findViewById(R.id.text_project_name);
            TextView pathTextView = view.findViewById(R.id.text_project_path);
            
            nameTextView.setText(projectItem.getName());
            pathTextView.setText(projectItem.getPath());
        }
        
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}