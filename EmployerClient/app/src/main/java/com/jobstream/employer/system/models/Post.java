package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Post {
    DocumentReference employer;
    String title,
            description,
            location,
            imageUrl,
            type,
            status;
    Double salary;
    Timestamp timestamp;
    List<String> keyword;

    public Post() {
    }

    public Post(DocumentReference employer, String title, String description, String location, Double salary, String imageUrl, String type, String status, Timestamp timestamp, List<String> keyword) {
        this.employer = employer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.imageUrl = imageUrl;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
        this.keyword = keyword;
    }

    public Post(DocumentReference employer, String title, String description, String location, Double salary, String type, String status, Timestamp timestamp, List<String> keyword) {
        this.employer = employer;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
        this.keyword = keyword;
    }

    public DocumentReference getEmployer() {
        return employer;
    }

    public void setEmployer(DocumentReference employer) {
        this.employer = employer;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }
}
