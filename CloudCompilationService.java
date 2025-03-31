package com.mobiledev.androidstudio.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.mobiledev.androidstudio.model.Project;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for cloud compilation
 */
public class CloudCompilationService {
    
    private static final String TAG = CloudCompilationService.class.getSimpleName();
    private static final String CLOUD_COMPILATION_ENDPOINT = "https://cloud-compiler.mobiledev.studio/compile";
    private static final int BUFFER_SIZE = 8192;
    
    // Compilation status constants
    public static final int STATUS_NOT_STARTED = 0;
    public static final int STATUS_PREPARING = 1;
    public static final int STATUS_UPLOADING = 2;
    public static final int STATUS_COMPILING = 3;
    public static final int STATUS_DOWNLOADING = 4;
    public static final int STATUS_COMPLETED = 5;
    public static final int STATUS_ERROR = -1;
    
    /**
     * Interface for compilation callbacks
     */
    public interface CompilationCallback {
        /**
         * Called when compilation status changes
         * 
         * @param status Status code
         * @param message Status message
         * @param progress Progress percentage (0-100)
         */
        void onStatusUpdate(int status, String message, int progress);
        
        /**
         * Called when compilation is complete
         * 
         * @param success Whether compilation was successful
         * @param apkFile Path to the compiled APK file
         * @param errorMessage Error message if compilation failed
         */
        void onComplete(boolean success, String apkFile, String errorMessage);
    }
    
    private final Context context;
    
    /**
     * Constructor
     * 
     * @param context Context
     */
    public CloudCompilationService(Context context) {
        this.context = context;
    }
    
    /**
     * Compile a project
     * 
     * @param project Project to compile
     * @param config Compilation configuration
     * @param callback Callback for compilation events
     */
    public void compileProject(Project project, CloudCompilationConfig config, CompilationCallback callback) {
        new CompilationTask(project, config, callback).execute();
    }
    
    /**
     * Background task for compilation
     */
    private class CompilationTask extends AsyncTask<Void, Object, Object[]> {
        
        private final Project project;
        private final CloudCompilationConfig config;
        private final CompilationCallback callback;
        private final StringBuilder errorLog;
        
        /**
         * Constructor
         * 
         * @param project Project to compile
         * @param config Compilation configuration
         * @param callback Callback for compilation events
         */
        CompilationTask(Project project, CloudCompilationConfig config, CompilationCallback callback) {
            this.project = project;
            this.config = config;
            this.callback = callback;
            this.errorLog = new StringBuilder();
        }
        
        @Override
        protected void onPreExecute() {
            updateStatus(STATUS_NOT_STARTED, "Preparing for compilation", 0);
        }
        
        @Override
        protected Object[] doInBackground(Void... params) {
            try {
                // Create a temporary zip file of the project
                updateStatus(STATUS_PREPARING, "Preparing project files", 10);
                File projectZip = createProjectZip();
                
                if (projectZip == null) {
                    throw new IOException("Failed to create project zip file");
                }
                
                // Upload to cloud compilation service
                updateStatus(STATUS_UPLOADING, "Uploading project to cloud", 30);
                String compilationId = uploadProject(projectZip, config);
                
                if (compilationId == null || compilationId.isEmpty()) {
                    throw new IOException("Failed to get compilation ID from server");
                }
                
                // Wait for compilation to complete
                updateStatus(STATUS_COMPILING, "Compiling project", 50);
                JSONObject compilationResult = waitForCompilation(compilationId);
                
                if (compilationResult == null) {
                    throw new IOException("Failed to get compilation result");
                }
                
                boolean success = compilationResult.optBoolean("success", false);
                
                if (!success) {
                    String errorMessage = compilationResult.optString("error", "Unknown error");
                    throw new IOException("Compilation failed: " + errorMessage);
                }
                
                // Download compiled APK
                updateStatus(STATUS_DOWNLOADING, "Downloading compiled APK", 80);
                String apkUrl = compilationResult.optString("apk_url", "");
                
                if (apkUrl.isEmpty()) {
                    throw new IOException("No APK URL in compilation result");
                }
                
                String apkPath = downloadApk(apkUrl, project.getName());
                
                if (apkPath == null || apkPath.isEmpty()) {
                    throw new IOException("Failed to download APK");
                }
                
                // Deployment (if enabled)
                if (!CloudCompilationConfig.DEPLOYMENT_NONE.equals(config.getDeploymentType())) {
                    // Handle deployment
                    // ...
                }
                
                return new Object[] { true, apkPath, null };
            } catch (Exception e) {
                Log.e(TAG, "Compilation error", e);
                errorLog.append(e.getMessage()).append("\n");
                return new Object[] { false, null, e.getMessage() };
            }
        }
        
        @Override
        protected void onProgressUpdate(Object... values) {
            if (values.length >= 3) {
                int status = (int) values[0];
                String message = (String) values[1];
                int progress = (int) values[2];
                
                if (callback != null) {
                    callback.onStatusUpdate(status, message, progress);
                }
            }
        }
        
        @Override
        protected void onPostExecute(Object[] result) {
            boolean success = (boolean) result[0];
            String apkFile = (String) result[1];
            String errorMessage = (String) result[2];
            
            if (success) {
                updateStatus(STATUS_COMPLETED, "Compilation completed", 100);
            } else {
                updateStatus(STATUS_ERROR, "Compilation failed: " + errorMessage, 0);
            }
            
            if (callback != null) {
                callback.onComplete(success, apkFile, errorMessage);
            }
        }
        
        /**
         * Update compilation status
         * 
         * @param status Status code
         * @param message Status message
         * @param progress Progress percentage
         */
        private void updateStatus(int status, String message, int progress) {
            publishProgress(status, message, progress);
        }
        
        /**
         * Create a zip file of the project
         * 
         * @return Zip file
         * @throws IOException If an I/O error occurs
         */
        private File createProjectZip() throws IOException {
            File projectDir = new File(project.getPath());
            
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                Log.e(TAG, "Project directory not found: " + projectDir.getAbsolutePath());
                return null;
            }
            
            File zipFile = File.createTempFile("project_", ".zip", context.getCacheDir());
            
            try (FileInputStream fis = new FileInputStream(projectDir);
                 ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                
                addDirectoryToZip(zos, projectDir, "");
            }
            
            return zipFile;
        }
        
        /**
         * Add a directory to a zip file
         * 
         * @param zos Zip output stream
         * @param directory Directory to add
         * @param basePath Base path in the zip file
         * @throws IOException If an I/O error occurs
         */
        private void addDirectoryToZip(ZipOutputStream zos, File directory, String basePath) throws IOException {
            File[] files = directory.listFiles();
            
            if (files == null) {
                return;
            }
            
            byte[] buffer = new byte[BUFFER_SIZE];
            
            for (File file : files) {
                String entryPath = basePath.isEmpty() ? file.getName() : basePath + "/" + file.getName();
                
                if (file.isDirectory()) {
                    // Add empty directory entry
                    ZipEntry entry = new ZipEntry(entryPath + "/");
                    zos.putNextEntry(entry);
                    zos.closeEntry();
                    
                    // Add directory contents
                    addDirectoryToZip(zos, file, entryPath);
                } else {
                    // Add file
                    ZipEntry entry = new ZipEntry(entryPath);
                    zos.putNextEntry(entry);
                    
                    try (FileInputStream fis = new FileInputStream(file)) {
                        int bytesRead;
                        
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    zos.closeEntry();
                }
            }
        }
        
        /**
         * Upload project to cloud compilation service
         * 
         * @param projectZip Project zip file
         * @param config Compilation configuration
         * @return Compilation ID
         * @throws IOException If an I/O error occurs
         */
        private String uploadProject(File projectZip, CloudCompilationConfig config) throws IOException {
            URL url = new URL(CLOUD_COMPILATION_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/zip");
            connection.setRequestProperty("X-Build-Type", config.getBuildType());
            connection.setRequestProperty("X-Deployment-Type", config.getDeploymentType());
            connection.setRequestProperty("X-API-Key", config.getApiKey());
            
            // Add extra options as headers
            for (Map.Entry<String, String> entry : config.getExtraOptions().entrySet()) {
                connection.setRequestProperty("X-Option-" + entry.getKey(), entry.getValue());
            }
            
            try (FileInputStream fis = new FileInputStream(projectZip);
                 OutputStream os = connection.getOutputStream()) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                long fileSize = projectZip.length();
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    
                    totalBytesRead += bytesRead;
                    int progress = (int) (30 + (totalBytesRead * 20) / fileSize);
                    updateStatus(STATUS_UPLOADING, "Uploading project to cloud", progress);
                }
            }
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + responseCode
                        + ": " + connection.getResponseMessage());
            }
            
            try (InputStream is = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                JSONObject json = new JSONObject(response.toString());
                return json.optString("compilation_id", "");
            }
        }
        
        /**
         * Wait for compilation to complete
         * 
         * @param compilationId Compilation ID
         * @return Compilation result
         * @throws IOException If an I/O error occurs
         * @throws InterruptedException If the thread is interrupted
         */
        private JSONObject waitForCompilation(String compilationId) throws IOException, InterruptedException {
            URL url = new URL(CLOUD_COMPILATION_ENDPOINT + "/" + compilationId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-API-Key", config.getApiKey());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int progress = 50;
            int retries = 0;
            int maxRetries = 60; // 5 minutes timeout
            
            while (retries < maxRetries) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("X-API-Key", config.getApiKey());
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + responseCode
                            + ": " + connection.getResponseMessage());
                    Thread.sleep(5000);
                    retries++;
                    continue;
                }
                
                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JSONObject json = new JSONObject(response.toString());
                    
                    String status = json.optString("status", "");
                    
                    if ("completed".equals(status)) {
                        // Compilation completed
                        return json;
                    } else if ("error".equals(status)) {
                        // Compilation failed
                        String error = json.optString("error", "Unknown error");
                        errorLog.append("Compilation error: ").append(error).append("\n");
                        return json;
                    } else if ("compiling".equals(status)) {
                        // Compilation in progress
                        int compilationProgress = json.optInt("progress", 0);
                        progress = 50 + (compilationProgress * 30) / 100;
                        updateStatus(STATUS_COMPILING, "Compiling project", progress);
                    }
                }
                
                Thread.sleep(5000);
                retries++;
            }
            
            throw new IOException("Compilation timed out");
        }
        
        /**
         * Download compiled APK
         * 
         * @param apkUrl APK URL
         * @param projectName Project name
         * @return Path to downloaded APK
         * @throws IOException If an I/O error occurs
         */
        private String downloadApk(String apkUrl, String projectName) throws IOException {
            URL url = new URL(apkUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-API-Key", config.getApiKey());
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + responseCode
                        + ": " + connection.getResponseMessage());
            }
            
            // Get content length for progress tracking
            int contentLength = connection.getContentLength();
            
            // Create APK file in downloads directory
            File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            
            if (downloadsDir == null) {
                throw new IOException("Failed to get downloads directory");
            }
            
            // Create a safe file name
            String safeProjectName = projectName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = safeProjectName + "_" + System.currentTimeMillis() + ".apk";
            File apkFile = new File(downloadsDir, fileName);
            
            try (InputStream is = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(apkFile)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    
                    if (contentLength > 0) {
                        totalBytesRead += bytesRead;
                        int progress = 80 + (int) ((totalBytesRead * 20) / contentLength);
                        updateStatus(STATUS_DOWNLOADING, "Downloading compiled APK", progress);
                    }
                }
            }
            
            return apkFile.getAbsolutePath();
        }
    }
}