package com.restman.model;

import java.math.BigDecimal;

public class Dish3 {
    private int id;
    private String dishName;
    private BigDecimal price;
    private String description;
    private String category;
    private String imageUrl;
    private boolean available;

    public Dish3() {
    }

    public Dish3(int id, String dishName, BigDecimal price, String description, String category, String imageUrl, boolean available) {
        this.id = id; this.dishName = dishName; this.price = price; this.description = description;
        this.category = category; this.imageUrl = imageUrl; this.available = available;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    @Override public String toString() { return dishName + " - " + price + " VND"; }
}
