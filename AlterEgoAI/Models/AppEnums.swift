import Foundation

protocol TitledOption {
    var title: String { get }
}

enum GoalCategory: String, CaseIterable, Codable, Identifiable, TitledOption {
    case fitness
    case confidence
    case discipline
    case productivity
    case sleep
    case focus
    case learning
    case moneyHabits = "money habits"
    case socialConfidence = "social confidence"

    var id: String { rawValue }

    var title: String {
        switch self {
        case .moneyHabits:
            return "Money Habits"
        case .socialConfidence:
            return "Social Confidence"
        default:
            return rawValue.capitalized
        }
    }

    var symbolName: String {
        switch self {
        case .fitness:
            return "figure.strengthtraining.traditional"
        case .confidence:
            return "sparkles"
        case .discipline:
            return "shield.lefthalf.filled"
        case .productivity:
            return "checklist"
        case .sleep:
            return "moon.stars"
        case .focus:
            return "scope"
        case .learning:
            return "book"
        case .moneyHabits:
            return "chart.line.uptrend.xyaxis"
        case .socialConfidence:
            return "person.2"
        }
    }
}

enum MotivationStyle: String, CaseIterable, Codable, Identifiable, TitledOption {
    case gentle
    case strict
    case cinematic
    case tactical
    case encouraging

    var id: String { rawValue }
    var title: String { rawValue.capitalized }
}

enum MissionCategory: String, CaseIterable, Codable, Identifiable, TitledOption {
    case body
    case mind
    case focus
    case sleep
    case confidence
    case learning
    case money
    case social
    case discipline

    var id: String { rawValue }
    var title: String { rawValue.capitalized }

    var symbolName: String {
        switch self {
        case .body:
            return "figure.run"
        case .mind:
            return "brain.head.profile"
        case .focus:
            return "timer"
        case .sleep:
            return "bed.double"
        case .confidence:
            return "bolt.heart"
        case .learning:
            return "graduationcap"
        case .money:
            return "creditcard"
        case .social:
            return "bubble.left.and.bubble.right"
        case .discipline:
            return "target"
        }
    }
}

enum MissionDifficulty: String, CaseIterable, Codable, Identifiable, TitledOption {
    case easy
    case medium
    case hard

    var id: String { rawValue }
    var title: String { rawValue.capitalized }
}

enum AvatarStage: String, CaseIterable, Codable, Identifiable, TitledOption {
    case drifter = "Drifter"
    case awakening = "Awakening"
    case focused = "Focused"
    case disciplined = "Disciplined"
    case ascendant = "Ascendant"
    case apexSelf = "Apex Self"

    var id: String { rawValue }
    var title: String { rawValue }
}

enum ChatRole: String, Codable {
    case user
    case assistant
    case system
}

enum Mood: String, CaseIterable, Codable, Identifiable, TitledOption {
    case charged
    case calm
    case focused
    case tired
    case stuck

    var id: String { rawValue }
    var title: String { rawValue.capitalized }
}

enum SubscriptionPlan: String, CaseIterable, Codable, Identifiable, TitledOption {
    case free
    case pro
    case elite

    var id: String { rawValue }
    var title: String { rawValue.capitalized }
}
