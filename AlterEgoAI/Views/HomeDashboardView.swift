import SwiftData
import SwiftUI

struct HomeDashboardView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.aiService) private var aiService
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @Query(sort: \Mission.dueDate, order: .forward) private var missions: [Mission]
    @Query(sort: \ChatMessage.createdAt, order: .reverse) private var messages: [ChatMessage]
    @StateObject private var viewModel = HomeViewModel()

    private var profile: UserProfile? { profiles.first }
    private var alterEgo: AlterEgoProfile? { alterEgos.first }
    private var todaysMissions: [Mission] {
        missions
            .filter { Calendar.current.isDateInToday($0.dueDate) }
            .sorted { $0.completed == $1.completed ? $0.dueDate < $1.dueDate : !$0.completed && $1.completed }
    }
    private var completedCount: Int { missions.filter(\.completed).count }
    private var missedCount: Int {
        missions.filter { !$0.completed && $0.dueDate < Calendar.current.startOfDay(for: Date()) }.count
    }
    private var streak: Int { XPSystem.currentStreak(from: missions) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if let alterEgo, let profile {
                    NavigationLink(destination: AlterEgoProfileView()) {
                        AlterEgoCard(alterEgo: alterEgo, subtitle: profile.futureIdentity)
                    }
                    .buttonStyle(.plain)

                    dashboardStats(alterEgo: alterEgo)
                    quickActions
                    todaySection
                    nextAIMessage
                    NavigationLink(destination: PaywallView()) {
                        UpgradeBanner(title: "Unlock Pro transformation tools", subtitle: "Unlimited AI messages, advanced insights, share cards, widgets, and premium themes.")
                    }
                    .buttonStyle(.plain)
                } else {
                    EmptyStateView(title: "No identity found", message: "Restart onboarding to create your Alter Ego profile.", symbol: "person.crop.circle.badge.questionmark")
                }
            }
            .padding(20)
            .padding(.bottom, 24)
        }
        .navigationTitle("Dashboard")
        .background(CinematicBackground())
    }

    private func dashboardStats(alterEgo: AlterEgoProfile) -> some View {
        HStack(alignment: .center, spacing: 14) {
            XPProgressRing(progress: XPSystem.progressToNextLevel(totalXP: alterEgo.totalXP), label: "Next level")
            VStack(spacing: 14) {
                StreakCard(
                    streak: streak,
                    disciplineScore: XPSystem.disciplineScore(completed: completedCount, missed: missedCount)
                )
                HStack {
                    InsightCard(title: "Completed", value: "\(completedCount)", symbol: "checkmark.seal", tint: AppTheme.success)
                    InsightCard(title: "Missed", value: "\(missedCount)", symbol: "clock.badge.exclamationmark", tint: AppTheme.warning)
                }
            }
        }
    }

    private var quickActions: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Quick actions")
                .font(.headline)

            LazyVGrid(columns: [GridItem(.adaptive(minimum: 150), spacing: 12)], spacing: 12) {
                actionButton(title: "Complete Mission", symbol: "checkmark.circle.fill") {
                    if let mission = todaysMissions.first(where: { !$0.completed }) {
                        viewModel.completeMission(mission, alterEgo: alterEgo, missions: missions, context: modelContext)
                    }
                }

                NavigationLink(destination: ChatView()) {
                    actionContent(title: "Talk to Future Self", symbol: "message.fill")
                }
                .buttonStyle(.plain)

                NavigationLink(destination: JournalView()) {
                    actionContent(title: "Journal", symbol: "square.and.pencil")
                }
                .buttonStyle(.plain)

                Button {
                    Task {
                        await viewModel.generateTodayPlan(
                            profile: profile,
                            alterEgo: alterEgo,
                            existingMissions: missions,
                            context: modelContext,
                            aiService: aiService
                        )
                    }
                } label: {
                    actionContent(title: viewModel.isGeneratingPlan ? "Generating" : "Generate Plan", symbol: "wand.and.stars")
                }
                .buttonStyle(.plain)

                NavigationLink(destination: TimelineView()) {
                    actionContent(title: "View Timeline", symbol: "sparkles.rectangle.stack")
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var todaySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Today's missions")
                    .font(.headline)
                Spacer()
                NavigationLink("View all", destination: MissionsView())
                    .font(.caption.bold())
            }

            if todaysMissions.isEmpty {
                EmptyStateView(title: "No missions yet", message: "Generate today's plan to start building momentum.", symbol: "target")
            } else {
                ForEach(todaysMissions.prefix(3)) { mission in
                    MissionCard(mission: mission) {
                        viewModel.completeMission(mission, alterEgo: alterEgo, missions: missions, context: modelContext)
                    }
                }
            }

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.footnote)
                    .foregroundStyle(.red.opacity(0.9))
            }
        }
    }

    private var nextAIMessage: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label("Next AI message", systemImage: "sparkles")
                .font(.headline)
            Text(messages.first?.content ?? "Your future self is ready when you are. Ask for the next move.")
                .font(.callout)
                .foregroundStyle(.white.opacity(0.82))
                .fixedSize(horizontal: false, vertical: true)
        }
        .cinematicCard(radius: 18)
    }

    private func actionButton(title: String, symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            actionContent(title: title, symbol: symbol)
        }
        .buttonStyle(.plain)
    }

    private func actionContent(title: String, symbol: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: symbol)
                .foregroundStyle(AppTheme.neonCyan)
            Text(title)
                .font(.callout.weight(.semibold))
                .lineLimit(2)
                .minimumScaleFactor(0.82)
            Spacer()
        }
        .frame(minHeight: 48)
        .cinematicCard(radius: 16)
    }
}
