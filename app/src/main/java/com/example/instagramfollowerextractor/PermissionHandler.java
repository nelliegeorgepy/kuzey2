package com.example.instagramfollowerextractor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class to handle runtime permissions
 */
public class PermissionHandler {

    private static final String TAG = "PermissionHandler";

    // Request codes
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    public static final int REQUEST_STORAGE_PERMISSION = 1002;

    /**
     * Checks if notification permission is granted
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // For older versions, permission is not needed
    }

    /**
     * Requests notification permission (for Android 13+)
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Requesting notification permission for Android 13+");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    /**
     * Checks if storage permission is granted
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we can use the Storage Access Framework
            return true;
        } else {
            // For older versions, we need the WRITE_EXTERNAL_STORAGE permission
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Requests storage permission
     */
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * Checks if a permission should show rationale dialog
     */
    public static boolean shouldShowPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Helper method to handle permission results
     */
    public static boolean handlePermissionResult(int requestCode, @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}