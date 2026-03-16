package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Geo Distance Signal Tool
 * 
 * Analyzes geographic distance between current and last known location
 * to detect impossible travel patterns (e.g., transactions in different
 * countries within minutes).
 */
public class GeoDistanceTool {

    private static final Logger logger = Logger.getLogger(GeoDistanceTool.class.getName());
    
    // Earth radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    // Maximum plausible travel speed (km/h) - commercial jet speed
    private static final double MAX_TRAVEL_SPEED_KMH = 900.0;
    
    // Distance threshold for flagging (km)
    private static final double GEO_MISMATCH_THRESHOLD_KM = 100.0;

    /**
     * Analyzes geographic distance and travel feasibility between transactions.
     * 
     * @param currentLat Current transaction latitude
     * @param currentLon Current transaction longitude
     * @param lastKnownLat Last known location latitude
     * @param lastKnownLon Last known location longitude
     * @param currentTimestamp Current transaction timestamp (ISO 8601)
     * @param lastTxTimestamp Last transaction timestamp (ISO 8601)
     * @return Analysis result with distance, travel feasibility, and flag
     */
    @Schema(name = "analyze_geo_distance", description = "Analyzes geographic distance between current transaction and last known location to detect impossible travel patterns. Returns distance in km, required travel speed, and whether GEO_MISMATCH flag should be raised.")
    public static Map<String, Object> analyzeGeoDistance(
            @Schema(name = "current_lat", description = "Current transaction latitude") double currentLat,
            @Schema(name = "current_lon", description = "Current transaction longitude") double currentLon,
            @Schema(name = "last_known_lat", description = "Last known location latitude") double lastKnownLat,
            @Schema(name = "last_known_lon", description = "Last known location longitude") double lastKnownLon,
            @Schema(name = "current_timestamp", description = "Current transaction timestamp in ISO 8601 format") String currentTimestamp,
            @Schema(name = "last_tx_timestamp", description = "Last transaction timestamp in ISO 8601 format") String lastTxTimestamp) {
        
        logger.info("🔍 Analyzing geo distance: current=(" + currentLat + "," + currentLon + 
                "), last=(" + lastKnownLat + "," + lastKnownLon + ")");
        
        Map<String, Object> result = new HashMap<>();
        
        // Calculate Haversine distance
        double distanceKm = haversineDistance(currentLat, currentLon, lastKnownLat, lastKnownLon);
        
        // Calculate time difference
        Instant current = Instant.parse(currentTimestamp);
        Instant last = Instant.parse(lastTxTimestamp);
        Duration timeDiff = Duration.between(last, current);
        double hoursElapsed = timeDiff.toMinutes() / 60.0;
        
        // Calculate required travel speed
        double requiredSpeedKmh = hoursElapsed > 0 ? distanceKm / hoursElapsed : Double.MAX_VALUE;
        
        // Determine if travel is physically possible
        boolean impossibleTravel = requiredSpeedKmh > MAX_TRAVEL_SPEED_KMH && distanceKm > GEO_MISMATCH_THRESHOLD_KM;
        
        // Normalize to [0,1] based on distance and travel feasibility
        double normalizedSignal = calculateNormalizedSignal(distanceKm, requiredSpeedKmh);
        
        result.put("distance_km", Math.round(distanceKm * 10.0) / 10.0);
        result.put("time_elapsed_minutes", timeDiff.toMinutes());
        result.put("required_speed_kmh", Math.round(requiredSpeedKmh * 10.0) / 10.0);
        result.put("max_plausible_speed_kmh", MAX_TRAVEL_SPEED_KMH);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", impossibleTravel ? "GEO_MISMATCH" : null);
        result.put("flag_raised", impossibleTravel);
        result.put("reasoning", generateReasoning(distanceKm, timeDiff.toMinutes(), requiredSpeedKmh, impossibleTravel));
        
        logger.info("✅ Geo analysis: distance=" + distanceKm + "km, speed=" + requiredSpeedKmh + "km/h, flag=" + impossibleTravel);
        
        return result;
    }
    
    /**
     * Calculate Haversine distance between two points
     */
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Calculate normalized signal [0,1] based on distance and speed
     */
    private static double calculateNormalizedSignal(double distanceKm, double requiredSpeedKmh) {
        // Combine distance and speed factors
        double distanceFactor = Math.min(1.0, distanceKm / 1000.0); // Normalize by 1000km
        double speedFactor = Math.min(1.0, requiredSpeedKmh / MAX_TRAVEL_SPEED_KMH);
        
        return (distanceFactor + speedFactor) / 2.0;
    }
    
    /**
     * Generate human-readable reasoning
     */
    private static String generateReasoning(double distanceKm, long minutes, double speedKmh, boolean flagRaised) {
        if (flagRaised) {
            return String.format("Impossible travel detected: %.1fkm in %d minutes requires %.1fkm/h (max plausible: %.0fkm/h)", 
                    distanceKm, minutes, speedKmh, MAX_TRAVEL_SPEED_KMH);
        } else if (distanceKm > 50) {
            return String.format("Travel %.1fkm in %d minutes (%.1fkm/h) is plausible", 
                    distanceKm, minutes, speedKmh);
        } else {
            return String.format("Location consistent: %.1fkm from last known position", distanceKm);
        }
    }

    /**
     * Create FunctionTool wrapper for ADK
     */
    public static FunctionTool create() {
        return FunctionTool.create(GeoDistanceTool.class, "analyzeGeoDistance");
    }
}

