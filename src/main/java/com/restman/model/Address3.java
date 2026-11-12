package com.restman.model;

public class Address3 {
    private String street;
    private String ward;
    private String district;
    private String city;

    public Address3() {
    }

    public Address3(String street, String ward, String district, String city) {
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getFullAddress() {
        return String.join(", ", java.util.Arrays.stream(new String[]{street, ward, district, city})
                .filter(value -> value != null && !value.isBlank()).toList());
    }
}
