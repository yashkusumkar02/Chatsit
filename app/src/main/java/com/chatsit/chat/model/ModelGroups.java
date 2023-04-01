package com.chatsit.chat.model;

public class ModelGroups {

    String role, id, timestamp, group;

    public ModelGroups() {

    }

    public ModelGroups(String role, String id, String timestamp, String group) {
        this.role = role;
        this.id = id;
        this.timestamp = timestamp;
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
