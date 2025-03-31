package com.mobiledev.androidstudio.adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter for displaying files in a RecyclerView
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    
    private final Context context;
    private final List<File> files;
    private final OnFileSelectedListener listener;
    private File currentDirectory;
    
    /**
     * Interface for handling file selection
     */
    public interface OnFileSelectedListener {
        void onFileSelected(File file);
        void onFileLongClick(View view, int position, File file);
    }
    
    /**
     * Constructor for FileAdapter
     * 
     * @param context Android context
     * @param directory Initial directory to display
     * @param listener Listener for file selection
     */
    public FileAdapter(Context context, File directory, OnFileSelectedListener listener) {
        this.context = context;
        this.files = new ArrayList<>();
        this.listener = listener;
        this.currentDirectory = directory;
        
        loadFiles(directory);
    }
    
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        
        holder.textFileName.setText(file.getName());
        
        // Set appropriate icon
        if (file.isDirectory()) {
            holder.imageFileType.setImageResource(android.R.drawable.ic_menu_more);
        } else {
            holder.imageFileType.setImageResource(android.R.drawable.ic_menu_edit);
        }
        
        // Set up click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileSelected(file);
            }
        });
        
        // Set up long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onFileLongClick(v, holder.getAdapterPosition(), file);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return files.size();
    }
    
    /**
     * Navigate to a new directory
     * 
     * @param directory Directory to navigate to
     */
    public void navigateTo(File directory) {
        if (directory.isDirectory()) {
            currentDirectory = directory;
            loadFiles(directory);
            notifyDataSetChanged();
        }
    }
    
    /**
     * Navigate to the parent directory
     * 
     * @return true if navigation was successful
     */
    public boolean navigateUp() {
        File parent = currentDirectory.getParentFile();
        if (parent != null) {
            navigateTo(parent);
            return true;
        }
        return false;
    }
    
    /**
     * Get the current directory
     * 
     * @return Current directory
     */
    public File getCurrentDirectory() {
        return currentDirectory;
    }
    
    /**
     * Refresh the file list
     */
    public void refresh() {
        loadFiles(currentDirectory);
        notifyDataSetChanged();
    }
    
    /**
     * Load files from the given directory
     * 
     * @param directory Directory to load files from
     */
    private void loadFiles(File directory) {
        files.clear();
        
        // Add parent directory if not at root
        if (directory.getParentFile() != null) {
            files.add(new File(directory, ".."));
        }
        
        // Add all files and directories
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            // Sort: directories first, then files, both alphabetically
            Arrays.sort(fileList, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            
            files.addAll(Arrays.asList(fileList));
        }
    }
    
    /**
     * ViewHolder for file items
     */
    static class FileViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        ImageView imageFileType;
        TextView textFileName;
        
        FileViewHolder(View itemView) {
            super(itemView);
            imageFileType = itemView.findViewById(R.id.image_file_type);
            textFileName = itemView.findViewById(R.id.text_file_name);
            
            // Set up context menu
            itemView.setOnCreateContextMenuListener(this);
        }
        
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(Menu.NONE, 1, Menu.NONE, "Delete");
            menu.add(Menu.NONE, 2, Menu.NONE, "Rename");
        }
    }
}