# Android release checklist

- [x] `versionName` and `versionCode` set for the intended release.
- [x] Permanent application ID `com.alteregoai.app` confirmed in Play Console.
- [x] Upload keystore created outside the repository; Google Play App Signing enabled.
- [x] Release signing supplied via local Gradle properties or CI environment variables.
- [x] R8/minification tested and mapping file produced; retain it securely for the intended release.
- [x] Gradle wrapper added and verified with Gradle 9.5.1.
- [x] JVM unit tests pass through `android/gradlew.bat test`.
- [x] Android lint passes through `android/gradlew.bat lint --rerun-tasks`; non-blocking dependency/version warnings remain.
- [ ] Connected Compose and manual device/emulator QA completed.
- [x] Signed release `.aab` generated and inspected from `android/app/build/outputs/bundle/release/`.
- [ ] Production AI backend URL configured; backend has authentication/rate limits, logging redaction, and provider-key isolation.
- [ ] Remote AI privacy disclosure updated if the remote path is enabled.
- [ ] Google Play subscription products and base plans configured; prices/trials reviewed.
- [ ] Server-side purchase verification and entitlement revocation tested.
- [ ] Privacy policy URL, Terms URL, support email, and Data Safety form completed.
- [ ] App icon, phone/tablet screenshots, feature graphic, description, category, content rating, and target audience reviewed.
- [ ] Crash monitoring and analytics decisions documented; no unapproved trackers added.
- [ ] Internal/closed testing track completed before production review.

Current readiness: signed bundle is ready for internal-test upload. Production readiness is still blocked by Play Console products, backend/privacy configuration, connected/manual QA, and store setup.
