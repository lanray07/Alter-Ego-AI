package com.alteregoai.app

import com.alteregoai.app.core.XPSystem
import com.alteregoai.app.data.MissionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class XPSystemTest {
    @Test fun levelAndProgressFollowTheProductRules() {
        assertEquals(1, XPSystem.level(0))
        assertEquals(2, XPSystem.level(250))
        assertEquals(50, XPSystem.level(50_000))
        assertEquals(.5f, XPSystem.progress(125), .001f)
    }

    @Test fun disciplineScoreUsesCompletedAndMissedMissions() {
        assertEquals(75, XPSystem.disciplineScore(3, 1))
        assertEquals(0, XPSystem.disciplineScore(0, 4))
    }

    @Test fun streakCountsConsecutiveCompletedDaysEndingToday() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val missions = (0..2).map { offset -> MissionEntity(title = "Mission $offset", category = "FOCUS", difficulty = "EASY", xpReward = 20, dueDate = today.minusDays(offset.toLong()).atStartOfDay(zone).toInstant().toEpochMilli(), completed = true, completedAt = today.minusDays(offset.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()) }
        assertEquals(3, XPSystem.currentStreak(missions))
    }
}
