package com.jobstream.employer.system.models;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

public class User {
    String firstName,
            lastName,
            sex,
            birthDate,
            sidNumber,
            status,
            program,
            campus,
            bio,
            email,
            photoUrl,
            bannerUrl;
    Timestamp timestamp;
    List<String> keyword;
    List<Map<String, Object>> experienceTimeline;
    List<Map<String, Object>> skillList;

    public User() {
    }

    public User(String firstName, String lastName, String sex, String birthDate, String sidNumber, String status, String program, String campus, String email, List<String> keyword, Timestamp timestamp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.sidNumber = sidNumber;
        this.status = status;
        this.program = program;
        this.campus = campus;
        this.email = email;
        this.keyword = keyword;
        this.timestamp = timestamp;
    }

    public User(String firstName, String lastName, String sex, String birthDate, String sidNumber, String status, String program, String campus, String email, List<String> keyword, Timestamp timestamp, String photoUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.sidNumber = sidNumber;
        this.status = status;
        this.program = program;
        this.campus = campus;
        this.email = email;
        this.keyword = keyword;
        this.timestamp = timestamp;
        this.photoUrl = photoUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getSidNumber() {
        return sidNumber;
    }

    public void setSidNumber(String sidNumber) {
        this.sidNumber = sidNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public List<Map<String, Object>> getExperienceTimeline() {
        return experienceTimeline;
    }

    public void setExperienceTimeline(List<Map<String, Object>> experienceTimeline) {
        this.experienceTimeline = experienceTimeline;
    }

    public List<Map<String, Object>> getSkillList() {
        return skillList;
    }

    public void setSkillList(List<Map<String, Object>> skillList) {
        this.skillList = skillList;
    }
}
