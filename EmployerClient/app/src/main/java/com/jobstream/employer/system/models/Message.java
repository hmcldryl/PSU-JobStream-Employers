package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class Message {
    DocumentReference user;
    String message, type;
    Timestamp timestamp;

    public Message() {
    }

    public Message(DocumentReference user, String message, String type, Timestamp timestamp) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
