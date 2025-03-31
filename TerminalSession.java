package com.mobiledev.androidstudio.terminal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A terminal session, which handles the communication with the shell process.
 */
public class TerminalSession {

    /**
     * Callback interface for terminal session events
     */
    public interface SessionCallback {
        void onSessionFinished(TerminalSession session);
        void onTextChanged(TerminalSession session);
        void onBell(TerminalSession session);
        void onDirectoryChanged(TerminalSession session, String newDirectory);
    }

    private final Process process;
    private final String shellPath;
    private final String[] arguments;
    private final String initialWorkingDirectory;
    private String currentWorkingDirectory;
    private final InputStream terminalOutput;
    private final OutputStream terminalInput;
    private final byte[] buffer = new byte[4096];
    private SessionCallback callback;

    /**
     * Create a new terminal session with a shell process
     *
     * @param shellPath Path to the shell executable
     * @param arguments Arguments to pass to the shell
     * @param initialWorkingDirectory Initial working directory
     * @throws IOException If the process cannot be started
     */
    public TerminalSession(String shellPath, String[] arguments, String initialWorkingDirectory) throws IOException {
        this.shellPath = shellPath;
        this.arguments = arguments;
        this.initialWorkingDirectory = initialWorkingDirectory;
        this.currentWorkingDirectory = initialWorkingDirectory;

        ProcessBuilder processBuilder = new ProcessBuilder(shellPath);
        if (arguments != null) {
            processBuilder.command().addAll(java.util.Arrays.asList(arguments));
        }
        
        // Set the working directory if it exists
        if (initialWorkingDirectory != null) {
            File workingDirectory = new File(initialWorkingDirectory);
            if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                processBuilder.directory(workingDirectory);
            }
        }
        
        // Start the process
        process = processBuilder.start();
        terminalOutput = process.getInputStream();
        terminalInput = process.getOutputStream();

        // Start reading output from the terminal
        startReading();
    }

    /**
     * Write data to the terminal
     *
     * @param data Data to write
     */
    public void write(String data) {
        try {
            terminalInput.write(data.getBytes());
            terminalInput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start reading data from the terminal
     */
    private void startReading() {
        new Thread(() -> {
            try {
                int read;
                while ((read = terminalOutput.read(buffer)) != -1) {
                    // Process the data (in a real implementation this would update the terminal screen)
                    processOutput(buffer, read);
                    
                    // Notify about text change
                    if (callback != null) {
                        callback.onTextChanged(this);
                    }
                }
                
                // Process exited
                if (callback != null) {
                    callback.onSessionFinished(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onSessionFinished(this);
                }
            }
        }).start();
    }

    /**
     * Process output from the terminal
     *
     * @param buffer Buffer containing the data
     * @param len Length of the data
     */
    private void processOutput(byte[] buffer, int len) {
        // In a real implementation, this would update the terminal screen
        // and check for escape sequences, etc.
        
        // For demo purposes, check for directory change
        String data = new String(buffer, 0, len);
        checkDirectoryChange(data);
    }

    /**
     * Check if the directory has changed
     *
     * @param output Output data to check
     */
    private void checkDirectoryChange(String output) {
        // This is a simplified version - in a real implementation,
        // we would parse the output for directory changes or
        // use a more reliable method to track the CWD
        
        if (output.contains("cd ") && output.contains("\n")) {
            // Detect a CD command and update currentWorkingDirectory
            // (this is oversimplified and would need more robust parsing)
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("cd ")) {
                    String newDir = line.trim().substring(3).trim();
                    File dir = new File(newDir);
                    if (!dir.isAbsolute() && currentWorkingDirectory != null) {
                        dir = new File(currentWorkingDirectory, newDir);
                    }
                    
                    if (dir.exists() && dir.isDirectory()) {
                        currentWorkingDirectory = dir.getAbsolutePath();
                        if (callback != null) {
                            callback.onDirectoryChanged(this, currentWorkingDirectory);
                        }
                    }
                }
            }
        }
    }

    /**
     * Set the session callback
     *
     * @param callback Callback to set
     */
    public void setCallback(SessionCallback callback) {
        this.callback = callback;
    }

    /**
     * Get the current working directory
     *
     * @return Current working directory
     */
    public String getCurrentWorkingDirectory() {
        return currentWorkingDirectory;
    }

    /**
     * Check if the session is alive
     *
     * @return true if the session is alive, false otherwise
     */
    public boolean isAlive() {
        return process.isAlive();
    }

    /**
     * Finish the session
     */
    public void finish() {
        process.destroy();
    }
}