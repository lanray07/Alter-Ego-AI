import Foundation

enum RemoteAIServiceError: LocalizedError {
    case invalidResponse
    case emptyContent

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "The AI backend returned an invalid response."
        case .emptyContent:
            return "The AI backend did not return content."
        }
    }
}

struct RemoteAIService: AIService {
    var endpoint: URL = AppConstants.backendEndpoint
    var session: URLSession = .shared

    func generateAlterEgoProfile(
        userName: String,
        currentIdentity: String,
        futureIdentity: String,
        goals: [GoalCategory],
        motivationStyle: MotivationStyle
    ) async throws -> GeneratedAlterEgoProfile {
        let response = try await perform(
            module: "alter_ego_profile",
            currentIdentity: currentIdentity,
            futureIdentity: futureIdentity,
            motivationStyle: motivationStyle.rawValue,
            goals: goals.map(\.rawValue),
            journalText: "",
            progressData: ["name": userName]
        )

        let traits = response.insights ?? ["Disciplined", "Focused", "Consistent"]
        let statement = response.summary ?? response.message ?? "I become my future self through identity-based action."
        return GeneratedAlterEgoProfile(
            alterEgoName: response.message?.components(separatedBy: "\n").first ?? "\(userName) Prime",
            identityStatement: statement,
            traits: traits,
            goals: goals.map(\.title)
        )
    }

    func generateDailyMissions(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        date: Date
    ) async throws -> [GeneratedMission] {
        let response = try await perform(
            module: "daily_missions",
            currentIdentity: profile.currentIdentity,
            futureIdentity: profile.futureIdentity,
            motivationStyle: profile.motivationStyle.rawValue,
            goals: profile.topGoals.map(\.rawValue),
            journalText: "",
            progressData: [
                "level": "\(alterEgo.level)",
                "totalXP": "\(alterEgo.totalXP)",
                "date": ISO8601DateFormatter().string(from: date)
            ]
        )
        guard let missions = response.missions, !missions.isEmpty else { throw RemoteAIServiceError.emptyContent }
        return missions
    }

    func generateFutureSelfReply(
        message: String,
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        recentMissions: [Mission]
    ) async throws -> String {
        let response = try await perform(
            module: "future_self_reply",
            currentIdentity: profile.currentIdentity,
            futureIdentity: profile.futureIdentity,
            motivationStyle: profile.motivationStyle.rawValue,
            goals: profile.topGoals.map(\.rawValue),
            journalText: message,
            progressData: [
                "alterEgoName": alterEgo.alterEgoName,
                "completedRecentMissions": "\(recentMissions.filter(\.completed).count)"
            ]
        )
        return response.message ?? response.summary ?? ""
    }

    func summarizeJournalEntry(text: String, mood: Mood, profile: UserProfile) async throws -> JournalInsight {
        let response = try await perform(
            module: "journal_summary",
            currentIdentity: profile.currentIdentity,
            futureIdentity: profile.futureIdentity,
            motivationStyle: profile.motivationStyle.rawValue,
            goals: profile.topGoals.map(\.rawValue),
            journalText: text,
            progressData: ["mood": mood.rawValue]
        )
        return JournalInsight(
            summary: response.summary ?? response.message ?? "",
            patternInsight: response.insights?.first ?? "Watch for the pattern behind the behavior.",
            nextAction: response.insights?.dropFirst().first ?? "Choose one small mission next."
        )
    }

    func generateWeeklyReview(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        missions: [Mission],
        journals: [JournalEntry]
    ) async throws -> String {
        let response = try await perform(
            module: "weekly_review",
            currentIdentity: profile.currentIdentity,
            futureIdentity: profile.futureIdentity,
            motivationStyle: profile.motivationStyle.rawValue,
            goals: profile.topGoals.map(\.rawValue),
            journalText: journals.map(\.content).joined(separator: "\n"),
            progressData: [
                "level": "\(alterEgo.level)",
                "completedMissions": "\(missions.filter(\.completed).count)"
            ]
        )
        return response.summary ?? response.message ?? ""
    }

    func generateComebackPlan(
        profile: UserProfile,
        missedDays: Int,
        motivationStyle: MotivationStyle
    ) async throws -> String {
        let response = try await perform(
            module: "comeback_plan",
            currentIdentity: profile.currentIdentity,
            futureIdentity: profile.futureIdentity,
            motivationStyle: motivationStyle.rawValue,
            goals: profile.topGoals.map(\.rawValue),
            journalText: "",
            progressData: ["missedDays": "\(missedDays)"]
        )
        return response.message ?? response.summary ?? ""
    }

    func generateShareCardText(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        context: String
    ) async throws -> String {
        let response = try await perform(
            module: "share_card",
            currentIdentity: profile.currentIdentity,
            futureIdentity: profile.futureIdentity,
            motivationStyle: profile.motivationStyle.rawValue,
            goals: profile.topGoals.map(\.rawValue),
            journalText: context,
            progressData: [
                "alterEgoName": alterEgo.alterEgoName,
                "level": "\(alterEgo.level)"
            ]
        )
        return response.message ?? response.summary ?? context
    }

    private func perform(
        module: String,
        currentIdentity: String,
        futureIdentity: String,
        motivationStyle: String,
        goals: [String],
        journalText: String,
        progressData: [String: String]
    ) async throws -> AIBackendResponse {
        var request = URLRequest(url: endpoint)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(AIBackendRequest(
            module: module,
            currentIdentity: currentIdentity,
            futureIdentity: futureIdentity,
            motivationStyle: motivationStyle,
            goals: goals,
            journalText: journalText,
            progressData: progressData
        ))

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, 200..<300 ~= httpResponse.statusCode else {
            throw RemoteAIServiceError.invalidResponse
        }
        return try JSONDecoder().decode(AIBackendResponse.self, from: data)
    }
}
