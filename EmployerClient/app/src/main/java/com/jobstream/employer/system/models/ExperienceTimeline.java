package com.jobstream.employer.system.models;

import java.util.Map;

public class ExperienceTimeline {
    String time, description;

    public ExperienceTimeline() {
    }

    public ExperienceTimeline(Map<String, Object> map) {
        this.time = (String) map.get("time");
        this.description = (String) map.get("description");
    }

    public ExperienceTimeline(String time, String description) {
        this.time = time;
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
