package com.example.instagramfollowerextractor;

import java.io.Serializable;

public class Follower implements Serializable {
    private String username;
    private String displayName;
    private String profilePicURL;
    private boolean isFollowingYou;  // Kullanıcı sizi takip ediyor mu?

    public Follower(String username, String displayName, String profilePicURL) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicURL = profilePicURL;
        this.isFollowingYou = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }

    public boolean isFollowingYou() {
        return isFollowingYou;
    }

    public void setFollowingYou(boolean followingYou) {
        isFollowingYou = followingYou;
    }
}