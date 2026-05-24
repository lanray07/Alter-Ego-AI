import SwiftData
import SwiftUI

struct MissionsView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @Query(sort: \Mission.dueDate, order: .forward) private var missions: [Mission]
    @StateObject private var homeViewModel = HomeViewModel()
    @State private var selectedCategory: MissionCategory?

    private var alterEgo: AlterEgoProfile? { alterEgos.first }
    private var filteredMissions: [Mission] {
        missions.filter { mission in
            selectedCategory == nil || mission.category == selectedCategory
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                header
                categoryPicker
                missionList
            }
            .padding(20)
        }
        .navigationTitle("Daily Missions")
        .background(CinematicBackground())
    }

    private var header: some View {
        HStack {
            InsightCard(title: "Completed", value: "\(missions.filter(\.completed).count)", symbol: "checkmark.seal", tint: AppTheme.success)
            InsightCard(title: "Open", value: "\(missions.filter { !$0.completed }.count)", symbol: "target", tint: AppTheme.neonCyan)
        }
    }

    private var categoryPicker: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                categoryButton(title: "All", symbol: "square.grid.2x2", isSelected: selectedCategory == nil) {
                    selectedCategory = nil
                }
                ForEach(MissionCategory.allCases) { category in
                    categoryButton(title: category.title, symbol: category.symbolName, isSelected: selectedCategory == category) {
                        selectedCategory = category
                    }
                }
            }
        }
    }

    private var missionList: some View {
        VStack(alignment: .leading, spacing: 12) {
            if filteredMissions.isEmpty {
                EmptyStateView(title: "No missions", message: "Generate a plan from the dashboard to fill this list.", symbol: "checklist")
            } else {
                ForEach(filteredMissions) { mission in
                    MissionCard(mission: mission) {
                        homeViewModel.completeMission(mission, alterEgo: alterEgo, missions: missions, context: modelContext)
                    }
                }
            }
        }
    }

    private func categoryButton(title: String, symbol: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Label(title, systemImage: symbol)
                .font(.caption.bold())
                .padding(.horizontal, 12)
                .padding(.vertical, 9)
                .background(Capsule().fill(isSelected ? AppTheme.neonCyan.opacity(0.22) : .white.opacity(0.08)))
                .overlay(Capsule().stroke(isSelected ? AppTheme.neonCyan : .white.opacity(0.1), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
