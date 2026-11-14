# Privacy

- No INTERNET permission in the manifest: the app cannot send data anywhere.
- No accounts, analytics, ads, crash reporters, or backend.
- android:allowBackup=false so settings/bundles are not copied to cloud backup.
- Permissions: PACKAGE_USAGE_STATS (user grants in system settings; reads this device's counters only), QUERY_ALL_PACKAGES (app names in the usage list; may need justification for some stores, fine for F-Droid/GitHub).
- Naira price is typed by the user; the app never fetches tariffs.
