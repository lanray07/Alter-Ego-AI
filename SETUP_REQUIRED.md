# Setup required

These items genuinely require developer or Play Console access:

1. Install Android Studio with JDK 17, Android SDK Platform 37, Android SDK Platform 36, Build Tools 36.0.0, and a compatible emulator/device. The checked-in Gradle wrapper, source build, lint, JVM unit tests, and optimized AAB have been verified in this workspace; a device or emulator is still required for connected tests and manual QA.
2. For remote AI, set `AI_BACKEND_URL` to the backend base URL ending in `/` and expose a compatible `POST /alter-ego-ai` endpoint. Keep provider API keys server-side. Leave it unset to use the bundled offline AI behavior.
3. In Google Play Console, create subscriptions `alteregoai_pro_monthly`, `alteregoai_pro_yearly`, and `alteregoai_elite_monthly`, configure base plans/offers, then test with license testers. Product mapping is in `docs/BILLING_MAPPING.md`.
4. Add server-side Google Play purchase-token verification before treating client-owned products as production entitlements.
5. Android upload signing has been created locally at `C:/Users/User/.gradle/alter-ego-ai-upload-keystore.jks` and configured through `C:/Users/User/.gradle/gradle.properties`. Back up the keystore and passwords securely; never commit the keystore or secrets.
6. Supply final HTTPS privacy policy and Terms URLs, support email, and any updated remote-AI disclosure. The current iOS source contains placeholder URLs and the Android UI intentionally calls this out.
7. Complete Play Console Data Safety, content rating, target-audience, subscription, developer verification, and store asset setup using the documents in `/docs`.
