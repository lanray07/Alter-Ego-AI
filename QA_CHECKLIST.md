# Android QA checklist

## Automated checks

- [x] JVM unit tests pass with `android/gradlew.bat test`.
- [x] Android lint passes with `android/gradlew.bat lint --rerun-tasks`.
- [x] Signed release bundle builds with `android/gradlew.bat bundleRelease`.
- [ ] Connected Compose tests are not yet run because no emulator or device is attached.

## Core flows

- [ ] Fresh install opens onboarding with no existing profile.
- [ ] Required fields and exactly three goals gate profile creation.
- [ ] Onboarding generates an Alter Ego, seven-day seed plan, achievements, initial message, and baseline snapshot.
- [ ] Dashboard shows identity, XP, streak, completion/missed metrics, and today's missions.
- [ ] Mission completion is idempotent and updates XP, level, stage, snapshots, and achievements.
- [ ] Mission filters work for all categories and individual categories.
- [ ] Chat prevents empty or duplicate submissions and preserves messages after process recreation.
- [ ] Journal saves a reflection and AI summary; failed generation keeps user input recoverable.
- [ ] Timeline generates a weekly review and lists snapshots.
- [ ] Insights show consistency, score, trend, weekly rows, and category completion.
- [ ] Text and image share use the Android system Sharesheet and do not share automatically.

## Billing and configuration

- [ ] Products load from Google Play with localized price strings.
- [ ] Purchase, pending, cancelled, reconnect, restore, and acknowledgement states are tested with Play Billing test accounts.
- [ ] Production entitlement is server-verified before premium access is granted.
- [ ] Missing `AI_BACKEND_URL` uses the documented bundled AI path; configured remote failures show a useful error.

## Platform and resilience

- [ ] Notification permission is requested only after the user enables reminders.
- [ ] Reminder channel and inexact daily alarm can be scheduled and cancelled.
- [ ] Airplane mode, timeout, malformed AI response, and billing unavailability show user-facing error states.
- [ ] Back navigation works from every secondary screen and does not exit the app unexpectedly.
- [ ] App survives rotation, background/foreground, and process recreation without losing Room data.
- [ ] Small phone, large phone, tablet, and landscape layouts are checked.
- [ ] Dark, light, large text, TalkBack labels, focus order, contrast, and touch targets are checked.
- [ ] Account deletion is not advertised because the current product has no accounts; local “Delete all data” is verified.

## Release audit

- [x] No production secrets, keystores, passwords, or provider keys are committed.
- [x] Manifest permission list and exported components are reviewed.
- [x] R8 release build is tested on a signed-like non-debug build.
- [ ] Play listing, privacy policy, Data Safety, content rating, subscription terms, support email, and screenshots are reviewed.
