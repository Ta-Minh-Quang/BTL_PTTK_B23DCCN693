package com.restman.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MembershipCard3 {
    private int id;
    private String cardNumber;
    private int points;
    private LocalDateTime issueDate;
    private BigDecimal discountRate;
    private String cardTier;

    public MembershipCard3() {
    }

    public MembershipCard3(int id, String cardNumber, int points, LocalDate issuedAt, BigDecimal discountRate) {
        this(id, cardNumber, points, issuedAt == null ? null : issuedAt.atStartOfDay(), discountRate);
    }

    public MembershipCard3(int id, String cardNumber, int points, LocalDateTime issueDate, BigDecimal discountRate) {
        this.id = id; this.cardNumber = cardNumber; this.points = points; this.issueDate = issueDate;
        this.discountRate = discountRate;
    }
    public MembershipCard3(int id, String cardNumber, int points, LocalDate issuedAt,
                           BigDecimal discountRate, String cardTier) {
        this(id, cardNumber, points, issuedAt, discountRate);
        this.cardTier = cardTier;
    }
    public MembershipCard3(int id, String cardNumber, int points, LocalDateTime issueDate,
                           BigDecimal discountRate, String cardTier) {
        this(id, cardNumber, points, issueDate, discountRate);
        this.cardTier = cardTier;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public LocalDate getIssuedAt() { return issueDate == null ? null : issueDate.toLocalDate(); }
    public void setIssuedAt(LocalDate issuedAt) { this.issueDate = issuedAt == null ? null : issuedAt.atStartOfDay(); }
    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    public String getCardTier() { return cardTier; }
    public void setCardTier(String cardTier) { this.cardTier = cardTier; }
}
