package com.mobiledev.androidstudio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Manager for backup and sync
 */
public class BackupManager {

    private static final String TAG = "BackupManager";
    private static final String PREF_NAME = "backup_prefs";
    private static final String PREF_AUTO_BACKUP = "auto_backup";
    private static final String PREF_BACKUP_SCHEDULE = "backup_schedule";
    private static final String PREF_BACKUP_LOCATION = "backup_location";
    private static final String PREF_CLOUD_SYNC = "cloud_sync";
    private static final String PREF_CLOUD_AUTH = "cloud_auth";
    private static final String PREF_BACKUP_INTERVAL = "backup_interval";
    private static final String PREF_LAST_BACKUP = "last_backup";
    
    private static final int MAX_LOCAL_BACKUPS = 5;
    
    private Context context;
    private SharedPreferences preferences;
    private String backupDir;

    /**
     * Constructor
     *
     * @param context Context
     */
    public BackupManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Create backup directory
        backupDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDevStudio/Backups";
        new File(backupDir).mkdirs();
    }

    /**
     * Enable or disable auto backup
     *
     * @param enabled True to enable
     */
    public void setAutoBackup(boolean enabled) {
        preferences.edit().putBoolean(PREF_AUTO_BACKUP, enabled).apply();
    }

    /**
     * Check if auto backup is enabled
     *
     * @return True if enabled
     */
    public boolean isAutoBackupEnabled() {
        return preferences.getBoolean(PREF_AUTO_BACKUP, false);
    }

    /**
     * Set the backup schedule
     *
     * @param schedule Schedule (daily, weekly, monthly)
     */
    public void setBackupSchedule(String schedule) {
        preferences.edit().putString(PREF_BACKUP_SCHEDULE, schedule).apply();
    }

    /**
     * Get the backup schedule
     *
     * @return Schedule
     */
    public String getBackupSchedule() {
        return preferences.getString(PREF_BACKUP_SCHEDULE, "daily");
    }

    /**
     * Set the backup location
     *
     * @param location Location (local, cloud)
     */
    public void setBackupLocation(String location) {
        preferences.edit().putString(PREF_BACKUP_LOCATION, location).apply();
    }

    /**
     * Get the backup location
     *
     * @return Location
     */
    public String getBackupLocation() {
        return preferences.getString(PREF_BACKUP_LOCATION, "local");
    }

    /**
     * Enable or disable cloud sync
     *
     * @param enabled True to enable
     */
    public void setCloudSync(boolean enabled) {
        preferences.edit().putBoolean(PREF_CLOUD_SYNC, enabled).apply();
    }

    /**
     * Check if cloud sync is enabled
     *
     * @return True if enabled
     */
    public boolean isCloudSyncEnabled() {
        return preferences.getBoolean(PREF_CLOUD_SYNC, false);
    }

    /**
     * Set cloud authentication
     *
     * @param auth Authentication data
     */
    public void setCloudAuth(String auth) {
        preferences.edit().putString(PREF_CLOUD_AUTH, auth).apply();
    }

    /**
     * Get cloud authentication
     *
     * @return Authentication data
     */
    public String getCloudAuth() {
        return preferences.getString(PREF_CLOUD_AUTH, "");
    }

    /**
     * Set backup interval
     *
     * @param interval Interval in hours
     */
    public void setBackupInterval(int interval) {
        preferences.edit().putInt(PREF_BACKUP_INTERVAL, interval).apply();
    }

    /**
     * Get backup interval
     *
     * @return Interval in hours
     */
    public int getBackupInterval() {
        return preferences.getInt(PREF_BACKUP_INTERVAL, 24);
    }

    /**
     * Set last backup time
     *
     * @param timestamp Timestamp
     */
    public void setLastBackup(long timestamp) {
        preferences.edit().putLong(PREF_LAST_BACKUP, timestamp).apply();
    }

    /**
     * Get last backup time
     *
     * @return Timestamp
     */
    public long getLastBackup() {
        return preferences.getLong(PREF_LAST_BACKUP, 0);
    }

    /**
     * Check if backup is needed
     *
     * @return True if backup is needed
     */
    public boolean isBackupNeeded() {
        if (!isAutoBackupEnabled()) {
            return false;
        }
        
        long lastBackup = getLastBackup();
        long interval = getBackupInterval() * 60 * 60 * 1000; // hours to milliseconds
        
        return System.currentTimeMillis() - lastBackup > interval;
    }

    /**
     * Backup a project
     *
     * @param projectPath Project path
     * @return Backup file path if successful, null otherwise
     */
    public String backupProject(String projectPath) {
        try {
            File projectDir = new File(projectPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                Log.e(TAG, "Project directory does not exist");
                return null;
            }
            
            // Create a timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp = sdf.format(new Date());
            
            // Create backup directory
            String projectName = projectDir.getName();
            File projectBackupDir = new File(backupDir, projectName);
            projectBackupDir.mkdirs();
            
            // Create backup file
            String backupFileName = projectName + "_" + timestamp + ".zip";
            String outputPath = new File(projectBackupDir, backupFileName).getAbsolutePath();
            
            // Create the zip file
            String result = FileUtils.createZipFile(projectPath, outputPath);
            
            if (result != null) {
                // Sync to cloud if enabled
                if (isCloudSyncEnabled()) {
                    syncToCloud(outputPath);
                }
                
                // Update last backup time
                setLastBackup(System.currentTimeMillis());
                
                // Clean up old backups
                cleanupOldBackups(projectBackupDir);
                
                return result;
            } else {
                Log.e(TAG, "Failed to create backup");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error backing up project", e);
            return null;
        }
    }

    /**
     * Sync a backup to the cloud
     *
     * @param backupPath Backup file path
     * @return True if successful
     */
    private boolean syncToCloud(String backupPath) {
        // TODO: Implement cloud sync
        return false;
    }

    /**
     * Clean up old backups
     *
     * @param backupDir Backup directory
     */
    private void cleanupOldBackups(File backupDir) {
        File[] backups = backupDir.listFiles();
        if (backups == null || backups.length <= MAX_LOCAL_BACKUPS) {
            return;
        }
        
        // Sort by modification time (newest first)
        Arrays.sort(backups, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        // Delete oldest backups
        for (int i = MAX_LOCAL_BACKUPS; i < backups.length; i++) {
            if (!backups[i].delete()) {
                Log.e(TAG, "Failed to delete old backup: " + backups[i].getAbsolutePath());
            }
        }
    }

    /**
     * Get backup list for a project
     *
     * @param projectName Project name
     * @return List of backups
     */
    public List<String> getBackupList(String projectName) {
        File projectBackupDir = new File(backupDir, projectName);
        if (!projectBackupDir.exists() || !projectBackupDir.isDirectory()) {
            return Collections.emptyList();
        }
        
        File[] backups = projectBackupDir.listFiles();
        if (backups == null) {
            return Collections.emptyList();
        }
        
        // Sort by modification time (newest first)
        Arrays.sort(backups, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        List<String> backupList = new ArrayList<>();
        for (File backup : backups) {
            backupList.add(backup.getAbsolutePath());
        }
        
        return backupList;
    }

    /**
     * Restore a backup
     *
     * @param backupPath Backup file path
     * @param targetPath Target path
     * @return True if successful
     */
    public boolean restoreBackup(String backupPath, String targetPath) {
        // TODO: Implement restore
        return false;
    }
}