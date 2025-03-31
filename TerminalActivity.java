package com.mobiledev.androidstudio.terminal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mobiledev.androidstudio.R;

/**
 * Activity for the terminal interface.
 */
public class TerminalActivity extends AppCompatActivity implements TerminalSession.SessionCallback {

    private TerminalView terminalView;
    private EditText inputEditText;
    private TextView infoTextView;
    private TerminalService terminalService;
    private TerminalSession currentSession;
    private boolean serviceBound = false;
    private String currentWorkingDirectory;

    /**
     * Service connection for the terminal service
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TerminalService.TerminalBinder binder = (TerminalService.TerminalBinder) service;
            terminalService = binder.getService();
            serviceBound = true;
            
            // Get or create a terminal session
            if (terminalService.getSessions().isEmpty()) {
                // Create a new session
                currentSession = terminalService.createSession();
            } else {
                // Use the first existing session
                currentSession = terminalService.getSessions().get(0);
            }
            
            // Attach session to view
            terminalView.attachSession(currentSession);
            currentSession.setCallback(TerminalActivity.this);
            
            // Update information
            updateSessionInfo();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            terminalService = null;
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.nav_terminal);
        }
        
        // Initialize views
        terminalView = findViewById(R.id.terminal_view);
        inputEditText = findViewById(R.id.edit_terminal_input);
        infoTextView = findViewById(R.id.text_terminal_info);
        
        // Set up the command input
        setupCommandInput();
        
        // Start the terminal service
        Intent intent = new Intent(this, TerminalService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Set up the command input field
     */
    private void setupCommandInput() {
        inputEditText.setOnEditorActionListener((v, actionId, event) -> {
            executeCommand(inputEditText.getText().toString());
            inputEditText.setText("");
            return true;
        });
        
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Implement command history and auto-completion here if desired
            }
        });
    }

    /**
     * Execute a command in the terminal
     *
     * @param command Command to execute
     */
    private void executeCommand(String command) {
        if (currentSession != null && command != null && !command.trim().isEmpty()) {
            // Send the command to the terminal session
            currentSession.write(command + "\n");
            
            // Focus the terminal view to show output
            terminalView.requestFocus();
        }
    }

    /**
     * Update the session information (CWD, etc.)
     */
    private void updateSessionInfo() {
        if (currentSession != null) {
            // In a real implementation, we would get this from the shell
            currentWorkingDirectory = currentSession.getCurrentWorkingDirectory();
            infoTextView.setText(currentWorkingDirectory);
        }
    }

    /**
     * Create a new terminal session
     */
    private void createNewSession() {
        if (terminalService != null) {
            currentSession = terminalService.createSession();
            terminalView.attachSession(currentSession);
            currentSession.setCallback(this);
            updateSessionInfo();
        }
    }

    /**
     * Clear the current terminal screen
     */
    private void clearScreen() {
        if (currentSession != null) {
            currentSession.write("clear\n");
        }
    }

    /**
     * Show or hide the soft keyboard
     *
     * @param show true to show, false to hide
     */
    private void toggleKeyboard(boolean show) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        
        if (show) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_terminal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_new_session) {
            createNewSession();
            return true;
        } else if (id == R.id.action_clear) {
            clearScreen();
            return true;
        } else if (id == R.id.action_keyboard) {
            toggleKeyboard(true);
            return true;
        } else if (id == R.id.action_preferences) {
            // Open terminal preferences (not implemented yet)
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onDestroy();
    }

    // --- TerminalSession.SessionCallback methods ---

    @Override
    public void onSessionFinished(TerminalSession session) {
        if (session == currentSession) {
            finish(); // Close the activity when session ends
        }
    }

    @Override
    public void onTextChanged(TerminalSession session) {
        // Update the terminal view when text changes
        if (session == currentSession) {
            terminalView.invalidate();
        }
    }

    @Override
    public void onDirectoryChanged(TerminalSession session, String newDirectory) {
        if (session == currentSession) {
            currentWorkingDirectory = newDirectory;
            infoTextView.setText(newDirectory);
        }
    }

    @Override
    public void onBell(TerminalSession session) {
        // Implement terminal bell behavior if desired
    }
}