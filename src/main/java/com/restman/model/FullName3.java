package com.restman.model;

public class FullName3 {
    private String firstName;
    private String lastName;

    public FullName3() {
    }

    public FullName3(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim(); }
}
