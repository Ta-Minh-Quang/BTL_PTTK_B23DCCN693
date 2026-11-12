package com.restman.model;

public class ComboItem3 {
    private int comboId;
    private int dishId;
    private int quantity;

    public ComboItem3() {
    }

    public ComboItem3(int comboId, int dishId, int quantity) { this.comboId = comboId; this.dishId = dishId; this.quantity = quantity; }
    public int getComboId() { return comboId; }
    public void setComboId(int comboId) { this.comboId = comboId; }
    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
