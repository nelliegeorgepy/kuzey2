package com.example.instagramfollowerextractor;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base Activity class that provides common functionality to all activities
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Shows a dialog prompting the user to upgrade to PRO
     */
    public void showProUpgradeDialog(String title, String message) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pro_upgrade);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        Button btnUpgrade = dialog.findViewById(R.id.btnUpgrade);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnUpgrade.setOnClickListener(v -> {
            // Launch subscription/upgrade activity
            Intent intent = new Intent(this, SubscriptionActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Shows a dialog for general information or confirmations
     */
    public void showInfoDialog(String title, String message, String buttonText,
                               View.OnClickListener onClickListener) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnConfirm.setText(buttonText);

        btnConfirm.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Shows a confirmation dialog with Yes/No options
     */
    public void showConfirmDialog(String title, String message,
                                  View.OnClickListener positiveListener,
                                  View.OnClickListener negativeListener) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnYes.setOnClickListener(v -> {
            if (positiveListener != null) {
                positiveListener.onClick(v);
            }
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> {
            if (negativeListener != null) {
                negativeListener.onClick(v);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Sets PRO user status (for testing purposes)
     */
    protected void setProStatus(boolean isPro) {
        getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_pro_user", isPro)
                .apply();
    }
}