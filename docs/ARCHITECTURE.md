# Architecture

Single-module, three layers, no DI framework yet.

- domain/ : pure Kotlin, JVM-unit-tested. Settings, Bundle, CostEstimator (kobo math, naira/bytes formatting, input parsing), BundleTracker (status state machine).
- data/ : UsageRepository (NetworkStatsManager + AppOps check for usage access), LocalStore (DataStore Preferences holding kotlinx-serialization JSON for settings and bundles).
- ui/ : MainViewModel (combines stored settings/bundles with a usage snapshot refreshed in onResume) and Compose screens: Usage, Bundles, Settings.

Decisions
- Money stored as kobo Long, no floats in persisted values.
- DataStore + JSON instead of Room for MVP: tiny data, fewer build moving parts. Move to Room when history/snapshots are added.
- minSdk 26, targetSdk/compileSdk 36. Kotlin 2.1.21, AGP 8.10.1, Compose BOM 2025.05.01.
- Bundle usage = querySummaryForDevice(TYPE_MOBILE) from purchase time to min(now, expiry).
