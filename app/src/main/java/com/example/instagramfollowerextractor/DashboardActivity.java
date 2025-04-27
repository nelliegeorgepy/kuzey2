package com.example.instagramfollowerextractor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    // UI Elements
    private ImageView ivProfilePic;
    private TextView tvUsername;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;
    private TextView tvStoriesTitle;
    private RecyclerView rvStories;
    private CardView cvProfileVisitors;
    private CardView cvBlockers;
    private CardView cvSecretAdmirers;
    private CardView cvNotFollowingBack;
    private CardView cvNotFollowing;
    private CardView cvLostFollowers;
    private CardView cvNewFollowers;
    private BottomNavigationView bottomNavigationView;

    // PRO badge images
    private ImageView ivProBadgeVisitors;
    private ImageView ivProBadgeBlockers;
    private ImageView ivProBadgeAdmirers;

    // Data
    private ProfileInfo profileInfo;
    private ArrayList<Story> stories;
    private ArrayList<Follower> followers;
    private ArrayList<Follower> following;
    private ArrayList<Follower> notFollowingBack;
    private ArrayList<Follower> notFollowing;
    private ArrayList<Follower> lostFollowers;
    private ArrayList<Follower> newFollowers;
    private ArrayList<Follower> profileVisitors;
    private ArrayList<Follower> blockers;
    private ArrayList<Follower> secretAdmirers;

    // PRO status
    private boolean isPro;

    // Adapters
    private StoryPreviewAdapter storyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Check PRO status
        isPro = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("is_pro_user", false);

        // Initialize UI components
        initViews();

        // Load data
        loadData();

        // Setup UI with data
        setupUI();

        // Setup card click listeners
        setupCardClickListeners();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        // Profile section
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUsername = findViewById(R.id.tvUsername);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        // Stories section
        tvStoriesTitle = findViewById(R.id.tvStoriesTitle);
        rvStories = findViewById(R.id.rvStories);

        // Statistics cards
        cvProfileVisitors = findViewById(R.id.cvProfileVisitors);
        cvBlockers = findViewById(R.id.cvBlockers);
        cvSecretAdmirers = findViewById(R.id.cvSecretAdmirers);
        cvNotFollowingBack = findViewById(R.id.cvNotFollowingBack);
        cvNotFollowing = findViewById(R.id.cvNotFollowing);
        cvLostFollowers = findViewById(R.id.cvLostFollowers);
        cvNewFollowers = findViewById(R.id.cvNewFollowers);

        // PRO badges
        ivProBadgeVisitors = findViewById(R.id.ivProBadgeVisitors);
        ivProBadgeBlockers = findViewById(R.id.ivProBadgeBlockers);
        ivProBadgeAdmirers = findViewById(R.id.ivProBadgeAdmirers);

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void loadData() {
        try {
            DataManager dataManager = DataManager.getInstance(this);

            // Get profile info from SharedPreferences
            profileInfo = new ProfileInfo();
            profileInfo.setUsername(getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE).getString("username", ""));
            profileInfo.setProfilePicURL(getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE).getString("profilePicURL", ""));
            profileInfo.setPostsCount(getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE).getInt("postsCount", 0));
            profileInfo.setFollowersCount(getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE).getInt("followersCount", 0));
            profileInfo.setFollowingCount(getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE).getInt("followingCount", 0));

            // Get main data
            stories = dataManager.getStoriesList();
            followers = dataManager.getFollowersList();
            following = dataManager.getFollowingList();
            notFollowingBack = dataManager.getNotFollowingBackList();
            notFollowing = dataManager.getNotFollowingList();
            lostFollowers = dataManager.getLostFollowersList();
            newFollowers = dataManager.getNewFollowersList();

            // Get PRO feature data - now always get the data, but access is controlled in UI
            profileVisitors = dataManager.getProfileVisitorsList();
            blockers = dataManager.getBlockersList();
            secretAdmirers = dataManager.getSecretAdmirersList();

            Log.d(TAG, "Data loaded: " + followers.size() + " followers, " + following.size() + " following, " + stories.size() + " stories");

            // Also log PRO features data for debugging
            Log.d(TAG, "PRO feature data loaded: " + profileVisitors.size() + " profile visitors, "
                    + blockers.size() + " blockers, " + secretAdmirers.size() + " secret admirers");
            Log.d(TAG, "User PRO status: " + (isPro ? "PRO" : "Free"));
        } catch (Exception e) {
            Log.e(TAG, "Error loading data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUI() {
        // Setup profile section
        if (profileInfo != null) {
            tvUsername.setText("@" + profileInfo.getUsername());
            tvFollowersCount.setText(String.valueOf(profileInfo.getFollowersCount()));
            tvFollowingCount.setText(String.valueOf(profileInfo.getFollowingCount()));

            if (profileInfo.getProfilePicURL() != null && !profileInfo.getProfilePicURL().isEmpty()) {
                Picasso.get()
                        .load(profileInfo.getProfilePicURL())
                        .placeholder(R.drawable.placeholder_profile)
                        .error(R.drawable.placeholder_profile)
                        .into(ivProfilePic);
            }
        }

        // Setup stories recycler view
        setupStoriesRecyclerView();

        // Update statistics card counts
        updateStatisticCardCounts();

        // Setup PRO badges visibility
        setupProBadges();

        // Setup PRO button click
        CardView cvPro = findViewById(R.id.cvPro);
        cvPro.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SubscriptionActivity.class);
            startActivity(intent);
        });
    }

    private void setupProBadges() {
        // Show PRO badges for free users, hide for PRO users
        int badgeVisibility = isPro ? View.GONE : View.VISIBLE;

        if (ivProBadgeVisitors != null) {
            ivProBadgeVisitors.setVisibility(badgeVisibility);
        }

        if (ivProBadgeBlockers != null) {
            ivProBadgeBlockers.setVisibility(badgeVisibility);
        }

        if (ivProBadgeAdmirers != null) {
            ivProBadgeAdmirers.setVisibility(badgeVisibility);
        }

        // Remove overlay if it exists

    }

    private void setupStoriesRecyclerView() {
        if (stories != null && !stories.isEmpty()) {
            rvStories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            storyAdapter = new StoryPreviewAdapter(this, stories);
            rvStories.setAdapter(storyAdapter);

            tvStoriesTitle.setText(getString(R.string.stories, stories.size()));
            tvStoriesTitle.setVisibility(View.VISIBLE);
            rvStories.setVisibility(View.VISIBLE);
        } else {
            tvStoriesTitle.setText(R.string.no_stories_available);
            rvStories.setVisibility(View.GONE);
        }
    }

    private void updateStatisticCardCounts() {
        // Update counts on all card views
        updateCardCount(cvProfileVisitors, R.id.tvProfileVisitorsCount, profileVisitors.size());
        updateCardCount(cvProfileVisitors, R.id.tvProfileVisitedCount, profileVisitors.size());

        updateCardCount(cvBlockers, R.id.tvBlockersCount, blockers.size());
        updateCardCount(cvBlockers, R.id.tvBlockedCount, blockers.size());

        updateCardCount(cvSecretAdmirers, R.id.tvSecretAdmirersCount, secretAdmirers.size());
        updateCardCount(cvSecretAdmirers, R.id.tvSecretAdmirerCount, secretAdmirers.size());

        updateCardCount(cvNotFollowingBack, R.id.tvNotFollowingBackCount, notFollowingBack.size());
        updateCardCount(cvNotFollowing, R.id.tvNotFollowingCount, notFollowing.size());
        updateCardCount(cvLostFollowers, R.id.tvLostFollowersCount, lostFollowers.size());
        updateCardCount(cvNewFollowers, R.id.tvNewFollowersCount, newFollowers.size());

        // Update last updated timestamp
        long lastUpdated = DataManager.getInstance(this).getLastUpdatedTimestamp();
        if (lastUpdated > 0) {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(lastUpdated));
            TextView tvLastUpdated = findViewById(R.id.tvLastUpdated);
            tvLastUpdated.setText(getString(R.string.last_updated, formattedDate));
            tvLastUpdated.setVisibility(View.VISIBLE);
        }
    }

    private void updateCardCount(CardView cardView, int textViewId, int count) {
        TextView textView = cardView.findViewById(textViewId);
        if (textView != null) {
            textView.setText(String.valueOf(count));
        }
    }

    private void setupCardClickListeners() {
        // Set listeners for all statistics cards

        // PRO features cards - show counts for all users, but limit details for non-PRO
        cvProfileVisitors.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_PROFILE_VISITORS);
            intent.putExtra("is_pro", isPro); // Pass PRO status to control access
            startActivity(intent);
        });

        cvBlockers.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_BLOCKERS);
            intent.putExtra("is_pro", isPro); // Pass PRO status to control access
            startActivity(intent);
        });

        cvSecretAdmirers.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_SECRET_ADMIRERS);
            intent.putExtra("is_pro", isPro); // Pass PRO status to control access
            startActivity(intent);
        });

        // Standard features with limited free access
        cvNotFollowingBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_NOT_FOLLOWING_BACK);
            startActivity(intent);
        });

        cvNotFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_NOT_FOLLOWING);
            startActivity(intent);
        });

        cvLostFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_LOST_FOLLOWERS);
            startActivity(intent);
        });

        cvNewFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("type", StatisticsActivity.TYPE_NEW_FOLLOWERS);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_visitors) {
                Intent intent = new Intent(this, StatisticsActivity.class);
                intent.putExtra("type", StatisticsActivity.TYPE_PROFILE_VISITORS);
                intent.putExtra("is_pro", isPro); // Pass PRO status to control access
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_stories) {
                Intent intent = new Intent(this, StoriesActivity.class);
                intent.putExtra("is_pro", isPro); // Pass PRO status to control access
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_tools) {
                Intent intent = new Intent(this, ToolsActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    // Refresh data when returning to the dashboard
    @Override
    protected void onResume() {
        super.onResume();
        // Check if PRO status might have changed
        isPro = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("is_pro_user", false);

        loadData();
        setupUI();
    }
}