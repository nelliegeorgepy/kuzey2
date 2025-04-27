package com.example.instagramfollowerextractor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter for displaying stories in a grid layout
 */
public class StoriesGridAdapter extends RecyclerView.Adapter<StoriesGridAdapter.StoryGridViewHolder> {

    private static final String TAG = "StoriesGridAdapter";

    private Context context;
    private ArrayList<Story> stories;
    private boolean isPro;
    private static final int FREE_LIMIT = 1; // Free users can view only 1 story

    public StoriesGridAdapter(Context context, ArrayList<Story> stories, boolean isPro) {
        this.context = context;
        this.stories = stories;
        this.isPro = isPro;
    }

    @NonNull
    @Override
    public StoryGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story_grid, parent, false);
        return new StoryGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryGridViewHolder holder, int position) {
        Story story = stories.get(position);

        // Set username
        holder.tvUsername.setText("@" + story.getUsername());

        // Set media type text
        holder.tvMediaType.setText(story.isVideo() ? R.string.video : R.string.photo);

        // Load profile picture
        if (story.getProfileImageUrl() != null && !story.getProfileImageUrl().isEmpty()) {
            Picasso.get()
                    .load(story.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(holder.ivProfilePic);

            // Always use profile picture for the media thumbnail (as per requirement #6)
            Picasso.get()
                    .load(story.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(holder.ivMediaThumbnail);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.placeholder_profile);
            holder.ivMediaThumbnail.setImageResource(R.drawable.placeholder_profile);
        }

        // If not PRO and beyond limit, show lock icon
        if (!isPro && position >= FREE_LIMIT) {
            holder.ivLock.setVisibility(View.VISIBLE);
        } else {
            holder.ivLock.setVisibility(View.GONE);
        }

        // Show video indicator if needed
        if (story.isVideo()) {
            holder.ivVideoIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.ivVideoIndicator.setVisibility(View.GONE);
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (isPro || position < FREE_LIMIT) {
                // Allow view for PRO users or first item for free users
                openStoryDetail(story, position);
            } else {
                // Show PRO upgrade dialog
                if (context instanceof BaseActivity) {
                    ((BaseActivity) context).showProUpgradeDialog(
                            context.getString(R.string.pro_feature_title),
                            context.getString(R.string.pro_feature_stories_description)
                    );
                } else {
                    Toast.makeText(context, R.string.upgrade_to_pro, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openStoryDetail(Story story, int position) {
        Intent intent = new Intent(context, StoryDetailActivity.class);
        intent.putExtra("story", story);
        intent.putExtra("position", position);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return stories != null ? stories.size() : 0;
    }

    static class StoryGridViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProfilePic;
        ImageView ivMediaThumbnail;
        ImageView ivVideoIndicator;
        ImageView ivLock;
        TextView tvUsername;
        TextView tvMediaType;

        public StoryGridViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            ivMediaThumbnail = itemView.findViewById(R.id.ivMediaThumbnail);
            ivVideoIndicator = itemView.findViewById(R.id.ivVideoIndicator);
            ivLock = itemView.findViewById(R.id.ivLock);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvMediaType = itemView.findViewById(R.id.tvMediaType);
        }
    }
}