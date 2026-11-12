package com.restman.model;

public class InventoryStaff3 {
    private int id;
    private int userId;
    private String fullName;

    public InventoryStaff3() {
    }

    public InventoryStaff3(int id, int userId, String fullName) { this.id = id; this.userId = userId; this.fullName = fullName; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
