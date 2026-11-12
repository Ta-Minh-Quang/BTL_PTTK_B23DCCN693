package com.restman.model;

import java.math.BigDecimal;

public class DishIngredient3 {
    private int dishId;
    private int ingredientId;
    private BigDecimal quantity;

    public DishIngredient3() {
    }

    public DishIngredient3(int dishId, int ingredientId, BigDecimal quantity) { this.dishId = dishId; this.ingredientId = ingredientId; this.quantity = quantity; }
    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
