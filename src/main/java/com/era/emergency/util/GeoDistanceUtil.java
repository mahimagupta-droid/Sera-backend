package com.era.emergency.util;

import org.springframework.stereotype.Component;

/**
 * Utility for geographic distance calculations using the Haversine formula.
 */
@Component
public class GeoDistanceUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate straight-line distance between two lat/lng points in kilometres.
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Estimate ETA in minutes given distance and average ambulance speed.
     * Urban average: 40 km/h.  Rural average: 70 km/h.
     * Uses 50 km/h as a mixed default plus 5 min dispatch overhead.
     */
    public int estimateEtaMinutes(double distanceKm) {
        double avgSpeedKmh = 50.0;
        double travelTimeHours = distanceKm / avgSpeedKmh;
        int travelMinutes = (int) Math.ceil(travelTimeHours * 60);
        return travelMinutes + 5; // +5 min dispatch overhead
    }

    /**
     * Normalise a distance value to a 0–1 score (closer = higher score).
     *
     * @param distanceKm   actual distance
     * @param maxDistanceKm maximum allowable distance
     */
    public double normaliseDistanceScore(double distanceKm, double maxDistanceKm) {
        if (distanceKm >= maxDistanceKm) return 0.0;
        return 1.0 - (distanceKm / maxDistanceKm);
    }
}
