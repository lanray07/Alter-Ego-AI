import Foundation

struct MockAIService: AIService {
    func generateAlterEgoProfile(
        userName: String,
        currentIdentity: String,
        futureIdentity: String,
        goals: [GoalCategory],
        motivationStyle: MotivationStyle
    ) async throws -> GeneratedAlterEgoProfile {
        let trimmedName = userName.trimmingCharacters(in: .whitespacesAndNewlines)
        let baseName = trimmedName.isEmpty ? "Apex" : trimmedName.components(separatedBy: " ").first ?? trimmedName
        let coreGoal = goals.first?.title ?? "Discipline"

        return GeneratedAlterEgoProfile(
            alterEgoName: "\(baseName) Prime",
            identityStatement: "I am becoming the version of myself who acts with \(motivationStyle.title.lowercased()) discipline, protects my energy, and proves \(futureIdentity.lowercased()) through daily action.",
            traits: ["Disciplined", "Focused", "Composed", "Consistent", "Brave"],
            goals: goals.map(\.title) + [coreGoal]
        )
    }

    func generateDailyMissions(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        date: Date
    ) async throws -> [GeneratedMission] {
        let preferred = profile.topGoals
        var missions: [GeneratedMission] = [
            GeneratedMission(title: "20-minute workout", category: .body, difficulty: .medium, xpReward: 60),
            GeneratedMission(title: "Drink water before your first scroll", category: .body, difficulty: .easy, xpReward: 25),
            GeneratedMission(title: "30-minute focus session", category: .focus, difficulty: .medium, xpReward: 55),
            GeneratedMission(title: "Read 10 pages", category: .learning, difficulty: .easy, xpReward: 35),
            GeneratedMission(title: "Journal the identity you practiced today", category: .mind, difficulty: .easy, xpReward: 35),
            GeneratedMission(title: "No scrolling challenge for one hour", category: .discipline, difficulty: .hard, xpReward: 80),
            GeneratedMission(title: "Clean your workspace", category: .discipline, difficulty: .easy, xpReward: 30),
            GeneratedMission(title: "Apply for one opportunity", category: .money, difficulty: .hard, xpReward: 85),
            GeneratedMission(title: "Send one confident message", category: .social, difficulty: .medium, xpReward: 50)
        ]

        for goal in preferred {
            switch goal {
            case .sleep:
                missions.insert(GeneratedMission(title: "Sleep before your target time", category: .sleep, difficulty: .medium, xpReward: 60), at: 0)
            case .confidence:
                missions.insert(GeneratedMission(title: "Do one small action you are avoiding", category: .confidence, difficulty: .medium, xpReward: 65), at: 0)
            case .productivity:
                missions.insert(GeneratedMission(title: "Plan your top 3 before noon", category: .focus, difficulty: .easy, xpReward: 40), at: 0)
            case .moneyHabits:
                missions.insert(GeneratedMission(title: "Review one spending decision", category: .money, difficulty: .easy, xpReward: 35), at: 0)
            default:
                continue
            }
        }

        let offset = Calendar.current.component(.day, from: date) % max(1, missions.count)
        let rotated = Array(missions.dropFirst(offset)) + Array(missions.prefix(offset))
        return Array(rotated.prefix(5))
    }

    func generateFutureSelfReply(
        message: String,
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        recentMissions: [Mission]
    ) async throws -> String {
        let completed = recentMissions.filter(\.completed).count
        let styleLine: String
        switch profile.motivationStyle {
        case .gentle:
            styleLine = "Start small, but start honestly."
        case .strict:
            styleLine = "Your standards are not punishment. They are proof."
        case .cinematic:
            styleLine = "This is the scene where you stop negotiating with the old script."
        case .tactical:
            styleLine = "Choose the next visible action, set a timer, execute."
        case .encouraging:
            styleLine = "You are closer than your tired mind is telling you."
        }

        return """
        \(alterEgo.alterEgoName): I heard you. The old identity says, "\(message.prefix(90))". The future identity answers with action.

        You have completed \(completed) recent missions. Today, win the next 20 minutes. \(styleLine)
        """
    }

    func summarizeJournalEntry(text: String, mood: Mood, profile: UserProfile) async throws -> JournalInsight {
        JournalInsight(
            summary: "You noticed your \(mood.title.lowercased()) state and translated it into self-awareness instead of autopilot.",
            patternInsight: "Your strongest pattern appears when you name the struggle plainly, then choose one concrete action.",
            nextAction: "Pick one mission that takes under 20 minutes and complete it before your next long break."
        )
    }

    func generateWeeklyReview(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        missions: [Mission],
        journals: [JournalEntry]
    ) async throws -> String {
        let completed = missions.filter(\.completed).count
        let xp = missions.filter(\.completed).map(\.xpReward).reduce(0, +)
        return "This week, \(profile.name) completed \(completed) missions and earned \(xp) XP. The signal is clear: \(alterEgo.alterEgoName) grows when action stays small enough to repeat and meaningful enough to respect."
    }

    func generateComebackPlan(
        profile: UserProfile,
        missedDays: Int,
        motivationStyle: MotivationStyle
    ) async throws -> String {
        "Comeback plan: forgive the gap, protect the next hour, complete one body mission, one focus mission, and one reflection. Missed days are data, not identity."
    }

    func generateShareCardText(
        profile: UserProfile,
        alterEgo: AlterEgoProfile,
        context: String
    ) async throws -> String {
        "\(context)\n\(alterEgo.alterEgoName) - Level \(alterEgo.level) \(alterEgo.avatarStage.title)\n\(AppConstants.viralHook)"
    }
}
