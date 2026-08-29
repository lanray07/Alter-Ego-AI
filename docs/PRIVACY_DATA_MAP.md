# Privacy data map

This map reflects the current source and the Android port. It is an implementation aid for Google Play Data Safety, not a substitute for legal review.

| Data | Purpose | Leaves device in current build? | Processor / notes |
| --- | --- | --- | --- |
| Name, current identity, future identity, goals, motivation style, availability | Generate and display the user's Alter Ego and missions | No when bundled AI is active; yes only if remote AI is explicitly configured | Stored in Room locally; remote backend receives the fields in the AI request |
| Missions, completion state, XP, streaks, achievements, timeline | Habit tracking and progress insights | No by default | Room local database |
| Journal text, mood, wins, struggles, lessons | Reflection history and AI summary | No when bundled AI is active; yes if remote AI is configured | Treat as user-generated content; remote AI processor requires disclosure |
| Chat messages | Future-self coaching history | No when bundled AI is active; yes if remote AI is configured | Room local database; remote request contains the current message and progress metadata |
| Subscription purchase state and product IDs | Entitlement and paywall display | Google Play processes purchase information | Android client caches ownership only; production server verification is required |
| Notification preference | Schedule or cancel daily reminder | No | Preferences DataStore and Android notification scheduler |
| Theme selection | Personalize display | No | Preferences DataStore |
| Camera, microphone, location, contacts, photos, files | Not used by the current product | No | No permissions declared for these capabilities |
| Analytics and crash reporting | Not implemented | No | Do not add a processor without updating this map and disclosures |
| Export summary / share card | User-selected export or share | Only when the user confirms a system share target | Android Sharesheet; generated image is cached temporarily and shared through FileProvider |

## Security and disclosure notes

- Android requests only `INTERNET` and `POST_NOTIFICATIONS`.
- The remote AI endpoint is a build-time configuration. No provider API key is included in the client.
- Journal/chat/identity data should be treated as sensitive user content even though the current default path is local-only.
- The existing `PRIVACY_POLICY.md` says the current default app does not send user data to an AI provider. Update it before enabling a remote Android backend.
- Play Data Safety declarations, privacy policy URLs, retention language, and third-party processor terms require developer/legal confirmation.
