package com.restman.model;

public class Manager3 {
    private int id;
    private int userId;
    private String fullName;

    public Manager3() {
    }

    public Manager3(int id, int userId, String fullName) { this.id = id; this.userId = userId; this.fullName = fullName; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
