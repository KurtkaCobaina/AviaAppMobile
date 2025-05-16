package com.example.aviaappmobile;



import java.io.Serializable;
import java.util.Date;

 class Flight implements Serializable {
    private int FlightID;
    private String FlightNumber;
    private int DepartureAirportID;
    private int ArrivalAirportID;
    private Date DepartureTime;
    private Date ArrivalTime;
    private int Price;
    private int SeatsAvailable;

    // Геттеры и сеттеры
    public int getFlightID() {
        return FlightID;
    }

    public void setFlightID(int flightID) {
        this.FlightID = flightID;
    }

    public String getFlightNumber() {
        return FlightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.FlightNumber = flightNumber;
    }

    public int getDepartureAirportID() {
        return DepartureAirportID;
    }

    public void setDepartureAirportID(int departureAirportID) {
        this.DepartureAirportID = departureAirportID;
    }

    public int getArrivalAirportID() {
        return ArrivalAirportID;
    }

    public void setArrivalAirportID(int arrivalAirportID) {
        this.ArrivalAirportID = arrivalAirportID;
    }

    public Date getDepartureTime() {
        return DepartureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.DepartureTime = departureTime;
    }

    public Date getArrivalTime() {
        return ArrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.ArrivalTime = arrivalTime;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        this.Price = price;
    }

    public int getSeatsAvailable() {
        return SeatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.SeatsAvailable = seatsAvailable;
    }


}
