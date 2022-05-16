package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;

public class ApplicationDocument {
    String name,
            type,
            documentUrl;
    Timestamp timestamp;

    public ApplicationDocument() {
    }

    public ApplicationDocument(String name, String type, String documentUrl, Timestamp timestamp) {
        this.name = name;
        this.type = type;
        this.documentUrl = documentUrl;
        this.timestamp = timestamp;
    }

    public String getDocumentName() {
        return name;
    }

    public void setDocumentName(String name) {
        this.name = name;
    }

    public String getDocumentType() {
        return type;
    }

    public void setDocumentType(String type) {
        this.type = type;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
