package com.example.instagramfollowerextractor;

import java.io.Serializable;

public class ProfileInfo implements Serializable {
    private String username;
    private String profilePicURL;
    private int postsCount;
    private int followersCount;
    private int followingCount;

    public ProfileInfo() {
        // Boş constructor
    }

    public ProfileInfo(String username, String profilePicURL, int postsCount, int followersCount, int followingCount) {
        this.username = username;
        this.profilePicURL = profilePicURL;
        this.postsCount = postsCount;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}