package com.example.instagramfollowerextractor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Helper class for creating and showing notifications
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    // Notification channel IDs
    private static final String CHANNEL_ID_SERVICE = "instagram_analyzer_service";
    private static final String CHANNEL_ID_UPDATES = "instagram_analyzer_updates";

    // Notification IDs
    private static final int NOTIFICATION_ID_SERVICE = 1;
    private static final int NOTIFICATION_ID_DEFAULT = 1000;
    private static final int NOTIFICATION_ID_NOT_FOLLOWING_BACK = 1001;
    private static final int NOTIFICATION_ID_PROFILE_VISITORS = 1002;
    private static final int NOTIFICATION_ID_LOST_FOLLOWER = 1003;
    private static final int NOTIFICATION_ID_NEW_FOLLOWERS = 1004;
    private static final int NOTIFICATION_ID_BLOCKER = 1005;

    /**
     * Creates notification channels for Android 8.0 (Oreo) and higher
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager not available");
                return;
            }

            try {
                // Create service channel
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID_SERVICE,
                        context.getString(R.string.notification_channel_service),
                        NotificationManager.IMPORTANCE_LOW
                );
                serviceChannel.setDescription(context.getString(R.string.notification_channel_service_description));
                serviceChannel.setShowBadge(false);
                serviceChannel.enableLights(false);
                serviceChannel.enableVibration(false);
                serviceChannel.setSound(null, null);

                // Create updates channel
                NotificationChannel updatesChannel = new NotificationChannel(
                        CHANNEL_ID_UPDATES,
                        context.getString(R.string.notification_channel_updates),
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                updatesChannel.setDescription(context.getString(R.string.notification_channel_updates_description));
                updatesChannel.setShowBadge(true);

                // Register channels
                notificationManager.createNotificationChannel(serviceChannel);
                notificationManager.createNotificationChannel(updatesChannel);

                Log.d(TAG, "Notification channels created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channels: " + e.getMessage(), e);
            }
        } else {
            Log.d(TAG, "Notification channels not needed for this Android version");
        }
    }

    /**
     * Creates a silent notification for the foreground service
     */
    public static Notification createSilentNotification(Context context) {
        // Intent to open Dashboard when notification is tapped
        Intent notificationIntent = new Intent(context, DashboardActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, flags);

        // Create the notification
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, CHANNEL_ID_SERVICE);
        } else {
            builder = new NotificationCompat.Builder(context)
                    .setPriority(NotificationCompat.PRIORITY_LOW);
        }

        return builder
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_service_running))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    /**
     * Shows the default notification
     */
    public static void showDefaultNotification(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_default_title))
                .setContentText(context.getString(R.string.notification_default_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(context, NOTIFICATION_ID_DEFAULT, builder.build());
    }

    /**
     * Shows a notification about a user not following back
     */
    public static void showNotFollowingBackNotification(Context context, Follower user) {
        Intent intent = new Intent(context, StatisticsActivity.class);
        intent.putExtra("type", StatisticsActivity.TYPE_NOT_FOLLOWING_BACK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap userIcon = loadUserIcon(context, user);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_not_following_back_title))
                .setContentText(context.getString(R.string.notification_not_following_back_message, user.getUsername()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (userIcon != null) {
            builder.setLargeIcon(userIcon);
        }

        showNotification(context, NOTIFICATION_ID_NOT_FOLLOWING_BACK, builder.build());
    }

    /**
     * Shows a notification about profile visitors
     */
    public static void showProfileVisitorsNotification(Context context, int visitorCount) {
        Intent intent = new Intent(context, StatisticsActivity.class);
        intent.putExtra("type", StatisticsActivity.TYPE_PROFILE_VISITORS);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_profile_visitors_title))
                .setContentText(context.getString(R.string.notification_profile_visitors_message, visitorCount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(context, NOTIFICATION_ID_PROFILE_VISITORS, builder.build());
    }

    /**
     * Shows a notification about a lost follower
     */
    public static void showLostFollowerNotification(Context context, Follower user) {
        Intent intent = new Intent(context, StatisticsActivity.class);
        intent.putExtra("type", StatisticsActivity.TYPE_LOST_FOLLOWERS);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap userIcon = loadUserIcon(context, user);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_lost_follower_title))
                .setContentText(context.getString(R.string.notification_lost_follower_message, user.getUsername()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (userIcon != null) {
            builder.setLargeIcon(userIcon);
        }

        showNotification(context, NOTIFICATION_ID_LOST_FOLLOWER, builder.build());
    }

    /**
     * Shows a notification about new followers
     */
    public static void showNewFollowersNotification(Context context, int followerCount) {
        Intent intent = new Intent(context, StatisticsActivity.class);
        intent.putExtra("type", StatisticsActivity.TYPE_NEW_FOLLOWERS);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_new_followers_title))
                .setContentText(context.getString(R.string.notification_new_followers_message, followerCount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(context, NOTIFICATION_ID_NEW_FOLLOWERS, builder.build());
    }

    /**
     * Shows a notification about a user who blocked you
     */
    public static void showBlockerNotification(Context context, Follower user) {
        Intent intent = new Intent(context, StatisticsActivity.class);
        intent.putExtra("type", StatisticsActivity.TYPE_BLOCKERS);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap userIcon = loadUserIcon(context, user);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_blocker_title))
                .setContentText(context.getString(R.string.notification_blocker_message, user.getUsername()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (userIcon != null) {
            builder.setLargeIcon(userIcon);
        }

        showNotification(context, NOTIFICATION_ID_BLOCKER, builder.build());
    }

    /**
     * Helper method to show a notification
     */
    private static void showNotification(Context context, int notificationId, Notification notification) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // Check notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(notificationId, notification);
                } else {
                    Log.d(TAG, "Notifications are disabled by the user");
                }
            } else {
                notificationManager.notify(notificationId, notification);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for showing notification: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
        }
    }

    /**
     * Helper method to load a user's profile image for notifications
     */
    private static Bitmap loadUserIcon(Context context, Follower user) {
        try {
            if (user.getProfilePicURL() != null && !user.getProfilePicURL().isEmpty()) {
                return Picasso.get()
                        .load(user.getProfilePicURL())
                        .get();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage());
        }

        // Return default icon if loading failed
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder_profile);
    }
}