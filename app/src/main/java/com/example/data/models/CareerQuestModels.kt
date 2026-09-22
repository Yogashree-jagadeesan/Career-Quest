package com.example.data.models

import java.util.UUID

data class UserStats(
    val id: String = "user_student_1",
    val name: String = "Student",
    val totalXP: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val awardedActionIds: Set<String> = emptySet(),
    val penalizedTaskIds: Set<String> = emptySet(),
    val unlockedRewardIds: Set<String> = emptySet()
)

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.CODING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: String = "", // YYYY-MM-DD
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val xp: Int = 10
)

enum class TaskPriority(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}


enum class TaskCategory(val displayName: String) {
    COLLEGE("College"),
    CODING("Coding"),
    CAREER("Career"),
    PROJECT("Project"),
    INTERNSHIP("Internship"),
    LEARNING("Learning"),
    PERSONAL("Personal"),
    OTHER("Other")
}

data class DailyProgressRecord(
    val date: String, // YYYY-MM-DD
    val leetcodeCompleted: Int = 0,
    val leetcodeTarget: Int = 2,
    val aptitudeCompleted: Int = 0,
    val aptitudeTarget: Int = 10,
    val completedTaskIds: List<String> = emptyList(),
    val notesCreatedIds: List<String> = emptyList(),
    val xpEarned: Int = 0,
    val leetcodeXpAwarded: Boolean = false,
    val aptitudeXpAwarded: Boolean = false,
    val allGoalsXpAwarded: Boolean = false
)

data class LeetCodeRecord(
    val id: String = UUID.randomUUID().toString(),
    val problemName: String,
    val problemNumber: Int,
    val url: String = "",
    val difficulty: LeetCodeDifficulty = LeetCodeDifficulty.MEDIUM,
    val topic: String = "Arrays & Hashing",
    val date: String, // YYYY-MM-DD
    val timeSpentMinutes: Int = 25,
    val attempts: Int = 1,
    val solved: Boolean = true,
    val notes: String = "",
    // LeetCode Learning Notes
    val approach: String = "",
    val whatILearned: String = "",
    val mistake: String = "",
    val betterApproach: String = "",
    val timeComplexity: String = "O(N)",
    val spaceComplexity: String = "O(1)"
)

enum class LeetCodeDifficulty {
    EASY, MEDIUM, HARD
}

data class AptitudeSession(
    val id: String = UUID.randomUUID().toString(),
    val category: AptitudeCategory = AptitudeCategory.QUANTITATIVE,
    val topic: String,
    val attempted: Int,
    val correct: Int,
    val incorrect: Int = attempted - correct,
    val accuracy: Double = if (attempted > 0) (correct.toDouble() / attempted * 100.0) else 0.0,
    val timeSpentMinutes: Int = 20,
    val date: String // YYYY-MM-DD
)

enum class AptitudeCategory(val displayName: String) {
    QUANTITATIVE("Quantitative"),
    LOGICAL_REASONING("Logical Reasoning"),
    VERBAL("Verbal"),
    DATA_INTERPRETATION("Data Interpretation")
}

data class LinkedInActivity(
    val id: String = UUID.randomUUID().toString(),
    val type: LinkedInActivityType,
    val title: String,
    val linkOrDetails: String = "",
    val date: String, // YYYY-MM-DD
    val weekId: String // e.g. 2026-W37
)

enum class LinkedInActivityType(val displayName: String) {
    CREATE_POST("Create post"),
    SHARE_PROJECT("Share project"),
    SHARE_CERTIFICATE("Share certificate"),
    SHARE_LEARNING("Share learning"),
    COMMENT_TECHNICAL("Comment on technical content"),
    NETWORKING("Networking"),
    PROFILE_IMPROVEMENT("Profile improvement")
}

data class GitHubActivity(
    val id: String = UUID.randomUUID().toString(),
    val type: GitHubActivityType,
    val repoName: String,
    val description: String = "",
    val link: String = "",
    val date: String, // YYYY-MM-DD
    val weekId: String // e.g. 2026-W37
)

enum class GitHubActivityType(val displayName: String) {
    COMMIT("Commit"),
    PUSH_CODE("Push code"),
    CREATE_REPO("Create repository"),
    FIX_ISSUE("Fix issue"),
    PULL_REQUEST("Pull request"),
    CODE_REVIEW("Code review"),
    UPDATE_README("Update README"),
    PROJECT_CONTRIBUTION("Project contribution")
}

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: NoteCategory = NoteCategory.CODING,
    val tags: List<String> = emptyList(),
    val createdAt: String, // YYYY-MM-DD
    val updatedAt: String,
    val favorite: Boolean = false,
    val pinned: Boolean = false,
    val reviewDate: String? = null, // YYYY-MM-DD
    val linkedActivity: String? = null
)

enum class NoteCategory(val displayName: String) {
    CODING("Coding"),
    APTITUDE("Aptitude"),
    CAREER("Career"),
    LEARNING("Learning"),
    PROJECTS("Projects"),
    GOALS("Goals"),
    IDEAS("Ideas"),
    INTERVIEW("Interview"),
    REVISION("Revision"),
    OTHER("Other")
}

data class DailyReflection(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val learned: String,
    val difficult: String,
    val improveTomorrow: String,
    val proudOf: String
)

data class Achievement(
    val id: String,
    val title: String,
    val iconSymbol: String,
    val description: String,
    val target: Int,
    val current: Int,
    val unlocked: Boolean = false,
    val unlockedDate: String? = null
)

data class SurpriseReward(
    val id: String,
    val title: String,
    val description: String,
    val bonusXP: Int,
    val icon: String,
    val triggerCondition: String
)

data class AppSettings(
    val studentName: String = "Student",
    val dailyLeetCodeTarget: Int = 2,
    val weeklyLinkedInTarget: Int = 2,
    val weeklyGitHubTarget: Int = 1,
    val dailyAptitudeTargetQuestions: Int = 15,
    val enableAnimations: Boolean = true,
    val enableMotivationalMessages: Boolean = true,
    val enableBackgroundMusic: Boolean = true
)
