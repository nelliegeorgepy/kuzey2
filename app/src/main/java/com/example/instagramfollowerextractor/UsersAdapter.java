package com.example.instagramfollowerextractor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
 * Adapter for displaying user lists (followers, following, etc.)
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private static final String TAG = "UsersAdapter";
    private Context context;
    private ArrayList<Follower> usersList;

    public UsersAdapter(Context context, ArrayList<Follower> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Follower user = usersList.get(position);

        // Set username and display name
        holder.tvUsername.setText("@" + user.getUsername());
        holder.tvDisplayName.setText(user.getDisplayName());

        // Load profile picture
        if (user.getProfilePicURL() != null && !user.getProfilePicURL().isEmpty()) {
            Picasso.get()
                    .load(user.getProfilePicURL())
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(holder.ivProfilePic);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.placeholder_profile);
        }

        // Show following status if applicable
        if (user.isFollowingYou()) {
            holder.tvFollowStatus.setVisibility(View.VISIBLE);
            holder.tvFollowStatus.setText(R.string.follows_you);
        } else {
            holder.tvFollowStatus.setVisibility(View.GONE);
        }

        // Set up card click to open Instagram profile
        holder.cardView.setOnClickListener(v -> {
            try {
                // Try to open Instagram app
                Uri uri = Uri.parse("http://instagram.com/_u/" + user.getUsername());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.instagram.android");

                // Check if Instagram is installed
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    // If Instagram is not installed, open in browser
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://instagram.com/" + user.getUsername()));
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(context, R.string.could_not_open_profile, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList != null ? usersList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProfilePic;
        TextView tvUsername;
        TextView tvDisplayName;
        TextView tvFollowStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvFollowStatus = itemView.findViewById(R.id.tvFollowStatus);
        }
    }
}