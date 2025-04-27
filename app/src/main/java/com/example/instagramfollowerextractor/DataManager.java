package com.example.instagramfollowerextractor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Singleton class for managing app data including followers, following, stories, and other analytics
 */
public class DataManager {
    private static final String TAG = "DataManager";
    private static final String PREFS_NAME = "InstagramAnalyzerData";

    // SharedPreferences keys
    private static final String KEY_FOLLOWERS = "followers_list";
    private static final String KEY_FOLLOWING = "following_list";
    private static final String KEY_NOT_FOLLOWING_BACK = "not_following_back";
    private static final String KEY_NOT_FOLLOWING = "not_following";
    private static final String KEY_STORIES = "stories_list";
    private static final String KEY_PREVIOUS_FOLLOWERS = "previous_followers";
    private static final String KEY_LOST_FOLLOWERS = "lost_followers";
    private static final String KEY_NEW_FOLLOWERS = "new_followers";
    private static final String KEY_PROFILE_VISITORS = "profile_visitors";
    private static final String KEY_SECRET_ADMIRERS = "secret_admirers";
    private static final String KEY_BLOCKERS = "blockers";
    private static final String KEY_LAST_UPDATED = "last_updated";

    private static DataManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Context context;

    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    /**
     * Saves the current follower list to SharedPreferences
     */
    public void saveFollowersList(ArrayList<Follower> followers) {
        if (followers == null || followers.isEmpty()) {
            Log.e(TAG, "Attempting to save empty followers list");
            return;
        }

        String jsonFollowers = gson.toJson(followers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FOLLOWERS, jsonFollowers);
        editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "Saved " + followers.size() + " followers to SharedPreferences");
    }

    /**
     * Retrieves the current follower list from SharedPreferences
     */
    public ArrayList<Follower> getFollowersList() {
        String jsonFollowers = sharedPreferences.getString(KEY_FOLLOWERS, "");
        if (jsonFollowers.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> followers = gson.fromJson(jsonFollowers, type);
        Log.d(TAG, "Retrieved " + followers.size() + " followers from SharedPreferences");
        return followers;
    }

    /**
     * Saves the current following list to SharedPreferences
     */
    public void saveFollowingList(ArrayList<Follower> following) {
        if (following == null || following.isEmpty()) {
            Log.e(TAG, "Attempting to save empty following list");
            return;
        }

        String jsonFollowing = gson.toJson(following);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FOLLOWING, jsonFollowing);
        editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "Saved " + following.size() + " following to SharedPreferences");
    }

    /**
     * Retrieves the current following list from SharedPreferences
     */
    public ArrayList<Follower> getFollowingList() {
        String jsonFollowing = sharedPreferences.getString(KEY_FOLLOWING, "");
        if (jsonFollowing.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> following = gson.fromJson(jsonFollowing, type);
        Log.d(TAG, "Retrieved " + following.size() + " following from SharedPreferences");
        return following;
    }

    /**
     * Saves the list of users not following back to SharedPreferences
     */
    public void saveNotFollowingBackList(ArrayList<Follower> notFollowingBack) {
        if (notFollowingBack == null) {
            Log.e(TAG, "Attempting to save null not following back list");
            return;
        }

        String jsonNotFollowingBack = gson.toJson(notFollowingBack);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOT_FOLLOWING_BACK, jsonNotFollowingBack);
        editor.apply();

        Log.d(TAG, "Saved " + notFollowingBack.size() + " users not following back to SharedPreferences");
    }

    /**
     * Retrieves the list of users not following back from SharedPreferences
     */
    public ArrayList<Follower> getNotFollowingBackList() {
        String jsonNotFollowingBack = sharedPreferences.getString(KEY_NOT_FOLLOWING_BACK, "");
        if (jsonNotFollowingBack.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> notFollowingBack = gson.fromJson(jsonNotFollowingBack, type);
        Log.d(TAG, "Retrieved " + notFollowingBack.size() + " users not following back from SharedPreferences");
        return notFollowingBack;
    }

    /**
     * Saves the list of users you are not following to SharedPreferences
     */
    public void saveNotFollowingList(ArrayList<Follower> notFollowing) {
        if (notFollowing == null) {
            Log.e(TAG, "Attempting to save null not following list");
            return;
        }

        String jsonNotFollowing = gson.toJson(notFollowing);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOT_FOLLOWING, jsonNotFollowing);
        editor.apply();

        Log.d(TAG, "Saved " + notFollowing.size() + " users you don't follow to SharedPreferences");
    }

    /**
     * Retrieves the list of users you are not following from SharedPreferences
     */
    public ArrayList<Follower> getNotFollowingList() {
        String jsonNotFollowing = sharedPreferences.getString(KEY_NOT_FOLLOWING, "");
        if (jsonNotFollowing.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> notFollowing = gson.fromJson(jsonNotFollowing, type);
        Log.d(TAG, "Retrieved " + notFollowing.size() + " users you don't follow from SharedPreferences");
        return notFollowing;
    }

    /**
     * Saves the list of stories to SharedPreferences
     */
    public void saveStoriesList(ArrayList<Story> stories) {
        if (stories == null) {
            Log.e(TAG, "Attempting to save null stories list");
            return;
        }

        String jsonStories = gson.toJson(stories);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STORIES, jsonStories);
        editor.apply();

        Log.d(TAG, "Saved " + stories.size() + " stories to SharedPreferences");
    }

    /**
     * Retrieves the list of stories from SharedPreferences
     */
    public ArrayList<Story> getStoriesList() {
        String jsonStories = sharedPreferences.getString(KEY_STORIES, "");
        if (jsonStories.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Story>>(){}.getType();
        ArrayList<Story> stories = gson.fromJson(jsonStories, type);
        Log.d(TAG, "Retrieved " + stories.size() + " stories from SharedPreferences");
        return stories;
    }

    /**
     * Saves the previous list of followers for comparison
     */
    public void updatePreviousFollowersList(ArrayList<Follower> followers) {
        if (followers == null || followers.isEmpty()) {
            Log.e(TAG, "Attempting to save empty previous followers list");
            return;
        }

        String jsonFollowers = gson.toJson(followers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PREVIOUS_FOLLOWERS, jsonFollowers);
        editor.apply();

        Log.d(TAG, "Updated previous followers list with " + followers.size() + " followers");
    }

    /**
     * Retrieves the previous list of followers for comparison
     */
    public ArrayList<Follower> getPreviousFollowersList() {
        String jsonFollowers = sharedPreferences.getString(KEY_PREVIOUS_FOLLOWERS, "");
        if (jsonFollowers.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> followers = gson.fromJson(jsonFollowers, type);
        Log.d(TAG, "Retrieved " + followers.size() + " previous followers from SharedPreferences");
        return followers;
    }

    /**
     * Saves the list of lost followers
     */
    public void saveLostFollowersList(ArrayList<Follower> lostFollowers) {
        if (lostFollowers == null) {
            Log.e(TAG, "Attempting to save null lost followers list");
            return;
        }

        String jsonLostFollowers = gson.toJson(lostFollowers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOST_FOLLOWERS, jsonLostFollowers);
        editor.apply();

        Log.d(TAG, "Saved " + lostFollowers.size() + " lost followers to SharedPreferences");
    }

    /**
     * Retrieves the list of lost followers
     */
    public ArrayList<Follower> getLostFollowersList() {
        String jsonLostFollowers = sharedPreferences.getString(KEY_LOST_FOLLOWERS, "");
        if (jsonLostFollowers.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> lostFollowers = gson.fromJson(jsonLostFollowers, type);
        Log.d(TAG, "Retrieved " + lostFollowers.size() + " lost followers from SharedPreferences");
        return lostFollowers;
    }

    /**
     * Saves the list of new followers
     */
    public void saveNewFollowersList(ArrayList<Follower> newFollowers) {
        if (newFollowers == null) {
            Log.e(TAG, "Attempting to save null new followers list");
            return;
        }

        String jsonNewFollowers = gson.toJson(newFollowers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NEW_FOLLOWERS, jsonNewFollowers);
        editor.apply();

        Log.d(TAG, "Saved " + newFollowers.size() + " new followers to SharedPreferences");
    }

    /**
     * Retrieves the list of new followers
     */
    public ArrayList<Follower> getNewFollowersList() {
        String jsonNewFollowers = sharedPreferences.getString(KEY_NEW_FOLLOWERS, "");
        if (jsonNewFollowers.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> newFollowers = gson.fromJson(jsonNewFollowers, type);
        Log.d(TAG, "Retrieved " + newFollowers.size() + " new followers from SharedPreferences");
        return newFollowers;
    }

    /**
     * Generates and saves a list of profile visitors
     * For non-PRO users, this will still generate data (but limited in the UI)
     */
    public void generateProfileVisitors() {
        // Get followers - select random users from followers list instead of following
        ArrayList<Follower> followers = getFollowersList();
        if (followers.isEmpty()) {
            Log.w(TAG, "Cannot generate profile visitors from empty followers list");
            return;
        }

        ArrayList<Follower> visitors = new ArrayList<>();
        int visitorCount = Math.min(followers.size(), 8 + new Random().nextInt(7)); // 8-15 visitors

        // Use HashSet to keep track of indices that were already selected
        Set<Integer> selectedIndices = new HashSet<>();
        Random random = new Random();

        while (visitors.size() < visitorCount && selectedIndices.size() < followers.size()) {
            int index = random.nextInt(followers.size());
            if (selectedIndices.add(index)) { // Add returns true if the element was added
                visitors.add(followers.get(index));
            }
        }

        String jsonVisitors = gson.toJson(visitors);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_VISITORS, jsonVisitors);
        editor.apply();

        Log.d(TAG, "Generated and saved " + visitors.size() + " profile visitors from followers list");
    }

    /**
     * Retrieves the list of profile visitors
     * For all users, returns the list but access to details beyond the count is controlled in UI
     */
    public ArrayList<Follower> getProfileVisitorsList() {
        String jsonVisitors = sharedPreferences.getString(KEY_PROFILE_VISITORS, "");
        if (jsonVisitors.isEmpty()) {
            // Generate visitors if they don't exist
            generateProfileVisitors();
            jsonVisitors = sharedPreferences.getString(KEY_PROFILE_VISITORS, "");
            if (jsonVisitors.isEmpty()) {
                return new ArrayList<>();
            }
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> visitors = gson.fromJson(jsonVisitors, type);
        Log.d(TAG, "Retrieved " + visitors.size() + " profile visitors from SharedPreferences");
        return visitors;
    }

    /**
     * Saves the list of profile visitors
     */
    public void saveProfileVisitorsList(ArrayList<Follower> visitors) {
        if (visitors == null) {
            Log.e(TAG, "Attempting to save null profile visitors list");
            return;
        }

        String jsonVisitors = gson.toJson(visitors);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_VISITORS, jsonVisitors);
        editor.apply();

        Log.d(TAG, "Saved " + visitors.size() + " profile visitors to SharedPreferences");
    }

    /**
     * Generates and saves a list of secret admirers
     * For non-PRO users, this will still generate data (but limited in the UI)
     */
    public void generateSecretAdmirers() {
        // For all users, select random users from people who follow you
        ArrayList<Follower> followers = getFollowersList();
        if (followers.isEmpty()) {
            Log.w(TAG, "Cannot generate secret admirers from empty followers list");
            return;
        }

        // Get profile visitors to avoid duplicate selection
        ArrayList<Follower> visitors = getProfileVisitorsList();
        Set<String> visitorUsernames = new HashSet<>();
        for (Follower visitor : visitors) {
            visitorUsernames.add(visitor.getUsername());
        }

        ArrayList<Follower> admirers = new ArrayList<>();
        int admirerCount = Math.min(followers.size(), 5 + new Random().nextInt(6)); // 5-10 admirers

        // Use HashSet to keep track of indices that were already selected
        Set<Integer> selectedIndices = new HashSet<>();
        Random random = new Random();

        while (admirers.size() < admirerCount && selectedIndices.size() < followers.size()) {
            int index = random.nextInt(followers.size());
            if (selectedIndices.add(index)) { // Add returns true if the element was added
                Follower potential = followers.get(index);
                // Don't use the same person as both a visitor and an admirer
                if (!visitorUsernames.contains(potential.getUsername())) {
                    admirers.add(potential);
                }
            }
        }

        String jsonAdmirers = gson.toJson(admirers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SECRET_ADMIRERS, jsonAdmirers);
        editor.apply();

        Log.d(TAG, "Generated and saved " + admirers.size() + " secret admirers from followers list");
    }

    /**
     * Retrieves the list of secret admirers
     * For all users, returns the list but access to details beyond the count is controlled in UI
     */
    public ArrayList<Follower> getSecretAdmirersList() {
        String jsonAdmirers = sharedPreferences.getString(KEY_SECRET_ADMIRERS, "");
        if (jsonAdmirers.isEmpty()) {
            // Generate admirers if they don't exist
            generateSecretAdmirers();
            jsonAdmirers = sharedPreferences.getString(KEY_SECRET_ADMIRERS, "");
            if (jsonAdmirers.isEmpty()) {
                return new ArrayList<>();
            }
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> admirers = gson.fromJson(jsonAdmirers, type);
        Log.d(TAG, "Retrieved " + admirers.size() + " secret admirers from SharedPreferences");
        return admirers;
    }

    /**
     * Saves the list of secret admirers
     */
    public void saveSecretAdmirersList(ArrayList<Follower> admirers) {
        if (admirers == null) {
            Log.e(TAG, "Attempting to save null secret admirers list");
            return;
        }

        String jsonAdmirers = gson.toJson(admirers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SECRET_ADMIRERS, jsonAdmirers);
        editor.apply();

        Log.d(TAG, "Saved " + admirers.size() + " secret admirers to SharedPreferences");
    }

    /**
     * Generates and saves a list of blockers
     * For non-PRO users, this will still generate data (but limited in the UI)
     */
    public void generateBlockers() {
        // For all users, select from users who don't follow you (not followers)
        ArrayList<Follower> notFollowingBack = getNotFollowingBackList();
        ArrayList<Follower> blockers = new ArrayList<>();

        if (!notFollowingBack.isEmpty()) {
            // Use users who don't follow back for blockers list
            int blockerCount = Math.min(notFollowingBack.size(), 5); // Select up to 5 users

            // Use HashSet to keep track of indices that were already selected
            Set<Integer> selectedIndices = new HashSet<>();
            Random random = new Random();

            while (blockers.size() < blockerCount && selectedIndices.size() < notFollowingBack.size()) {
                int index = random.nextInt(notFollowingBack.size());
                if (selectedIndices.add(index)) { // Add returns true if the element was added
                    blockers.add(notFollowingBack.get(index));
                }
            }
        } else {
            // Fallback to lost followers if available
            ArrayList<Follower> lostFollowers = getLostFollowersList();
            if (!lostFollowers.isEmpty()) {
                int blockerCount = Math.min(lostFollowers.size(), 5); // Select up to 5 users
                for (int i = 0; i < blockerCount; i++) {
                    blockers.add(lostFollowers.get(i));
                }
            } else {
                // Fallback to random following users if no better option
                ArrayList<Follower> following = getFollowingList();
                if (!following.isEmpty()) {
                    int blockerCount = Math.min(following.size(), 3); // Select up to 3 users

                    Set<Integer> selectedIndices = new HashSet<>();
                    Random random = new Random();

                    while (blockers.size() < blockerCount && selectedIndices.size() < following.size()) {
                        int index = random.nextInt(following.size());
                        if (selectedIndices.add(index)) {
                            blockers.add(following.get(index));
                        }
                    }
                }
            }
        }

        String jsonBlockers = gson.toJson(blockers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BLOCKERS, jsonBlockers);
        editor.apply();

        Log.d(TAG, "Generated and saved " + blockers.size() + " blockers from non-followers");
    }

    /**
     * Retrieves the list of blockers
     * For all users, returns the list but access to details beyond the count is controlled in UI
     */
    public ArrayList<Follower> getBlockersList() {
        String jsonBlockers = sharedPreferences.getString(KEY_BLOCKERS, "");
        if (jsonBlockers.isEmpty()) {
            // Generate blockers if they don't exist
            generateBlockers();
            jsonBlockers = sharedPreferences.getString(KEY_BLOCKERS, "");
            if (jsonBlockers.isEmpty()) {
                return new ArrayList<>();
            }
        }

        Type type = new TypeToken<ArrayList<Follower>>(){}.getType();
        ArrayList<Follower> blockers = gson.fromJson(jsonBlockers, type);
        Log.d(TAG, "Retrieved " + blockers.size() + " blockers from SharedPreferences");
        return blockers;
    }

    /**
     * Saves the list of blockers
     */
    public void saveBlockersList(ArrayList<Follower> blockers) {
        if (blockers == null) {
            Log.e(TAG, "Attempting to save null blockers list");
            return;
        }

        String jsonBlockers = gson.toJson(blockers);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BLOCKERS, jsonBlockers);
        editor.apply();

        Log.d(TAG, "Saved " + blockers.size() + " blockers to SharedPreferences");
    }

    /**
     * Gets the timestamp of when the data was last updated
     */
    public long getLastUpdatedTimestamp() {
        return sharedPreferences.getLong(KEY_LAST_UPDATED, 0);
    }

    /**
     * Clears all saved data
     */
    public void clearAllData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "Cleared all data from SharedPreferences");
    }
}