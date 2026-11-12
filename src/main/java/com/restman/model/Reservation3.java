package com.restman.model;

import java.time.LocalDateTime;

public class Reservation3 {
    private int id;
    private int customerId;
    private int tableId;
    private LocalDateTime reservationTime;
    private int guestCount;
    private String status;

    public Reservation3() {
    }

    public Reservation3(int id, int customerId, int tableId, LocalDateTime reservationTime, int guestCount, String status) {
        this.id = id; this.customerId = customerId; this.tableId = tableId; this.reservationTime = reservationTime; this.guestCount = guestCount; this.status = status;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }
    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
