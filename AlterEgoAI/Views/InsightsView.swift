import Charts
import SwiftData
import SwiftUI

struct InsightsView: View {
    @Query(sort: \Mission.dueDate, order: .forward) private var missions: [Mission]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]

    private var alterEgo: AlterEgoProfile? { alterEgos.first }
    private var completed: [Mission] { missions.filter(\.completed) }
    private var missed: [Mission] {
        missions.filter { !$0.completed && $0.dueDate < Calendar.current.startOfDay(for: Date()) }
    }
    private var completionRate: Int {
        XPSystem.disciplineScore(completed: completed.count, missed: missed.count)
    }
    private var strongestCategory: String {
        categoryScores.sorted { $0.completed > $1.completed }.first?.category.title ?? "None"
    }
    private var weakestCategory: String {
        categoryScores.sorted { $0.rate < $1.rate }.first?.category.title ?? "None"
    }
    private var transformationScore: Int {
        min(100, completionRate + min(20, XPSystem.currentStreak(from: missions) * 2))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                topMetrics
                xpChart
                streakChart
                categorySection
            }
            .padding(20)
        }
        .navigationTitle("Insights")
        .background(CinematicBackground())
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                NavigationLink(destination: ShareCardsView()) {
                    Image(systemName: "square.and.arrow.up")
                }
                .accessibilityLabel("Share cards")
            }
        }
    }

    private var topMetrics: some View {
        VStack(spacing: 12) {
            HStack {
                InsightCard(title: "Consistency", value: "\(completionRate)%", symbol: "calendar.badge.checkmark", tint: AppTheme.success)
                InsightCard(title: "Score", value: "\(transformationScore)", symbol: "sparkles", tint: AppTheme.neonCyan)
            }
            HStack {
                InsightCard(title: "Strongest", value: strongestCategory, symbol: "arrow.up.right.circle", tint: AppTheme.success)
                InsightCard(title: "Weakest", value: weakestCategory, symbol: "arrow.down.right.circle", tint: AppTheme.warning)
            }
        }
    }

    private var xpChart: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("XP trend")
                .font(.headline)
            if xpTrend.isEmpty {
                EmptyStateView(title: "No XP yet", message: "Complete missions to chart your progress.", symbol: "chart.line.uptrend.xyaxis")
            } else {
                Chart(xpTrend) { point in
                    LineMark(
                        x: .value("Day", point.date, unit: .day),
                        y: .value("XP", point.xp)
                    )
                    .foregroundStyle(AppTheme.neonCyan)
                    AreaMark(
                        x: .value("Day", point.date, unit: .day),
                        y: .value("XP", point.xp)
                    )
                    .foregroundStyle(AppTheme.neonCyan.opacity(0.16))
                }
                .chartXAxis(.hidden)
                .frame(height: 210)
            }
        }
        .cinematicCard()
    }

    private var streakChart: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Weekly consistency")
                .font(.headline)
            Chart(weeklyConsistency) { point in
                BarMark(
                    x: .value("Day", point.label),
                    y: .value("Completed", point.completed)
                )
                .foregroundStyle(AppTheme.accentGradient)
            }
            .frame(height: 180)
        }
        .cinematicCard()
    }

    private var categorySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Category completion")
                .font(.headline)
            ForEach(categoryScores) { score in
                HStack {
                    Label(score.category.title, systemImage: score.category.symbolName)
                    Spacer()
                    Text("\(score.completed)/\(score.total)")
                        .foregroundStyle(AppTheme.mutedText)
                }
                ProgressView(value: score.rate)
                    .tint(score.rate > 0.6 ? AppTheme.success : AppTheme.neonCyan)
            }
        }
        .cinematicCard()
    }

    private var xpTrend: [XPPoint] {
        let grouped = Dictionary(grouping: completed) { Calendar.current.startOfDay(for: $0.completedAt ?? $0.dueDate) }
        return grouped.map { date, missions in
            XPPoint(date: date, xp: missions.map(\.xpReward).reduce(0, +))
        }
        .sorted { $0.date < $1.date }
    }

    private var weeklyConsistency: [ConsistencyPoint] {
        let calendar = Calendar.current
        return (0..<7).compactMap { offset in
            guard let date = calendar.date(byAdding: .day, value: -6 + offset, to: Date()) else { return nil }
            let day = calendar.startOfDay(for: date)
            let count = completed.filter { calendar.isDate($0.completedAt ?? $0.dueDate, inSameDayAs: day) }.count
            return ConsistencyPoint(label: weekdayLabel(for: day), completed: count)
        }
    }

    private var categoryScores: [CategoryScore] {
        MissionCategory.allCases.map { category in
            let categoryMissions = missions.filter { $0.category == category }
            let completedCount = categoryMissions.filter(\.completed).count
            return CategoryScore(category: category, completed: completedCount, total: max(1, categoryMissions.count))
        }
        .filter { $0.total > 1 || $0.completed > 0 }
    }

    private func weekdayLabel(for date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "E"
        return formatter.string(from: date)
    }
}

struct XPPoint: Identifiable {
    var id: Date { date }
    let date: Date
    let xp: Int
}

struct ConsistencyPoint: Identifiable {
    var id: String { label }
    let label: String
    let completed: Int
}

struct CategoryScore: Identifiable {
    var id: String { category.rawValue }
    let category: MissionCategory
    let completed: Int
    let total: Int
    var rate: Double { Double(completed) / Double(total) }
}
