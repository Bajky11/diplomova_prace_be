package com.friends.friends.Exception.Location;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException() {
        super("Location not found");
    }
}
