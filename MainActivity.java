package com.mobiledev.androidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.mobiledev.androidstudio.build.ApkBuilderActivity;
import com.mobiledev.androidstudio.installation.PackageManagerActivity;
import com.mobiledev.androidstudio.terminal.MultiTabTerminalActivity;
import com.mobiledev.androidstudio.utils.PreRootManager;

/**
 * Main Activity for the Mobile Developer Studio app
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This would normally be done with a layout resource
        // setContentView(R.layout.activity_main);
        
        // For simplicity, create views programmatically
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        
        // Create buttons for main features
        String[] features = {
            "Terminal",
            "Package Manager",
            "APK Builder",
            "Project Manager",
            "Code Editor"
        };
        
        for (String feature : features) {
            Button button = new Button(this);
            button.setText(feature);
            button.setOnClickListener(v -> handleFeatureClick(feature));
            mainLayout.addView(button);
        }
        
        // Create toolbar
        Toolbar toolbar = new Toolbar(this);
        setSupportActionBar(toolbar);
        
        // Create drawer layout
        drawerLayout = new DrawerLayout(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Create navigation view
        navigationView = new NavigationView(this);
        navigationView.setNavigationItemSelectedListener(this);
        
        // Add views to drawer layout
        drawerLayout.addView(mainLayout);
        drawerLayout.addView(navigationView);
        
        setContentView(drawerLayout);
        
        // Check if PRoot environment is ready
        checkPRootEnvironment();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            // Open settings activity
            return true;
        }
        */
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks
        /*
        int id = item.getItemId();
        
        if (id == R.id.nav_terminal) {
            startActivity(new Intent(this, MultiTabTerminalActivity.class));
        } else if (id == R.id.nav_package_manager) {
            startActivity(new Intent(this, PackageManagerActivity.class));
        } else if (id == R.id.nav_apk_builder) {
            startActivity(new Intent(this, ApkBuilderActivity.class));
        } else if (id == R.id.nav_project_manager) {
            // Open project manager
        } else if (id == R.id.nav_code_editor) {
            // Open code editor
        } else if (id == R.id.nav_settings) {
            // Open settings
        } else if (id == R.id.nav_help) {
            // Open help
        }
        */
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    
    /**
     * Handle clicks on feature buttons
     */
    private void handleFeatureClick(String feature) {
        switch (feature) {
            case "Terminal":
                startActivity(new Intent(this, MultiTabTerminalActivity.class));
                break;
            case "Package Manager":
                startActivity(new Intent(this, PackageManagerActivity.class));
                break;
            case "APK Builder":
                startActivity(new Intent(this, ApkBuilderActivity.class));
                break;
            case "Project Manager":
                Toast.makeText(this, "Project Manager not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case "Code Editor":
                Toast.makeText(this, "Code Editor not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    /**
     * Check if PRoot environment is ready
     */
    private void checkPRootEnvironment() {
        PreRootManager prootManager = MobileDevApplication.getInstance().getPreRootManager();
        
        if (!prootManager.isEnvironmentReady()) {
            Toast.makeText(this, "Setting up terminal environment. This may take a few minutes...", Toast.LENGTH_LONG).show();
            prootManager.scheduleInitialSetup();
        }
    }
}