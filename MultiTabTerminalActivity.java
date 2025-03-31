package com.mobiledev.androidstudio.terminal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Activity that hosts multiple terminal tabs
 */
public class MultiTabTerminalActivity extends AppCompatActivity implements TerminalFragment.TerminalSessionCallback {
    private static final String TAG = "MultiTabTerminalActivity";
    private static final int MAX_TABS = 5;
    
    private TabHost tabHost;
    private Map<String, TerminalFragment> terminalFragments;
    private String currentSessionId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // For simplicity, using a simple layout - in a real app this would be a proper XML layout
        // setContentView(R.layout.activity_multi_tab_terminal);
        
        // Initialize tabs
        terminalFragments = new HashMap<>();
        
        // Set up floating action button for new terminal
        FloatingActionButton fab = new FloatingActionButton(this);
        fab.setOnClickListener(view -> addNewTerminalTab());
        
        // Add initial terminal tab
        if (savedInstanceState == null) {
            addNewTerminalTab();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Start terminal service
        startService(new Intent(this, TerminalService.class));
    }
    
    @Override
    protected void onStop() {
        // Don't stop terminal service when activity is stopped
        // as we want terminal to continue in background
        super.onStop();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.add(Menu.NONE, 1, Menu.NONE, "New Tab");
        // menu.add(Menu.NONE, 2, Menu.NONE, "Close Tab");
        // menu.add(Menu.NONE, 3, Menu.NONE, "Settings");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        switch (item.getItemId()) {
            case 1:
                addNewTerminalTab();
                return true;
            case 2:
                closeCurrentTab();
                return true;
            case 3:
                // Open settings
                return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Add a new terminal tab
     */
    private void addNewTerminalTab() {
        if (terminalFragments.size() >= MAX_TABS) {
            Toast.makeText(this, "Maximum number of terminals reached", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String sessionId = UUID.randomUUID().toString();
        String tabName = "Term " + (terminalFragments.size() + 1);
        
        // Create new terminal fragment
        TerminalFragment fragment = TerminalFragment.newInstance(sessionId);
        terminalFragments.put(sessionId, fragment);
        
        // Add fragment to container
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // transaction.add(R.id.terminal_container, fragment, sessionId);
        
        // If this is not the first tab, hide other fragments
        if (!terminalFragments.isEmpty()) {
            for (Fragment existingFragment : fragmentManager.getFragments()) {
                transaction.hide(existingFragment);
            }
        }
        
        transaction.show(fragment);
        transaction.commit();
        
        // Create tab in tab host
        /*
        TabSpec tabSpec = tabHost.newTabSpec(sessionId);
        tabSpec.setIndicator(tabName);
        tabSpec.setContent(tag -> new View(getApplicationContext()));
        tabHost.addTab(tabSpec);
        
        // Select the new tab
        tabHost.setCurrentTabByTag(sessionId);
        */
        
        currentSessionId = sessionId;
        
        Log.d(TAG, "Added new terminal tab: " + sessionId);
    }
    
    /**
     * Close the current terminal tab
     */
    private void closeCurrentTab() {
        if (currentSessionId != null) {
            // Get the terminal fragment
            TerminalFragment fragment = terminalFragments.get(currentSessionId);
            if (fragment != null) {
                // Remove fragment
                getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
                
                // Remove from map
                terminalFragments.remove(currentSessionId);
                
                // Remove tab
                // tabHost.clearAllTabs();
                
                // Add tabs back except the closed one
                for (Map.Entry<String, TerminalFragment> entry : terminalFragments.entrySet()) {
                    /*
                    TabSpec tabSpec = tabHost.newTabSpec(entry.getKey());
                    tabSpec.setIndicator("Term");
                    tabSpec.setContent(tag -> new View(getApplicationContext()));
                    tabHost.addTab(tabSpec);
                    */
                }
                
                // Select first tab if available
                if (!terminalFragments.isEmpty()) {
                    String firstKey = terminalFragments.keySet().iterator().next();
                    // tabHost.setCurrentTabByTag(firstKey);
                    currentSessionId = firstKey;
                    
                    // Show the fragment
                    getSupportFragmentManager().beginTransaction()
                        .show(terminalFragments.get(firstKey))
                        .commit();
                } else {
                    // No more tabs, add a new one
                    addNewTerminalTab();
                }
            }
        }
    }
    
    @Override
    public void onSessionStarted(String sessionId) {
        Log.d(TAG, "Terminal session started: " + sessionId);
    }
    
    @Override
    public void onSessionClosed(String sessionId) {
        Log.d(TAG, "Terminal session closed: " + sessionId);
        
        // If this is the current session, close the tab
        if (sessionId.equals(currentSessionId)) {
            runOnUiThread(this::closeCurrentTab);
        }
    }
}