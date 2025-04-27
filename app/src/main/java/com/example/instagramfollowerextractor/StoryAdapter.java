package com.example.instagramfollowerextractor;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private static final String TAG = "StoryAdapter";
    private Context context;
    private ArrayList<Story> stories;
    private boolean isPro; // If true, all stories are viewable, otherwise only first one

    public StoryAdapter(Context context, ArrayList<Story> stories) {
        this.context = context;
        this.stories = stories;
        Log.d(TAG, "StoryAdapter oluşturuldu, hikaye sayısı: " + (stories != null ? stories.size() : "null"));

        // Check if user has PRO subscription
        this.isPro = context.getSharedPreferences("InstagramAnalyzerPrefs", Context.MODE_PRIVATE)
                .getBoolean("is_pro_user", false);

        // Hikaye listesinin içeriğini loglama
        if (stories != null) {
            for (int i = 0; i < stories.size(); i++) {
                Story story = stories.get(i);
                Log.d(TAG, String.format("Hikaye %d: %s - %s",
                        i+1,
                        story.getUsername(),
                        story.getMediaUrl() != null ?
                                story.getMediaUrl().substring(0, Math.min(30, story.getMediaUrl().length())) + "..." : "null"));
            }
        }
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            Log.d(TAG, "onCreateViewHolder başladı");
            View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
            Log.d(TAG, "View inflate edildi");
            return new StoryViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "onCreateViewHolder hatası: " + e.getMessage(), e);
            View emptyView = new View(context);
            emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new StoryViewHolder(emptyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        try {
            Log.d(TAG, "onBindViewHolder başladı, position: " + position);

            if (stories == null || position >= stories.size()) {
                Log.e(TAG, "Geçersiz position veya boş stories listesi");
                return;
            }

            Story story = stories.get(position);
            Log.d(TAG, String.format("Hikaye %d bağlanıyor: %s", position, story.getUsername()));

            // Kullanıcı adını göster
            if (holder.tvUsername != null) {
                holder.tvUsername.setText(story.getUsername());
            }

            // Medya türünü göster (Video mu, fotoğraf mı)
            if (holder.tvMediaType != null) {
                holder.tvMediaType.setText(story.isVideo() ? "Video" : "Fotoğraf");
            }

            // Profil resmini yükle
            if (holder.ivProfilePic != null && story.getProfileImageUrl() != null && !story.getProfileImageUrl().isEmpty()) {
                Picasso.get()
                        .load(story.getProfileImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.ivProfilePic);
                Log.d(TAG, "Profil resmi yükleniyor: " + story.getProfileImageUrl().substring(0, Math.min(30, story.getProfileImageUrl().length())) + "...");
            }

            // If not first story and not PRO, show the lock icon
            if (position > 0 && !isPro) {
                holder.ivLock.setVisibility(View.VISIBLE);
            } else {
                holder.ivLock.setVisibility(View.GONE);
            }

            // Medya önizlemesini yükle
            if (story.getMediaUrl() != null && !story.getMediaUrl().isEmpty()) {
                if (story.isVideo()) {
                    // Video için VideoView kullan
                    holder.ivMediaPreview.setVisibility(View.GONE);
                    holder.vvMediaPreview.setVisibility(View.VISIBLE);
                    holder.ivVideoIndicator.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.VISIBLE);

                    holder.vvMediaPreview.setVideoURI(Uri.parse(story.getMediaUrl()));
                    holder.vvMediaPreview.setOnPreparedListener(mp -> {
                        holder.progressBar.setVisibility(View.GONE);
                        mp.setLooping(true); // Videoyu döngüye al
                        holder.vvMediaPreview.start(); // Videoyu otomatik oynat
                        Log.d(TAG, "Video hazır ve oynatılıyor: " + story.getMediaUrl().substring(0, Math.min(30, story.getMediaUrl().length())) + "...");
                    });
                    holder.vvMediaPreview.setOnErrorListener((mp, what, extra) -> {
                        holder.progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Video oynatma hatası: what=" + what + ", extra=" + extra);
                        Toast.makeText(context, "Video oynatılamadı", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                } else {
                    // Fotoğraf için ImageView kullan
                    holder.ivMediaPreview.setVisibility(View.VISIBLE);
                    holder.vvMediaPreview.setVisibility(View.GONE);
                    holder.ivVideoIndicator.setVisibility(View.GONE);
                    holder.progressBar.setVisibility(View.VISIBLE);

                    Picasso.get()
                            .load(story.getMediaUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(holder.ivMediaPreview, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "Fotoğraf yüklendi: " + story.getMediaUrl().substring(0, Math.min(30, story.getMediaUrl().length())) + "...");
                                }

                                @Override
                                public void onError(Exception e) {
                                    holder.progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Fotoğraf yükleme hatası: " + e.getMessage());
                                    Toast.makeText(context, "Fotoğraf yüklenemedi", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                holder.ivMediaPreview.setVisibility(View.VISIBLE);
                holder.vvMediaPreview.setVisibility(View.GONE);
                holder.ivVideoIndicator.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                holder.ivMediaPreview.setImageResource(android.R.drawable.ic_menu_report_image);
                Log.w(TAG, "Medya URL'si boş: " + story.getUsername());
            }

            // Medya açma butonu
            if (holder.btnOpenMedia != null) {
                holder.btnOpenMedia.setOnClickListener(v -> {
                    if (position == 0 || isPro) {
                        // Allow view for first story or PRO users
                        openStoryDetail(story);
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

            // Tüm kartı tıklanabilir yap
            if (holder.cardView != null) {
                holder.cardView.setOnClickListener(v -> {
                    if (position == 0 || isPro) {
                        // Allow view for first story or PRO users
                        openStoryDetail(story);
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

            Log.d(TAG, "onBindViewHolder tamamlandı, position: " + position);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder ana hatası: " + e.getMessage(), e);
        }
    }

    private void openStoryDetail(Story story) {
        Intent intent = new Intent(context, StoryDetailActivity.class);
        intent.putExtra("story", story);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        int count = stories != null ? stories.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProfilePic, ivMediaPreview, ivVideoIndicator, ivLock;
        VideoView vvMediaPreview;
        TextView tvUsername, tvMediaType;
        Button btnOpenMedia;
        ProgressBar progressBar;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                Log.d(TAG, "StoryViewHolder başladı");
                cardView = itemView.findViewById(R.id.cardView);
                ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
                ivMediaPreview = itemView.findViewById(R.id.ivMediaPreview);
                vvMediaPreview = itemView.findViewById(R.id.vvMediaPreview);
                ivVideoIndicator = itemView.findViewById(R.id.ivVideoIndicator);
                ivLock = itemView.findViewById(R.id.ivLock);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvMediaType = itemView.findViewById(R.id.tvMediaType);
                btnOpenMedia = itemView.findViewById(R.id.btnOpenMedia);
                progressBar = itemView.findViewById(R.id.progressBar);
                Log.d(TAG, "StoryViewHolder tamamlandı");
            } catch (Exception e) {
                Log.e(TAG, "StoryViewHolder hatası: " + e.getMessage(), e);
            }
        }
    }
}