package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Conversation {
    List<DocumentReference> participant;
    Timestamp timestamp;

    public Conversation() {
    }

    public Conversation(List<DocumentReference> participant, Timestamp timestamp) {
        this.participant = participant;
        this.timestamp = timestamp;
    }

    public List<DocumentReference> getParticipant() {
        return participant;
    }

    public void setParticipant(List<DocumentReference> participant) {
        this.participant = participant;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
