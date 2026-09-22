package com.example

import com.example.data.models.*
import com.example.data.storage.CareerQuestData
import com.example.services.AchievementService
import com.example.services.ScoringService
import com.example.services.StreakService
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class CareerQuestUnitTest {

    @Test
    fun testLevelCalculation() {
        val level1 = ScoringService.calculateLevelInfo(150)
        assertEquals(1, level1.level)
        assertEquals("Beginner", level1.title)

        val level6 = ScoringService.calculateLevelInfo(2550)
        assertEquals(6, level6.level)
        assertEquals("Developer", level6.title)

        val level10 = ScoringService.calculateLevelInfo(7000)
        assertEquals(10, level10.level)
        assertEquals("Future Engineer", level10.title)
    }

    @Test
    fun testXpRewardSystemValues() {
        // LeetCode: +5 XP per completed problem
        assertEquals(5, ScoringService.XP_LEETCODE)
        // Aptitude: +1 XP per completed question
        assertEquals(1, ScoringService.XP_APTITUDE)
        // LinkedIn: +5 XP when weekly target is completed
        assertEquals(5, ScoringService.XP_LINKEDIN_WEEKLY)
        // GitHub: +10 XP when weekly target is completed
        assertEquals(10, ScoringService.XP_GITHUB_WEEKLY)
    }

    @Test
    fun testXpNeverDropsBelowZero() {
        val userWithLowXp = UserStats(
            id = "user_low_xp",
            totalXP = 3
        )
        // Deduct 10 XP
        val newTotalXP = (userWithLowXp.totalXP - 10).coerceAtLeast(0)
        assertEquals(0, newTotalXP)
    }

    @Test
    fun testAwardXpPreventsDuplicate() {
        val initialUser = UserStats(
            id = "test_user",
            totalXP = 100,
            awardedActionIds = setOf("task_1")
        )

        // Try to award XP for the same task again
        val (userAfterDuplicate, xpAwarded1) = ScoringService.tryAwardXP(initialUser, "task_1", 10)
        assertEquals(0, xpAwarded1)
        assertEquals(100, userAfterDuplicate.totalXP)

        // Award XP for a new task
        val (userAfterNewTask, xpAwarded2) = ScoringService.tryAwardXP(userAfterDuplicate, "task_2", 10)
        assertEquals(10, xpAwarded2)
        assertEquals(110, userAfterNewTask.totalXP)
        assertTrue(userAfterNewTask.awardedActionIds.contains("task_2"))
    }

    @Test
    fun testSurpriseRewardTrigger() {
        val rewardStreak = AchievementService.checkSurpriseReward(
            totalXP = 500,
            streak = 7,
            alreadyUnlockedIds = emptySet()
        )
        assertNotNull(rewardStreak)
        assertEquals("reward_streak_7", rewardStreak?.id)

        // Once unlocked, should not trigger again
        val rewardAlreadyClaimed = AchievementService.checkSurpriseReward(
            totalXP = 500,
            streak = 7,
            alreadyUnlockedIds = setOf("reward_streak_7")
        )
        assertNull(rewardAlreadyClaimed)
    }

    @Test
    fun testDateFormatting() {
        val today = StreakService.getTodayDate()
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val weekId = StreakService.getCurrentWeekId()
        assertTrue(weekId.matches(Regex("\\d{4}-W\\d{2}")))
    }

    @Test
    fun testFreshInstallDefaults() {
        val freshUser = UserStats()
        assertEquals(0, freshUser.totalXP)
        assertEquals(1, freshUser.level)
        assertEquals(0, freshUser.currentStreak)
        assertEquals(0, freshUser.longestStreak)
        assertTrue(freshUser.penalizedTaskIds.isEmpty())
        assertTrue(freshUser.awardedActionIds.isEmpty())

        val freshSettings = AppSettings()
        assertEquals("Student", freshSettings.studentName)
        assertTrue(freshSettings.enableBackgroundMusic)

        val freshData = CareerQuestData()
        assertEquals(0, freshData.user.totalXP)
        assertTrue(freshData.tasks.isEmpty())
        assertTrue(freshData.notes.isEmpty())
        assertTrue(freshData.dailyRecords.isEmpty())
    }

    @Test
    fun testStreakCalculation() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgoStr = sdf.format(cal.time)

        val dailyRecords = mapOf(
            todayStr to DailyProgressRecord(date = todayStr, leetcodeCompleted = 1),
            yesterdayStr to DailyProgressRecord(date = yesterdayStr, aptitudeCompleted = 3),
            twoDaysAgoStr to DailyProgressRecord(date = twoDaysAgoStr, leetcodeCompleted = 2)
        )

        val (currentStreak, longestStreak) = StreakService.calculateStreak(
            tasks = emptyList(),
            leetCode = emptyList(),
            aptitude = emptyList(),
            notes = emptyList(),
            reflections = emptyList(),
            dailyRecords = dailyRecords
        )

        assertEquals(3, currentStreak)
        assertTrue(longestStreak >= 3)
    }

    @Test
    fun testStreakCalculationEmpty() {
        val (currentStreak, longestStreak) = StreakService.calculateStreak(
            tasks = emptyList(),
            leetCode = emptyList(),
            aptitude = emptyList(),
            notes = emptyList(),
            reflections = emptyList(),
            dailyRecords = emptyMap()
        )

        assertEquals(0, currentStreak)
        assertEquals(0, longestStreak)
    }

    @Test
    fun testBottomNavRoutes() {
        val primaryRoutes = com.example.ui.navigation.ScreenRoute.entries.filter { it.isPrimaryBottomNav }.map { it.route }
        assertEquals(listOf("dashboard", "progress", "todos", "settings"), primaryRoutes)
    }
}
