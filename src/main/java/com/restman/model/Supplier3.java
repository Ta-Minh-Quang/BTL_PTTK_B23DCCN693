package com.restman.model;

public class Supplier3 {
    private int id;
    private String supplierName;
    private String phone;
    private String email;
    private String address;

    public Supplier3() {
    }

    public Supplier3(int id, String supplierName, String phone, String email, String address) {
        this.id = id; this.supplierName = supplierName; this.phone = phone; this.email = email; this.address = address;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
