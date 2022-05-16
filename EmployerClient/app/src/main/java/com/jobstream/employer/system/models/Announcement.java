package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class Announcement {

    DocumentReference user;
    String type,
            title,
            description,
            imageUrl;
    Timestamp timestamp;

    public Announcement() {
    }

    public Announcement(DocumentReference user, String type, String title, String description, Timestamp timestamp) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Announcement(DocumentReference user, String type, String title, String description, Timestamp timestamp, String imageUrl) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
