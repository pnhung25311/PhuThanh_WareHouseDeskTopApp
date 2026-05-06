package com.phuthanh.model.info;

public class Segment {
    private int SegmentID;
    private String Name;

    public Segment(int segmentID, String name) {
        SegmentID = segmentID;
        Name = name;
    }

    public int getSegmentID() {
        return SegmentID;
    }

    public void setSegmentID(int segmentID) {
        SegmentID = segmentID;
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
