package com.restman.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Combo3 {
    private int id;
    private String comboName;
    private BigDecimal price;
    private String description;
    private List<Dish3> dishes = new ArrayList<>();

    public Combo3() {
    }

    public Combo3(int id, String comboName, BigDecimal price, String description, List<Dish3> dishes) {
        this.id = id; this.comboName = comboName; this.price = price; this.description = description;
        this.dishes = dishes == null ? new ArrayList<>() : new ArrayList<>(dishes);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getComboName() { return comboName; }
    public void setComboName(String comboName) { this.comboName = comboName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Dish3> getDishes() { return new ArrayList<>(dishes); }
    public void setDishes(List<Dish3> dishes) { this.dishes = dishes == null ? new ArrayList<>() : new ArrayList<>(dishes); }
}
