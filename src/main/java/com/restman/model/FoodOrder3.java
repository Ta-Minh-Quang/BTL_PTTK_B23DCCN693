package com.restman.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FoodOrder3 {
    private int id;
    private int customerId;
    private int salesStaffId;
    private Integer tableId;
    private LocalDateTime orderDate;
    private String orderStatus;
    private BigDecimal totalAmount;
    private String notes;

    public FoodOrder3() {
    }

    public FoodOrder3(int id, int customerId, int salesStaffId, LocalDateTime orderedAt, String status) {
        this.id = id;
        this.customerId = customerId;
        this.salesStaffId = salesStaffId;
        this.orderDate = orderedAt;
        this.orderStatus = status;
    }

    public FoodOrder3(int id, int customerId, int salesStaffId, Integer tableId, LocalDateTime orderDate,
                     String orderStatus, BigDecimal totalAmount, String notes) {
        this.id = id;
        this.customerId = customerId;
        this.salesStaffId = salesStaffId;
        this.tableId = tableId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.notes = notes;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getSalesStaffId() { return salesStaffId; }
    public void setSalesStaffId(int salesStaffId) { this.salesStaffId = salesStaffId; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getOrderedAt() { return orderDate; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderDate = orderedAt; }
    public String getStatus() { return orderStatus; }
    public void setStatus(String status) { this.orderStatus = status; }
}
