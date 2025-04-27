package com.example.instagramfollowerextractor;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Application class for initializing app-wide components
 */
public class InstagramAnalyzerApp extends Application {

    private static final String TAG = "InstagramAnalyzerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channels for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createNotificationChannels(this);
        }

        // Only start the notification service if user has opted in
        // or on first run, start it by default
        boolean notificationsEnabled = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("notifications_enabled", true);

        if (notificationsEnabled) {
            // Start the notification service after a slight delay
            // to ensure application is fully initialized
            new Handler(Looper.getMainLooper()).postDelayed(this::startNotificationService, 1000);
        }

        Log.d(TAG, "InstagramAnalyzerApp initialized");
    }

    /**
     * Starts the background notification service
     */
    private void startNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, NotificationService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Log.d(TAG, "Notification service started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting notification service: " + e.getMessage(), e);
        }
    }
}