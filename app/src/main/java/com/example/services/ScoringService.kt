package com.example.services

import com.example.data.models.UserStats

data class LevelInfo(
    val level: Int,
    val title: String,
    val currentLevelMinXP: Int,
    val nextLevelXP: Int,
    val progress: Float
)

object ScoringService {
    const val XP_LEETCODE = 5
    const val XP_APTITUDE = 1
    const val XP_TODO_TASK = 10
    const val XP_LEARNING_NOTE = 5
    const val XP_DAILY_REFLECTION = 5
    const val XP_LINKEDIN_WEEKLY = 5
    const val XP_GITHUB_WEEKLY = 10
    const val XP_ALL_DAILY_GOALS = 100
    const val XP_STREAK_7 = 250
    const val XP_STREAK_30 = 750


    private val LEVEL_TIERS = listOf(
        Pair(0, "Beginner"),
        Pair(300, "Explorer"),
        Pair(700, "Learner"),
        Pair(1200, "Problem Solver"),
        Pair(1800, "Builder"),
        Pair(2500, "Developer"),
        Pair(3300, "Achiever"),
        Pair(4200, "Career Warrior"),
        Pair(5200, "Tech Master"),
        Pair(6500, "Future Engineer")
    )

    fun calculateLevelInfo(totalXP: Int): LevelInfo {
        var currentLevel = 1
        var title = LEVEL_TIERS[0].second
        var minXP = 0
        var nextXP = LEVEL_TIERS[1].first

        for (i in LEVEL_TIERS.indices) {
            val (threshold, name) = LEVEL_TIERS[i]
            if (totalXP >= threshold) {
                currentLevel = i + 1
                title = name
                minXP = threshold
                nextXP = if (i + 1 < LEVEL_TIERS.size) LEVEL_TIERS[i + 1].first else threshold + 1500
            } else {
                break
            }
        }

        val range = (nextXP - minXP).coerceAtLeast(1)
        val progress = ((totalXP - minXP).toFloat() / range.toFloat()).coerceIn(0f, 1f)

        return LevelInfo(
            level = currentLevel,
            title = title,
            currentLevelMinXP = minXP,
            nextLevelXP = nextXP,
            progress = progress
        )
    }

    fun tryAwardXP(
        user: UserStats,
        actionKey: String,
        xpAmount: Int
    ): Pair<UserStats, Int> {
        // Prevent duplicate XP if already awarded for this action
        if (user.awardedActionIds.contains(actionKey)) {
            return Pair(user, 0)
        }

        val newTotalXP = (user.totalXP + xpAmount).coerceAtLeast(0)
        val levelInfo = calculateLevelInfo(newTotalXP)
        val updatedUser = user.copy(
            totalXP = newTotalXP,
            level = levelInfo.level,
            awardedActionIds = user.awardedActionIds + actionKey
        )
        return Pair(updatedUser, xpAmount)
    }
}
