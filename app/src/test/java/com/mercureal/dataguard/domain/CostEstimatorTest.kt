package com.mercureal.dataguard.domain

import org.junit.Assert.*
import org.junit.Test

class CostEstimatorTest {
    private val s = Settings(pricePerGbKobo = 50_000) // N500/GB

    @Test fun oneGbCostsPrice() = assertEquals(50_000L, CostEstimator.costKobo(1_000_000_000L, s))
    @Test fun halfGb() = assertEquals(25_000L, CostEstimator.costKobo(500_000_000L, s))
    @Test fun zero() = assertEquals(0L, CostEstimator.costKobo(0, s))
    @Test fun binaryGbIsCheaperPerByte() =
        assertEquals(50_000L, CostEstimator.costKobo(1_073_741_824L, s.copy(binaryGb = true)))
    @Test fun formatNaira() {
        assertEquals("\u20A61,234.50", CostEstimator.formatNaira(123_450))
        assertEquals("\u20A60.05", CostEstimator.formatNaira(5))
        assertEquals("\u20A61,000,000.00", CostEstimator.formatNaira(100_000_000))
    }
    @Test fun parse() {
        assertEquals(50_000L, CostEstimator.parseNairaToKobo("500"))
        assertEquals(125_050L, CostEstimator.parseNairaToKobo("\u20A61,250.5"))
        assertNull(CostEstimator.parseNairaToKobo("abc"))
        assertNull(CostEstimator.parseNairaToKobo("-5"))
        assertNull(CostEstimator.parseNairaToKobo(""))
    }
    @Test fun formatBytes() {
        assertEquals("512 B", CostEstimator.formatBytes(512))
        assertEquals("1.50 GB", CostEstimator.formatBytes(1_500_000_000))
        assertEquals("1.00 KB", CostEstimator.formatBytes(1024, binary = true))
    }
}
