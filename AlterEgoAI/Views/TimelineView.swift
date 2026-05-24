import SwiftData
import SwiftUI

struct TimelineView: View {
    @Environment(\.aiService) private var aiService
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @Query(sort: \TransformationSnapshot.createdAt, order: .reverse) private var snapshots: [TransformationSnapshot]
    @Query(sort: \Mission.dueDate, order: .reverse) private var missions: [Mission]
    @Query(sort: \JournalEntry.createdAt, order: .reverse) private var journals: [JournalEntry]
    @State private var weeklyReview = ""
    @State private var isReviewLoading = false
    @State private var reviewError: String?

    private var profile: UserProfile? { profiles.first }
    private var alterEgo: AlterEgoProfile? { alterEgos.first }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                reviewSection
                if snapshots.isEmpty {
                    EmptyStateView(title: "Timeline is waiting", message: "Complete missions and journal reflections to build your transformation record.", symbol: "sparkles.rectangle.stack")
                } else {
                    ForEach(snapshots) { snapshot in
                        timelineItem(snapshot)
                    }
                }
            }
            .padding(20)
        }
        .navigationTitle("Timeline")
        .background(CinematicBackground())
    }

    private var reviewSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label("Weekly review", systemImage: "wand.and.stars")
                    .font(.headline)
                Spacer()
                Button {
                    Task { await generateReview() }
                } label: {
                    if isReviewLoading {
                        ProgressView()
                    } else {
                        Text("Generate")
                            .font(.caption.bold())
                    }
                }
                .disabled(isReviewLoading)
            }

            Text(weeklyReview.isEmpty ? "Generate an AI progress summary from missions, streaks, and reflections." : weeklyReview)
                .font(.callout)
                .foregroundStyle(.white.opacity(0.82))
                .fixedSize(horizontal: false, vertical: true)

            if let reviewError {
                Text(reviewError)
                    .font(.caption)
                    .foregroundStyle(.red.opacity(0.9))
            }
        }
        .cinematicCard()
    }

    private func timelineItem(_ snapshot: TransformationSnapshot) -> some View {
        HStack(alignment: .top, spacing: 14) {
            VStack(spacing: 6) {
                Circle()
                    .fill(AppTheme.neonCyan)
                    .frame(width: 12, height: 12)
                Rectangle()
                    .fill(.white.opacity(0.16))
                    .frame(width: 2, height: 74)
            }

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(snapshot.title)
                        .font(.headline)
                    Spacer()
                    Text(snapshot.createdAt, style: .date)
                        .font(.caption)
                        .foregroundStyle(AppTheme.mutedText)
                }
                Text(snapshot.summary)
                    .font(.callout)
                    .foregroundStyle(.white.opacity(0.82))
                HStack(spacing: 12) {
                    Label("\(snapshot.xp) XP", systemImage: "bolt.fill")
                    Label("\(snapshot.streak) streak", systemImage: "flame.fill")
                }
                .font(.caption.bold())
                .foregroundStyle(AppTheme.neonCyan)
            }
            .cinematicCard(radius: 18)
        }
    }

    private func generateReview() async {
        guard let profile, let alterEgo else { return }
        isReviewLoading = true
        reviewError = nil
        do {
            weeklyReview = try await aiService.generateWeeklyReview(
                profile: profile,
                alterEgo: alterEgo,
                missions: missions,
                journals: journals
            )
        } catch {
            reviewError = error.localizedDescription
        }
        isReviewLoading = false
    }
}
