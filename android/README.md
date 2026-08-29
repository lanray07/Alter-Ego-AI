# Alter Ego AI Android

Native Android implementation of the existing iOS Alter Ego AI app. The iOS project remains at the repository root and is not modified by this port.

## Stack

- Kotlin 2.3.20, Gradle 9.5.1 wrapper, Gradle Kotlin DSL, Android Gradle Plugin 9.3.0
- Jetpack Compose BOM 2026.08.00 and Material 3
- MVVM with `MainViewModel`, repository boundaries, Coroutines, and StateFlow
- Room for structured local data and Preferences DataStore for theme settings
- Retrofit/Gson for the compatible optional remote AI endpoint
- Google Play Billing for subscription products
- Android notification channels, inexact daily AlarmManager reminders, FileProvider, and Sharesheet

## Prerequisites

Install Android Studio with JDK 17, SDK Platform 37, SDK Platform 36, Build Tools 36.0.0, and an emulator or device. The project targets API 36 for Play and compiles against API 37 for the current stable Compose release.

## Configure and run

From this directory:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedDebugAndroidTest
./gradlew bundleRelease
```

The checked-in Gradle wrapper uses Gradle 9.5.1. In a fresh checkout, either set `ANDROID_HOME` or create an ignored `local.properties` file with `sdk.dir` pointing at the local Android SDK. Verification in this workspace was completed with the wrapper, JDK 17, Android lint, JVM unit tests, and `bundleRelease`. Connected instrumentation tests still require an attached emulator or device.

The debug application ID is `com.alteregoai.app.debug`. The release application ID is `com.alteregoai.app`.

## Optional remote AI

The default path is an offline bundled AI service matching the iOS behavior. To use the shared backend, pass the base URL as a Gradle property:

```bash
./gradlew assembleDebug -PAI_BACKEND_URL=https://your-backend.example/
```

The backend must implement `POST /alter-ego-ai` and return the request/response shape described in `data/AiService.kt`. Never embed an OpenAI or other provider key in the app.

## Release signing

Keep upload credentials in `~/.gradle/gradle.properties` or CI secrets. This local machine is configured with an upload keystore at `C:/Users/User/.gradle/alter-ego-ai-upload-keystore.jks`, and release builds are signed when the `ANDROID_UPLOAD_*` Gradle properties are present. Use Google Play App Signing and retain the upload keystore securely. See `/SETUP_REQUIRED.md` for the exact variables and outstanding Play setup.

## Output

- Debug APK: `app/build/outputs/apk/debug/`
- Release Android App Bundle: `app/build/outputs/bundle/release/`

See the root `PORTING_NOTES.md`, `QA_CHECKLIST.md`, `RELEASE_CHECKLIST.md`, and `/docs` for product mapping, privacy, billing, listing, assets, and readiness details.
