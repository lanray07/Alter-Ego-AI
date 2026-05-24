# Alter Ego AI

Alter Ego AI is a SwiftUI iOS app for general wellness, habit-building, motivation, identity-based goals, daily missions, journaling, progress insights, streaks, XP, and future-self coaching.

Core positioning: "Become the person you were supposed to be."

Viral hook: "Your future self is watching."

## Build

Open `Alter Ego AI.xcodeproj` in Xcode and run the shared scheme `Alter Ego AI`.

Project defaults:
- iOS 17.0+
- SwiftUI with NavigationStack
- MVVM view models
- SwiftData local persistence
- MockAIService enabled by default
- StoreKit 2 subscription scaffolding
- Swift Charts insights
- Local notification scaffolding
- Native ShareLink share sheet
- WidgetKit placeholder code
- Apple Watch placeholder view

## StoreKit product identifiers

Create matching auto-renewable subscriptions in App Store Connect or in a StoreKit test configuration:

- `com.alteregoai.pro.monthly` - Pro Monthly - £9.99
- `com.alteregoai.pro.yearly` - Pro Yearly - £79.99
- `com.alteregoai.elite.monthly` - Elite Monthly - £19.99

## AI backend

`MockAIService` is installed in `AlterEgoAIApp.swift`.

`RemoteAIService` is scaffolded for:

`POST https://YOUR_BACKEND_URL.com/alter-ego-ai`

Never store API keys in the app. Put provider keys on your backend and disclose backend and third-party AI data sharing in App Store Connect App Privacy details before release.

## Safety framing

The app is intentionally framed as general wellness, habits, motivation, and lifestyle software. It should not be marketed as medical, mental health, therapy, diagnosis, crisis counseling, financial, legal, or treatment software.

See `AlterEgoAI/Resources/AppPrivacyDetails.md` for App Privacy preparation notes.
