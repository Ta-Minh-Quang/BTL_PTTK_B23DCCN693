package com.restman.model;

import java.math.BigDecimal;

public class Ingredient3 {
    private int id;
    private int supplierId;
    private String ingredientName;
    private String unit;
    private BigDecimal stockQuantity;

    public Ingredient3() {
    }

    public Ingredient3(int id, int supplierId, String ingredientName, String unit, BigDecimal stockQuantity) {
        this.id = id; this.supplierId = supplierId; this.ingredientName = ingredientName; this.unit = unit; this.stockQuantity = stockQuantity;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(BigDecimal stockQuantity) { this.stockQuantity = stockQuantity; }
}
