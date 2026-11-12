package com.restman.model;

import java.math.BigDecimal;

public class OrderItem3 {
    private int id;
    private int orderId;
    private Integer dishId;
    private Integer comboId;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem3() {
    }

    public OrderItem3(int id, int orderId, Integer dishId, Integer comboId, int quantity, BigDecimal unitPrice) {
        this.id = id; this.orderId = orderId; this.dishId = dishId; this.comboId = comboId; this.quantity = quantity; this.unitPrice = unitPrice;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public Integer getDishId() { return dishId; }
    public void setDishId(Integer dishId) { this.dishId = dishId; }
    public Integer getComboId() { return comboId; }
    public void setComboId(Integer comboId) { this.comboId = comboId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
