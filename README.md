# MercuReal Data Guard

Free, open-source, offline-first Android app that shows where your mobile data goes, what it costs in naira (using a price YOU set), and when your bundles expire.

Status: MVP v0.1.0. Project site lives in `site/` (static, Vercel root directory `site`).

## Features (MVP)
- Mobile-data usage this month / today, per-app list (Android NetworkStatsManager, needs Usage Access).
- Naira estimate from a user-configurable price per GB (and 1000 vs 1024 MB toggle) plus optional monthly budget.
- Bundle tracker: add bundles (name, size, price, validity days), see time left, estimated data remaining, expiring-soon / expired / depleted state.
- All data in a local DataStore file. No INTERNET permission, no backend, no analytics, backup disabled.

## Honest limits
- Costs are estimates from your own price; operator billing may differ (rollover, night plans, VAT, expiry rules).
- Bundle "remaining" is total mobile traffic on this device since you added the bundle (all mobile SIMs combined). It is not read from the operator.
- Per-app numbers depend on OEM support of NetworkStats; hotspot / removed-app buckets are labelled separately.
- Bundle purchase time is "now" when added; editing purchase date is not built yet.
- Not yet run on a device or emulator (none in the build environment). See docs/VERIFICATION.md.

## Build
```
export JAVA_HOME=<jdk17> ANDROID_HOME=<sdk>   # sdk needs platforms;android-36 + build-tools
gradle :app:testDebugUnitTest :app:assembleDebug
```
See docs/ARCHITECTURE.md, docs/PRIVACY.md, docs/ROADMAP.md. License: MIT.
