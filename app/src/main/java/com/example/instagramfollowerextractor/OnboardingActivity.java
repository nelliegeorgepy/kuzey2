package com.example.instagramfollowerextractor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Onboarding activity shown to first-time users
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnContinue;
    private TabLayout tabLayout;

    private ImageView[] onboardingImages;
    private LottieAnimationView[] onboardingAnimations;
    private TextView[] onboardingTitles;
    private TextView[] onboardingDescriptions;

    private static final int NUM_PAGES = 3;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        initViews();

        // Setup ViewPager
        setupViewPager();

        // Setup Continue button
        setupContinueButton();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnContinue = findViewById(R.id.btnContinue);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize arrays for the content of each page
        onboardingImages = new ImageView[NUM_PAGES];
        onboardingAnimations = new LottieAnimationView[NUM_PAGES];
        onboardingTitles = new TextView[NUM_PAGES];
        onboardingDescriptions = new TextView[NUM_PAGES];

        // Find views for each page
        onboardingImages[0] = findViewById(R.id.ivOnboarding1);
        onboardingImages[1] = findViewById(R.id.ivOnboarding2);
        onboardingImages[2] = findViewById(R.id.ivOnboarding3);

        onboardingAnimations[0] = findViewById(R.id.animationView1);
        onboardingAnimations[1] = findViewById(R.id.animationView2);
        onboardingAnimations[2] = findViewById(R.id.animationView3);

        onboardingTitles[0] = findViewById(R.id.tvTitle1);
        onboardingTitles[1] = findViewById(R.id.tvTitle2);
        onboardingTitles[2] = findViewById(R.id.tvTitle3);

        onboardingDescriptions[0] = findViewById(R.id.tvDescription1);
        onboardingDescriptions[1] = findViewById(R.id.tvDescription2);
        onboardingDescriptions[2] = findViewById(R.id.tvDescription3);
    }

    private void setupViewPager() {
        // Create adapter for ViewPager
        OnboardingPagerAdapter pagerAdapter = new OnboardingPagerAdapter(this, NUM_PAGES);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // No text for tabs, just indicators
        }).attach();

        // Set page change listener to update UI
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateContinueButtonText();
                updatePageVisibility();
            }
        });
    }

    private void setupContinueButton() {
        btnContinue.setOnClickListener(v -> {
            if (currentPage < NUM_PAGES - 1) {
                // Go to next page
                viewPager.setCurrentItem(currentPage + 1);
            } else {
                // Complete onboarding
                completeOnboarding();
            }
        });

        // Set initial button text
        updateContinueButtonText();
    }

    private void updateContinueButtonText() {
        if (currentPage == NUM_PAGES - 1) {
            // Last page
            btnContinue.setText(R.string.login_with_instagram);
        } else {
            // Not last page
            btnContinue.setText(R.string.continue_text);
        }
    }

    private void updatePageVisibility() {
        // Hide all pages
        for (int i = 0; i < NUM_PAGES; i++) {
            if (onboardingImages[i] != null) {
                onboardingImages[i].setVisibility(View.GONE);
            }
            if (onboardingAnimations[i] != null) {
                onboardingAnimations[i].setVisibility(View.GONE);
            }
            if (onboardingTitles[i] != null) {
                onboardingTitles[i].setVisibility(View.GONE);
            }
            if (onboardingDescriptions[i] != null) {
                onboardingDescriptions[i].setVisibility(View.GONE);
            }
        }

        // Show current page
        if (onboardingImages[currentPage] != null) {
            onboardingImages[currentPage].setVisibility(View.VISIBLE);
        }
        if (onboardingAnimations[currentPage] != null) {
            onboardingAnimations[currentPage].setVisibility(View.VISIBLE);
            onboardingAnimations[currentPage].playAnimation();
        }
        if (onboardingTitles[currentPage] != null) {
            onboardingTitles[currentPage].setVisibility(View.VISIBLE);
        }
        if (onboardingDescriptions[currentPage] != null) {
            onboardingDescriptions[currentPage].setVisibility(View.VISIBLE);
        }
    }

    private void completeOnboarding() {
        // Mark onboarding as completed
        getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("has_completed_onboarding", true)
                .apply();

        // Navigate to login
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Adapter for the onboarding ViewPager
     */
    private static class OnboardingPagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

        private int pageCount;
        private AppCompatActivity activity;

        public OnboardingPagerAdapter(AppCompatActivity activity, int pageCount) {
            this.activity = activity;
            this.pageCount = pageCount;
        }

        @androidx.annotation.NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            return new OnboardingViewHolder(
                    android.view.LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_onboarding_page, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull OnboardingViewHolder holder, int position) {
            // Content is managed by the activity
        }

        @Override
        public int getItemCount() {
            return pageCount;
        }

        static class OnboardingViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            public OnboardingViewHolder(@androidx.annotation.NonNull View itemView) {
                super(itemView);
            }
        }
    }
}