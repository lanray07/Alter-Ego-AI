package com.alteregoai.app.core

import java.util.Calendar
import java.util.Locale

object AppConstants {
    const val appName = "Alter Ego AI"
    const val tagline = "Become the person you were supposed to be."
    const val viralHook = "Your future self is watching."
    const val coachingDisclaimer = "Alter Ego AI is a general wellness, habit-building, motivation, and lifestyle app. AI coaching is informational and motivational only. It is not medical advice, mental health care, therapy, diagnosis, crisis counseling, financial advice, legal advice, or treatment. For medical, mental health, legal, financial, or safety concerns, seek qualified professional support."
    const val aiBackendPath = "alter-ego-ai"
    const val proMonthlyProductId = "alteregoai_pro_monthly"
    const val proYearlyProductId = "alteregoai_pro_yearly"
    const val eliteMonthlyProductId = "alteregoai_elite_monthly"
    val productIds = listOf(proMonthlyProductId, proYearlyProductId, eliteMonthlyProductId)
}

object DateUtils {
    fun dayKey(epochMillis: Long): Int {
        val calendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return calendar.get(Calendar.YEAR) * 10_000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun shiftMillisByDays(epochMillis: Long, days: Int): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        add(Calendar.DAY_OF_YEAR, days)
    }.timeInMillis

    fun startOfTodayMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun shortDayName(epochMillis: Long): String = Calendar.getInstance().apply {
        timeInMillis = epochMillis
    }.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())?.uppercase(Locale.getDefault())?.take(3) ?: "DAY"
}

object XPSystem {
    fun level(totalXp: Int): Int = (totalXp / 250 + 1).coerceIn(1, 50)
    fun title(level: Int): String = when (level) {
        in 50..Int.MAX_VALUE -> "Apex Self"
        in 35..49 -> "Ascendant"
        in 20..34 -> "Disciplined"
        in 10..19 -> "Focused"
        in 5..9 -> "Initiate"
        else -> "Drifter"
    }
    fun avatarStage(level: Int) = when {
        level >= 50 -> com.alteregoai.app.data.AvatarStage.APEX_SELF
        level >= 35 -> com.alteregoai.app.data.AvatarStage.ASCENDANT
        level >= 20 -> com.alteregoai.app.data.AvatarStage.DISCIPLINED
        level >= 10 -> com.alteregoai.app.data.AvatarStage.FOCUSED
        level >= 5 -> com.alteregoai.app.data.AvatarStage.AWAKENING
        else -> com.alteregoai.app.data.AvatarStage.DRIFTER
    }
    fun progress(totalXp: Int): Float = (totalXp % 250) / 250f
    fun disciplineScore(completed: Int, missed: Int): Int = ((completed.toFloat() / (completed + missed).coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
    fun currentStreak(missions: List<com.alteregoai.app.data.MissionEntity>): Int {
        val completedDays = missions.filter { it.completed }.map { DateUtils.dayKey(it.completedAt ?: it.dueDate) }.toSet()
        var streak = 0
        var day = System.currentTimeMillis()
        while (completedDays.contains(DateUtils.dayKey(day))) { streak++; day = DateUtils.shiftMillisByDays(day, -1) }
        return streak
    }
}
