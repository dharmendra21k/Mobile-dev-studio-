package com.mobiledev.androidstudio.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * Fragment for the file explorer
 */
public class FileExplorerFragment extends Fragment {

    private static final String ARG_PATH = "path";

    private String rootPath;
    private String currentPath;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private TextView pathTextView;
    private TextView emptyTextView;
    private Stack<String> pathHistory;
    private FileClickListener fileClickListener;

    /**
     * Interface for file click events
     */
    public interface FileClickListener {
        void onFileClick(File file);
    }

    /**
     * Create a new instance of FileExplorerFragment
     *
     * @param path Root path
     * @return A new instance of FileExplorerFragment
     */
    public static FileExplorerFragment newInstance(String path) {
        FileExplorerFragment fragment = new FileExplorerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            rootPath = getArguments().getString(ARG_PATH);
            currentPath = rootPath;
        }
        
        pathHistory = new Stack<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_explorer, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_files);
        pathTextView = view.findViewById(R.id.text_path);
        emptyTextView = view.findViewById(R.id.text_empty);
        
        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        
        fileAdapter = new FileAdapter();
        recyclerView.setAdapter(fileAdapter);
        
        // Load files
        if (savedInstanceState != null) {
            currentPath = savedInstanceState.getString("currentPath", rootPath);
        }
        
        loadFiles(currentPath);
        
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentPath", currentPath);
    }

    /**
     * Set the file click listener
     *
     * @param listener Listener to set
     */
    public void setFileClickListener(FileClickListener listener) {
        this.fileClickListener = listener;
    }

    /**
     * Load files from a directory
     *
     * @param path Directory path
     */
    private void loadFiles(String path) {
        currentPath = path;
        pathTextView.setText(getRelativePath(path));
        
        File directory = new File(path);
        File[] files = directory.listFiles();
        
        List<File> fileList = new ArrayList<>();
        
        if (files != null) {
            fileList.addAll(Arrays.asList(files));
            
            // Sort directories first, then files
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    if (file1.isDirectory() && !file2.isDirectory()) {
                        return -1;
                    } else if (!file1.isDirectory() && file2.isDirectory()) {
                        return 1;
                    } else {
                        return file1.getName().compareToIgnoreCase(file2.getName());
                    }
                }
            });
        }
        
        // Show empty view if there are no files
        if (fileList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
        
        // Add parent directory if not at root
        if (!path.equals(rootPath)) {
            fileList.add(0, new File(directory.getParent()));
        }
        
        fileAdapter.setFiles(fileList);
    }

    /**
     * Get the path relative to the root
     *
     * @param path Absolute path
     * @return Relative path
     */
    private String getRelativePath(String path) {
        if (path.equals(rootPath)) {
            return "/";
        } else if (path.startsWith(rootPath)) {
            return path.substring(rootPath.length());
        } else {
            return path;
        }
    }

    /**
     * Navigate to a directory
     *
     * @param directory Directory to navigate to
     */
    private void navigateToDirectory(File directory) {
        if (directory.getAbsolutePath().equals(new File(currentPath).getParent())) {
            // Going up, pop from history
            if (!pathHistory.isEmpty()) {
                pathHistory.pop();
            }
        } else {
            // Going down, push to history
            pathHistory.push(currentPath);
        }
        
        loadFiles(directory.getAbsolutePath());
    }

    /**
     * Navigate back to the previous directory
     *
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateBack() {
        if (!pathHistory.isEmpty()) {
            String previousPath = pathHistory.pop();
            loadFiles(previousPath);
            return true;
        }
        
        return false;
    }

    /**
     * Adapter for files
     */
    private class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
        
        private List<File> files = new ArrayList<>();
        
        /**
         * Set the files to display
         *
         * @param files Files to display
         */
        public void setFiles(List<File> files) {
            this.files = files;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file, parent, false);
            return new FileViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            File file = files.get(position);
            
            boolean isParent = position == 0 && !currentPath.equals(rootPath);
            
            holder.bind(file, isParent);
        }
        
        @Override
        public int getItemCount() {
            return files.size();
        }
    }

    /**
     * ViewHolder for files
     */
    private class FileViewHolder extends RecyclerView.ViewHolder {
        
        private TextView nameTextView;
        private TextView infoTextView;
        
        /**
         * Create a new FileViewHolder
         *
         * @param itemView Item view
         */
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            
            nameTextView = itemView.findViewById(R.id.text_file_name);
            infoTextView = itemView.findViewById(R.id.text_file_info);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    File file = fileAdapter.files.get(position);
                    
                    if (position == 0 && !currentPath.equals(rootPath)) {
                        // Parent directory
                        navigateToDirectory(file);
                    } else if (file.isDirectory()) {
                        navigateToDirectory(file);
                    } else if (fileClickListener != null) {
                        fileClickListener.onFileClick(file);
                    }
                }
            });
        }
        
        /**
         * Bind a file to this ViewHolder
         *
         * @param file File to bind
         * @param isParent true if this is the parent directory
         */
        public void bind(File file, boolean isParent) {
            if (isParent) {
                nameTextView.setText("..");
                infoTextView.setText("Parent Directory");
            } else {
                nameTextView.setText(file.getName());
                
                if (file.isDirectory()) {
                    infoTextView.setText("Directory");
                } else {
                    String size = formatFileSize(file.length());
                    infoTextView.setText(size);
                }
            }
        }
        
        /**
         * Format a file size
         *
         * @param size Size in bytes
         * @return Formatted size
         */
        private String formatFileSize(long size) {
            if (size < 1024) {
                return size + " B";
            } else if (size < 1024 * 1024) {
                return String.format("%.1f KB", size / 1024.0);
            } else if (size < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", size / (1024.0 * 1024.0));
            } else {
                return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
            }
        }
    }
}