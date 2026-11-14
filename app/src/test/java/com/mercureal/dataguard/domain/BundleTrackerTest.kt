package com.mercureal.dataguard.domain

import org.junit.Assert.*
import org.junit.Test

class BundleTrackerTest {
    private val h = 3_600_000L
    private val b = Bundle(1, "5GB", 5_000_000_000L, 150_000, 0, 100 * h)

    @Test fun active() {
        val st = BundleTracker.status(b, 1_000_000_000L, 10 * h)
        assertEquals(BundleState.ACTIVE, st.state)
        assertEquals(4_000_000_000L, st.remainingBytes)
    }
    @Test fun expiringSoon() = assertEquals(BundleState.EXPIRING_SOON, BundleTracker.status(b, 0, 60 * h).state)
    @Test fun expiredWinsOverDepleted() = assertEquals(BundleState.EXPIRED, BundleTracker.status(b, 9_000_000_000L, 101 * h).state)
    @Test fun depleted() {
        val st = BundleTracker.status(b, 6_000_000_000L, 10 * h)
        assertEquals(BundleState.DEPLETED, st.state)
        assertEquals(0L, st.remainingBytes)
        assertEquals(5_000_000_000L, st.usedBytes)
    }
    @Test fun negativeUsageClamped() = assertEquals(5_000_000_000L, BundleTracker.status(b, -5, 0).remainingBytes)
    @Test fun formatLeft() {
        assertEquals("2d 3h left", BundleTracker.formatLeft(51 * h))
        assertEquals("5h 30m left", BundleTracker.formatLeft(5 * h + 30 * 60_000))
        assertEquals("expired", BundleTracker.formatLeft(0))
    }
    @Test fun validate() {
        assertNotNull(BundleTracker.validate("", 1, 0, 1))
        assertNotNull(BundleTracker.validate("x", null, 0, 1))
        assertNotNull(BundleTracker.validate("x", 1, 5, 5))
        assertNull(BundleTracker.validate("x", 1, 0, 1))
    }
}
