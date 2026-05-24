import Foundation
import SwiftData

@Model
final class UserProfile {
    @Attribute(.unique) var id: UUID
    var name: String
    var currentIdentity: String
    var futureIdentity: String
    var motivationStyleRaw: String
    var dailyAvailability: Int
    var notificationPreference: Bool
    var topGoalsRaw: String
    var createdAt: Date

    init(
        id: UUID = UUID(),
        name: String,
        currentIdentity: String,
        futureIdentity: String,
        motivationStyle: MotivationStyle,
        dailyAvailability: Int,
        notificationPreference: Bool,
        topGoals: [GoalCategory],
        createdAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.currentIdentity = currentIdentity
        self.futureIdentity = futureIdentity
        self.motivationStyleRaw = motivationStyle.rawValue
        self.dailyAvailability = dailyAvailability
        self.notificationPreference = notificationPreference
        self.topGoalsRaw = topGoals.map(\.rawValue).joined(separator: "|")
        self.createdAt = createdAt
    }

    var motivationStyle: MotivationStyle {
        get { MotivationStyle(rawValue: motivationStyleRaw) ?? .cinematic }
        set { motivationStyleRaw = newValue.rawValue }
    }

    var topGoals: [GoalCategory] {
        get { topGoalsRaw.split(separator: "|").compactMap { GoalCategory(rawValue: String($0)) } }
        set { topGoalsRaw = newValue.map(\.rawValue).joined(separator: "|") }
    }
}

@Model
final class AlterEgoProfile {
    @Attribute(.unique) var id: UUID
    var userId: UUID
    var alterEgoName: String
    var identityStatement: String
    var avatarStageRaw: String
    var level: Int
    var totalXP: Int
    var traitsRaw: String
    var goalsRaw: String
    var createdAt: Date

    init(
        id: UUID = UUID(),
        userId: UUID,
        alterEgoName: String,
        identityStatement: String,
        avatarStage: AvatarStage,
        level: Int,
        totalXP: Int,
        traits: [String],
        goals: [String],
        createdAt: Date = Date()
    ) {
        self.id = id
        self.userId = userId
        self.alterEgoName = alterEgoName
        self.identityStatement = identityStatement
        self.avatarStageRaw = avatarStage.rawValue
        self.level = level
        self.totalXP = totalXP
        self.traitsRaw = traits.joined(separator: "|")
        self.goalsRaw = goals.joined(separator: "|")
        self.createdAt = createdAt
    }

    var avatarStage: AvatarStage {
        get { AvatarStage(rawValue: avatarStageRaw) ?? .drifter }
        set { avatarStageRaw = newValue.rawValue }
    }

    var traits: [String] {
        get { traitsRaw.split(separator: "|").map(String.init) }
        set { traitsRaw = newValue.joined(separator: "|") }
    }

    var goals: [String] {
        get { goalsRaw.split(separator: "|").map(String.init) }
        set { goalsRaw = newValue.joined(separator: "|") }
    }
}

@Model
final class Mission {
    @Attribute(.unique) var id: UUID
    var title: String
    var categoryRaw: String
    var difficultyRaw: String
    var xpReward: Int
    var dueDate: Date
    var completed: Bool
    var completedAt: Date?

    init(
        id: UUID = UUID(),
        title: String,
        category: MissionCategory,
        difficulty: MissionDifficulty,
        xpReward: Int,
        dueDate: Date,
        completed: Bool = false,
        completedAt: Date? = nil
    ) {
        self.id = id
        self.title = title
        self.categoryRaw = category.rawValue
        self.difficultyRaw = difficulty.rawValue
        self.xpReward = xpReward
        self.dueDate = dueDate
        self.completed = completed
        self.completedAt = completedAt
    }

    var category: MissionCategory {
        get { MissionCategory(rawValue: categoryRaw) ?? .discipline }
        set { categoryRaw = newValue.rawValue }
    }

    var difficulty: MissionDifficulty {
        get { MissionDifficulty(rawValue: difficultyRaw) ?? .easy }
        set { difficultyRaw = newValue.rawValue }
    }
}

@Model
final class JournalEntry {
    @Attribute(.unique) var id: UUID
    var moodRaw: String
    var content: String
    var wins: String
    var struggles: String
    var lessonLearned: String
    var aiSummary: String
    var patternInsight: String
    var nextRecommendedAction: String
    var createdAt: Date

    init(
        id: UUID = UUID(),
        mood: Mood,
        content: String,
        wins: String,
        struggles: String,
        lessonLearned: String,
        aiSummary: String,
        patternInsight: String,
        nextRecommendedAction: String,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.moodRaw = mood.rawValue
        self.content = content
        self.wins = wins
        self.struggles = struggles
        self.lessonLearned = lessonLearned
        self.aiSummary = aiSummary
        self.patternInsight = patternInsight
        self.nextRecommendedAction = nextRecommendedAction
        self.createdAt = createdAt
    }

    var mood: Mood {
        get { Mood(rawValue: moodRaw) ?? .focused }
        set { moodRaw = newValue.rawValue }
    }
}

@Model
final class ChatMessage {
    @Attribute(.unique) var id: UUID
    var roleRaw: String
    var content: String
    var createdAt: Date

    init(id: UUID = UUID(), role: ChatRole, content: String, createdAt: Date = Date()) {
        self.id = id
        self.roleRaw = role.rawValue
        self.content = content
        self.createdAt = createdAt
    }

    var role: ChatRole {
        get { ChatRole(rawValue: roleRaw) ?? .assistant }
        set { roleRaw = newValue.rawValue }
    }
}

@Model
final class TransformationSnapshot {
    @Attribute(.unique) var id: UUID
    var title: String
    var summary: String
    var xp: Int
    var streak: Int
    var createdAt: Date

    init(
        id: UUID = UUID(),
        title: String,
        summary: String,
        xp: Int,
        streak: Int,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.summary = summary
        self.xp = xp
        self.streak = streak
        self.createdAt = createdAt
    }
}

@Model
final class Achievement {
    @Attribute(.unique) var id: UUID
    var title: String
    var achievementDescription: String
    var unlocked: Bool
    var unlockedAt: Date?

    init(
        id: UUID = UUID(),
        title: String,
        description: String,
        unlocked: Bool = false,
        unlockedAt: Date? = nil
    ) {
        self.id = id
        self.title = title
        self.achievementDescription = description
        self.unlocked = unlocked
        self.unlockedAt = unlockedAt
    }
}

@Model
final class SubscriptionState {
    @Attribute(.unique) var id: UUID
    var planRaw: String
    var isActive: Bool
    var renewsAt: Date?

    init(
        id: UUID = UUID(),
        plan: SubscriptionPlan,
        isActive: Bool,
        renewsAt: Date? = nil
    ) {
        self.id = id
        self.planRaw = plan.rawValue
        self.isActive = isActive
        self.renewsAt = renewsAt
    }

    var plan: SubscriptionPlan {
        get { SubscriptionPlan(rawValue: planRaw) ?? .free }
        set { planRaw = newValue.rawValue }
    }
}
