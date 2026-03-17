package com.ing.sentinel.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MerchantNoveltySignal.
 * Tests merchant novelty detection and result structure.
 */
@DisplayName("MerchantNoveltySignal Tests")
class MerchantNoveltySignalTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should analyze known merchant")
    void testKnownMerchant() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_001", "GROCERY",
                "MERCHANT_001,MERCHANT_002,MERCHANT_003",
                "GROCERY:0.35,FUEL:0.15");
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("is_top_merchant"));
    }

    @Test
    @DisplayName("Should detect rare merchant")
    void testRareMerchant() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_999", "ELECTRONICS",
                "MERCHANT_001,MERCHANT_002",
                "GROCERY:0.35,FUEL:0.15,ELECTRONICS:0.02");
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("is_top_merchant"));
    }

    @Test
    @DisplayName("Should detect rare MCC")
    void testRareMcc() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_001", "JEWELRY",
                "MERCHANT_001",
                "GROCERY:0.35,FUEL:0.15,JEWELRY:0.01");
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("is_rare_mcc"));
    }

    @Test
    @DisplayName("Should include MCC weight")
    void testMccWeight() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_001", "GROCERY",
                "MERCHANT_001",
                "GROCERY:0.35,FUEL:0.15");
        
        assertNotNull(result.get("mcc_weight_in_history"));
        double weight = ((Number) result.get("mcc_weight_in_history")).doubleValue();
        assertThat(weight).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_001", "GROCERY",
                "MERCHANT_001",
                "GROCERY:0.35");
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to range 0-1")
    void testNormalizedSignalRange() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_999", "ELECTRONICS",
                "MERCHANT_001",
                "GROCERY:0.35,ELECTRONICS:0.02");
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertThat(signal).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should handle empty merchant list")
    void testEmptyMerchantList() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_001", "GROCERY",
                "",
                "GROCERY:0.35");
        
        assertNotNull(result);
        assertNotNull(result.get("normalized_signal"));
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = MerchantNoveltySignal.analyzeMerchantNovelty(
                "MERCHANT_001", "GROCERY",
                "MERCHANT_001",
                "GROCERY:0.35");
        
        assertTrue(result.containsKey("merchant_id"));
        assertTrue(result.containsKey("merchant_category"));
        assertTrue(result.containsKey("is_top_merchant"));
        assertTrue(result.containsKey("is_rare_mcc"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

