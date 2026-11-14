package com.mercureal.dataguard.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mercureal.dataguard.data.LocalStore
import com.mercureal.dataguard.data.UsageRepository
import com.mercureal.dataguard.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class UiState(
    val hasAccess: Boolean = false,
    val settings: Settings = Settings(),
    val monthBytes: Long = 0,
    val todayBytes: Long = 0,
    val apps: List<AppUsage> = emptyList(),
    val bundles: List<BundleStatus> = emptyList(),
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = LocalStore(app)
    private val usage = UsageRepository(app)
    private val usageSnapshot = MutableStateFlow(UiState())

    val state: StateFlow<UiState> = combine(usageSnapshot, store.settings, store.bundles) { u, s, b ->
        val now = System.currentTimeMillis()
        u.copy(settings = s, bundles = b.map { bundle ->
            val used = if (u.hasAccess) usageForBundle(bundle, now) else 0L
            BundleTracker.status(bundle, used, now)
        })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    private val bundleUsageCache = HashMap<Long, Pair<Long, Long>>() // id -> (asOfMinute, bytes)

    private fun usageForBundle(b: Bundle, now: Long): Long {
        val minute = now / 60_000
        bundleUsageCache[b.id]?.let { if (it.first == minute) return it.second }
        val bytes = usage.totalMobileBytes(b.purchasedAtMillis, minOf(now, b.expiresAtMillis))
        bundleUsageCache[b.id] = minute to bytes
        return bytes
    }

    fun usageAccessIntent(): Intent = usage.usageAccessIntent()

    fun refresh() {
        viewModelScope.launch {
            val access = usage.hasUsageAccess()
            val snap = withContext(Dispatchers.IO) {
                if (!access) UiState(hasAccess = false) else {
                    val now = System.currentTimeMillis()
                    val monthStart = startOfMonth(now); val dayStart = startOfDay(now)
                    UiState(
                        hasAccess = true,
                        monthBytes = usage.totalMobileBytes(monthStart, now),
                        todayBytes = usage.totalMobileBytes(dayStart, now),
                        apps = usage.perApp(monthStart, now),
                    )
                }
            }
            bundleUsageCache.clear()
            usageSnapshot.value = snap
        }
    }

    fun saveSettings(s: Settings) = viewModelScope.launch { store.saveSettings(s) }

    fun addBundle(name: String, sizeBytes: Long, priceKobo: Long, purchased: Long, expires: Long) {
        viewModelScope.launch {
            val list = store.bundles.first()
            store.saveBundles(list + Bundle(System.currentTimeMillis(), name.trim(), sizeBytes, priceKobo, purchased, expires))
            refresh()
        }
    }

    fun deleteBundle(id: Long) = viewModelScope.launch {
        store.saveBundles(store.bundles.first().filterNot { it.id == id })
    }

    companion object {
        fun startOfDay(now: Long): Long = Calendar.getInstance().apply {
            timeInMillis = now; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        fun startOfMonth(now: Long): Long = Calendar.getInstance().apply {
            timeInMillis = startOfDay(now); set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
    }
}
