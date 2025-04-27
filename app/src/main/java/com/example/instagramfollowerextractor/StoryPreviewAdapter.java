package com.example.instagramfollowerextractor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter for showing story previews in horizontal recycler view
 */
public class StoryPreviewAdapter extends RecyclerView.Adapter<StoryPreviewAdapter.StoryViewHolder> {

    private static final String TAG = "StoryPreviewAdapter";
    private Context context;
    private ArrayList<Story> stories;
    private boolean isPro; // If true, all stories are viewable, otherwise only first one

    public StoryPreviewAdapter(Context context, ArrayList<Story> stories) {
        this.context = context;
        this.stories = stories;

        // Check if user has PRO subscription
        this.isPro = context.getSharedPreferences("InstagramAnalyzerPrefs", Context.MODE_PRIVATE)
                .getBoolean("is_pro_user", false);
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story_preview, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = stories.get(position);

        // Set username
        holder.tvUsername.setText(story.getUsername());

        // Load profile picture
        if (story.getProfileImageUrl() != null && !story.getProfileImageUrl().isEmpty()) {
            Picasso.get()
                    .load(story.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(holder.ivProfilePic);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.placeholder_profile);
        }

        // Show video indicator if needed
        if (story.isVideo()) {
            holder.ivVideoIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.ivVideoIndicator.setVisibility(View.GONE);
        }

        // If not first story and not PRO, show the lock icon
        if (position > 0 && !isPro) {
            holder.ivLock.setVisibility(View.VISIBLE);
        } else {
            holder.ivLock.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (position == 0 || isPro) {
                // Allow view for first story or PRO users
                openStoryDetail(story, position == 0);
            } else {
                // Show PRO upgrade dialog
                if (context instanceof BaseActivity) {
                    ((BaseActivity) context).showProUpgradeDialog(
                            context.getString(R.string.pro_feature_title),
                            context.getString(R.string.pro_feature_stories_description)
                    );
                } else {
                    // Fallback if context is not BaseActivity
                    Intent intent = new Intent(context, SubscriptionActivity.class);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void openStoryDetail(Story story, boolean isFirstFreeStory) {
        Intent intent = new Intent(context, StoryDetailActivity.class);
        intent.putExtra("story", story);
        intent.putExtra("is_first_free_story", isFirstFreeStory);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return stories != null ? stories.size() : 0;
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfilePic;
        ImageView ivVideoIndicator;
        ImageView ivLock;
        TextView tvUsername;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            ivVideoIndicator = itemView.findViewById(R.id.ivVideoIndicator);
            ivLock = itemView.findViewById(R.id.ivLock);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }
    }
}