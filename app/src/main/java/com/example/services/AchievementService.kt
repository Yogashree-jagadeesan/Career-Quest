package com.example.services

import com.example.data.models.Achievement
import com.example.data.models.SurpriseReward
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AchievementService {

    val INITIAL_ACHIEVEMENTS = listOf(
        Achievement(
            id = "ach_lotus_beginner",
            title = "Lotus Beginner",
            iconSymbol = "🪷",
            description = "Complete your very first career task.",
            target = 1,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_consistency_flame",
            title = "Consistency Flame",
            iconSymbol = "🔥",
            description = "Maintain a 7-day productivity streak.",
            target = 7,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_peacock_feather",
            title = "Peacock Feather",
            iconSymbol = "🦚",
            description = "Complete weekly goals for 4 consecutive weeks.",
            target = 4,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_flute_of_focus",
            title = "Flute of Focus",
            iconSymbol = "🎵",
            description = "Complete 10 focused learning or revision sessions.",
            target = 10,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_code_warrior",
            title = "Code Warrior",
            iconSymbol = "💻",
            description = "Solve 50 LeetCode problems.",
            target = 50,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_aptitude_master",
            title = "Aptitude Master",
            iconSymbol = "🧠",
            description = "Solve 500 aptitude practice questions.",
            target = 500,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_github_builder",
            title = "GitHub Builder",
            iconSymbol = "🚀",
            description = "Complete GitHub activity goals for 10 weeks.",
            target = 10,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_knowledge_keeper",
            title = "Knowledge Keeper",
            iconSymbol = "📝",
            description = "Create 50 insightful technical learning notes.",
            target = 50,
            current = 0,
            unlocked = false,
            unlockedDate = null
        ),
        Achievement(
            id = "ach_career_star",
            title = "Career Star",
            iconSymbol = "🌟",
            description = "Reach 5,000 total career XP.",
            target = 5000,
            current = 0,
            unlocked = false,
            unlockedDate = null
        )
    )

    fun evaluateAchievements(
        currentAchievements: List<Achievement>,
        completedTasksCount: Int,
        streakDays: Int,
        leetcodeCount: Int,
        aptitudeQuestionsCount: Int,
        notesCount: Int,
        totalXP: Int
    ): Pair<List<Achievement>, List<Achievement>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newlyUnlocked = mutableListOf<Achievement>()

        val updated = currentAchievements.map { ach ->
            val curValue = when (ach.id) {
                "ach_lotus_beginner" -> completedTasksCount
                "ach_consistency_flame" -> streakDays
                "ach_flute_of_focus" -> notesCount / 2
                "ach_code_warrior" -> leetcodeCount
                "ach_aptitude_master" -> aptitudeQuestionsCount
                "ach_knowledge_keeper" -> notesCount
                "ach_career_star" -> totalXP
                else -> ach.current
            }

            val isNowUnlocked = curValue >= ach.target
            if (isNowUnlocked && !ach.unlocked) {
                val unlockedAch = ach.copy(
                    current = curValue,
                    unlocked = true,
                    unlockedDate = today
                )
                newlyUnlocked.add(unlockedAch)
                unlockedAch
            } else {
                ach.copy(current = curValue)
            }
        }

        return Pair(updated, newlyUnlocked)
    }

    private val MOTIVATIONAL_MESSAGES = listOf(
        "🌸 Excellent! Your problem-solving skills are growing day by day.",
        "🦚 Great work! Your engineering presence is getting stronger.",
        "✨ Fantastic! Consistent practice builds legendary mastery.",
        "📚 Excellent! Consistency beats intensity every single time.",
        "🔥 Keep the momentum! Your discipline is forging your career.",
        "🪷 Beautiful focus! Every small effort compound into excellence.",
        "🎵 Flowing like the flute of focus. Great session!"
    )

    val ALL_SURPRISE_REWARDS = listOf(
        SurpriseReward(
            id = "reward_streak_7",
            title = "🎁 Consistency Blossom!",
            description = "You completed your 7-day streak with dedication.",
            bonusXP = 100,
            icon = "🔥",
            triggerCondition = "7-Day Streak Achieved"
        ),
        SurpriseReward(
            id = "reward_xp_1000",
            title = "🎁 Golden Milestone!",
            description = "You soared past 1,000 XP! Your hard work is shining.",
            bonusXP = 50,
            icon = "⭐",
            triggerCondition = "1,000 XP Milestone"
        ),
        SurpriseReward(
            id = "reward_xp_2500",
            title = "🎁 Developer Tier Unlocked!",
            description = "Reached Level 6 Developer! You have proved your commitment.",
            bonusXP = 150,
            icon = "🏆",
            triggerCondition = "Level 6 Developer Milestone"
        ),
        SurpriseReward(
            id = "reward_streak_14",
            title = "🎁 Flute of Focus Blessing!",
            description = "Two full weeks of relentless career development.",
            bonusXP = 200,
            icon = "🪷",
            triggerCondition = "14-Day Streak Achieved"
        )
    )

    fun getRandomAppreciationMessage(): String {
        return MOTIVATIONAL_MESSAGES.random()
    }


    fun checkSurpriseReward(
        totalXP: Int,
        streak: Int,
        alreadyUnlockedIds: Set<String>
    ): SurpriseReward? {
        if (streak >= 7 && !alreadyUnlockedIds.contains("reward_streak_7")) {
            return SurpriseReward(
                id = "reward_streak_7",
                title = "🎁 Consistency Blossom!",
                description = "You completed your 7-day streak with dedication.",
                bonusXP = 100,
                icon = "🔥",
                triggerCondition = "7-Day Streak Achieved"
            )
        }
        if (totalXP >= 1000 && !alreadyUnlockedIds.contains("reward_xp_1000")) {
            return SurpriseReward(
                id = "reward_xp_1000",
                title = "🎁 Golden Milestone!",
                description = "You soared past 1,000 XP! Your hard work is shining.",
                bonusXP = 50,
                icon = "⭐",
                triggerCondition = "1,000 XP Milestone"
            )
        }
        if (totalXP >= 2500 && !alreadyUnlockedIds.contains("reward_xp_2500")) {
            return SurpriseReward(
                id = "reward_xp_2500",
                title = "🎁 Developer Tier Unlocked!",
                description = "Reached Level 6 Developer! You have proved your commitment.",
                bonusXP = 150,
                icon = "🏆",
                triggerCondition = "Level 6 Developer Milestone"
            )
        }
        return null
    }
}
