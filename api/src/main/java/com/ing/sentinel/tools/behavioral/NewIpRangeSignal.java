package com.ing.sentinel.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * New IP Range Signal (Behavioral)
 * 
 * Detects if the IP address is from an unusual CIDR range for this customer.
 */
public class NewIpRangeSignal {

    private static final Logger logger = Logger.getLogger(NewIpRangeSignal.class.getName());

    /**
     * Analyzes if the IP address is from a new/unusual range for this customer.
     * 
     * @param ipAddress Current transaction IP address
     * @param usualIpRanges Comma-separated list of customer's usual IP CIDR ranges
     * @return Analysis result with IP novelty status and flag
     */
    @Schema(name = "analyze_new_ip_range", description = "Analyzes if the IP address is from an unusual CIDR range for this customer. Returns whether NEW_IP_RANGE flag should be raised.")
    public static Map<String, Object> analyzeNewIpRange(
            @Schema(name = "ip_address", description = "Current transaction IP address") String ipAddress,
            @Schema(name = "usual_ip_ranges", description = "Comma-separated list of customer's usual IP CIDR ranges (e.g., '203.0.113.0/24,192.168.1.0/24')") String usualIpRanges) {
        
        logger.info("🔍 Analyzing IP range novelty: ip=" + maskIp(ipAddress));
        
        Map<String, Object> result = new HashMap<>();
        
        // Parse usual IP ranges
        List<String> knownRanges = usualIpRanges != null && !usualIpRanges.isEmpty()
                ? Arrays.asList(usualIpRanges.split(","))
                : List.of();
        
        // Check if IP is in any known range
        boolean isInKnownRange = knownRanges.stream()
                .map(String::trim)
                .anyMatch(cidr -> isIpInCidr(ipAddress, cidr));
        
        boolean isNewIpRange = !isInKnownRange;
        double normalizedSignal = isNewIpRange ? 1.0 : 0.0;
        
        result.put("ip_masked", maskIp(ipAddress));
        result.put("is_in_known_range", isInKnownRange);
        result.put("known_ranges_count", knownRanges.size());
        result.put("new_ip_range", isNewIpRange ? 1 : 0);
        result.put("normalized_signal", normalizedSignal);
        result.put("flag", isNewIpRange ? "NEW_IP_RANGE" : null);
        result.put("flag_raised", isNewIpRange);
        result.put("reasoning", generateReasoning(isNewIpRange, knownRanges.size()));
        
        logger.info("✅ IP range analysis: isNew=" + isNewIpRange);
        
        return result;
    }
    
    /**
     * Check if an IP address falls within a CIDR range.
     * Simplified implementation for common cases.
     */
    private static boolean isIpInCidr(String ip, String cidr) {
        try {
            String[] cidrParts = cidr.split("/");
            if (cidrParts.length != 2) {
                return ip.equals(cidr); // Exact match if not CIDR
            }
            
            String cidrIp = cidrParts[0];
            int prefixLength = Integer.parseInt(cidrParts[1]);
            
            long ipLong = ipToLong(ip);
            long cidrIpLong = ipToLong(cidrIp);
            long mask = (-1L) << (32 - prefixLength);
            
            return (ipLong & mask) == (cidrIpLong & mask);
        } catch (Exception e) {
            logger.warning("Error parsing CIDR: " + cidr);
            return false;
        }
    }
    
    private static long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(octets[i]);
        }
        return result;
    }
    
    private static String maskIp(String ip) {
        if (ip == null) return "****";
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***." + "***";
        }
        return "****";
    }
    
    private static String generateReasoning(boolean isNew, int knownCount) {
        if (isNew) {
            return String.format("IP address is from an unknown range (customer has %d known IP ranges)", knownCount);
        } else {
            return "IP address is within customer's usual IP ranges";
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(NewIpRangeSignal.class, "analyzeNewIpRange");
    }
}

