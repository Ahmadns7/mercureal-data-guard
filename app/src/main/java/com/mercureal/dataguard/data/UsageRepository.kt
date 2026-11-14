package com.mercureal.dataguard.data

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Process
import android.provider.Settings
import com.mercureal.dataguard.domain.AppUsage

/** Reads this device's own mobile-data counters. Read-only, local, no network calls. */
class UsageRepository(private val context: Context) {
    private val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    fun hasUsageAccess(): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageAccessIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Total mobile bytes (rx+tx) between [start] and [end]. Device-wide, all SIMs combined. */
    fun totalMobileBytes(start: Long, end: Long): Long = runCatching {
        val b = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, start, end)
        b.rxBytes + b.txBytes
    }.getOrDefault(0L)

    /** Mobile bytes per uid between [start] and [end], largest first. */
    fun perApp(start: Long, end: Long): List<AppUsage> {
        val totals = HashMap<Int, Long>()
        runCatching {
            nsm.querySummary(ConnectivityManager.TYPE_MOBILE, null, start, end).use { stats ->
                val bucket = NetworkStats.Bucket()
                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)
                    totals.merge(bucket.uid, bucket.rxBytes + bucket.txBytes, Long::plus)
                }
            }
        }
        val pm = context.packageManager
        return totals.filterValues { it > 0 }.map { (uid, bytes) ->
            val pkg = pm.getPackagesForUid(uid)?.firstOrNull()
            val label = when (uid) {
                NetworkStats.Bucket.UID_REMOVED -> "Removed apps"
                NetworkStats.Bucket.UID_TETHERING -> "Hotspot / tethering"
                Process.SYSTEM_UID -> "Android system"
                else -> pkg?.let { runCatching { pm.getApplicationLabel(pm.getApplicationInfo(it, 0)).toString() }.getOrNull() }
                    ?: "UID $uid"
            }
            AppUsage(uid, label, pkg, bytes)
        }.sortedByDescending { it.bytes }
    }
}
