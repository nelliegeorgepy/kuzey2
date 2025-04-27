package com.example.instagramfollowerextractor;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Activity for displaying full story details (image or video)
 */
public class StoryDetailActivity extends AppCompatActivity {

    private static final String TAG = "StoryDetailActivity";

    private ImageView ivBack;
    private TextView tvUsername;
    private ImageView ivProfilePic;
    private VideoView videoView;
    private ImageView ivImageView;
    private ProgressBar progressBar;
    private TextView tvMediaType;

    private Story story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        // Get the story data from intent
        story = (Story) getIntent().getParcelableExtra("story");
        if (story == null) {
            Toast.makeText(this, R.string.story_data_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Set up UI with story data
        setupUI();

        // Load media content
        loadMediaContent();

        // Set up back button
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvUsername = findViewById(R.id.tvUsername);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        videoView = findViewById(R.id.videoView);
        ivImageView = findViewById(R.id.ivImageView);
        progressBar = findViewById(R.id.progressBar);
        tvMediaType = findViewById(R.id.tvMediaType);
    }

    private void setupUI() {
        // Set username
        tvUsername.setText("@" + story.getUsername());

        // Set media type text
        tvMediaType.setText(story.isVideo() ? R.string.video : R.string.photo);

        // Load profile picture
        if (story.getProfileImageUrl() != null && !story.getProfileImageUrl().isEmpty()) {
            Picasso.get()
                    .load(story.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(ivProfilePic);
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
    }

    private void loadMediaContent() {
        if (story.getMediaUrl() == null || story.getMediaUrl().isEmpty()) {
            Toast.makeText(this, R.string.media_url_not_found, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (story.isVideo()) {
            // Load video
            loadVideo();
        } else {
            // Load image
            loadImage();
        }
    }

    private void loadVideo() {
        try {
            // Hide image view, show video view
            ivImageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

            // Create media controller
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // Set up the video
            Uri videoUri = Uri.parse(story.getMediaUrl());
            videoView.setVideoURI(videoUri);

            // Set up listeners
            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                mp.setLooping(true);
                videoView.start();
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Video error: " + what + ", " + extra);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoryDetailActivity.this, R.string.video_load_error, Toast.LENGTH_SHORT).show();
                return true;
            });

            // Start loading the video
            videoView.requestFocus();
            videoView.start();
        } catch (Exception e) {
            Log.e(TAG, "Error loading video: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.video_load_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage() {
        try {
            // Hide video view, show image view
            videoView.setVisibility(View.GONE);
            ivImageView.setVisibility(View.VISIBLE);

            // Load the image with Picasso
            Picasso.get()
                    .load(story.getMediaUrl())
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(ivImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage(), e);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(StoryDetailActivity.this, R.string.image_load_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.image_load_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the video if it's playing
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}