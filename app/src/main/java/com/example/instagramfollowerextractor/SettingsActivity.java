package com.example.instagramfollowerextractor;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

/**
 * Activity for app settings
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private ImageView ivBack;
    private TextView tvTitle;

    private CardView cvLanguage;
    private CardView cvNotifications;
    private CardView cvPrivacyPolicy;
    private CardView cvTermsOfService;
    private CardView cvSupport;
    private CardView cvAbout;
    private CardView cvLogout;

    private Switch switchNotifications;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get shared preferences
        prefs = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE);

        // Initialize UI
        initViews();

        // Set up UI state
        setupUI();

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);

        cvLanguage = findViewById(R.id.cvLanguage);
        cvNotifications = findViewById(R.id.cvNotifications);
        cvPrivacyPolicy = findViewById(R.id.cvPrivacyPolicy);
        cvTermsOfService = findViewById(R.id.cvTermsOfService);
        cvSupport = findViewById(R.id.cvSupport);
        cvAbout = findViewById(R.id.cvAbout);
        cvLogout = findViewById(R.id.cvLogout);

        switchNotifications = findViewById(R.id.switchNotifications);

        // Set title
        tvTitle.setText(R.string.settings);
    }

    private void setupUI() {
        // Set notification switch state
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Language settings
        cvLanguage.setOnClickListener(v -> showLanguageDialog());

        // Notification switch
        switchNotifications.setOnCheckedChangeListener(this::onNotificationSwitchChanged);

        // Privacy Policy
        cvPrivacyPolicy.setOnClickListener(v -> openWebPage(getString(R.string.privacy_policy_url)));

        // Terms of Service
        cvTermsOfService.setOnClickListener(v -> openWebPage(getString(R.string.terms_of_service_url)));

        // Support
        cvSupport.setOnClickListener(v -> openSupportEmail());

        // About
        cvAbout.setOnClickListener(v -> showAboutDialog());

        // Logout
        cvLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void onNotificationSwitchChanged(CompoundButton buttonView, boolean isChecked) {
        // Save notification preference
        prefs.edit().putBoolean("notifications_enabled", isChecked).apply();

        // Show feedback toast
        if (isChecked) {
            Toast.makeText(this, R.string.notifications_enabled, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.notifications_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLanguageDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_language);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        // Get language buttons
        Button btnEnglish = dialog.findViewById(R.id.btnEnglish);
        Button btnTurkish = dialog.findViewById(R.id.btnTurkish);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Current language
        String currentLanguage = prefs.getString("app_language", "en");

        // Highlight current language button
        if ("en".equals(currentLanguage)) {
            btnEnglish.setBackgroundResource(R.drawable.button_primary);
        } else if ("tr".equals(currentLanguage)) {
            btnTurkish.setBackgroundResource(R.drawable.button_secondary);
        }

        // Set click listeners
        btnEnglish.setOnClickListener(v -> {
            setAppLanguage("en");
            dialog.dismiss();
        });

        btnTurkish.setOnClickListener(v -> {
            setAppLanguage("tr");
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setAppLanguage(String languageCode) {
        // Save language preference
        prefs.edit().putString("app_language", languageCode).apply();

        // Show restart dialog
        showInfoDialog(
                getString(R.string.language_changed),
                getString(R.string.app_restart_required),
                getString(R.string.restart_now),
                v -> restartApp()
        );
    }

    private void restartApp() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void openWebPage(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.browser_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void openSupportEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + getString(R.string.support_email)));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.email_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextView tvVersion = dialog.findViewById(R.id.tvVersion);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        // Set app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText(getString(R.string.version_format, versionName));
        } catch (Exception e) {
            tvVersion.setText(getString(R.string.version_unknown));
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showLogoutConfirmation() {
        showConfirmDialog(
                getString(R.string.logout_confirmation_title),
                getString(R.string.logout_confirmation_message),
                v -> logout(),
                null
        );
    }

    private void logout() {
        // Clear login state
        prefs.edit().putBoolean("is_logged_in", false).apply();

        // Show toast
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}