package com.phuthanh.model.info;

public class Country {
    private int CountryID;
    private String Name;

    public Country(int countryID, String name) {
        CountryID = countryID;
        Name = name;
    }

    public int getCountryID() {
        return CountryID;
    }

    public void setCountryID(int countryID) {
        CountryID = countryID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return Name;
    }
}
