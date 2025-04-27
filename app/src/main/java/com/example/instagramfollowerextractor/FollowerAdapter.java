package com.example.instagramfollowerextractor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Follower> followers;

    public FollowerAdapter(Context context, ArrayList<Follower> followers) {
        this.context = context;
        this.followers = followers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follower, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Follower follower = followers.get(position);

        // Kullanıcı adı ve görünen adı ayarla
        holder.tvUsername.setText("@" + follower.getUsername());
        holder.tvDisplayName.setText(follower.getDisplayName());

        // Takip durumunu göster
        if (follower.isFollowingYou()) {
            holder.tvFollowStatus.setText("Sizi takip ediyor");
            holder.tvFollowStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvFollowStatus.setVisibility(View.GONE);
        }

        // Profil resmini yükle
        Picasso.get()
                .load(follower.getProfilePicURL())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgProfile);
    }

    @Override
    public int getItemCount() {
        return followers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvDisplayName, tvFollowStatus;
        ImageView imgProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvFollowStatus = itemView.findViewById(R.id.tvFollowStatus);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}