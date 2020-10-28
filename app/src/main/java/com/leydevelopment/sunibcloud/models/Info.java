package com.leydevelopment.sunibcloud.models;

import java.io.Serializable;

public class Info implements Serializable {
    private String title;
    private String description;
    private String time;
    private String name;
    private String position;

    public Info() {
        //empty constructor need
    }

    public Info(String title, String description, String time) {
        this.title = title;
        this.description = description;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }
}
