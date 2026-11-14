# Verification log (2026-11-14, sandbox, Linux x64)

Toolchain: gradle 8.13, JDK 17.0.21, Android SDK platform 36, build-tools 36.0.0.
Command: gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
Result: BUILD SUCCESSFUL. Unit tests pass (CostEstimatorTest, BundleTrackerTest). Debug APK built at app/build/outputs/apk/debug/app-debug.apk (about 10 MB). Output in docs/last-build.log.

Not verified: running on a device or emulator, real NetworkStatsManager numbers, OEM behaviour, release/minified build, lint.
Sandbox note: the NDK llvm-strip cannot execute here, so app/build.gradle.kts has keepDebugSymbols for all .so files. Fine to keep or remove on a normal machine.
