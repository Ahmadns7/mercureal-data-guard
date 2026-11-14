package com.mercureal.dataguard.domain

object BundleTracker {
    const val SOON_MILLIS = 48L * 60 * 60 * 1000

    /**
     * [usedSinceBytes] is mobile data used on this device since the bundle was bought.
     * It is an ESTIMATE: it counts all mobile traffic (including data not drawn from this
     * bundle, e.g. night plans or other SIMs when counters are combined).
     */
    fun status(b: Bundle, usedSinceBytes: Long, now: Long): BundleStatus {
        val used = usedSinceBytes.coerceAtLeast(0)
        val remaining = (b.sizeBytes - used).coerceAtLeast(0)
        val left = b.expiresAtMillis - now
        val state = when {
            left <= 0 -> BundleState.EXPIRED
            remaining == 0L -> BundleState.DEPLETED
            left <= SOON_MILLIS -> BundleState.EXPIRING_SOON
            else -> BundleState.ACTIVE
        }
        return BundleStatus(b, minOf(used, b.sizeBytes), remaining, left, state)
    }

    fun formatLeft(millis: Long): String {
        if (millis <= 0) return "expired"
        val mins = millis / 60_000
        val d = mins / 1440; val h = (mins % 1440) / 60; val m = mins % 60
        return when {
            d > 0 -> "${d}d ${h}h left"
            h > 0 -> "${h}h ${m}m left"
            else -> "${m}m left"
        }
    }

    /** Validates a new bundle; returns an error message or null. */
    fun validate(name: String, sizeBytes: Long?, purchased: Long, expires: Long): String? = when {
        name.isBlank() -> "Name is required"
        sizeBytes == null || sizeBytes <= 0 -> "Enter a size above 0"
        expires <= purchased -> "Expiry must be after purchase"
        else -> null
    }
}
