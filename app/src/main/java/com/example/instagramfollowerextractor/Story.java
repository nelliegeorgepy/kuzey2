package com.example.instagramfollowerextractor;

import android.os.Parcel;
import android.os.Parcelable;

// Serializable yerine Parcelable kullanarak veri aktarımını iyileştir
public class Story implements Parcelable {
    private String username;
    private String profileImageUrl;
    private String mediaUrl;
    private boolean isVideo;

    public Story(String username, String profileImageUrl, String mediaUrl, boolean isVideo) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.mediaUrl = mediaUrl;
        this.isVideo = isVideo;
    }

    // Parcel'dan okuma constructor'ı
    protected Story(Parcel in) {
        username = in.readString();
        profileImageUrl = in.readString();
        mediaUrl = in.readString();
        isVideo = in.readByte() != 0;
    }

    // Parcelable için CREATOR
    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(profileImageUrl);
        dest.writeString(mediaUrl);
        dest.writeByte((byte) (isVideo ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Story{" +
                "username='" + username + '\'' +
                ", profileImageUrl='" + (profileImageUrl != null ? profileImageUrl.substring(0, Math.min(30, profileImageUrl.length())) + "..." : "null") + '\'' +
                ", mediaUrl='" + (mediaUrl != null ? mediaUrl.substring(0, Math.min(30, mediaUrl.length())) + "..." : "null") + '\'' +
                ", isVideo=" + isVideo +
                '}';
    }
}