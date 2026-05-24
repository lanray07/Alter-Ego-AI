import SwiftData
import SwiftUI

struct AlterEgoProfileView: View {
    @Query(sort: \UserProfile.createdAt, order: .forward) private var profiles: [UserProfile]
    @Query(sort: \AlterEgoProfile.createdAt, order: .forward) private var alterEgos: [AlterEgoProfile]
    @Query(sort: \Achievement.title, order: .forward) private var achievements: [Achievement]

    private var profile: UserProfile? { profiles.first }
    private var alterEgo: AlterEgoProfile? { alterEgos.first }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if let alterEgo {
                    AlterEgoCard(alterEgo: alterEgo, subtitle: profile?.futureIdentity ?? "Future self")
                    identitySection(alterEgo: alterEgo)
                    stagesSection(current: alterEgo.avatarStage)
                    achievementsSection
                } else {
                    EmptyStateView(title: "Profile unavailable", message: "Create an Alter Ego profile from onboarding.", symbol: "person.crop.circle")
                }
            }
            .padding(20)
        }
        .navigationTitle("Future Self")
        .background(CinematicBackground())
    }

    private func identitySection(alterEgo: AlterEgoProfile) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Identity statement")
                .font(.headline)
            Text(alterEgo.identityStatement)
                .font(.body)
                .foregroundStyle(.white.opacity(0.82))

            Text("Traits")
                .font(.headline)
                .padding(.top, 4)
            FlowLayout(items: alterEgo.traits)

            Text("Goals")
                .font(.headline)
                .padding(.top, 4)
            FlowLayout(items: alterEgo.goals)
        }
        .cinematicCard()
    }

    private func stagesSection(current: AvatarStage) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Avatar evolution")
                .font(.headline)
            ForEach(AvatarStage.allCases) { stage in
                HStack {
                    AvatarGlyph(stage: stage)
                        .scaleEffect(0.48)
                        .frame(width: 38, height: 38)
                    Text(stage.title)
                    Spacer()
                    Image(systemName: stageIndex(stage) <= stageIndex(current) ? "checkmark.seal.fill" : "lock")
                        .foregroundStyle(stageIndex(stage) <= stageIndex(current) ? AppTheme.success : .white.opacity(0.38))
                }
            }
        }
        .cinematicCard()
    }

    private var achievementsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Unlocked milestones")
                .font(.headline)
            if achievements.isEmpty {
                Text("Milestones will appear as your identity compounds.")
                    .foregroundStyle(AppTheme.mutedText)
            } else {
                ForEach(achievements) { achievement in
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: achievement.unlocked ? "checkmark.seal.fill" : "lock")
                            .foregroundStyle(achievement.unlocked ? AppTheme.success : .white.opacity(0.42))
                        VStack(alignment: .leading, spacing: 4) {
                            Text(achievement.title)
                                .font(.headline)
                            Text(achievement.achievementDescription)
                                .font(.caption)
                                .foregroundStyle(AppTheme.mutedText)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 4)
                }
            }
        }
        .cinematicCard()
    }

    private func stageIndex(_ stage: AvatarStage) -> Int {
        AvatarStage.allCases.firstIndex(of: stage) ?? 0
    }
}

struct FlowLayout: View {
    let items: [String]

    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 120), spacing: 8)], spacing: 8) {
            ForEach(items, id: \.self) { item in
                Text(item)
                    .font(.caption.bold())
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(Capsule().fill(.white.opacity(0.08)))
            }
        }
    }
}
