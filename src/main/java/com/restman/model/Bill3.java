package com.restman.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bill3 {
    private int id;
    private int foodOrderId;
    private int salesStaffId;
    private String customerName;
    private Integer customerId;
    private Integer membershipCardId;
    private LocalDateTime billDate;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String paymentMethod;
    private BigDecimal subtotal;

    public Bill3() {
    }

    public Bill3(int id, int orderId, BigDecimal subtotal, BigDecimal discount, BigDecimal total, LocalDateTime issuedAt) {
        this.id = id;
        this.foodOrderId = orderId;
        this.subtotal = subtotal;
        this.discountAmount = discount;
        this.totalAmount = total;
        this.billDate = issuedAt;
    }

    public Bill3(int id, int foodOrderId, int salesStaffId, LocalDateTime billDate,
                 String customerName, Integer customerId, Integer membershipCardId,
                 BigDecimal discountAmount, BigDecimal totalAmount, String paymentMethod) {
        this.id = id;
        this.foodOrderId = foodOrderId;
        this.salesStaffId = salesStaffId;
        this.billDate = billDate;
        this.customerName = customerName;
        this.customerId = customerId;
        this.membershipCardId = membershipCardId;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFoodOrderId() { return foodOrderId; }
    public void setFoodOrderId(int foodOrderId) { this.foodOrderId = foodOrderId; }
    public int getSalesStaffId() { return salesStaffId; }
    public void setSalesStaffId(int salesStaffId) { this.salesStaffId = salesStaffId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getMembershipCardId() { return membershipCardId; }
    public void setMembershipCardId(Integer membershipCardId) { this.membershipCardId = membershipCardId; }
    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public int getOrderId() { return foodOrderId; }
    public void setOrderId(int orderId) { this.foodOrderId = orderId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscount() { return discountAmount; }
    public void setDiscount(BigDecimal discount) { this.discountAmount = discount; }
    public BigDecimal getTotal() { return totalAmount; }
    public void setTotal(BigDecimal total) { this.totalAmount = total; }
    public LocalDateTime getIssuedAt() { return billDate; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.billDate = issuedAt; }
}
