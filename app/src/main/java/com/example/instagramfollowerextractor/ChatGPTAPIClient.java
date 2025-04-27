package com.example.instagramfollowerextractor;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Client for integrating with ChatGPT API
 */
public class ChatGPTAPIClient {
    private static final String TAG = "ChatGPTAPIClient";

    // ChatGPT API constants
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String MODEL = "gpt-3.5-turbo";
    private static final int MAX_TOKENS = 300;

    // Set your API key here
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    // OkHttp client for network calls
    private final OkHttpClient client;

    public ChatGPTAPIClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Sends a message to the ChatGPT API and returns the response
     */
    public void sendMessage(String message, String systemPrompt, final ChatCallback callback) {
        try {
            // Create request JSON
            JSONObject requestJson = new JSONObject();
            requestJson.put("model", MODEL);
            requestJson.put("max_tokens", MAX_TOKENS);

            // Add messages array
            JSONArray messagesArray = new JSONArray();

            // Add system message if provided
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesArray.put(systemMessage);
            }

            // Add user message
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messagesArray.put(userMessage);

            requestJson.put("messages", messagesArray);

            // Create request - fixed method signature
            RequestBody body = RequestBody.create(JSON, requestJson.toString());
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "API call failed: " + e.getMessage(), e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "API error: " + response.code() + " - " + response.message());
                            callback.onError("API error: " + response.code() + " - " + response.message());
                            return;
                        }

                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");

                        if (choices.length() > 0) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject message = choice.getJSONObject("message");
                            String content = message.getString("content");

                            callback.onResponse(content.trim());
                        } else {
                            callback.onError("No response from API");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage(), e);
            callback.onError("Error creating request: " + e.getMessage());
        }
    }

    /**
     * Overloaded method without system prompt
     */
    public void sendMessage(String message, final ChatCallback callback) {
        sendMessage(message, getDefaultSystemPrompt(), callback);
    }

    /**
     * Get default system prompt for Instagram analysis
     */
    private String getDefaultSystemPrompt() {
        return "You are an Instagram analytics assistant. You help users understand their Instagram " +
                "follower statistics, engagement metrics, and provide helpful advice for improving " +
                "their Instagram presence. Focus on providing specific, data-driven insights about " +
                "followers, engagement, and content strategy based on the information shared. " +
                "Keep your responses concise, informative, and focused on Instagram analytics.";
    }

    /**
     * Callback interface for chat responses
     */
    public interface ChatCallback {
        void onResponse(@NonNull String response);
        void onError(@NonNull String error);
    }
}