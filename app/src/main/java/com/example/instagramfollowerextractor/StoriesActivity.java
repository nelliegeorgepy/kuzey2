package com.example.instagramfollowerextractor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * Activity to display all Instagram stories
 */
public class StoriesActivity extends BaseActivity {

    private static final String TAG = "StoriesActivity";

    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvNoStories;
    private RecyclerView rvStories;
    private BottomNavigationView bottomNavigationView;
    private View proOverlayView;
    private TextView tvProMessage;
    private ImageView ivProBadge;

    private ArrayList<Story> stories = new ArrayList<>();
    private boolean isPro;

    // Free limit for non-PRO users
    private static final int FREE_LIMIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        // Check if user has PRO subscription
        isPro = getIntent().getBooleanExtra("is_pro", false);
        if (!isPro) {
            // Double-check with shared preferences
            isPro = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                    .getBoolean("is_pro_user", false);
        }

        // Initialize views
        initViews();

        // Load stories data
        loadStoriesData();

        // Set up UI
        setupUI();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up back button
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvNoStories = findViewById(R.id.tvNoStories);
        rvStories = findViewById(R.id.rvStories);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        proOverlayView = findViewById(R.id.proOverlayView);
        tvProMessage = findViewById(R.id.tvProMessage);
        ivProBadge = findViewById(R.id.ivProBadge);
    }

    private void loadStoriesData() {
        try {
            // Get stories from DataManager
            stories = DataManager.getInstance(this).getStoriesList();
            Log.d(TAG, "Loaded " + stories.size() + " stories from DataManager");
        } catch (Exception e) {
            Log.e(TAG, "Error loading stories: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading stories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUI() {
        // Set title
        tvTitle.setText(R.string.stories);

        if (stories == null || stories.isEmpty()) {
            // No stories available
            tvNoStories.setVisibility(View.VISIBLE);
            rvStories.setVisibility(View.GONE);

            // Hide PRO related UI when no stories
            if (proOverlayView != null) proOverlayView.setVisibility(View.GONE);
            if (tvProMessage != null) tvProMessage.setVisibility(View.GONE);
            if (ivProBadge != null) ivProBadge.setVisibility(View.GONE);

            return;
        }

        // Stories available - show the grid
        tvNoStories.setVisibility(View.GONE);
        rvStories.setVisibility(View.VISIBLE);

        // Set up RecyclerView with grid layout
        rvStories.setLayoutManager(new GridLayoutManager(this, 2));

        // Create and set adapter with all stories
        StoriesGridAdapter adapter = new StoriesGridAdapter(this, stories, isPro);
        rvStories.setAdapter(adapter);

        // Show PRO overlay if needed
        if (!isPro && stories.size() > FREE_LIMIT) {
            if (ivProBadge != null) ivProBadge.setVisibility(View.VISIBLE);

            if (tvProMessage != null) {
                tvProMessage.setVisibility(View.VISIBLE);
                tvProMessage.setText(getString(R.string.pro_feature_limited_description, stories.size()));
                tvProMessage.setOnClickListener(v -> showProUpgradeDialog());
            }
        } else {
            if (ivProBadge != null) ivProBadge.setVisibility(View.GONE);
            if (tvProMessage != null) tvProMessage.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_stories);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Go back to dashboard
                finish();
                return true;
            } else if (itemId == R.id.nav_visitors) {
                // Show profile visitors (PRO feature)
                if (isPro) {
                    showStatisticsScreen(StatisticsActivity.TYPE_PROFILE_VISITORS);
                } else {
                    showProUpgradeDialog(
                            getString(R.string.pro_feature_title),
                            getString(R.string.pro_feature_description)
                    );
                }
                return true;
            } else if (itemId == R.id.nav_stories) {
                // Already on stories page
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Go to chat
                openChatScreen();
                return true;
            } else if (itemId == R.id.nav_tools) {
                // Go to tools
                openToolsScreen();
                return true;
            }

            return false;
        });
    }

    private void showStatisticsScreen(int type) {
        android.content.Intent intent = new android.content.Intent(this, StatisticsActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("is_pro", isPro);
        startActivity(intent);
    }

    private void openChatScreen() {
        android.content.Intent intent = new android.content.Intent(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    private void openToolsScreen() {
        android.content.Intent intent = new android.content.Intent(this, ToolsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showProUpgradeDialog() {
        // Show upgrade dialog using BaseActivity method
        showProUpgradeDialog(
                getString(R.string.pro_feature_title),
                getString(R.string.pro_feature_stories_description)
        );
    }
}