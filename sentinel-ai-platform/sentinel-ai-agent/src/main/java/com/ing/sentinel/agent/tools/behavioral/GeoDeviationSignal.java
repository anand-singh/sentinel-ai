package com.ing.sentinel.agent.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Geo Deviation Signal (Behavioral)
 * 
 * Checks distance and travel speed from customer's last known location.
 * Detects impossible travel based on customer's recent transaction history.
 */
public class GeoDeviationSignal {

    private static final Logger logger = Logger.getLogger(GeoDeviationSignal.class.getName());
    
    // Earth radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    // Maximum plausible travel speed (km/h) - commercial jet speed
    private static final double MAX_TRAVEL_SPEED_KMH = 900.0;
    
    // Distance threshold for flagging (km)
    private static final double GEO_DEVIATION_THRESHOLD_KM = 100.0;

    /**
     * Analyzes geographic deviation from customer's last known location.
     * 
     * @param currentLat Current transaction latitude
     * @param currentLon Current transaction longitude
     * @param lastKnownLat Customer's last known location latitude
     * @param lastKnownLon Customer's last known location longitude
     * @param currentTimestamp Current transaction timestamp (ISO 8601)
     * @param lastLocationTimestamp Customer's last location timestamp (ISO 8601)
     * @param homeCountries Comma-separated list of customer's home countries
     * @param currentCountry Current transaction country
     * @return Analysis result with distance, travel feasibility, and flag
     */
    @Schema(name = "analyze_geo_deviation", description = "Analyzes geographic distance from customer's last known location to detect impossible travel patterns. Returns distance, implied speed, and whether GEO_DEVIATION flag should be raised.")
    public static Map<String, Object> analyzeGeoDeviation(
            @Schema(name = "current_lat", description = "Current transaction latitude") double currentLat,
            @Schema(name = "current_lon", description = "Current transaction longitude") double currentLon,
            @Schema(name = "last_known_lat", description = "Customer's last known location latitude") double lastKnownLat,
            @Schema(name = "last_known_lon", description = "Customer's last known location longitude") double lastKnownLon,
            @Schema(name = "current_timestamp", description = "Current transaction timestamp in ISO 8601 format") String currentTimestamp,
            @Schema(name = "last_location_timestamp", description = "Customer's last location timestamp in ISO 8601 format") String lastLocationTimestamp,
            @Schema(name = "home_countries", description = "Comma-separated list of customer's home countries (e.g., 'NL,BE')") String homeCountries,
            @Schema(name = "current_country", description = "Current transaction country code") String currentCountry) {
        
        logger.info("🔍 Analyzing geo deviation from customer's last location");
        
        Map<String, Object> result = new HashMap<>();
        
        // Calculate Haversine distance
        double distanceKm = haversineDistance(currentLat, currentLon, lastKnownLat, lastKnownLon);
        
        // Calculate time difference
        Instant current = Instant.parse(currentTimestamp);
        Instant last = Instant.parse(lastLocationTimestamp);
        Duration timeDiff = Duration.between(last, current);
        double hoursElapsed = timeDiff.toMinutes() / 60.0;
        
        // Calculate required travel speed
        double requiredSpeedKmh = hoursElapsed > 0 ? distanceKm / hoursElapsed : Double.MAX_VALUE;
        
        // Check if country is a home country
        boolean isHomeCountry = homeCountries != null && 
                homeCountries.toUpperCase().contains(currentCountry.toUpperCase());
        
        // Determine if travel is physically impossible
        boolean impossibleTravel = requiredSpeedKmh > MAX_TRAVEL_SPEED_KMH && distanceKm > GEO_DEVIATION_THRESHOLD_KM;
        
        // Flag if impossible travel OR significant distance from non-home country
        boolean flagRaised = impossibleTravel || (distanceKm > 500 && !isHomeCountry);
        
        // Normalize to [0,1] based on distance and travel feasibility
        double normalizedSignal = calculateNormalizedSignal(distanceKm, requiredSpeedKmh, isHomeCountry);
        
        result.put("geo_distance_km", Math.round(distanceKm * 10.0) / 10.0);
        result.put("time_elapsed_minutes", timeDiff.toMinutes());
        result.put("required_speed_kmh", Math.round(requiredSpeedKmh * 10.0) / 10.0);
        result.put("impossible_travel", impossibleTravel);
        result.put("is_home_country", isHomeCountry);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", flagRaised ? "GEO_DEVIATION" : null);
        result.put("flag_raised", flagRaised);
        result.put("reasoning", generateReasoning(distanceKm, timeDiff.toMinutes(), requiredSpeedKmh, 
                impossibleTravel, isHomeCountry, currentCountry));
        
        logger.info("✅ Geo deviation: distance=" + distanceKm + "km, impossible=" + impossibleTravel);
        
        return result;
    }
    
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    private static double calculateNormalizedSignal(double distanceKm, double speedKmh, boolean isHomeCountry) {
        double signal = 0.0;
        
        // Distance factor
        signal += Math.min(1.0, distanceKm / 1000.0) * 0.4;
        
        // Speed factor (impossible travel)
        if (speedKmh > MAX_TRAVEL_SPEED_KMH) {
            signal += 0.4;
        } else {
            signal += (speedKmh / MAX_TRAVEL_SPEED_KMH) * 0.2;
        }
        
        // Non-home country factor
        if (!isHomeCountry && distanceKm > 100) {
            signal += 0.2;
        }
        
        return Math.min(1.0, signal);
    }
    
    private static String generateReasoning(double distanceKm, long minutes, double speedKmh, 
            boolean impossible, boolean isHome, String country) {
        if (impossible) {
            return String.format("Impossible travel: %.1fkm in %d minutes requires %.1fkm/h", 
                    distanceKm, minutes, speedKmh);
        } else if (!isHome && distanceKm > 500) {
            return String.format("Transaction %.1fkm away in non-home country %s", distanceKm, country);
        } else if (distanceKm > 100) {
            return String.format("Traveled %.1fkm since last transaction %d minutes ago (%.1fkm/h)", 
                    distanceKm, minutes, speedKmh);
        } else {
            return String.format("Location consistent: %.1fkm from last known position", distanceKm);
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(GeoDeviationSignal.class, "analyzeGeoDeviation");
    }
}

