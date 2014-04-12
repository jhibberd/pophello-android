package com.example.pophello.app.model;

public class Tag {

    public final String id;
    public final String text;
    public final double latitude;
    public final double longitude;

    public Tag(String id, double latitude, double longitude, String text) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Tag && ((Tag) o).id.equals(id);
    }
}
