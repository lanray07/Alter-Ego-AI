package com.alteregoai.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alteregoai.app.core.DateUtils
import java.util.UUID

enum class GoalCategory(val title: String) {
    FITNESS("Fitness"), CONFIDENCE("Confidence"), DISCIPLINE("Discipline"),
    PRODUCTIVITY("Productivity"), SLEEP("Sleep"), FOCUS("Focus"), LEARNING("Learning"),
    MONEY_HABITS("Money Habits"), SOCIAL_CONFIDENCE("Social Confidence")
}

enum class MotivationStyle(val title: String) { GENTLE("Gentle"), STRICT("Strict"), CINEMATIC("Cinematic"), TACTICAL("Tactical"), ENCOURAGING("Encouraging") }
enum class MissionCategory(val title: String) { BODY("Body"), MIND("Mind"), FOCUS("Focus"), SLEEP("Sleep"), CONFIDENCE("Confidence"), LEARNING("Learning"), MONEY("Money"), SOCIAL("Social"), DISCIPLINE("Discipline") }
enum class MissionDifficulty(val title: String) { EASY("Easy"), MEDIUM("Medium"), HARD("Hard") }
enum class AvatarStage(val title: String) { DRIFTER("Drifter"), AWAKENING("Awakening"), FOCUSED("Focused"), DISCIPLINED("Disciplined"), ASCENDANT("Ascendant"), APEX_SELF("Apex Self") }
enum class ChatRole { USER, ASSISTANT, SYSTEM }
enum class Mood(val title: String) { CHARGED("Charged"), CALM("Calm"), FOCUSED("Focused"), TIRED("Tired"), STUCK("Stuck") }
enum class SubscriptionPlan(val title: String) { FREE("Free"), PRO("Pro"), ELITE("Elite") }

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val currentIdentity: String,
    val futureIdentity: String,
    val motivationStyle: String,
    val dailyAvailability: Int,
    val notificationPreference: Boolean,
    val topGoals: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun goals(): List<GoalCategory> = topGoals.split('|').mapNotNull { value -> GoalCategory.entries.firstOrNull { it.name == value } }
    fun style(): MotivationStyle = MotivationStyle.entries.firstOrNull { it.name == motivationStyle } ?: MotivationStyle.CINEMATIC
}

@Entity(tableName = "alter_ego_profiles")
data class AlterEgoProfileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val alterEgoName: String,
    val identityStatement: String,
    val avatarStage: String,
    val level: Int,
    val totalXp: Int,
    val traits: String,
    val goals: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun stage(): AvatarStage = AvatarStage.entries.firstOrNull { it.name == avatarStage } ?: AvatarStage.DRIFTER
    fun traitList(): List<String> = traits.split('|').filter(String::isNotBlank)
    fun goalList(): List<String> = goals.split('|').filter(String::isNotBlank)
}

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val difficulty: String,
    val xpReward: Int,
    val dueDate: Long,
    val completed: Boolean = false,
    val completedAt: Long? = null
) {
    fun missionCategory(): MissionCategory = MissionCategory.entries.firstOrNull { it.name == category } ?: MissionCategory.DISCIPLINE
    fun missionDifficulty(): MissionDifficulty = MissionDifficulty.entries.firstOrNull { it.name == difficulty } ?: MissionDifficulty.EASY
    fun isToday(): Boolean = DateUtils.dayKey(dueDate) == DateUtils.dayKey(System.currentTimeMillis())
}

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mood: String,
    val content: String,
    val wins: String,
    val struggles: String,
    val lessonLearned: String,
    val aiSummary: String,
    val patternInsight: String,
    val nextRecommendedAction: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun journalMood(): Mood = Mood.entries.firstOrNull { it.name == mood } ?: Mood.FOCUSED
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun chatRole(): ChatRole = ChatRole.entries.firstOrNull { it.name == role } ?: ChatRole.ASSISTANT
}

@Entity(tableName = "transformation_snapshots")
data class TransformationSnapshotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val xp: Int,
    val streak: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "subscription_state")
data class SubscriptionStateEntity(
    @PrimaryKey val id: String = "local-subscription",
    val plan: String = SubscriptionPlan.FREE.name,
    val isActive: Boolean = false,
    val renewsAt: Long? = null
)

data class GeneratedAlterEgoProfile(val alterEgoName: String, val identityStatement: String, val traits: List<String>, val goals: List<String>)
data class GeneratedMission(val title: String, val category: MissionCategory, val difficulty: MissionDifficulty, val xpReward: Int)
data class JournalInsight(val summary: String, val patternInsight: String, val nextAction: String)

data class AIBackendRequest(
    val module: String,
    val currentIdentity: String,
    val futureIdentity: String,
    val motivationStyle: String,
    val goals: List<String>,
    val journalText: String,
    val progressData: Map<String, String>
)

data class AIBackendResponse(
    val message: String? = null,
    val missions: List<RemoteMission>? = null,
    val summary: String? = null,
    val insights: List<String>? = null
)

data class RemoteMission(val title: String, val category: String, val difficulty: String, val xpReward: Int)
