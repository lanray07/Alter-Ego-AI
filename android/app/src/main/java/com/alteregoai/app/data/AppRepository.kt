package com.alteregoai.app.data

import com.alteregoai.app.core.XPSystem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class AppRepository(private val dao: AppDao, private val aiService: AiService) {
    val profile: Flow<UserProfileEntity?> = dao.observeProfile()
    val alterEgo: Flow<AlterEgoProfileEntity?> = dao.observeAlterEgo()
    val missions: Flow<List<MissionEntity>> = dao.observeMissions()
    val journals: Flow<List<JournalEntryEntity>> = dao.observeJournals()
    val messages: Flow<List<ChatMessageEntity>> = dao.observeMessages()
    val snapshots: Flow<List<TransformationSnapshotEntity>> = dao.observeSnapshots()
    val achievements: Flow<List<AchievementEntity>> = dao.observeAchievements()
    val subscription: Flow<SubscriptionStateEntity?> = dao.observeSubscription()

    suspend fun createOnboarding(name: String, currentIdentity: String, futureIdentity: String, goals: List<GoalCategory>, style: MotivationStyle, dailyAvailability: Int, notificationPreference: Boolean) {
        val profile = UserProfileEntity(name = name.trim(), currentIdentity = currentIdentity.trim(), futureIdentity = futureIdentity.trim(), motivationStyle = style.name, dailyAvailability = dailyAvailability, notificationPreference = notificationPreference, topGoals = goals.joinToString("|") { it.name })
        val generated = aiService.generateAlterEgoProfile(profile.name, profile.currentIdentity, profile.futureIdentity, goals, style)
        val alterEgo = AlterEgoProfileEntity(userId = profile.id, alterEgoName = generated.alterEgoName, identityStatement = generated.identityStatement, avatarStage = AvatarStage.DRIFTER.name, level = 1, totalXp = 0, traits = generated.traits.joinToString("|"), goals = generated.goals.joinToString("|"))

        val achievements = listOf(
            "First Mission" to "Complete your first identity-based mission.",
            "Three-Day Signal" to "Complete at least one mission for three days.",
            "Focused Level 10" to "Reach Level 10 and unlock the Focused stage.",
            "Thirty Missions" to "Complete 30 missions in one week.",
            "Comeback Architect" to "Create a comeback plan after missed days."
        ).map { (title, description) -> AchievementEntity(title = title, description = description) }

        val today = LocalDate.now()
        val seeded = buildList {
            for (offset in 0..6) {
                val date = today.plusDays(offset.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant()
                val missions = aiService.generateDailyMissions(profile, alterEgo, date)
                addAll(missions.take(3).map { mission -> MissionEntity(title = mission.title, category = mission.category.name, difficulty = mission.difficulty.name, xpReward = mission.xpReward, dueDate = date.toEpochMilli()) })
            }
        }
        dao.insertOnboardingData(profile, alterEgo, SubscriptionStateEntity(), ChatMessageEntity(role = ChatRole.ASSISTANT.name, content = "${generated.alterEgoName}: I am the future self you are building. Start with one mission. Keep the promise small enough to keep."), TransformationSnapshotEntity(title = "Day 1 Baseline", summary = "Current identity: ${profile.currentIdentity}. Future identity: ${profile.futureIdentity}.", xp = 0, streak = 0), achievements, seeded)
    }

    suspend fun completeMission(mission: MissionEntity, alterEgo: AlterEgoProfileEntity?, allMissions: List<MissionEntity>) {
        if (mission.completed) return
        val completedAt = System.currentTimeMillis()
        if (dao.updateMissionCompletion(mission.id, true, completedAt) != 1) return
        if (alterEgo != null) {
            val totalXp = alterEgo.totalXp + mission.xpReward
            val level = XPSystem.level(totalXp)
            val streak = XPSystem.currentStreak(allMissions.map { if (it.id == mission.id) it.copy(completed = true, completedAt = completedAt) else it })
            dao.updateAlterEgo(alterEgo.copy(totalXp = totalXp, level = level, avatarStage = XPSystem.avatarStage(level).name))
            dao.insertSnapshot(TransformationSnapshotEntity(title = if (level > alterEgo.level) "Level $level Unlocked" else "Mission Complete", summary = "Completed ${mission.title} and earned ${mission.xpReward} XP.", xp = totalXp, streak = streak))
        }
        val completedCount = allMissions.count { it.completed } + 1
        // Achievement updates are intentionally driven by the repository; the Flow is collected by the UI.
        if (completedCount >= 1) unlockAchievement("First Mission")
        if (completedCount >= 30) unlockAchievement("Thirty Missions")
    }

    private suspend fun unlockAchievement(title: String) {
        // DAO lookup is not needed for the normal path; a small query keeps the achievement state centralized.
        dao.findAchievement(title)?.takeUnless { it.unlocked }?.let { achievement ->
            dao.updateAchievement(achievement.copy(unlocked = true, unlockedAt = System.currentTimeMillis()))
        }
    }

    suspend fun addMessage(role: ChatRole, content: String) = dao.insertMessage(ChatMessageEntity(role = role.name, content = content))

    suspend fun saveJournal(mood: Mood, content: String, wins: String, struggles: String, lesson: String, profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity?): JournalInsight {
        val combined = listOf(content, wins, struggles, lesson).filter(String::isNotBlank).joinToString("\n")
        val insight = aiService.summarizeJournalEntry(combined, mood, profile)
        dao.insertJournal(JournalEntryEntity(mood = mood.name, content = content, wins = wins, struggles = struggles, lessonLearned = lesson, aiSummary = insight.summary, patternInsight = insight.patternInsight, nextRecommendedAction = insight.nextAction))
        if (alterEgo != null) dao.insertSnapshot(TransformationSnapshotEntity(title = "Reflection Logged", summary = insight.summary, xp = alterEgo.totalXp, streak = 0))
        return insight
    }

    suspend fun sendMessage(message: String, profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, missions: List<MissionEntity>) {
        addMessage(ChatRole.USER, message)
        val reply = aiService.generateFutureSelfReply(message, profile, alterEgo, missions.takeLast(12))
        addMessage(ChatRole.ASSISTANT, reply)
    }

    suspend fun generatePlan(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, existing: List<MissionEntity>) {
        val generated = aiService.generateDailyMissions(profile, alterEgo, java.time.Instant.now())
        val limit = if (existing.count { it.isToday() } == 0) 3 else 2
        dao.insertMissions(generated.take(limit).map { mission -> MissionEntity(title = mission.title, category = mission.category.name, difficulty = mission.difficulty.name, xpReward = mission.xpReward, dueDate = System.currentTimeMillis()) })
    }

    suspend fun generateWeeklyReview(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, missions: List<MissionEntity>, journals: List<JournalEntryEntity>): String = aiService.generateWeeklyReview(profile, alterEgo, missions, journals)

    suspend fun generateShareCardText(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, context: String): String = aiService.generateShareCardText(profile, alterEgo, context)

    suspend fun updatePreferences(profile: UserProfileEntity, style: MotivationStyle, reminders: Boolean) = dao.updateProfilePreferences(profile.id, style.name, reminders)

    suspend fun clearAll() {
        dao.deleteAll()
    }
}
