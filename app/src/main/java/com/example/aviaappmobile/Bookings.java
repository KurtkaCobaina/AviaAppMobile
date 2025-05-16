package com.example.aviaappmobile;


import java.util.Date;

class Booking {
    private int BookingID;
    private int UserID;
    private int FlightID;
    private Date BookingDate;
    private String Status;
    private Integer Amount; // Может быть null

    // Геттеры и сеттеры
    public int getBookingID() {
        return BookingID;
    }

    public void setBookingID(int bookingID) {
        this.BookingID = bookingID;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        this.UserID = userID;
    }

    public int getFlightID() {
        return FlightID;
    }

    public void setFlightID(int flightID) {
        this.FlightID = flightID;
    }

    public Date getBookingDate() {
        return BookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.BookingDate = bookingDate;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public Integer getAmount() {
        return Amount;
    }

    public void setAmount(Integer amount) {
        this.Amount = amount;
    }
}