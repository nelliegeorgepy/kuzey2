package com.example.instagramfollowerextractor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter for the onboarding view pager
 */
public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final int pageCount;

    public OnboardingPagerAdapter(int pageCount) {
        this.pageCount = pageCount;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Content is managed by the activity
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}