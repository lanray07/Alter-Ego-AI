import Foundation

enum XPSystem {
    static func level(for totalXP: Int) -> Int {
        max(1, min(50, (totalXP / 250) + 1))
    }

    static func title(for level: Int) -> String {
        switch level {
        case 50...:
            return "Apex Self"
        case 35...:
            return "Ascendant"
        case 20...:
            return "Disciplined"
        case 10...:
            return "Focused"
        case 5...:
            return "Initiate"
        default:
            return "Drifter"
        }
    }

    static func avatarStage(for level: Int) -> AvatarStage {
        switch level {
        case 50...:
            return .apexSelf
        case 35...:
            return .ascendant
        case 20...:
            return .disciplined
        case 10...:
            return .focused
        case 5...:
            return .awakening
        default:
            return .drifter
        }
    }

    static func progressToNextLevel(totalXP: Int) -> Double {
        let xpIntoLevel = totalXP % 250
        return Double(xpIntoLevel) / 250.0
    }

    static func disciplineScore(completed: Int, missed: Int) -> Int {
        let total = max(1, completed + missed)
        return min(100, max(0, Int((Double(completed) / Double(total)) * 100)))
    }

    static func currentStreak(from missions: [Mission], calendar: Calendar = .current) -> Int {
        let completedDays = Set(
            missions
                .filter(\.completed)
                .map { calendar.startOfDay(for: $0.dueDate) }
        )

        var streak = 0
        var day = calendar.startOfDay(for: Date())
        while completedDays.contains(day) {
            streak += 1
            guard let previous = calendar.date(byAdding: .day, value: -1, to: day) else { break }
            day = previous
        }
        return streak
    }
}
