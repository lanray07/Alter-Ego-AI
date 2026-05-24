import StoreKit
import SwiftData
import SwiftUI
import UIKit

struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @EnvironmentObject private var storeManager: StoreManager
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query private var alterEgos: [AlterEgoProfile]
    @Query private var missions: [Mission]
    @Query private var journals: [JournalEntry]
    @Query private var messages: [ChatMessage]
    @Query private var snapshots: [TransformationSnapshot]
    @Query private var achievements: [Achievement]
    @Query private var subscriptions: [SubscriptionState]
    @AppStorage("themeSelection") private var themeSelection = "Cinematic"
    @State private var showDeleteConfirmation = false

    private var profile: UserProfile? { profiles.first }

    var body: some View {
        Form {
            Section("Subscription") {
                HStack {
                    Text("Current plan")
                    Spacer()
                    Text(storeManager.activePlan.title)
                        .foregroundStyle(AppTheme.neonCyan)
                }
                NavigationLink("View plans", destination: PaywallView())
                Button("Manage subscription") {
                    Task { await manageSubscriptions() }
                }
            }

            Section("Coaching and safety") {
                Text(AppConstants.coachingDisclaimer)
                    .font(.footnote)
                Link("Privacy policy placeholder", destination: URL(string: "https://YOUR_DOMAIN.com/privacy")!)
                Link("Terms of use placeholder", destination: URL(string: "https://YOUR_DOMAIN.com/terms")!)
            }

            if let profile {
                Section("Preferences") {
                    Picker("Motivation style", selection: Binding(
                        get: { profile.motivationStyle },
                        set: {
                            profile.motivationStyle = $0
                            try? modelContext.save()
                        }
                    )) {
                        ForEach(MotivationStyle.allCases) { style in
                            Text(style.title).tag(style)
                        }
                    }

                    Toggle("Daily reminders", isOn: Binding(
                        get: { profile.notificationPreference },
                        set: { enabled in
                            profile.notificationPreference = enabled
                            try? modelContext.save()
                            if enabled {
                                Task {
                                    let granted = await NotificationService.shared.requestAuthorization()
                                    if granted { await NotificationService.shared.scheduleDailyMissionReminder() }
                                }
                            } else {
                                NotificationService.shared.cancelDailyReminder()
                            }
                        }
                    ))

                    Picker("Theme", selection: $themeSelection) {
                        Text("Cinematic").tag("Cinematic")
                        Text("Neon Cyan").tag("Neon Cyan")
                        Text("Apex Dark").tag("Apex Dark")
                    }
                }
            }

            Section("Data") {
                ShareLink(item: exportSummary) {
                    Label("Export data summary", systemImage: "square.and.arrow.up")
                }
                Button(role: .destructive) {
                    showDeleteConfirmation = true
                } label: {
                    Text("Delete all data")
                }
            }

            Section("AI backend") {
                Text("Mock AI is enabled by default. RemoteAIService points to \(AppConstants.backendEndpoint.absoluteString). Never store API keys in the app.")
                    .font(.footnote)
            }
        }
        .scrollContentBackground(.hidden)
        .background(CinematicBackground())
        .navigationTitle("Settings")
        .confirmationDialog("Delete all local Alter Ego AI data?", isPresented: $showDeleteConfirmation, titleVisibility: .visible) {
            Button("Delete all data", role: .destructive) {
                deleteAllData()
            }
            Button("Cancel", role: .cancel) {}
        }
    }

    private var exportSummary: String {
        """
        Alter Ego AI Data Export Summary
        Profiles: \(profiles.count)
        Alter Ego Profiles: \(alterEgos.count)
        Missions: \(missions.count)
        Journals: \(journals.count)
        Chat Messages: \(messages.count)
        Timeline Snapshots: \(snapshots.count)
        Achievements: \(achievements.count)
        """
    }

    private func manageSubscriptions() async {
        guard let scene = UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene }).first else { return }
        try? await AppStore.showManageSubscriptions(in: scene)
    }

    private func deleteAllData() {
        delete(profiles)
        delete(alterEgos)
        delete(missions)
        delete(journals)
        delete(messages)
        delete(snapshots)
        delete(achievements)
        delete(subscriptions)
        try? modelContext.save()
        NotificationService.shared.cancelDailyReminder()
    }

    private func delete<T: PersistentModel>(_ models: [T]) {
        for model in models {
            modelContext.delete(model)
        }
    }
}
