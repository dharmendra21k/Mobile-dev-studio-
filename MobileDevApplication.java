package com.mobiledev.androidstudio;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.mobiledev.androidstudio.utils.PerformanceOptimizer;
import com.mobiledev.androidstudio.utils.PreRootManager;

/**
 * Main Application class for Mobile Developer Studio
 * Handles initialization of services and global configurations
 */
public class MobileDevApplication extends Application implements Configuration.Provider {
    private static final String TAG = "MobileDevApplication";
    private static final String PREFS_NAME = "MobileDevPrefs";
    
    private static MobileDevApplication instance;
    private SharedPreferences preferences;
    private PerformanceOptimizer performanceOptimizer;
    private PreRootManager preRootManager;
    private Handler mainHandler;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Application started");
        
        // Initialize main thread handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize shared preferences
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize performance optimizer
        performanceOptimizer = new PerformanceOptimizer(this);
        performanceOptimizer.optimize();
        
        // Initialize PRoot environment manager
        preRootManager = new PreRootManager(this);
        
        // First run checks
        if (isFirstRun()) {
            Log.d(TAG, "First run detected, performing initial setup");
            preRootManager.scheduleInitialSetup();
            setFirstRunComplete();
        }
    }
    
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build();
    }
    
    public static MobileDevApplication getInstance() {
        return instance;
    }
    
    public void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }
    
    public SharedPreferences getPreferences() {
        return preferences;
    }
    
    public PerformanceOptimizer getPerformanceOptimizer() {
        return performanceOptimizer;
    }
    
    public PreRootManager getPreRootManager() {
        return preRootManager;
    }
    
    private boolean isFirstRun() {
        return preferences.getBoolean("first_run", true);
    }
    
    private void setFirstRunComplete() {
        preferences.edit().putBoolean("first_run", false).apply();
    }
}