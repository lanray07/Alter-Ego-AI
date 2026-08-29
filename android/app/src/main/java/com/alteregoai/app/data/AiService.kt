package com.alteregoai.app.data

import com.alteregoai.app.core.AppConstants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.time.Instant

interface AiService {
    suspend fun generateAlterEgoProfile(name: String, currentIdentity: String, futureIdentity: String, goals: List<GoalCategory>, style: MotivationStyle): GeneratedAlterEgoProfile
    suspend fun generateDailyMissions(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, date: Instant): List<GeneratedMission>
    suspend fun generateFutureSelfReply(message: String, profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, recentMissions: List<MissionEntity>): String
    suspend fun summarizeJournalEntry(text: String, mood: Mood, profile: UserProfileEntity): JournalInsight
    suspend fun generateWeeklyReview(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, missions: List<MissionEntity>, journals: List<JournalEntryEntity>): String
    suspend fun generateShareCardText(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, context: String): String
}

class MockAiService : AiService {
    override suspend fun generateAlterEgoProfile(name: String, currentIdentity: String, futureIdentity: String, goals: List<GoalCategory>, style: MotivationStyle): GeneratedAlterEgoProfile {
        val firstName = name.trim().split(" ").firstOrNull().orEmpty().ifBlank { "Apex" }
        return GeneratedAlterEgoProfile("$firstName Prime", "I am becoming the version of myself who acts with ${style.title.lowercase()} discipline, protects my energy, and proves ${futureIdentity.lowercase()} through daily action.", listOf("Disciplined", "Focused", "Composed", "Consistent", "Brave"), goals.map { it.title })
    }

    override suspend fun generateDailyMissions(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, date: Instant): List<GeneratedMission> {
        val missions = mutableListOf(
            GeneratedMission("20-minute workout", MissionCategory.BODY, MissionDifficulty.MEDIUM, 60),
            GeneratedMission("Drink water before your first scroll", MissionCategory.BODY, MissionDifficulty.EASY, 25),
            GeneratedMission("30-minute focus session", MissionCategory.FOCUS, MissionDifficulty.MEDIUM, 55),
            GeneratedMission("Read 10 pages", MissionCategory.LEARNING, MissionDifficulty.EASY, 35),
            GeneratedMission("Journal the identity you practiced today", MissionCategory.MIND, MissionDifficulty.EASY, 35),
            GeneratedMission("No scrolling challenge for one hour", MissionCategory.DISCIPLINE, MissionDifficulty.HARD, 80),
            GeneratedMission("Clean your workspace", MissionCategory.DISCIPLINE, MissionDifficulty.EASY, 30),
            GeneratedMission("Apply for one opportunity", MissionCategory.MONEY, MissionDifficulty.HARD, 85),
            GeneratedMission("Send one confident message", MissionCategory.SOCIAL, MissionDifficulty.MEDIUM, 50)
        )
        profile.goals().forEach { goal ->
            when (goal) {
                GoalCategory.SLEEP -> missions.add(0, GeneratedMission("Sleep before your target time", MissionCategory.SLEEP, MissionDifficulty.MEDIUM, 60))
                GoalCategory.CONFIDENCE -> missions.add(0, GeneratedMission("Do one small action you are avoiding", MissionCategory.CONFIDENCE, MissionDifficulty.MEDIUM, 65))
                GoalCategory.PRODUCTIVITY -> missions.add(0, GeneratedMission("Plan your top 3 before noon", MissionCategory.FOCUS, MissionDifficulty.EASY, 40))
                GoalCategory.MONEY_HABITS -> missions.add(0, GeneratedMission("Review one spending decision", MissionCategory.MONEY, MissionDifficulty.EASY, 35))
                else -> Unit
            }
        }
        val offset = java.time.ZonedDateTime.ofInstant(date, java.time.ZoneId.systemDefault()).dayOfMonth % missions.size
        return (missions.drop(offset) + missions.take(offset)).take(5)
    }

    override suspend fun generateFutureSelfReply(message: String, profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, recentMissions: List<MissionEntity>): String {
        val styleLine = when (profile.style()) {
            MotivationStyle.GENTLE -> "Start small, but start honestly."
            MotivationStyle.STRICT -> "Your standards are not punishment. They are proof."
            MotivationStyle.CINEMATIC -> "This is the scene where you stop negotiating with the old script."
            MotivationStyle.TACTICAL -> "Choose the next visible action, set a timer, execute."
            MotivationStyle.ENCOURAGING -> "You are closer than your tired mind is telling you."
        }
        return "${alterEgo.alterEgoName}: I heard you. The old identity says, \"${message.take(90)}\". The future identity answers with action.\n\nYou have completed ${recentMissions.count { it.completed }} recent missions. Today, win the next 20 minutes. $styleLine"
    }

    override suspend fun summarizeJournalEntry(text: String, mood: Mood, profile: UserProfileEntity): JournalInsight = JournalInsight("You noticed your ${mood.title.lowercase()} state and translated it into self-awareness instead of autopilot.", "Your strongest pattern appears when you name the struggle plainly, then choose one concrete action.", "Pick one mission that takes under 20 minutes and complete it before your next long break.")
    override suspend fun generateWeeklyReview(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, missions: List<MissionEntity>, journals: List<JournalEntryEntity>): String = "This week, ${profile.name} completed ${missions.count { it.completed }} missions and earned ${missions.filter { it.completed }.sumOf { it.xpReward }} XP. The signal is clear: ${alterEgo.alterEgoName} grows when action stays small enough to repeat and meaningful enough to respect."
    override suspend fun generateShareCardText(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, context: String): String = "$context\n${alterEgo.alterEgoName} - Level ${alterEgo.level} ${alterEgo.stage().title}\n${AppConstants.viralHook}"
}

private interface AlterEgoApi {
    @POST(AppConstants.aiBackendPath)
    suspend fun perform(@Body request: AIBackendRequest): AIBackendResponse
}

class RemoteAiService(baseUrl: String) : AiService {
    private val api: AlterEgoApi = Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AlterEgoApi::class.java)

    private suspend fun perform(module: String, profile: UserProfileEntity, journal: String = "", progress: Map<String, String> = emptyMap()): AIBackendResponse = api.perform(AIBackendRequest(module, profile.currentIdentity, profile.futureIdentity, profile.motivationStyle, profile.goals().map { it.name }, journal, progress))

    override suspend fun generateAlterEgoProfile(name: String, currentIdentity: String, futureIdentity: String, goals: List<GoalCategory>, style: MotivationStyle): GeneratedAlterEgoProfile {
        val profile = UserProfileEntity(name = name, currentIdentity = currentIdentity, futureIdentity = futureIdentity, motivationStyle = style.name, dailyAvailability = 30, notificationPreference = false, topGoals = goals.joinToString("|") { it.name })
        val response = perform("alter_ego_profile", profile, progress = mapOf("name" to name))
        return GeneratedAlterEgoProfile(response.message?.lineSequence()?.firstOrNull() ?: "$name Prime", response.summary ?: response.message ?: "I become my future self through identity-based action.", response.insights ?: listOf("Disciplined", "Focused", "Consistent"), goals.map { it.title })
    }

    override suspend fun generateDailyMissions(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, date: Instant): List<GeneratedMission> {
        val response = perform("daily_missions", profile, progress = mapOf("level" to alterEgo.level.toString(), "totalXP" to alterEgo.totalXp.toString(), "date" to date.toString()))
        return response.missions.orEmpty().mapNotNull { mission ->
            val category = MissionCategory.entries.firstOrNull { it.name.equals(mission.category, true) } ?: return@mapNotNull null
            val difficulty = MissionDifficulty.entries.firstOrNull { it.name.equals(mission.difficulty, true) } ?: MissionDifficulty.EASY
            GeneratedMission(mission.title, category, difficulty, mission.xpReward)
        }.ifEmpty { error("The AI backend did not return missions.") }
    }
    override suspend fun generateFutureSelfReply(message: String, profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, recentMissions: List<MissionEntity>): String = perform("future_self_reply", profile, message, mapOf("alterEgoName" to alterEgo.alterEgoName, "completedRecentMissions" to recentMissions.count { it.completed }.toString())).message ?: ""
    override suspend fun summarizeJournalEntry(text: String, mood: Mood, profile: UserProfileEntity): JournalInsight { val response = perform("journal_summary", profile, text, mapOf("mood" to mood.name)); val insights = response.insights.orEmpty(); return JournalInsight(response.summary ?: response.message.orEmpty(), insights.firstOrNull() ?: "Watch for the pattern behind the behavior.", insights.drop(1).firstOrNull() ?: "Choose one small mission next.") }
    override suspend fun generateWeeklyReview(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, missions: List<MissionEntity>, journals: List<JournalEntryEntity>): String = perform("weekly_review", profile, journals.joinToString("\n") { it.content }, mapOf("level" to alterEgo.level.toString(), "completedMissions" to missions.count { it.completed }.toString())).summary.orEmpty()
    override suspend fun generateShareCardText(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, context: String): String = perform("share_card", profile, context, mapOf("alterEgoName" to alterEgo.alterEgoName, "level" to alterEgo.level.toString())).message ?: context
}
