# Alter Ego AI App Privacy Details

Default build mode:
- Mock AI is enabled by default.
- User profile, identity goals, missions, journal entries, chat messages, achievements, subscription cache, and timeline snapshots are stored locally with SwiftData.
- No API keys are stored in the app.
- No private data is shared from viral cards unless the user taps Share and confirms the native share sheet.

If RemoteAIService is enabled:
- The app may send current identity, future identity, motivation style, selected goals, journal text, and progress data to the configured backend endpoint.
- The backend and any third-party AI provider must be disclosed in App Store Connect App Privacy details.
- Disclose User Content and Product Interaction data as appropriate for the production backend.
- Do not use the data for tracking unless the production policy and App Tracking Transparency flow explicitly support it.

App Review notes:
- Alter Ego AI is positioned as general wellness, habit-building, motivation, and lifestyle software.
- It must not be marketed as medical, therapy, mental health diagnosis, treatment, crisis counseling, legal, or financial advice.
- Premium digital features must use StoreKit and In-App Purchase.
