package com.example.instagramfollowerextractor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash screen activity shown at app startup
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000; // 2 seconds

    private ImageView ivLogo;
    private TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);

        // Load animations
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animations
        ivLogo.startAnimation(logoAnimation);
        tvAppName.startAnimation(textAnimation);

        // Navigate to the appropriate screen after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DURATION);
    }

    private void navigateNext() {
        // Check if the user has completed onboarding
        boolean hasCompletedOnboarding = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("has_completed_onboarding", false);

        // Check if user is logged in
        boolean isLoggedIn = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("is_logged_in", false);

        if (!hasCompletedOnboarding) {
            // Navigate to onboarding
            startActivity(new Intent(this, OnboardingActivity.class));
        } else if (!isLoggedIn) {
            // Navigate to login
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Navigate to dashboard
            startActivity(new Intent(this, DashboardActivity.class));
        }

        // Close splash activity
        finish();
    }
}