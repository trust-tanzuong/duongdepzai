package com.example.lab6.model;

import android.net.Uri;
import java.io.Serializable;

public class MediaModel implements Serializable {
    private long id;
    private String uriString;
    private String displayName;
    private String path;
    private long dateAdded;
    private boolean isVideo;
    private long duration;
    private long size;
    private boolean isSelected;

    public MediaModel(long id, Uri uri, String displayName, String path, long dateAdded, boolean isVideo, long duration, long size) {
        this.id = id;
        this.uriString = uri != null ? uri.toString() : "";
        this.displayName = displayName;
        this.path = path;
        this.dateAdded = dateAdded;
        this.isVideo = isVideo;
        this.duration = duration;
        this.size = size;
        this.isSelected = false;
    }

    public long getId() {
        return id;
    }

    public Uri getUri() {
        return uriString != null ? Uri.parse(uriString) : null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPath() {
        return path;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFormattedDuration() {
        if (!isVideo || duration <= 0) return "";
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60));
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}