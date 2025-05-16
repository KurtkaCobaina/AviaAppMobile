package com.example.aviaappmobile;

class Airport {
    private int AirportID;
    private String Name;
    private String City;
    private String Country;
    private String Code;

    // Геттеры и сеттеры
    public int getAirportID() {
        return AirportID;
    }

    public void setAirportID(int airportID) {
        this.AirportID = airportID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        this.City = city;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        this.Country = country;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        this.Code = code;
    }
}