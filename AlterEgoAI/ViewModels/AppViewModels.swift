import Combine
import Foundation
import SwiftData

@MainActor
final class OnboardingViewModel: ObservableObject {
    @Published var name = ""
    @Published var currentIdentity = ""
    @Published var futureIdentity = ""
    @Published var selectedGoals: Set<GoalCategory> = [.discipline, .focus, .fitness]
    @Published var motivationStyle: MotivationStyle = .cinematic
    @Published var dailyAvailability = 30
    @Published var notificationPreference = true
    @Published var isGenerating = false
    @Published var errorMessage: String?

    var canSubmit: Bool {
        !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !currentIdentity.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !futureIdentity.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        selectedGoals.count == 3
    }

    func toggleGoal(_ goal: GoalCategory) {
        if selectedGoals.contains(goal) {
            selectedGoals.remove(goal)
        } else if selectedGoals.count < 3 {
            selectedGoals.insert(goal)
        }
    }

    func complete(context: ModelContext, aiService: any AIService) async {
        guard canSubmit else { return }
        isGenerating = true
        errorMessage = nil

        do {
            let sortedGoals = selectedGoals.sorted { $0.title < $1.title }
            let profile = UserProfile(
                name: name.trimmingCharacters(in: .whitespacesAndNewlines),
                currentIdentity: currentIdentity.trimmingCharacters(in: .whitespacesAndNewlines),
                futureIdentity: futureIdentity.trimmingCharacters(in: .whitespacesAndNewlines),
                motivationStyle: motivationStyle,
                dailyAvailability: dailyAvailability,
                notificationPreference: notificationPreference,
                topGoals: sortedGoals
            )

            let generated = try await aiService.generateAlterEgoProfile(
                userName: profile.name,
                currentIdentity: profile.currentIdentity,
                futureIdentity: profile.futureIdentity,
                goals: sortedGoals,
                motivationStyle: motivationStyle
            )

            let alterEgo = AlterEgoProfile(
                userId: profile.id,
                alterEgoName: generated.alterEgoName,
                identityStatement: generated.identityStatement,
                avatarStage: .drifter,
                level: 1,
                totalXP: 0,
                traits: generated.traits,
                goals: generated.goals
            )

            context.insert(profile)
            context.insert(alterEgo)
            context.insert(SubscriptionState(plan: .free, isActive: false))
            context.insert(ChatMessage(
                role: .assistant,
                content: "\(generated.alterEgoName): I am the future self you are building. Start with one mission. Keep the promise small enough to keep."
            ))
            context.insert(TransformationSnapshot(
                title: "Day 1 Baseline",
                summary: "Current identity: \(profile.currentIdentity). Future identity: \(profile.futureIdentity).",
                xp: 0,
                streak: 0
            ))

            seedAchievements(into: context)
            try await seedSevenDayPlan(profile: profile, alterEgo: alterEgo, context: context, aiService: aiService)

            if notificationPreference {
                let granted = await NotificationService.shared.requestAuthorization()
                if granted {
                    await NotificationService.shared.scheduleDailyMissionReminder()
                }
            }

            try context.save()
        } catch {
            errorMessage = error.localizedDescription
        }

        isGenerating = false
    }

    private func seedSevenDayPlan(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        context: ModelContext,
        aiService: any AIService
    ) async throws {
        for day in 0..<7 {
            guard let dueDate = Calendar.current.date(byAdding: .day, value: day, to: Date()) else { continue }
            let missions = try await aiService.generateDailyMissions(profile: profile, alterEgo: alterEgo, date: dueDate)
            for mission in missions.prefix(3) {
                context.insert(Mission(
                    title: mission.title,
                    category: mission.category,
                    difficulty: mission.difficulty,
                    xpReward: mission.xpReward,
                    dueDate: dueDate
                ))
            }
        }
    }

    private func seedAchievements(into context: ModelContext) {
        [
            Achievement(title: "First Mission", description: "Complete your first identity-based mission."),
            Achievement(title: "Three-Day Signal", description: "Complete at least one mission for three days."),
            Achievement(title: "Focused Level 10", description: "Reach Level 10 and unlock the Focused stage."),
            Achievement(title: "Thirty Missions", description: "Complete 30 missions in one week."),
            Achievement(title: "Comeback Architect", description: "Create a comeback plan after missed days.")
        ].forEach { context.insert($0) }
    }
}

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var isGeneratingPlan = false
    @Published var errorMessage: String?

    func completeMission(
        _ mission: Mission,
        alterEgo: AlterEgoProfile?,
        missions: [Mission],
        context: ModelContext
    ) {
        guard !mission.completed else { return }
        mission.completed = true
        mission.completedAt = Date()

        if let alterEgo {
            let oldLevel = alterEgo.level
            alterEgo.totalXP += mission.xpReward
            alterEgo.level = XPSystem.level(for: alterEgo.totalXP)
            alterEgo.avatarStage = XPSystem.avatarStage(for: alterEgo.level)

            let streak = XPSystem.currentStreak(from: missions + [mission])
            if alterEgo.level > oldLevel || streak > 0 {
                context.insert(TransformationSnapshot(
                    title: alterEgo.level > oldLevel ? "Level \(alterEgo.level) Unlocked" : "Mission Complete",
                    summary: "Completed \(mission.title) and earned \(mission.xpReward) XP.",
                    xp: alterEgo.totalXP,
                    streak: streak
                ))
            }
        }

        unlockAchievementsIfNeeded(missions: missions, context: context)
        try? context.save()
    }

    func generateTodayPlan(
        profile: UserProfile?,
        alterEgo: AlterEgoProfile?,
        existingMissions: [Mission],
        context: ModelContext,
        aiService: any AIService
    ) async {
        guard let profile, let alterEgo else { return }
        isGeneratingPlan = true
        errorMessage = nil

        do {
            let todayCount = existingMissions.filter { Calendar.current.isDateInToday($0.dueDate) }.count
            let missionLimit = todayCount == 0 ? 3 : 2
            let generated = try await aiService.generateDailyMissions(profile: profile, alterEgo: alterEgo, date: Date())
            for mission in generated.prefix(missionLimit) {
                context.insert(Mission(
                    title: mission.title,
                    category: mission.category,
                    difficulty: mission.difficulty,
                    xpReward: mission.xpReward,
                    dueDate: Date()
                ))
            }
            try context.save()
        } catch {
            errorMessage = error.localizedDescription
        }

        isGeneratingPlan = false
    }

    private func unlockAchievementsIfNeeded(missions: [Mission], context: ModelContext) {
        let completedCount = missions.filter(\.completed).count
        let descriptor = FetchDescriptor<Achievement>(sortBy: [SortDescriptor(\.title)])
        guard let achievements = try? context.fetch(descriptor) else { return }

        func unlock(title: String) {
            guard let achievement = achievements.first(where: { $0.title == title && !$0.unlocked }) else { return }
            achievement.unlocked = true
            achievement.unlockedAt = Date()
        }

        if completedCount >= 1 { unlock(title: "First Mission") }
        if completedCount >= 30 { unlock(title: "Thirty Missions") }
    }
}

@MainActor
final class ChatViewModel: ObservableObject {
    @Published var draft = ""
    @Published var isSending = false
    @Published var errorMessage: String?

    func send(
        profile: UserProfile?,
        alterEgo: AlterEgoProfile?,
        missions: [Mission],
        context: ModelContext,
        aiService: any AIService
    ) async {
        let trimmed = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        guard let profile, let alterEgo else { return }

        draft = ""
        errorMessage = nil
        isSending = true

        context.insert(ChatMessage(role: .user, content: trimmed))
        do {
            let reply = try await aiService.generateFutureSelfReply(
                message: trimmed,
                profile: profile,
                alterEgo: alterEgo,
                recentMissions: missions
            )
            context.insert(ChatMessage(role: .assistant, content: reply))
            try context.save()
        } catch {
            errorMessage = error.localizedDescription
        }

        isSending = false
    }
}

@MainActor
final class JournalViewModel: ObservableObject {
    @Published var mood: Mood = .focused
    @Published var content = ""
    @Published var wins = ""
    @Published var struggles = ""
    @Published var lessonLearned = ""
    @Published var isSaving = false
    @Published var errorMessage: String?

    var canSave: Bool {
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
        !wins.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
        !struggles.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    func save(
        profile: UserProfile?,
        alterEgo: AlterEgoProfile?,
        context: ModelContext,
        aiService: any AIService
    ) async {
        guard let profile, canSave else { return }
        isSaving = true
        errorMessage = nil

        do {
            let combinedText = [content, wins, struggles, lessonLearned].filter { !$0.isEmpty }.joined(separator: "\n")
            let insight = try await aiService.summarizeJournalEntry(text: combinedText, mood: mood, profile: profile)
            context.insert(JournalEntry(
                mood: mood,
                content: content,
                wins: wins,
                struggles: struggles,
                lessonLearned: lessonLearned,
                aiSummary: insight.summary,
                patternInsight: insight.patternInsight,
                nextRecommendedAction: insight.nextAction
            ))

            if let alterEgo {
                context.insert(TransformationSnapshot(
                    title: "Reflection Logged",
                    summary: insight.summary,
                    xp: alterEgo.totalXP,
                    streak: 0
                ))
            }

            try context.save()
            reset()
        } catch {
            errorMessage = error.localizedDescription
        }

        isSaving = false
    }

    private func reset() {
        mood = .focused
        content = ""
        wins = ""
        struggles = ""
        lessonLearned = ""
    }
}

@MainActor
final class ShareCardsViewModel: ObservableObject {
    @Published var selectedTemplate = "Day 7 of becoming my alter ego"
    @Published var generatedText = ""
    @Published var isGenerating = false
    @Published var errorMessage: String?

    let templates = [
        "Day 7 of becoming my alter ego",
        "My future self unlocked Discipline Level 10",
        "I completed 30 missions this week",
        "Current identity vs future identity",
        "My comeback plan"
    ]

    func generate(profile: UserProfile?, alterEgo: AlterEgoProfile?, aiService: any AIService) async {
        guard let profile, let alterEgo else { return }
        isGenerating = true
        errorMessage = nil

        do {
            generatedText = try await aiService.generateShareCardText(
                profile: profile,
                alterEgo: alterEgo,
                context: selectedTemplate
            )
        } catch {
            errorMessage = error.localizedDescription
        }

        isGenerating = false
    }
}
