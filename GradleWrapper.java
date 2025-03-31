package com.mobiledev.androidstudio.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GradleWrapper {
    private static final String TAG = "GradleWrapper";
    
    private final Context context;
    private String projectPath;
    private OnGradleTaskListener listener;
    
    public interface OnGradleTaskListener {
        void onTaskStarted();
        void onTaskProgress(String message);
        void onTaskCompleted(boolean success, String output);
    }
    
    public GradleWrapper(Context context) {
        this.context = context;
    }
    
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
    
    public void setOnGradleTaskListener(OnGradleTaskListener listener) {
        this.listener = listener;
    }
    
    public void assembleDebug() {
        executeGradleTask("assembleDebug");
    }
    
    public void clean() {
        executeGradleTask("clean");
    }
    
    public void build() {
        executeGradleTask("build");
    }
    
    public void cleanBuild() {
        List<String> tasks = new ArrayList<>();
        tasks.add("clean");
        tasks.add("build");
        executeGradleTasks(tasks);
    }
    
    private void executeGradleTask(String task) {
        List<String> tasks = new ArrayList<>();
        tasks.add(task);
        executeGradleTasks(tasks);
    }
    
    private void executeGradleTasks(List<String> tasks) {
        if (projectPath == null || projectPath.isEmpty()) {
            if (listener != null) {
                listener.onTaskCompleted(false, "Project path not set");
            }
            return;
        }
        
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            if (listener != null) {
                listener.onTaskCompleted(false, "Invalid project directory");
            }
            return;
        }
        
        new GradleAsyncTask(tasks).execute(projectDir);
    }
    
    private class GradleAsyncTask extends AsyncTask<File, String, Boolean> {
        private final List<String> tasks;
        private final StringBuilder outputBuilder = new StringBuilder();
        
        GradleAsyncTask(List<String> tasks) {
            this.tasks = tasks;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listener != null) {
                listener.onTaskStarted();
            }
        }
        
        @Override
        protected Boolean doInBackground(File... dirs) {
            File projectDir = dirs[0];
            
            try {
                // Construct command: ./gradlew task1 task2 ...
                List<String> command = new ArrayList<>();
                
                // Use the appropriate wrapper script based on OS
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("windows")) {
                    command.add("cmd");
                    command.add("/c");
                    command.add("gradlew.bat");
                } else {
                    command.add("./gradlew");
                }
                
                // Add tasks
                command.addAll(tasks);
                
                // Create process builder
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(projectDir);
                processBuilder.redirectErrorStream(true);
                
                // Start process
                publishProgress("Starting Gradle tasks: " + String.join(" ", tasks));
                Process process = processBuilder.start();
                
                // Read output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                    publishProgress(line);
                }
                
                // Wait for process to complete
                int exitCode = process.waitFor();
                publishProgress("Gradle process completed with exit code: " + exitCode);
                
                return exitCode == 0;
            } catch (IOException | InterruptedException e) {
                String errorMessage = "Error executing Gradle task: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                outputBuilder.append(errorMessage).append("\n");
                publishProgress(errorMessage);
                return false;
            }
        }
        
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (listener != null && values.length > 0) {
                listener.onTaskProgress(values[0]);
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (listener != null) {
                listener.onTaskCompleted(success, outputBuilder.toString());
            }
        }
    }
}