package com.example.pophello.app.model;

public class Tag {

    public final String id;
    public final double latitude;
    public final double longitude;
    public final String text;
    public final String userId;
    public final String userImageUrl;

    public Tag(
            String id, double latitude, double longitude, String text, String userId,
            String userImageUrl) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
        this.userId = userId;
        this.userImageUrl = userImageUrl;
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
