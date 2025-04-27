package com.example.instagramfollowerextractor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

/**
 * Activity for handling PRO subscription
 */
public class SubscriptionActivity extends BaseActivity {

    private static final String TAG = "SubscriptionActivity";

    private ImageView ivBack;
    private TextView tvTitle;

    private CardView cvMonthly;
    private CardView cvYearly;
    private CardView cvLifetime;

    private Button btnMonthly;
    private Button btnYearly;
    private Button btnLifetime;

    private TextView tvYearlySavings;
    private TextView tvPopular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);

        cvMonthly = findViewById(R.id.cvMonthly);
        cvYearly = findViewById(R.id.cvYearly);
        cvLifetime = findViewById(R.id.cvLifetime);

        btnMonthly = findViewById(R.id.btnMonthly);
        btnYearly = findViewById(R.id.btnYearly);
        btnLifetime = findViewById(R.id.btnLifetime);

        tvYearlySavings = findViewById(R.id.tvYearlySavings);
        tvPopular = findViewById(R.id.tvPopular);

        // Set title
        tvTitle.setText(R.string.upgrade_to_pro);
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Subscription plan cards
        cvMonthly.setOnClickListener(v -> selectPlan(1));
        cvYearly.setOnClickListener(v -> selectPlan(2));
        cvLifetime.setOnClickListener(v -> selectPlan(3));

        // Subscription buttons
        btnMonthly.setOnClickListener(v -> purchaseSubscription(1));
        btnYearly.setOnClickListener(v -> purchaseSubscription(2));
        btnLifetime.setOnClickListener(v -> purchaseSubscription(3));
    }

    /**
     * Highlight the selected plan
     *
     * @param planType 1: Monthly, 2: Yearly, 3: Lifetime
     */
    private void selectPlan(int planType) {
        // Reset all card backgrounds
        cvMonthly.setCardBackgroundColor(getResources().getColor(R.color.background_tertiary));
        cvYearly.setCardBackgroundColor(getResources().getColor(R.color.background_tertiary));
        cvLifetime.setCardBackgroundColor(getResources().getColor(R.color.background_tertiary));

        // Highlight selected card
        switch (planType) {
            case 1:
                cvMonthly.setCardBackgroundColor(getResources().getColor(R.color.background_tertiary));
                break;
            case 2:
                cvYearly.setCardBackgroundColor(getResources().getColor(R.color.background_tertiary));
                break;
            case 3:
                cvLifetime.setCardBackgroundColor(getResources().getColor(R.color.background_tertiary));
                break;
        }
    }

    /**
     * Process the subscription purchase
     *
     * @param planType 1: Monthly, 2: Yearly, 3: Lifetime
     */
    private void purchaseSubscription(int planType) {
        // In a real app, this would initiate the payment flow
        // For this demo, we'll simulate the purchase and enable PRO features

        String planName;
        switch (planType) {
            case 1:
                planName = "Monthly";
                break;
            case 2:
                planName = "Yearly";
                break;
            case 3:
                planName = "Lifetime";
                break;
            default:
                planName = "Unknown";
        }

        // Show a progress indicator (not implemented for simplicity)

        // Simulate API call delay (1-2 seconds)
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                Thread.sleep(1500);

                runOnUiThread(() -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                    // Set user as PRO
                    setProStatus(true);

                    // Show success message
                    Toast.makeText(this, getString(R.string.subscription_success, planName), Toast.LENGTH_LONG).show();

                    // Navigate back to dashboard
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Toast.makeText(this, R.string.subscription_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}