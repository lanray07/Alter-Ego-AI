import Foundation
import SwiftUI

protocol AIService {
    func generateAlterEgoProfile(
        userName: String,
        currentIdentity: String,
        futureIdentity: String,
        goals: [GoalCategory],
        motivationStyle: MotivationStyle
    ) async throws -> GeneratedAlterEgoProfile

    func generateDailyMissions(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        date: Date
    ) async throws -> [GeneratedMission]

    func generateFutureSelfReply(
        message: String,
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        recentMissions: [Mission]
    ) async throws -> String

    func summarizeJournalEntry(
        text: String,
        mood: Mood,
        profile: UserProfile
    ) async throws -> JournalInsight

    func generateWeeklyReview(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        missions: [Mission],
        journals: [JournalEntry]
    ) async throws -> String

    func generateComebackPlan(
        profile: UserProfile,
        missedDays: Int,
        motivationStyle: MotivationStyle
    ) async throws -> String

    func generateShareCardText(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        context: String
    ) async throws -> String
}

private struct AIServiceEnvironmentKey: EnvironmentKey {
    static let defaultValue: any AIService = MockAIService()
}

extension EnvironmentValues {
    var aiService: any AIService {
        get { self[AIServiceEnvironmentKey.self] }
        set { self[AIServiceEnvironmentKey.self] = newValue }
    }
}
