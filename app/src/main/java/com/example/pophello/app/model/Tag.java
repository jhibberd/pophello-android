package com.example.pophello.app.model;

public class Tag {

    // used for indicating the absence of a tag
    public static final String NULL_ID = "null";

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
}
