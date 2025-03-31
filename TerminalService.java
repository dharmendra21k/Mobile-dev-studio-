package com.mobiledev.androidstudio.terminal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Foreground service for keeping terminal sessions running in the background
 */
public class TerminalService extends Service {
    private static final String TAG = "TerminalService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "terminal_service_channel";
    
    private final IBinder binder = new TerminalBinder();
    private Map<String, Process> backgroundProcesses;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Terminal service created");
        
        backgroundProcesses = new HashMap<>();
        
        // Create notification channel for Android O and above
        createNotificationChannel();
        
        // Start service in foreground
        startForeground(NOTIFICATION_ID, createNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Terminal service started");
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "Terminal service destroyed");
        
        // Kill all background processes
        for (Process process : backgroundProcesses.values()) {
            if (process.isAlive()) {
                process.destroy();
            }
        }
        
        backgroundProcesses.clear();
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    /**
     * Register a terminal process to keep running in the background
     * @param sessionId The ID of the terminal session
     * @param process The process to keep running
     */
    public void registerBackgroundProcess(String sessionId, Process process) {
        backgroundProcesses.put(sessionId, process);
        
        // Update notification to show number of background processes
        updateNotification();
    }
    
    /**
     * Unregister a terminal process
     * @param sessionId The ID of the terminal session
     */
    public void unregisterBackgroundProcess(String sessionId) {
        Process process = backgroundProcesses.remove(sessionId);
        
        // Update notification
        updateNotification();
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Terminal Service",
                    NotificationManager.IMPORTANCE_LOW);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Create notification for foreground service
     */
    private Notification createNotification() {
        // Intent to open terminal activity when notification is clicked
        Intent notificationIntent = new Intent(this, MultiTabTerminalActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Terminal Service")
                .setContentText("Running terminal sessions: " + backgroundProcesses.size())
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        
        return builder.build();
    }
    
    /**
     * Update notification with current status
     */
    private void updateNotification() {
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }
    
    /**
     * Binder class for terminal service
     */
    public class TerminalBinder extends Binder {
        public TerminalService getService() {
            return TerminalService.this;
        }
    }
}