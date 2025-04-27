package com.example.instagramfollowerextractor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity for tools like profile photo downloader, media downloader, etc.
 */
public class ToolsActivity extends BaseActivity {

    private static final String TAG = "ToolsActivity";

    private ImageView ivBack;
    private TextView tvTitle;
    private CardView cvProfilePhotoDownloader;
    private CardView cvMediaDownloader;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        cvProfilePhotoDownloader = findViewById(R.id.cvProfilePhotoDownloader);
        cvMediaDownloader = findViewById(R.id.cvMediaDownloader);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set title
        tvTitle.setText(R.string.tools);
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Profile Photo Downloader card
        cvProfilePhotoDownloader.setOnClickListener(v -> {
            // Show a message that this feature is coming soon
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();
        });

        // Media Downloader card
        cvMediaDownloader.setOnClickListener(v -> {
            // Show a message that this feature is coming soon
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_tools);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Go to dashboard
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_visitors) {
                // Go to profile visitors stats
                Intent intent = new Intent(this, StatisticsActivity.class);
                intent.putExtra("type", StatisticsActivity.TYPE_PROFILE_VISITORS);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_stories) {
                // Go to stories
                Intent intent = new Intent(this, StoriesActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Go to chat
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_tools) {
                // Already on tools page
                return true;
            }

            return false;
        });
    }
}