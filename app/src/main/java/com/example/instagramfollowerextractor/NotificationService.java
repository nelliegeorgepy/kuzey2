package com.example.instagramfollowerextractor;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

/**
 * Background service for sending periodic notifications
 */
public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final long NOTIFICATION_INTERVAL = 30 * 60 * 1000; // 30 minutes

    private Handler handler;
    private Random random;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification service created");

        handler = new Handler(Looper.getMainLooper());
        random = new Random();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Notification service started");

        // IMPORTANT: Call startForeground immediately to prevent ANR
        // Create a notification and start as foreground service
        startForeground(1, createForegroundNotification());

        // Schedule the first notification
        handler.postDelayed(notificationRunnable, NOTIFICATION_INTERVAL);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Notification service destroyed");

        // Remove callbacks to avoid memory leaks
        handler.removeCallbacks(notificationRunnable);

        super.onDestroy();
    }

    private Notification createForegroundNotification() {
        // Create a silent notification for the foreground service
        return NotificationHelper.createSilentNotification(this);
    }

    private final Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            // Send a random notification
            sendRandomNotification();

            // Schedule the next notification
            handler.postDelayed(this, NOTIFICATION_INTERVAL);
        }
    };

    private void sendRandomNotification() {
        try {
            DataManager dataManager = DataManager.getInstance(getApplicationContext());

            // Get the data needed for notifications
            ArrayList<Follower> notFollowingBack = dataManager.getNotFollowingBackList();
            ArrayList<Follower> profileVisitors = dataManager.getProfileVisitorsList();
            ArrayList<Follower> lostFollowers = dataManager.getLostFollowersList();
            ArrayList<Follower> newFollowers = dataManager.getNewFollowersList();
            ArrayList<Follower> blockers = dataManager.getBlockersList();

            // Create a list of possible notification types
            ArrayList<Integer> notificationTypes = new ArrayList<>();

            // Add notification types based on available data
            if (!notFollowingBack.isEmpty()) notificationTypes.add(1);
            if (!profileVisitors.isEmpty()) notificationTypes.add(2);
            if (!lostFollowers.isEmpty()) notificationTypes.add(3);
            if (!newFollowers.isEmpty()) notificationTypes.add(4);
            if (!blockers.isEmpty()) notificationTypes.add(5);

            // If no data available, add default notification type
            if (notificationTypes.isEmpty()) notificationTypes.add(0);

            // Choose a random notification type
            int notificationType = notificationTypes.get(random.nextInt(notificationTypes.size()));

            // Send the notification
            switch (notificationType) {
                case 0:
                    // Default notification
                    NotificationHelper.showDefaultNotification(getApplicationContext());
                    break;
                case 1:
                    // Not following back notification
                    if (!notFollowingBack.isEmpty()) {
                        Follower randomUser = notFollowingBack.get(random.nextInt(notFollowingBack.size()));
                        NotificationHelper.showNotFollowingBackNotification(
                                getApplicationContext(), randomUser);
                    }
                    break;
                case 2:
                    // Profile visitors notification
                    if (!profileVisitors.isEmpty()) {
                        int visitorCount = Math.min(profileVisitors.size(), 3 + random.nextInt(3));
                        NotificationHelper.showProfileVisitorsNotification(
                                getApplicationContext(), visitorCount);
                    }
                    break;
                case 3:
                    // Lost followers notification
                    if (!lostFollowers.isEmpty()) {
                        Follower randomUser = lostFollowers.get(random.nextInt(lostFollowers.size()));
                        NotificationHelper.showLostFollowerNotification(
                                getApplicationContext(), randomUser);
                    }
                    break;
                case 4:
                    // New followers notification
                    if (!newFollowers.isEmpty()) {
                        int newFollowerCount = Math.min(newFollowers.size(), 2 + random.nextInt(3));
                        NotificationHelper.showNewFollowersNotification(
                                getApplicationContext(), newFollowerCount);
                    }
                    break;
                case 5:
                    // Blockers notification
                    if (!blockers.isEmpty()) {
                        Follower randomUser = blockers.get(random.nextInt(blockers.size()));
                        NotificationHelper.showBlockerNotification(
                                getApplicationContext(), randomUser);
                    }
                    break;
            }

            Log.d(TAG, "Sent notification of type: " + notificationType);

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
        }
    }
}