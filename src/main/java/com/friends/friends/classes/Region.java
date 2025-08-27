package com.friends.friends.classes;

public class Region {
    private final String name;
    private final double latitude;
    private final double longitude;
    private final int radiusKm;

    public Region(String name, double latitude, double longitude, int radiusKm) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadiusKm() {
        return radiusKm;
    }
}
