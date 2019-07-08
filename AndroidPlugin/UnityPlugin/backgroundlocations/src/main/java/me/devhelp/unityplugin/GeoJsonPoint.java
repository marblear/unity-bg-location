package me.devhelp.unityplugin;

import android.location.Location;

public class GeoJsonPoint {
    private final float EARTH_RADIUS = 6372.8f;
    public String type = "Point";
    public double[] coordinates = {0.0, 0.0};
    public double getLongitude() {
        return coordinates[0];
    }
    public double getLatitude() {
        return coordinates[1];
    }
    private double haversin(double value) {
        return Math.pow(Math.sin(value / 2), 2);
    }
    public double haversineDistance(Location from) {
        double dLat = Math.toRadians((getLatitude() - from.getLatitude()));
        double dLon = Math.toRadians((getLongitude() - from.getLongitude()));
        double ownLat = Math.toRadians(getLatitude());
        double fromLat = Math.toRadians(from.getLatitude());
        double a = haversin(dLat) + Math.cos(ownLat) * Math.cos(fromLat) * haversin(dLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;
        return distance;
    }

}
