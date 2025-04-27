package com.example.instagramfollowerextractor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Activity to display different statistics lists (followers, following, etc.)
 */
public class StatisticsActivity extends BaseActivity {

    private static final String TAG = "StatisticsActivity";

    // UI Elements
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvNoData;
    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private TextView tvProMessage;
    private View proOverlayView;
    private TextView tvStatName;
    private TextView tvStatCount;

    // Type constants for different statistics lists
    public static final int TYPE_PROFILE_VISITORS = 1;
    public static final int TYPE_BLOCKERS = 2;
    public static final int TYPE_SECRET_ADMIRERS = 3;
    public static final int TYPE_NOT_FOLLOWING_BACK = 4;
    public static final int TYPE_NOT_FOLLOWING = 5;
    public static final int TYPE_LOST_FOLLOWERS = 6;
    public static final int TYPE_NEW_FOLLOWERS = 7;

    // Data
    private ArrayList<Follower> usersList = new ArrayList<>();

    // Pro status
    private boolean isPro;

    // Free preview limit for non-PRO users
    private static final int FREE_PREVIEW_LIMIT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Get the statistics type from intent
        int type = getIntent().getIntExtra("type", TYPE_NOT_FOLLOWING_BACK);

        // Check PRO status - first from intent, then from SharedPreferences as backup
        isPro = getIntent().getBooleanExtra("is_pro", false);
        if (!isPro) {
            // Double-check with shared preferences
            isPro = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                    .getBoolean("is_pro_user", false);
        }

        // Initialize views
        initViews();

        // For PRO features, directly go to subscription for non-PRO users
        boolean isProFeature = (type == TYPE_PROFILE_VISITORS || type == TYPE_BLOCKERS || type == TYPE_SECRET_ADMIRERS);
        if (!isPro && isProFeature) {
            // Doğrudan subscription activity'ye yönlendir
            startActivity(new Intent(this, SubscriptionActivity.class));
            finish();
            return;
        }

        // Load appropriate data based on type
        loadData(type);

        // Setup UI with loaded data
        setupUI(type);
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
       // tvNoData = findViewById(R.id.tvNoData);
        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        tvProMessage = findViewById(R.id.tvProMessage);
        proOverlayView = findViewById(R.id.proOverlayView);
        tvStatName = findViewById(R.id.tvStatName);
        tvStatCount = findViewById(R.id.tvStatCount);

        // Set back button click listener
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadData(int type) {
        try {
            progressBar.setVisibility(View.VISIBLE);

            DataManager dataManager = DataManager.getInstance(this);

            switch (type) {
                case TYPE_PROFILE_VISITORS:
                    usersList = dataManager.getProfileVisitorsList();
                    Log.d(TAG, "Loaded " + usersList.size() + " profile visitors");
                    break;

                case TYPE_BLOCKERS:
                    usersList = dataManager.getBlockersList();
                    Log.d(TAG, "Loaded " + usersList.size() + " blockers");
                    break;

                case TYPE_SECRET_ADMIRERS:
                    usersList = dataManager.getSecretAdmirersList();
                    Log.d(TAG, "Loaded " + usersList.size() + " secret admirers");
                    break;

                case TYPE_NOT_FOLLOWING_BACK:
                    usersList = dataManager.getNotFollowingBackList();
                    Log.d(TAG, "Loaded " + usersList.size() + " users not following back");
                    break;

                case TYPE_NOT_FOLLOWING:
                    usersList = dataManager.getNotFollowingList();
                    Log.d(TAG, "Loaded " + usersList.size() + " users you don't follow");
                    break;

                case TYPE_LOST_FOLLOWERS:
                    usersList = dataManager.getLostFollowersList();
                    Log.d(TAG, "Loaded " + usersList.size() + " lost followers");
                    break;

                case TYPE_NEW_FOLLOWERS:
                    usersList = dataManager.getNewFollowersList();
                    Log.d(TAG, "Loaded " + usersList.size() + " new followers");
                    break;

                default:
                    Log.e(TAG, "Unknown statistics type: " + type);
                    usersList = new ArrayList<>();
                    break;
            }

            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error loading data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            usersList = new ArrayList<>();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupUI(int type) {
        // Set title and statistic name based on type
        switch (type) {
            case TYPE_PROFILE_VISITORS:
                tvTitle.setText(R.string.profile_visitors);
                tvStatName.setText(R.string.profile_visitors);
                break;
            case TYPE_BLOCKERS:
                tvTitle.setText(R.string.blockers);
                tvStatName.setText(R.string.blockers);
                break;
            case TYPE_SECRET_ADMIRERS:
                tvTitle.setText(R.string.secret_admirers);
                tvStatName.setText(R.string.secret_admirers);
                break;
            case TYPE_NOT_FOLLOWING_BACK:
                tvTitle.setText(R.string.not_following_back);
                tvStatName.setText(R.string.not_following_back);
                break;
            case TYPE_NOT_FOLLOWING:
                tvTitle.setText(R.string.not_following);
                tvStatName.setText(R.string.not_following);
                break;
            case TYPE_LOST_FOLLOWERS:
                tvTitle.setText(R.string.lost_followers);
                tvStatName.setText(R.string.lost_followers);
                break;
            case TYPE_NEW_FOLLOWERS:
                tvTitle.setText(R.string.new_followers);
                tvStatName.setText(R.string.new_followers);
                break;
        }

        // Set count
        if (usersList != null) {
            tvStatCount.setText(String.valueOf(usersList.size()));
        } else {
            tvStatCount.setText("0");
        }

        if (usersList == null || usersList.isEmpty()) {
            // No data available
           // tvNoData.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
            tvProMessage.setVisibility(View.GONE);
            proOverlayView.setVisibility(View.GONE);
            return;
        }

        // For all features, limit to 3 items for non-PRO users
        if (!isPro) {
            setupLimitedPreview(type);
        } else {
            // Show complete data for PRO users
            setupFullList();
        }
    }

    private void setupFullList() {
        // Show full list without limitations
       // tvNoData.setVisibility(View.GONE);
        tvProMessage.setVisibility(View.GONE);
        proOverlayView.setVisibility(View.GONE);
        rvUsers.setVisibility(View.VISIBLE);

        // Setup RecyclerView with full data
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        UsersAdapter adapter = new UsersAdapter(this, usersList);
        rvUsers.setAdapter(adapter);
    }

    private void setupLimitedPreview(int type) {
        if (usersList.size() <= 0) {
            // No data to show
         //   tvNoData.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
            tvProMessage.setVisibility(View.GONE);
            proOverlayView.setVisibility(View.GONE);
            return;
        }

        // Create a preview list with limited number of items
        ArrayList<Follower> previewList = new ArrayList<>();
        int previewCount = Math.min(usersList.size(), FREE_PREVIEW_LIMIT);

        for (int i = 0; i < previewCount; i++) {
            previewList.add(usersList.get(i));
        }

        // Show PRO message
      //  tvNoData.setVisibility(View.GONE);
        tvProMessage.setVisibility(View.VISIBLE);
        proOverlayView.setVisibility(View.GONE);

        // Customize the PRO message based on type
        String proMessageText = "";
        int remainingCount = usersList.size() - previewCount;

        switch (type) {
            case TYPE_NOT_FOLLOWING_BACK:
                proMessageText = "Daha fazlasını görmek için PRO'ya yükselt. " + remainingCount +
                        " kullanıcı daha bulundu. " +
                        getString(R.string.upgrade_to_see_all_not_following_back);
                break;
            case TYPE_NOT_FOLLOWING:
                proMessageText = "Daha fazlasını görmek için PRO'ya yükselt. " + remainingCount +
                        " kullanıcı daha bulundu. " +
                        getString(R.string.upgrade_to_see_all_not_following);
                break;
            case TYPE_LOST_FOLLOWERS:
                proMessageText = "Daha fazlasını görmek için PRO'ya yükselt. " + remainingCount +
                        " kullanıcı daha bulundu. " +
                        getString(R.string.upgrade_to_see_all_lost_followers);
                break;
            case TYPE_NEW_FOLLOWERS:
                proMessageText = "Daha fazlasını görmek için PRO'ya yükselt. " + remainingCount +
                        " kullanıcı daha bulundu. " +
                        getString(R.string.upgrade_to_see_all_new_followers);
                break;
        }

        tvProMessage.setText(proMessageText);

        // Make PRO message clickable - go directly to subscription
        tvProMessage.setOnClickListener(v -> {
            startActivity(new Intent(this, SubscriptionActivity.class));
        });

        // Setup RecyclerView with limited data
        rvUsers.setVisibility(View.VISIBLE);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        UsersAdapter adapter = new UsersAdapter(this, previewList);
        rvUsers.setAdapter(adapter);
    }

    /**
     * Updates UI when PRO status changes (e.g., after subscription)
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Check if PRO status has changed
        boolean newProStatus = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("is_pro_user", false);

        if (newProStatus != isPro) {
            isPro = newProStatus;
            // Reload UI with current type
            int currentType = getIntent().getIntExtra("type", TYPE_NOT_FOLLOWING_BACK);
            loadData(currentType);
            setupUI(currentType);
        }
    }
}