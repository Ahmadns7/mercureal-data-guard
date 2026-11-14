package com.mercureal.dataguard.domain

import kotlinx.serialization.Serializable

/** Money is stored as kobo (1 naira = 100 kobo) to avoid float drift in saved values. */
@Serializable
data class Settings(
    val pricePerGbKobo: Long = 50_000L, // N500/GB default, user-editable. Not fetched from anywhere.
    val binaryGb: Boolean = false,      // false: 1 GB = 1000 MB (how bundles are usually sold)
    val monthlyBudgetKobo: Long = 0L,   // 0 = no budget
)

@Serializable
data class Bundle(
    val id: Long,
    val name: String,
    val sizeBytes: Long,
    val priceKobo: Long,
    val purchasedAtMillis: Long,
    val expiresAtMillis: Long,
)

enum class BundleState { ACTIVE, EXPIRING_SOON, EXPIRED, DEPLETED }

data class BundleStatus(
    val bundle: Bundle,
    val usedBytes: Long,
    val remainingBytes: Long,
    val millisLeft: Long,
    val state: BundleState,
)

data class AppUsage(val uid: Int, val label: String, val packageName: String?, val bytes: Long)
