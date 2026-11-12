package com.restman.model;

import java.time.LocalDate;

public class Customer3 {
    private int id;
    private int userId;
    private String customerCode;
    private FullName3 fullName;
    private LocalDate dateOfBirth;
    private Address3 address;
    private Integer membershipCardId;

    public Customer3() {
    }

    public Customer3(int id, int userId, FullName3 fullName, LocalDate dateOfBirth, Address3 address, Integer membershipCardId) {
        this.id = id; this.userId = userId; this.fullName = fullName; this.dateOfBirth = dateOfBirth;
        this.address = address; this.membershipCardId = membershipCardId;
    }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public FullName3 getFullName() { return fullName; }
    public void setFullName(FullName3 fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Address3 getAddress() { return address; }
    public void setAddress(Address3 address) { this.address = address; }
    public Integer getMembershipCardId() { return membershipCardId; }
    public void setMembershipCardId(Integer membershipCardId) { this.membershipCardId = membershipCardId; }
}
