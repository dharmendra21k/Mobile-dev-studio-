package com.mobiledev.androidstudio.terminal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobiledev.androidstudio.MobileDevApplication;
import com.mobiledev.androidstudio.utils.PreRootManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Terminal Fragment for handling terminal sessions
 */
public class TerminalFragment extends Fragment {
    private static final String TAG = "TerminalFragment";
    private static final String ARG_SESSION_ID = "session_id";
    
    private String sessionId;
    private TextView terminalOutput;
    private Process terminalProcess;
    private BufferedWriter terminalInput;
    private ExecutorService executorService;
    private TerminalSessionCallback callback;
    
    public static TerminalFragment newInstance(String sessionId) {
        TerminalFragment fragment = new TerminalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionId = getArguments().getString(ARG_SESSION_ID);
        }
        
        executorService = Executors.newFixedThreadPool(2);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create a TextView for now (in a real app, this would be a custom terminal view)
        terminalOutput = new TextView(getContext());
        terminalOutput.setTextColor(0xFFFFFFFF); // White text
        terminalOutput.setBackgroundColor(0xFF000000); // Black background
        terminalOutput.setTextSize(14);
        return terminalOutput;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Start terminal session
        startTerminalSession();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TerminalSessionCallback) {
            callback = (TerminalSessionCallback) context;
        } else {
            throw new RuntimeException(context + " must implement TerminalSessionCallback");
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
    
    @Override
    public void onDestroy() {
        stopTerminalSession();
        executorService.shutdown();
        super.onDestroy();
    }
    
    /**
     * Start the terminal session
     */
    private void startTerminalSession() {
        try {
            PreRootManager prootManager = MobileDevApplication.getInstance().getPreRootManager();
            
            // Check if PRoot environment is ready
            if (!prootManager.isEnvironmentReady()) {
                appendToTerminal("Setting up terminal environment... Please wait.");
                return;
            }
            
            // For simplicity, we'll use a basic shell process
            // In a real implementation, this would use the PRoot environment
            ProcessBuilder processBuilder = new ProcessBuilder("/system/bin/sh");
            processBuilder.redirectErrorStream(true);
            
            terminalProcess = processBuilder.start();
            
            // Get process streams
            InputStream stdout = terminalProcess.getInputStream();
            OutputStream stdin = terminalProcess.getOutputStream();
            
            // Create buffered writers for input
            terminalInput = new BufferedWriter(new OutputStreamWriter(stdin));
            
            // Start output reading thread
            executorService.execute(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                String line;
                
                try {
                    while ((line = reader.readLine()) != null) {
                        final String output = line;
                        MobileDevApplication.getInstance().runOnUiThread(() -> {
                            appendToTerminal(output);
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading terminal output", e);
                }
                
                // Terminal session ended
                MobileDevApplication.getInstance().runOnUiThread(() -> {
                    appendToTerminal("\nTerminal session ended");
                    if (callback != null) {
                        callback.onSessionClosed(sessionId);
                    }
                });
            });
            
            // Notify activity that session started
            if (callback != null) {
                callback.onSessionStarted(sessionId);
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error starting terminal session", e);
            appendToTerminal("Error starting terminal: " + e.getMessage());
        }
    }
    
    /**
     * Send input to the terminal
     * @param input The input to send
     */
    public void sendInput(String input) {
        if (terminalInput != null) {
            executorService.execute(() -> {
                try {
                    terminalInput.write(input);
                    terminalInput.flush();
                } catch (IOException e) {
                    Log.e(TAG, "Error writing to terminal", e);
                }
            });
        }
    }
    
    /**
     * Append text to the terminal output
     * @param text The text to append
     */
    private void appendToTerminal(String text) {
        terminalOutput.append(text + "\n");
    }
    
    /**
     * Stop the terminal session
     */
    private void stopTerminalSession() {
        if (terminalProcess != null) {
            terminalProcess.destroy();
            terminalProcess = null;
        }
    }
    
    /**
     * Callback interface for terminal session events
     */
    public interface TerminalSessionCallback {
        void onSessionStarted(String sessionId);
        void onSessionClosed(String sessionId);
    }
}