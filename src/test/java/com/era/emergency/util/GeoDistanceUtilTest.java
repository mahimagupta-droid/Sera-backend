package com.era.emergency.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GeoDistanceUtilTest {

    private final GeoDistanceUtil util = new GeoDistanceUtil();

    @Test
    void samePointShouldBeZeroDistance() {
        double dist = util.calculateDistanceKm(28.55, 77.25, 28.55, 77.25);
        assertThat(dist).isEqualTo(0.0);
    }

    @Test
    void knownDistanceDelhiToNoida() {
        // AIIMS Delhi → Noida Sector 18: roughly 18–22 km
        double dist = util.calculateDistanceKm(28.5673, 77.2100, 28.5700, 77.3210);
        assertThat(dist).isBetween(8.0, 15.0);
    }

    @Test
    void normalisedScoreShouldBeOneAtZeroDistance() {
        double score = util.normaliseDistanceScore(0, 100);
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    void normalisedScoreShouldBeZeroAtMaxDistance() {
        double score = util.normaliseDistanceScore(100, 100);
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void etaShouldIncludeMinimumOverhead() {
        int eta = util.estimateEtaMinutes(0);
        assertThat(eta).isGreaterThanOrEqualTo(5); // dispatch overhead
    }

    @Test
    void etaShouldScaleWithDistance() {
        int shortEta = util.estimateEtaMinutes(5);
        int longEta  = util.estimateEtaMinutes(50);
        assertThat(longEta).isGreaterThan(shortEta);
    }
}
