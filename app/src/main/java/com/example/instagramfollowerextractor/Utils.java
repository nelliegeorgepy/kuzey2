package com.example.instagramfollowerextractor;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for common functions used throughout the app
 */
public class Utils {

    private static final String TAG = "Utils";

    /**
     * Checks if network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return connectivityManager.getActiveNetwork() != null;
                } else {
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Sets the app's locale
     */
    public static void setLocale(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("InstagramAnalyzerPrefs",
                    Context.MODE_PRIVATE);
            String languageCode = prefs.getString("app_language", "en");

            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);

            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(locale);
            } else {
                configuration.locale = locale;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(configuration);
            } else {
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            }

            Log.d(TAG, "App locale set to: " + languageCode);
        } catch (Exception e) {
            Log.e(TAG, "Error setting locale: " + e.getMessage());
        }
    }

    /**
     * Returns a formatted date string
     */
    public static String formatDate(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return "";
        }
    }

    /**
     * Returns a relative time span string (e.g., "5 minutes ago")
     */
    public static String getRelativeTimeSpan(Context context, long timestamp) {
        try {
            return DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();
        } catch (Exception e) {
            Log.e(TAG, "Error getting relative time span: " + e.getMessage());
            return formatDate(timestamp);
        }
    }

    /**
     * Applies a click animation to a view
     */
    public static void applyClickAnimation(View view) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.7f);
        animation.setDuration(100);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);

        view.startAnimation(animation);
    }

    /**
     * Saves bitmap to a file
     */
    public static File saveBitmapToFile(Context context, android.graphics.Bitmap bitmap, String filename) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(context.getExternalFilesDir(null), "InstagramAnalyzer");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file
            File file = new File(directory, filename);
            FileOutputStream fos = new FileOutputStream(file);

            // Save bitmap to file
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the device's display metrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * Converts DP to pixels
     */
    public static int dpToPx(Context context, float dp) {
        return (int) (dp * getDisplayMetrics(context).density);
    }

    /**
     * Reads raw resource file as string
     */
    public static String readRawResourceAsString(Context context, int resourceId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer);
        } catch (Exception e) {
            Log.e(TAG, "Error reading raw resource: " + e.getMessage());
            return "";
        }
    }
}