import SwiftData
import SwiftUI

struct AppRootView: View {
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]

    var body: some View {
        ZStack {
            CinematicBackground()
            if profiles.isEmpty {
                OnboardingView()
            } else {
                MainTabView()
            }
        }
    }
}

enum AppTab: String, CaseIterable, Identifiable {
    case home
    case missions
    case chat
    case timeline
    case insights

    var id: String { rawValue }

    var title: String {
        switch self {
        case .home:
            return "Home"
        case .missions:
            return "Missions"
        case .chat:
            return "AI"
        case .timeline:
            return "Timeline"
        case .insights:
            return "Insights"
        }
    }

    var symbol: String {
        switch self {
        case .home:
            return "house"
        case .missions:
            return "checklist"
        case .chat:
            return "message"
        case .timeline:
            return "sparkles.rectangle.stack"
        case .insights:
            return "chart.xyaxis.line"
        }
    }
}

struct MainTabView: View {
    @State private var selectedTab: AppTab = .home

    var body: some View {
        TabView(selection: $selectedTab) {
            ForEach(AppTab.allCases) { tab in
                NavigationStack {
                    content(for: tab)
                        .toolbar {
                            ToolbarItem(placement: .topBarTrailing) {
                                NavigationLink(destination: SettingsView()) {
                                    Image(systemName: "gearshape")
                                }
                                .accessibilityLabel("Settings")
                            }
                        }
                }
                .tabItem {
                    Label(tab.title, systemImage: tab.symbol)
                }
                .tag(tab)
            }
        }
        .tint(AppTheme.neonCyan)
    }

    @ViewBuilder
    private func content(for tab: AppTab) -> some View {
        switch tab {
        case .home:
            HomeDashboardView()
        case .missions:
            MissionsView()
        case .chat:
            ChatView()
        case .timeline:
            TimelineView()
        case .insights:
            InsightsView()
        }
    }
}
