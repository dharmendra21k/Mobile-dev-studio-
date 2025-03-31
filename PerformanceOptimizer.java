package com.mobiledev.androidstudio.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;

/**
 * Utility class to optimize app performance based on device capabilities
 * and available resources.
 */
public class PerformanceOptimizer {
    private static final String TAG = "PerformanceOptimizer";
    private static final String PERF_PREFS = "performance_settings";
    
    private final Context context;
    private final SharedPreferences preferences;
    
    // Performance profile constants
    public static final int PROFILE_LOW = 0;
    public static final int PROFILE_MEDIUM = 1;
    public static final int PROFILE_HIGH = 2;
    
    public PerformanceOptimizer(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PERF_PREFS, Context.MODE_PRIVATE);
    }
    
    /**
     * Main optimization method that analyzes device capabilities and
     * sets appropriate performance settings
     */
    public void optimize() {
        Log.d(TAG, "Running performance optimization");
        
        // Analyze device capabilities
        int availableMemoryMb = getAvailableMemoryMb();
        long availableStorageMb = getAvailableStorageMb();
        int numProcessors = Runtime.getRuntime().availableProcessors();
        
        Log.d(TAG, String.format("Device specs: RAM: %d MB, Storage: %d MB, CPUs: %d",
                availableMemoryMb, availableStorageMb, numProcessors));
        
        // Determine performance profile based on device capabilities
        int performanceProfile = determinePerformanceProfile(availableMemoryMb, availableStorageMb, numProcessors);
        
        // Save performance profile
        savePerformanceProfile(performanceProfile);
        
        // Apply optimizations based on profile
        applyOptimizations(performanceProfile);
    }
    
    /**
     * Determines the appropriate performance profile based on device specs
     */
    private int determinePerformanceProfile(int memoryMb, long storageMb, int cpuCores) {
        // Low-end device
        if (memoryMb < 1024 || storageMb < 1024 || cpuCores <= 2) {
            return PROFILE_LOW;
        }
        
        // High-end device
        if (memoryMb >= 3072 && storageMb >= 5120 && cpuCores >= 6) {
            return PROFILE_HIGH;
        }
        
        // Medium-range device (default)
        return PROFILE_MEDIUM;
    }
    
    /**
     * Applies optimizations based on the determined performance profile
     */
    private void applyOptimizations(int profile) {
        switch (profile) {
            case PROFILE_LOW:
                Log.d(TAG, "Applying low-end device optimizations");
                // Minimal background work, reduced features
                preferences.edit()
                    .putBoolean("enable_background_compilation", false)
                    .putBoolean("enable_code_analysis", false)
                    .putBoolean("enable_syntax_highlighting", true)
                    .putBoolean("enable_auto_complete", false)
                    .putInt("max_terminal_buffer", 5000)
                    .putInt("max_editor_tabs", 3)
                    .apply();
                break;
                
            case PROFILE_MEDIUM:
                Log.d(TAG, "Applying medium-range device optimizations");
                // Balanced settings
                preferences.edit()
                    .putBoolean("enable_background_compilation", true)
                    .putBoolean("enable_code_analysis", true)
                    .putBoolean("enable_syntax_highlighting", true)
                    .putBoolean("enable_auto_complete", true)
                    .putInt("max_terminal_buffer", 10000)
                    .putInt("max_editor_tabs", 5)
                    .apply();
                break;
                
            case PROFILE_HIGH:
                Log.d(TAG, "Applying high-end device optimizations");
                // Full feature set
                preferences.edit()
                    .putBoolean("enable_background_compilation", true)
                    .putBoolean("enable_code_analysis", true)
                    .putBoolean("enable_syntax_highlighting", true)
                    .putBoolean("enable_auto_complete", true)
                    .putInt("max_terminal_buffer", 20000)
                    .putInt("max_editor_tabs", 10)
                    .apply();
                break;
        }
    }
    
    /**
     * Gets the current performance profile
     */
    public int getPerformanceProfile() {
        return preferences.getInt("performance_profile", PROFILE_MEDIUM);
    }
    
    /**
     * Saves the determined performance profile
     */
    private void savePerformanceProfile(int profile) {
        preferences.edit().putInt("performance_profile", profile).apply();
    }
    
    /**
     * Calculates available system memory in MB
     */
    private int getAvailableMemoryMb() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        
        return (int) (memoryInfo.availMem / (1024 * 1024));
    }
    
    /**
     * Calculates available storage in MB
     */
    private long getAvailableStorageMb() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        
        return (availableBlocks * blockSize) / (1024 * 1024);
    }
}