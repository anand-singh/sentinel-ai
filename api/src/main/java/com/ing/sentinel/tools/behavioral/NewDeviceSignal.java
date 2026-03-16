package com.ing.sentinel.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * New Device Signal (Behavioral)
 * 
 * Detects if the device fingerprint is new/unknown for this customer.
 */
public class NewDeviceSignal {

    private static final Logger logger = Logger.getLogger(NewDeviceSignal.class.getName());

    /**
     * Analyzes if the device fingerprint is new for this customer.
     * 
     * @param deviceFingerprint Current transaction device fingerprint
     * @param usualDevices Comma-separated list of customer's usual device fingerprints
     * @return Analysis result with novelty status and flag
     */
    @Schema(name = "analyze_new_device", description = "Analyzes if the device fingerprint is new/unknown for this customer. Returns whether NEW_DEVICE flag should be raised.")
    public static Map<String, Object> analyzeNewDevice(
            @Schema(name = "device_fingerprint", description = "Current transaction device fingerprint/hash") String deviceFingerprint,
            @Schema(name = "usual_devices", description = "Comma-separated list of customer's known device fingerprints") String usualDevices) {
        
        logger.info("🔍 Analyzing device novelty: device=" + maskFingerprint(deviceFingerprint));
        
        Map<String, Object> result = new HashMap<>();
        
        // Parse usual devices
        List<String> knownDevices = usualDevices != null && !usualDevices.isEmpty() 
                ? Arrays.asList(usualDevices.split(","))
                : List.of();
        
        // Check if device is known
        boolean isKnownDevice = knownDevices.stream()
                .map(String::trim)
                .anyMatch(d -> d.equals(deviceFingerprint));
        
        boolean isNewDevice = !isKnownDevice;
        double normalizedSignal = isNewDevice ? 1.0 : 0.0;
        
        result.put("device_fingerprint_masked", maskFingerprint(deviceFingerprint));
        result.put("is_known_device", isKnownDevice);
        result.put("known_devices_count", knownDevices.size());
        result.put("new_device", isNewDevice ? 1 : 0);
        result.put("normalized_signal", normalizedSignal);
        result.put("flag", isNewDevice ? "NEW_DEVICE" : null);
        result.put("flag_raised", isNewDevice);
        result.put("reasoning", generateReasoning(isNewDevice, knownDevices.size()));
        
        logger.info("✅ Device analysis: isNew=" + isNewDevice);
        
        return result;
    }
    
    private static String maskFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.length() < 8) {
            return "****";
        }
        return fingerprint.substring(0, 4) + "****" + fingerprint.substring(fingerprint.length() - 4);
    }
    
    private static String generateReasoning(boolean isNew, int knownCount) {
        if (isNew) {
            return String.format("Device fingerprint not seen before (customer has %d known devices)", knownCount);
        } else {
            return "Device fingerprint matches customer's known devices";
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(NewDeviceSignal.class, "analyzeNewDevice");
    }
}

