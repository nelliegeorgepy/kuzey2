package com.example.instagramfollowerextractor;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for ChatGPT integration
 */
public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";

    private ImageView ivBack;
    private TextView tvTitle;
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView ivSend;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;

    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private boolean isPro;

    // Chat API client
    private ChatGPTAPIClient chatGPTClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Check if user has PRO subscription
        isPro = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE)
                .getBoolean("is_pro_user", false);

        // Initialize views
        initViews();

        // Initialize chat API client
        initChatClient();

        // Setup chat RecyclerView
        setupChatRecyclerView();

        // Add welcome message
        addWelcomeMessage();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        ivSend = findViewById(R.id.ivSend);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set title
        tvTitle.setText(R.string.chat_assistant);
    }

    private void setupChatRecyclerView() {
        // Set up RecyclerView
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatMessages);
        rvChat.setAdapter(chatAdapter);
    }

    private void addWelcomeMessage() {
        // Add welcome message
        ChatMessage welcomeMessage = new ChatMessage(
                getString(R.string.chat_welcome_message),
                ChatMessage.SENDER_BOT,
                System.currentTimeMillis()
        );
        chatMessages.add(welcomeMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        rvChat.scrollToPosition(chatMessages.size() - 1);
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Send button
        ivSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        // Add user message to chat
        ChatMessage userMessage = new ChatMessage(
                message,
                ChatMessage.SENDER_USER,
                System.currentTimeMillis()
        );
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        rvChat.scrollToPosition(chatMessages.size() - 1);

        // Clear input field
        etMessage.setText("");

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Process message with ChatGPT API (simulated or real)
        sendChatMessage(message, new ChatGPTAPIClient.ChatCallback() {
            @Override
            public void onResponse(@NonNull String response) {
                runOnUiThread(() -> {
                    // Add bot response to chat
                    ChatMessage botMessage = new ChatMessage(
                            response,
                            ChatMessage.SENDER_BOT,
                            System.currentTimeMillis()
                    );
                    chatMessages.add(botMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    rvChat.scrollToPosition(chatMessages.size() - 1);

                    // Hide progress
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(@NonNull String error) {
                runOnUiThread(() -> {
                    // Show error message
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();

                    // Hide progress
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Go to dashboard
                finish();
                return true;
            } else if (itemId == R.id.nav_visitors) {
                // Go to profile visitors
                finish();
                return true;
            } else if (itemId == R.id.nav_stories) {
                // Go to stories
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Already on chat page
                return true;
            } else if (itemId == R.id.nav_tools) {
                // Go to tools
                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Class for chat messages
     */
    public static class ChatMessage {
        public static final int SENDER_USER = 0;
        public static final int SENDER_BOT = 1;

        private String message;
        private int sender;
        private long timestamp;

        public ChatMessage(String message, int sender, long timestamp) {
            this.message = message;
            this.sender = sender;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public int getSender() {
            return sender;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Simulated ChatGPT API client or real client
     */


    /**
     * Initialize chat client
     */
    private void initChatClient() {
        // Using real ChatGPT API client if you want to enable it
        // Remove this commented section and remove the simulation logic below
        // when you're ready to use the real API

        chatGPTClient = new ChatGPTAPIClient();


        // For now, we'll use a simulated response system
    }

    /**
     * Sends a message using the appropriate client
     */
    private void sendChatMessage(String message, final ChatGPTAPIClient.ChatCallback callback) {
        // Simulate network delay and responses
        new Thread(() -> {
            try {
                // Simulate API call delay (1-2 seconds)
                Thread.sleep(1000 + (int)(Math.random() * 1000));

                // Generate a simulated response
                String[] RESPONSES = {
                        "I'm analyzing your Instagram data and noticing some interesting patterns.",
                        "Based on your account analysis, your engagement rate is improving steadily.",
                        "You've recently gained several new followers. Would you like tips on retaining them?",
                        "Looking at your content performance, your video posts are generating 30% more engagement than photos.",
                        "I noticed some users who consistently engage with your content. They might be good candidates for collaboration.",
                        "According to your profile statistics, your optimal posting time appears to be between 7-9pm.",
                        "Your follower growth rate has increased by 15% compared to last month.",
                        "Several accounts that don't follow you back appear to be business accounts. This is common and not necessarily a concern.",
                        "Based on your engagement patterns, hashtag strategies could help increase your visibility further.",
                        "I've analyzed your followers' activity and found most of them are active on weekends."
                };

                int responseIndex = (int)(Math.random() * RESPONSES.length);
                String response = RESPONSES[responseIndex];

                callback.onResponse(response);
            } catch (Exception e) {
                Log.e(TAG, "Error simulating API call: " + e.getMessage(), e);
                callback.onError("Network error. Please try again.");
            }
        }).start();


        if (chatGPTClient != null) {
            chatGPTClient.sendMessage(message, callback);
        }


    }

    /**
     * Adapter for chat messages
     */
    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        // These are non-static final variables (instance variables), which is fine
        private final int VIEW_TYPE_USER = 0;
        private final int VIEW_TYPE_BOT = 1;

        private Context context;
        private List<ChatMessage> messages;

        public ChatAdapter(Context context, List<ChatMessage> messages) {
            this.context = context;
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).getSender();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_USER) {
                View view = getLayoutInflater().inflate(R.layout.item_chat_user, parent, false);
                return new UserMessageViewHolder(view);
            } else {
                View view = getLayoutInflater().inflate(R.layout.item_chat_bot, parent, false);
                return new BotMessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage message = messages.get(position);

            if (holder instanceof UserMessageViewHolder) {
                ((UserMessageViewHolder) holder).tvMessage.setText(message.getMessage());
            } else if (holder instanceof BotMessageViewHolder) {
                ((BotMessageViewHolder) holder).tvMessage.setText(message.getMessage());
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class UserMessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;

            public UserMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
            }
        }

        class BotMessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;

            public BotMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
            }
        }
    }
}