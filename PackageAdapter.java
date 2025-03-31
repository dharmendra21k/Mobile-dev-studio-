package com.mobiledev.androidstudio.installation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for package list
 */
public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
    
    private List<PackageInfo> packages;
    private List<PackageInfo> filteredPackages;
    private OnPackageClickListener listener;
    
    /**
     * Interface for package click events
     */
    public interface OnPackageClickListener {
        void onPackageClick(int position);
    }

    /**
     * Constructor
     *
     * @param packages Package list
     * @param listener Click listener
     */
    public PackageAdapter(List<PackageInfo> packages, OnPackageClickListener listener) {
        this.packages = packages;
        this.filteredPackages = new ArrayList<>(packages);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PackageInfo packageInfo = filteredPackages.get(position);
        
        holder.nameTextView.setText(packageInfo.getName());
        holder.descriptionTextView.setText(packageInfo.getDescription());
        holder.versionTextView.setText(packageInfo.getVersion());
        
        // Set installed badge
        if (packageInfo.isInstalled()) {
            holder.installedTextView.setVisibility(View.VISIBLE);
            holder.checkBox.setEnabled(false);
            holder.checkBox.setChecked(false);
        } else {
            holder.installedTextView.setVisibility(View.GONE);
            holder.checkBox.setEnabled(true);
            holder.checkBox.setChecked(packageInfo.isSelected());
        }
        
        // Set click listener
        holder.checkBox.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPackageClick(getOriginalPosition(adapterPosition));
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPackageClick(getOriginalPosition(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredPackages.size();
    }

    /**
     * Filter the package list
     *
     * @param query Filter query
     */
    public void filter(String query) {
        filteredPackages.clear();
        
        if (query == null || query.isEmpty()) {
            filteredPackages.addAll(packages);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            
            for (PackageInfo packageInfo : packages) {
                if (packageInfo.getName().toLowerCase().contains(lowerCaseQuery) ||
                    packageInfo.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredPackages.add(packageInfo);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    /**
     * Get the original position of a package in the unfiltered list
     *
     * @param filteredPosition Position in the filtered list
     * @return Position in the original list
     */
    private int getOriginalPosition(int filteredPosition) {
        PackageInfo packageInfo = filteredPackages.get(filteredPosition);
        return packages.indexOf(packageInfo);
    }

    /**
     * ViewHolder for package items
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        
        TextView nameTextView;
        TextView descriptionTextView;
        TextView versionTextView;
        TextView installedTextView;
        CheckBox checkBox;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            nameTextView = itemView.findViewById(R.id.text_package_name);
            descriptionTextView = itemView.findViewById(R.id.text_package_description);
            versionTextView = itemView.findViewById(R.id.text_package_version);
            installedTextView = itemView.findViewById(R.id.text_installed);
            checkBox = itemView.findViewById(R.id.checkbox_package);
        }
    }
}