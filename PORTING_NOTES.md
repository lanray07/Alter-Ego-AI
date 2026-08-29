# Android porting notes

## Product readout

Alter Ego AI is a local-first general wellness and habit-building product. Its positioning is “Become the person you were supposed to be” and its core hook is “Your future self is watching.” The product loop is identity setup → generated Alter Ego → small daily missions → XP/streak progression → reflection and future-self coaching → timeline/insights → shareable progress cards.

The iOS source is SwiftUI on iOS 17 with SwiftData persistence, MVVM view models, bundled `MockAIService`, optional `RemoteAIService`, StoreKit 2 subscription scaffolding, local notifications, native sharing, and placeholder WidgetKit/Watch destinations. No authentication, Supabase, Firebase, analytics, or cloud database is currently implemented. The app is intentionally general wellness software, not medical or mental-health software.

## Android implementation plan and status

| iOS capability | Android equivalent | Status |
| --- | --- | --- |
| SwiftUI / NavigationStack / TabView | Jetpack Compose, Material 3, Navigation Compose, Material 3 bottom navigation | Implemented |
| SwiftData entities and queries | Room entities/DAO and observable Flows | Implemented |
| `@AppStorage` theme selection | Preferences DataStore | Implemented |
| MVVM view models | `MainViewModel` + repository + StateFlow | Implemented |
| Bundled mock AI | Offline `MockAiService` preserving the iOS prompt/response behavior | Implemented |
| Remote AI POST contract | Retrofit/Gson adapter to the existing `/alter-ego-ai` request contract | Implemented; endpoint configuration required |
| Onboarding and seven-day seed plan | Compose onboarding and Room seeding | Implemented |
| Dashboard, missions, journal, chat, timeline, insights | Compose screens and Android back behavior | Implemented |
| Swift Charts | Responsive textual progress bars and weekly consistency rows | Implemented as Android-native replacement |
| StoreKit 2 | Google Play Billing subscription repository and localized product pricing | Implemented; Play Console and server verification required |
| Local reminders | Notification channel + inexact daily AlarmManager reminder | Implemented; runtime permission is requested only when enabled |
| `ShareLink` and rendered image card | Android Sharesheet + FileProvider-rendered PNG | Implemented |
| Widgets / Apple Watch placeholder | Not ported; no working iOS feature exists to reproduce | Documented limitation |
| Authentication / account compatibility | Not applicable to the current local-only product | No auth exists in source |

## Decisions and limitations

- The Android application lives in `/android`; the iOS project was not restructured or modified.
- The application ID remains `com.alteregoai.app`, matching the existing product namespace. Treat this as permanent once Play publishing begins.
- `minSdk 26`, `compileSdk 37`, and `targetSdk 36` are configured. As of August 29, 2026, Android 16/API 36 is the Play target requirement that becomes mandatory for new apps and updates on August 31, 2026; compile SDK 37 is used because the current stable Compose BOM requires it.
- The iOS app prefers dark mode. Android defaults to the cinematic dark theme but also offers a light theme and keeps layouts responsive for phones and larger windows.
- There are no existing user accounts or cloud records to migrate. If authentication or sync is added later, introduce a server-backed identity layer for both platforms rather than copying the local database blindly.
- The remote AI endpoint is never called until `AI_BACKEND_URL` is supplied at build time. Provider credentials must stay on that backend.
- Product prices are never hard-coded in Android. Google Play supplies localized prices. The iOS reference prices are recorded only as historical product strategy in `docs/BILLING_MAPPING.md`.
