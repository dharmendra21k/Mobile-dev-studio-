package com.mobiledev.androidstudio.installation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.MobileDevApplication;
import com.mobiledev.androidstudio.utils.PreRootManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for installing and managing packages in the PRoot environment
 */
public class PackageManagerActivity extends AppCompatActivity {
    private static final String TAG = "PackageManagerActivity";
    
    private RecyclerView packagesRecyclerView;
    private PackageAdapter adapter;
    private List<PackageInfo> availablePackages;
    private ExecutorService executorService;
    private ProgressBar progressBar;
    private TextView statusText;
    private SearchView searchView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This would normally be done with a layout resource
        // setContentView(R.layout.activity_package_manager);
        
        progressBar = new ProgressBar(this);
        statusText = new TextView(this);
        packagesRecyclerView = new RecyclerView(this);
        
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize the list of available packages
        availablePackages = new ArrayList<>();
        
        // Add some predefined packages
        initPredefinedPackages();
        
        // Set up the RecyclerView
        adapter = new PackageAdapter(availablePackages);
        packagesRecyclerView.setAdapter(adapter);
        packagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load installed packages
        refreshInstalledPackages();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        getMenuInflater().inflate(R.menu.menu_package_manager, menu);
        
        // Configure the search view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPackages(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                filterPackages(newText);
                return true;
            }
        });
        */
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        int id = item.getItemId();
        
        if (id == R.id.action_refresh) {
            refreshInstalledPackages();
            return true;
        }
        */
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }
    
    /**
     * Initialize the list of predefined packages
     */
    private void initPredefinedPackages() {
        // Languages
        availablePackages.add(new PackageInfo("python", "Python 3.9", "Python programming language", "apt install python3"));
        availablePackages.add(new PackageInfo("nodejs", "Node.js", "JavaScript runtime", "apt install nodejs npm"));
        availablePackages.add(new PackageInfo("ruby", "Ruby", "Ruby programming language", "apt install ruby"));
        availablePackages.add(new PackageInfo("php", "PHP", "PHP programming language", "apt install php"));
        availablePackages.add(new PackageInfo("golang", "Go", "Go programming language", "apt install golang"));
        availablePackages.add(new PackageInfo("rust", "Rust", "Rust programming language", "apt install rustc cargo"));
        availablePackages.add(new PackageInfo("java", "OpenJDK", "Java Development Kit", "apt install openjdk-11-jdk"));
        availablePackages.add(new PackageInfo("lua", "Lua", "Lua programming language", "apt install lua5.3"));
        
        // Build tools and compilers
        availablePackages.add(new PackageInfo("gcc", "GCC", "GNU Compiler Collection", "apt install gcc"));
        availablePackages.add(new PackageInfo("clang", "Clang", "C language compiler", "apt install clang"));
        availablePackages.add(new PackageInfo("make", "Make", "Build automation tool", "apt install make"));
        availablePackages.add(new PackageInfo("cmake", "CMake", "Cross-platform build system", "apt install cmake"));
        
        // Version control
        availablePackages.add(new PackageInfo("git", "Git", "Version control system", "apt install git"));
        
        // Editors
        availablePackages.add(new PackageInfo("vim", "Vim", "Improved vi editor", "apt install vim"));
        availablePackages.add(new PackageInfo("nano", "Nano", "Simple text editor", "apt install nano"));
        
        // Utilities
        availablePackages.add(new PackageInfo("curl", "curl", "Command line tool for transferring data", "apt install curl"));
        availablePackages.add(new PackageInfo("wget", "wget", "Internet file retriever", "apt install wget"));
        availablePackages.add(new PackageInfo("tmux", "tmux", "Terminal multiplexer", "apt install tmux"));
        availablePackages.add(new PackageInfo("zip", "zip/unzip", "Compression utilities", "apt install zip unzip"));
        
        // Databases
        availablePackages.add(new PackageInfo("sqlite", "SQLite", "Lightweight database", "apt install sqlite3"));
        availablePackages.add(new PackageInfo("mysql", "MySQL Client", "MySQL database client", "apt install mysql-client"));
        
        // Web servers
        availablePackages.add(new PackageInfo("nginx", "NGINX", "Lightweight web server", "apt install nginx"));
        availablePackages.add(new PackageInfo("apache", "Apache", "HTTP server", "apt install apache2"));
    }
    
    /**
     * Filter packages based on search query
     */
    private void filterPackages(String query) {
        List<PackageInfo> filteredList = new ArrayList<>();
        
        if (query == null || query.isEmpty()) {
            filteredList.addAll(availablePackages);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            
            for (PackageInfo packageInfo : availablePackages) {
                if (packageInfo.getName().toLowerCase().contains(lowerCaseQuery) ||
                        packageInfo.getDisplayName().toLowerCase().contains(lowerCaseQuery) ||
                        packageInfo.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(packageInfo);
                }
            }
        }
        
        adapter.updatePackages(filteredList);
        adapter.notifyDataSetChanged();
    }
    
    /**
     * Refresh the list of installed packages
     */
    private void refreshInstalledPackages() {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Refreshing packages...");
        
        executorService.execute(() -> {
            // Get the PRoot manager
            PreRootManager prootManager = MobileDevApplication.getInstance().getPreRootManager();
            
            // Check if PRoot environment is ready
            if (!prootManager.isEnvironmentReady()) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("PRoot environment not ready");
                    Toast.makeText(PackageManagerActivity.this, "PRoot environment not ready", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            
            // Execute dpkg -l command to list installed packages
            prootManager.executeCommand("dpkg -l | grep \"^ii\" > /tmp/installed_packages.txt");
            
            // In a real implementation, we would read the output file and parse the list of installed packages
            // For now, just simulate a delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while refreshing packages", e);
            }
            
            // Update package status
            for (PackageInfo packageInfo : availablePackages) {
                // Randomly set some packages as installed for demonstration
                packageInfo.setInstalled(Math.random() > 0.7);
            }
            
            // Update UI on the main thread
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                statusText.setText("Ready");
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    /**
     * Install or uninstall a package
     */
    private void installOrUninstallPackage(PackageInfo packageInfo) {
        progressBar.setVisibility(View.VISIBLE);
        
        if (packageInfo.isInstalled()) {
            // Uninstall package
            statusText.setText("Uninstalling " + packageInfo.getDisplayName() + "...");
            packageInfo.setInstalling(true);
            adapter.notifyDataSetChanged();
            
            executorService.execute(() -> {
                // Get the PRoot manager
                PreRootManager prootManager = MobileDevApplication.getInstance().getPreRootManager();
                
                // Create uninstall command
                String uninstallCommand = "apt remove -y " + packageInfo.getName();
                
                // Execute command
                int result = prootManager.executeCommand(uninstallCommand);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (result == 0) {
                        // Uninstall successful
                        packageInfo.setInstalled(false);
                        Toast.makeText(PackageManagerActivity.this, 
                                packageInfo.getDisplayName() + " uninstalled successfully", 
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Uninstall failed
                        Toast.makeText(PackageManagerActivity.this, 
                                "Failed to uninstall " + packageInfo.getDisplayName(), 
                                Toast.LENGTH_SHORT).show();
                    }
                    
                    packageInfo.setInstalling(false);
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Ready");
                    adapter.notifyDataSetChanged();
                });
            });
        } else {
            // Install package
            statusText.setText("Installing " + packageInfo.getDisplayName() + "...");
            packageInfo.setInstalling(true);
            adapter.notifyDataSetChanged();
            
            executorService.execute(() -> {
                // Get the PRoot manager
                PreRootManager prootManager = MobileDevApplication.getInstance().getPreRootManager();
                
                // Execute install command
                int result = prootManager.executeCommand(packageInfo.getInstallCommand());
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (result == 0) {
                        // Install successful
                        packageInfo.setInstalled(true);
                        Toast.makeText(PackageManagerActivity.this, 
                                packageInfo.getDisplayName() + " installed successfully", 
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Install failed
                        Toast.makeText(PackageManagerActivity.this, 
                                "Failed to install " + packageInfo.getDisplayName(), 
                                Toast.LENGTH_SHORT).show();
                    }
                    
                    packageInfo.setInstalling(false);
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Ready");
                    adapter.notifyDataSetChanged();
                });
            });
        }
    }
    
    /**
     * Adapter for the package list
     */
    private class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {
        private List<PackageInfo> packages;
        
        public PackageAdapter(List<PackageInfo> packages) {
            this.packages = new ArrayList<>(packages);
        }
        
        public void updatePackages(List<PackageInfo> newPackages) {
            this.packages = new ArrayList<>(newPackages);
        }
        
        @NonNull
        @Override
        public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // In a real app, we would inflate from a layout resource
            // View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
            
            // For simplicity, create views programmatically
            View view = new View(parent.getContext());
            
            return new PackageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
            PackageInfo packageInfo = packages.get(position);
            holder.bind(packageInfo);
        }
        
        @Override
        public int getItemCount() {
            return packages.size();
        }
        
        /**
         * ViewHolder for package items
         */
        class PackageViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView;
            private TextView descriptionTextView;
            private Button actionButton;
            private ProgressBar installProgressBar;
            
            public PackageViewHolder(@NonNull View itemView) {
                super(itemView);
                
                // In a real app, we would get views from the inflated layout
                // nameTextView = itemView.findViewById(R.id.package_name);
                // descriptionTextView = itemView.findViewById(R.id.package_description);
                // actionButton = itemView.findViewById(R.id.package_action_button);
                // installProgressBar = itemView.findViewById(R.id.package_progress);
                
                // For simplicity, create views programmatically
                nameTextView = new TextView(itemView.getContext());
                descriptionTextView = new TextView(itemView.getContext());
                actionButton = new Button(itemView.getContext());
                installProgressBar = new ProgressBar(itemView.getContext());
            }
            
            public void bind(PackageInfo packageInfo) {
                nameTextView.setText(packageInfo.getDisplayName());
                descriptionTextView.setText(packageInfo.getDescription());
                
                // Update button text based on package state
                if (packageInfo.isInstalling()) {
                    actionButton.setEnabled(false);
                    actionButton.setText(packageInfo.isInstalled() ? "Uninstalling..." : "Installing...");
                    installProgressBar.setVisibility(View.VISIBLE);
                } else {
                    actionButton.setEnabled(true);
                    actionButton.setText(packageInfo.isInstalled() ? "Uninstall" : "Install");
                    installProgressBar.setVisibility(View.GONE);
                }
                
                // Set click listener for action button
                actionButton.setOnClickListener(v -> {
                    installOrUninstallPackage(packageInfo);
                });
            }
        }
    }
}