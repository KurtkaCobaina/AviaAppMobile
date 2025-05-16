package com.example.aviaappmobile;



import java.util.Date;

public class Payment {
    private int PaymentID;
    private int BookingID;
    private int CardId;
    private Date PaymentDate;
    private String PaymentMethod;
    private String PaymentStatus;

    // Геттеры и сеттеры
    public int getPaymentID() {
        return PaymentID;
    }

    public void setPaymentID(int paymentID) {
        this.PaymentID = paymentID;
    }

    public int getBookingID() {
        return BookingID;
    }

    public void setBookingID(int bookingID) {
        this.BookingID = bookingID;
    }

    public int getCardId() {
        return CardId;
    }

    public void setCardId(int cardId) {
        this.CardId = cardId;
    }

    public Date getPaymentDate() {
        return PaymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.PaymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.PaymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return PaymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.PaymentStatus = paymentStatus;
    }
}