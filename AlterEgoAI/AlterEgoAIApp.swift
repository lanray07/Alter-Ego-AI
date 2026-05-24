import SwiftData
import SwiftUI

@main
struct AlterEgoAIApp: App {
    @StateObject private var storeManager = StoreManager()

    var body: some Scene {
        WindowGroup {
            AppRootView()
                .modelContainer(for: [
                    UserProfile.self,
                    AlterEgoProfile.self,
                    Mission.self,
                    JournalEntry.self,
                    ChatMessage.self,
                    TransformationSnapshot.self,
                    Achievement.self,
                    SubscriptionState.self
                ])
                .environment(\.aiService, MockAIService())
                .environmentObject(storeManager)
                .preferredColorScheme(.dark)
                .task {
                    await storeManager.start()
                }
        }
    }
}
