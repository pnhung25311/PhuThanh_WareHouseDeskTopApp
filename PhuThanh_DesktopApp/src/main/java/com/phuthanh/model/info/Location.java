package com.phuthanh.model.info;

public class Location {
    private int LocationID;
    private String NameLocation;

    public Location(int locationID, String nameLocation) {
        LocationID = locationID;
        NameLocation = nameLocation;
    }

    public int getLocationID() {
        return LocationID;
    }

    public void setLocationID(int locationID) {
        LocationID = locationID;
    }

    public String getNameLocation() {
        return NameLocation;
    }

    public void setNameLocation(String nameLocation) {
        NameLocation = nameLocation;
    }

    
}
