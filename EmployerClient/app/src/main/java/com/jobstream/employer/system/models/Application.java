package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Application {
    DocumentReference user, post;
    String intro;
    List<DocumentReference> document;
    Timestamp timestamp;

    public Application(DocumentReference user, DocumentReference post, String intro, List<DocumentReference> document, Timestamp timestamp) {
        this.user = user;
        this.post = post;
        this.intro = intro;
        this.document = document;
        this.timestamp = timestamp;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public DocumentReference getPost() {
        return post;
    }

    public void setPost(DocumentReference post) {
        this.post = post;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<DocumentReference> getDocument() {
        return document;
    }

    public void setDocument(List<DocumentReference> document) {
        this.document = document;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
