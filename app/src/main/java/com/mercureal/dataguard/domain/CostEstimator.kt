package com.mercureal.dataguard.domain

import kotlin.math.roundToLong

object CostEstimator {
    fun bytesPerGb(binary: Boolean): Long = if (binary) 1_073_741_824L else 1_000_000_000L

    /** Estimated cost in kobo for [bytes] at the user's own price per GB. */
    fun costKobo(bytes: Long, s: Settings): Long {
        require(bytes >= 0) { "bytes must be >= 0" }
        return (bytes.toDouble() / bytesPerGb(s.binaryGb) * s.pricePerGbKobo).roundToLong()
    }

    /** "₦1,234.50" */
    fun formatNaira(kobo: Long): String {
        val neg = kobo < 0
        val abs = kotlin.math.abs(kobo)
        val whole = (abs / 100).toString().reversed().chunked(3).joinToString(",").reversed()
        val frac = (abs % 100).toString().padStart(2, '0')
        return (if (neg) "-" else "") + "\u20A6" + whole + "." + frac
    }

    /** Parses user text like "500", "1,250.5" into kobo; null if invalid or negative. */
    fun parseNairaToKobo(text: String): Long? {
        val t = text.trim().replace(",", "").removePrefix("\u20A6")
        if (t.isEmpty()) return null
        val d = t.toBigDecimalOrNull() ?: return null
        if (d.signum() < 0) return null
        return d.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toLong()
    }

    fun formatBytes(bytes: Long, binary: Boolean = false): String {
        val base = if (binary) 1024.0 else 1000.0
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var v = bytes.toDouble(); var i = 0
        while (v >= base && i < units.lastIndex) { v /= base; i++ }
        return if (i == 0) "$bytes B" else String.format(java.util.Locale.US, "%.2f %s", v, units[i])
    }
}
