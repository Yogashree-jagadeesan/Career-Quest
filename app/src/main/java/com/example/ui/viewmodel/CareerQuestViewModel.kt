package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.storage.CareerQuestData
import com.example.data.storage.CareerQuestStorage
import com.example.services.AchievementService
import com.example.services.ScoringService
import com.example.services.StreakService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


sealed class UiToastMessage {
    data class Success(val message: String, val xpAwarded: Int = 0) : UiToastMessage()
    data class Error(val message: String) : UiToastMessage()
    data class Reward(val reward: SurpriseReward) : UiToastMessage()
}

class CareerQuestViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = CareerQuestStorage(application)

    private val _dataState = MutableStateFlow(evaluateIncompleteTasks(storage.loadData()))
    val dataState: StateFlow<CareerQuestData> = _dataState.asStateFlow()

    private fun evaluateIncompleteTasks(data: CareerQuestData): CareerQuestData {
        val today = StreakService.getTodayDate()
        var user = data.user
        var changed = false
        val newPenalizedIds = user.penalizedTaskIds.toMutableSet()
        var totalXP = user.totalXP

        val updatedTasks = data.tasks.map { task ->
            if (!task.completed && task.dueDate.isNotBlank() && task.dueDate < today) {
                if (!newPenalizedIds.contains(task.id) && !user.awardedActionIds.contains("task_${task.id}")) {
                    val deduction = task.xp
                    totalXP = (totalXP - deduction).coerceAtLeast(0)
                    newPenalizedIds.add(task.id)
                    changed = true
                }
            }
            task
        }

        if (changed) {
            val levelInfo = ScoringService.calculateLevelInfo(totalXP)
            user = user.copy(
                totalXP = totalXP,
                level = levelInfo.level,
                penalizedTaskIds = newPenalizedIds
            )
            val result = data.copy(user = user, tasks = updatedTasks)
            storage.saveData(result)
            return result
        }

        return data.copy(user = user, tasks = updatedTasks)
    }

    private val _toastEvent = MutableSharedFlow<UiToastMessage>()
    val toastEvent: SharedFlow<UiToastMessage> = _toastEvent.asSharedFlow()

    private val _activeSurpriseReward = MutableStateFlow<SurpriseReward?>(null)
    val activeSurpriseReward: StateFlow<SurpriseReward?> = _activeSurpriseReward.asStateFlow()

    fun dismissSurpriseReward() {
        _activeSurpriseReward.value = null
    }

    private fun persist(newData: CareerQuestData) {
        val (curStreak, longStreak) = StreakService.calculateStreak(
            tasks = newData.tasks,
            leetCode = newData.leetCodeRecords,
            aptitude = newData.aptitudeSessions,
            notes = newData.notes,
            reflections = newData.reflections,
            dailyRecords = newData.dailyRecords
        )
        val finalUser = newData.user.copy(
            currentStreak = curStreak,
            longestStreak = maxOf(newData.user.longestStreak, longStreak, curStreak)
        )
        val finalData = newData.copy(user = finalUser)
        _dataState.value = finalData
        storage.saveData(finalData)
    }

    private fun notifyUser(message: String, xp: Int = 0) {
        viewModelScope.launch {
            _toastEvent.emit(UiToastMessage.Success(message, xp))
        }
    }

    private fun notifyError(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(UiToastMessage.Error(message))
        }
    }

    // --- Task Actions ---

    fun addTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDate: String
    ): Boolean {
        if (title.isBlank()) {
            notifyError("Task title is required.")
            return false
        }
        val current = _dataState.value
        val newTask = TaskItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            category = category,
            priority = priority,
            dueDate = dueDate.ifBlank { StreakService.getTodayDate() },
            completed = false,
            xp = ScoringService.XP_TODO_TASK
        )
        val updatedTasks = listOf(newTask) + current.tasks
        persist(current.copy(tasks = updatedTasks))
        notifyUser("Task added: ${newTask.title}")
        return true
    }

    fun toggleTaskComplete(taskId: String) {
        val current = _dataState.value
        val task = current.tasks.firstOrNull { it.id == taskId } ?: return
        val newCompleted = !task.completed
        val completedAt = if (newCompleted) System.currentTimeMillis() else null

        val updatedTasks = current.tasks.map {
            if (it.id == taskId) it.copy(completed = newCompleted, completedAt = completedAt) else it
        }

        var user = current.user
        var awardedXP = 0
        val actionKey = "task_${task.id}"

        if (newCompleted) {
            if (user.penalizedTaskIds.contains(task.id)) {
                val newTotalXP = (user.totalXP + task.xp).coerceAtLeast(0)
                val levelInfo = ScoringService.calculateLevelInfo(newTotalXP)
                user = user.copy(
                    totalXP = newTotalXP,
                    level = levelInfo.level,
                    penalizedTaskIds = user.penalizedTaskIds - task.id,
                    awardedActionIds = user.awardedActionIds + actionKey
                )
                awardedXP = task.xp
            } else {
                val (updatedUser, xp) = ScoringService.tryAwardXP(user, actionKey, task.xp)
                user = updatedUser
                awardedXP = xp
            }

            val appreciation = AchievementService.getRandomAppreciationMessage()
            val msg = if (awardedXP > 0) "$appreciation (+${awardedXP} XP)" else appreciation
            notifyUser(msg, awardedXP)
        } else {
            // Task uncompleted: deduct corresponding XP if it was previously awarded, minimum XP 0
            if (user.awardedActionIds.contains(actionKey)) {
                val newTotalXP = (user.totalXP - task.xp).coerceAtLeast(0)
                val levelInfo = ScoringService.calculateLevelInfo(newTotalXP)
                user = user.copy(
                    totalXP = newTotalXP,
                    level = levelInfo.level,
                    awardedActionIds = user.awardedActionIds - actionKey
                )
                notifyUser("Task marked incomplete (-${task.xp} XP)")
            }
        }

        // Evaluate achievements & rewards
        val completedCount = updatedTasks.count { it.completed }
        val (updatedAch, newlyUnlocked) = AchievementService.evaluateAchievements(
            currentAchievements = current.achievements,
            completedTasksCount = completedCount,
            streakDays = user.currentStreak,
            leetcodeCount = current.leetCodeRecords.size,
            aptitudeQuestionsCount = current.aptitudeSessions.sumOf { it.attempted },
            notesCount = current.notes.size,
            totalXP = user.totalXP
        )

        newlyUnlocked.forEach { ach ->
            notifyUser("🏆 Achievement Unlocked: ${ach.title}!")
        }

        val reward = AchievementService.checkSurpriseReward(
            totalXP = user.totalXP,
            streak = user.currentStreak,
            alreadyUnlockedIds = user.unlockedRewardIds
        )
        if (reward != null) {
            user = user.copy(
                totalXP = user.totalXP + reward.bonusXP,
                unlockedRewardIds = user.unlockedRewardIds + reward.id
            )
            _activeSurpriseReward.value = reward
        }

        persist(current.copy(user = user, tasks = updatedTasks, achievements = updatedAch))
    }

    fun deleteTask(taskId: String) {
        val current = _dataState.value
        val task = current.tasks.firstOrNull { it.id == taskId }
        var user = current.user
        val actionKey = "task_$taskId"
        if (task != null && task.completed && user.awardedActionIds.contains(actionKey)) {
            val newTotalXP = (user.totalXP - task.xp).coerceAtLeast(0)
            val levelInfo = ScoringService.calculateLevelInfo(newTotalXP)
            user = user.copy(
                totalXP = newTotalXP,
                level = levelInfo.level,
                awardedActionIds = user.awardedActionIds - actionKey
            )
        }
        val updated = current.tasks.filterNot { it.id == taskId }
        persist(current.copy(user = user, tasks = updated))
        notifyUser("Task deleted.")
    }

    fun editTask(
        id: String,
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDate: String
    ): Boolean {
        if (title.isBlank()) {
            notifyError("Task title cannot be empty.")
            return false
        }
        val current = _dataState.value
        val updated = current.tasks.map {
            if (it.id == id) {
                it.copy(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    priority = priority,
                    dueDate = dueDate
                )
            } else it
        }
        persist(current.copy(tasks = updated))
        notifyUser("Task updated successfully.")
        return true
    }

    // --- LeetCode Actions ---

    fun addLeetCodeRecord(
        name: String,
        number: Int,
        url: String,
        difficulty: LeetCodeDifficulty,
        topic: String,
        date: String,
        timeSpentMinutes: Int,
        attempts: Int,
        notes: String,
        approach: String,
        whatILearned: String,
        mistake: String,
        betterApproach: String,
        timeComplexity: String,
        spaceComplexity: String
    ): Boolean {
        if (name.isBlank()) {
            notifyError("Problem name is required.")
            return false
        }
        val current = _dataState.value
        val newRecord = LeetCodeRecord(
            id = UUID.randomUUID().toString(),
            problemName = name.trim(),
            problemNumber = number.coerceAtLeast(1),
            url = url.trim(),
            difficulty = difficulty,
            topic = topic.ifBlank { "Algorithms" },
            date = date.ifBlank { StreakService.getTodayDate() },
            timeSpentMinutes = timeSpentMinutes.coerceAtLeast(1),
            attempts = attempts.coerceAtLeast(1),
            solved = true,
            notes = notes.trim(),
            approach = approach.trim(),
            whatILearned = whatILearned.trim(),
            mistake = mistake.trim(),
            betterApproach = betterApproach.trim(),
            timeComplexity = timeComplexity.ifBlank { "O(N)" },
            spaceComplexity = spaceComplexity.ifBlank { "O(1)" }
        )

        // Also add learning note to Notes section if there are reflections/approaches
        val linkedNotes = if (approach.isNotBlank() || whatILearned.isNotBlank()) {
            val noteContent = buildString {
                append("LeetCode #${newRecord.problemNumber}: ${newRecord.problemName}\n\n")
                if (approach.isNotBlank()) append("📌 Approach:\n$approach\n\n")
                if (whatILearned.isNotBlank()) append("💡 What I Learned:\n$whatILearned\n\n")
                if (mistake.isNotBlank()) append("⚠️ Mistake Made:\n$mistake\n\n")
                if (betterApproach.isNotBlank()) append("🚀 Better Approach:\n$betterApproach\n\n")
                append("⏱ Time: ${newRecord.timeComplexity} | 💾 Space: ${newRecord.spaceComplexity}")
            }
            val autoNote = NoteItem(
                id = UUID.randomUUID().toString(),
                title = "LC #${newRecord.problemNumber} ${newRecord.problemName} Analysis",
                content = noteContent,
                category = NoteCategory.CODING,
                tags = listOf("LeetCode", newRecord.topic, newRecord.difficulty.name),
                createdAt = newRecord.date,
                updatedAt = newRecord.date,
                linkedActivity = newRecord.id
            )
            listOf(autoNote) + current.notes
        } else {
            current.notes
        }

        val updatedRecords = listOf(newRecord) + current.leetCodeRecords

        val actionKey = "leetcode_${newRecord.id}"
        val (updatedUser, xp) = ScoringService.tryAwardXP(
            current.user,
            actionKey,
            ScoringService.XP_LEETCODE
        )

        val (updatedAch, newlyUnlocked) = AchievementService.evaluateAchievements(
            currentAchievements = current.achievements,
            completedTasksCount = current.tasks.count { it.completed },
            streakDays = updatedUser.currentStreak,
            leetcodeCount = updatedRecords.size,
            aptitudeQuestionsCount = current.aptitudeSessions.sumOf { it.attempted },
            notesCount = linkedNotes.size,
            totalXP = updatedUser.totalXP
        )

        newlyUnlocked.forEach { ach ->
            notifyUser("🏆 Achievement Unlocked: ${ach.title}!")
        }

        persist(
            current.copy(
                user = updatedUser,
                leetCodeRecords = updatedRecords,
                notes = linkedNotes,
                achievements = updatedAch
            )
        )
        notifyUser("🌸 Excellent! Problem solved: ${newRecord.problemName}", xp)
        return true
    }

    fun deleteLeetCodeRecord(id: String) {
        val current = _dataState.value
        val updated = current.leetCodeRecords.filterNot { it.id == id }
        persist(current.copy(leetCodeRecords = updated))
        notifyUser("LeetCode record removed.")
    }

    // --- Aptitude Actions ---

    fun addAptitudeSession(
        category: AptitudeCategory,
        topic: String,
        attempted: Int,
        correct: Int,
        timeSpentMinutes: Int,
        date: String
    ): Boolean {
        if (topic.isBlank()) {
            notifyError("Topic name is required.")
            return false
        }
        if (attempted <= 0) {
            notifyError("Attempted questions must be greater than 0.")
            return false
        }
        if (correct < 0 || correct > attempted) {
            notifyError("Correct answers cannot exceed attempted questions.")
            return false
        }

        val current = _dataState.value
        val session = AptitudeSession(
            id = UUID.randomUUID().toString(),
            category = category,
            topic = topic.trim(),
            attempted = attempted,
            correct = correct,
            incorrect = attempted - correct,
            accuracy = if (attempted > 0) (correct.toDouble() / attempted * 100.0) else 0.0,
            timeSpentMinutes = timeSpentMinutes.coerceAtLeast(1),
            date = date.ifBlank { StreakService.getTodayDate() }
        )

        val updatedSessions = listOf(session) + current.aptitudeSessions
        val actionKey = "aptitude_${session.id}"
        val xpAmount = session.attempted * ScoringService.XP_APTITUDE
        val (updatedUser, xp) = ScoringService.tryAwardXP(
            current.user,
            actionKey,
            xpAmount
        )

        val totalAptQuestions = updatedSessions.sumOf { it.attempted }
        val (updatedAch, newlyUnlocked) = AchievementService.evaluateAchievements(
            currentAchievements = current.achievements,
            completedTasksCount = current.tasks.count { it.completed },
            streakDays = updatedUser.currentStreak,
            leetcodeCount = current.leetCodeRecords.size,
            aptitudeQuestionsCount = totalAptQuestions,
            notesCount = current.notes.size,
            totalXP = updatedUser.totalXP
        )

        newlyUnlocked.forEach { ach ->
            notifyUser("🏆 Achievement Unlocked: ${ach.title}!")
        }

        persist(
            current.copy(
                user = updatedUser,
                aptitudeSessions = updatedSessions,
                achievements = updatedAch
            )
        )
        val accFormatted = String.format(Locale.getDefault(), "%.1f", session.accuracy)
        notifyUser("✨ Practice completed! Accuracy: $accFormatted%", xp)
        return true
    }

    fun deleteAptitudeSession(id: String) {
        val current = _dataState.value
        val updated = current.aptitudeSessions.filterNot { it.id == id }
        persist(current.copy(aptitudeSessions = updated))
        notifyUser("Aptitude practice session deleted.")
    }

    // --- LinkedIn Actions ---

    fun addLinkedInActivity(
        type: LinkedInActivityType,
        title: String,
        details: String
    ): Boolean {
        if (title.isBlank()) {
            notifyError("Activity title is required.")
            return false
        }
        val current = _dataState.value
        val weekId = StreakService.getCurrentWeekId()
        val activity = LinkedInActivity(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title.trim(),
            linkOrDetails = details.trim(),
            date = StreakService.getTodayDate(),
            weekId = weekId
        )
        val updatedActivities = listOf(activity) + current.linkedInActivities

        val thisWeekCount = updatedActivities.count { it.weekId == weekId }
        var user = current.user
        var bonus = 0

        if (thisWeekCount >= current.settings.weeklyLinkedInTarget) {
            val goalKey = "linkedin_week_$weekId"
            val (updatedUser, xp) = ScoringService.tryAwardXP(
                user,
                goalKey,
                ScoringService.XP_LINKEDIN_WEEKLY
            )
            user = updatedUser
            bonus = xp
        }

        persist(current.copy(user = user, linkedInActivities = updatedActivities))
        if (bonus > 0) {
            notifyUser("🎉 Weekly LinkedIn Target Met! (+${bonus} XP)", bonus)
        } else {
            notifyUser("✨ LinkedIn activity logged: ${activity.title}")
        }
        return true
    }

    fun deleteLinkedInActivity(id: String) {
        val current = _dataState.value
        persist(current.copy(linkedInActivities = current.linkedInActivities.filterNot { it.id == id }))
        notifyUser("LinkedIn activity removed.")
    }

    // --- GitHub Actions ---

    fun addGitHubActivity(
        type: GitHubActivityType,
        repoName: String,
        description: String,
        link: String
    ): Boolean {
        if (repoName.isBlank()) {
            notifyError("Repository name is required.")
            return false
        }
        val current = _dataState.value
        val weekId = StreakService.getCurrentWeekId()
        val activity = GitHubActivity(
            id = UUID.randomUUID().toString(),
            type = type,
            repoName = repoName.trim(),
            description = description.trim(),
            link = link.trim(),
            date = StreakService.getTodayDate(),
            weekId = weekId
        )
        val updatedActivities = listOf(activity) + current.gitHubActivities

        val thisWeekCount = updatedActivities.count { it.weekId == weekId }
        var user = current.user
        var bonus = 0

        if (thisWeekCount >= current.settings.weeklyGitHubTarget) {
            val goalKey = "github_week_$weekId"
            val (updatedUser, xp) = ScoringService.tryAwardXP(
                user,
                goalKey,
                ScoringService.XP_GITHUB_WEEKLY
            )
            user = updatedUser
            bonus = xp
        }

        persist(current.copy(user = user, gitHubActivities = updatedActivities))
        if (bonus > 0) {
            notifyUser("🦚 Great work! Weekly GitHub Goal Met (+${bonus} XP)", bonus)
        } else {
            notifyUser("💻 GitHub activity recorded: ${activity.repoName}")
        }
        return true
    }

    fun deleteGitHubActivity(id: String) {
        val current = _dataState.value
        persist(current.copy(gitHubActivities = current.gitHubActivities.filterNot { it.id == id }))
        notifyUser("GitHub record deleted.")
    }

    // --- Notes Actions ---

    fun addNote(
        title: String,
        content: String,
        category: NoteCategory,
        tags: List<String>,
        reviewDate: String?
    ): Boolean {
        if (title.isBlank()) {
            notifyError("Note title is required.")
            return false
        }
        val current = _dataState.value
        val today = StreakService.getTodayDate()
        val note = NoteItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            content = content.trim(),
            category = category,
            tags = tags.map { it.trim() }.filter { it.isNotBlank() },
            createdAt = today,
            updatedAt = today,
            reviewDate = reviewDate?.ifBlank { null }
        )
        val updatedNotes = listOf(note) + current.notes

        val actionKey = "note_${note.id}"
        val (updatedUser, xp) = ScoringService.tryAwardXP(
            current.user,
            actionKey,
            ScoringService.XP_LEARNING_NOTE
        )

        persist(current.copy(user = updatedUser, notes = updatedNotes))
        notifyUser("📝 Note saved to your knowledge garden.", xp)
        return true
    }

    fun editNote(
        id: String,
        title: String,
        content: String,
        category: NoteCategory,
        tags: List<String>,
        reviewDate: String?
    ): Boolean {
        if (title.isBlank()) {
            notifyError("Note title cannot be blank.")
            return false
        }
        val current = _dataState.value
        val today = StreakService.getTodayDate()
        val updated = current.notes.map {
            if (it.id == id) {
                it.copy(
                    title = title.trim(),
                    content = content.trim(),
                    category = category,
                    tags = tags.map { t -> t.trim() }.filter { t -> t.isNotBlank() },
                    updatedAt = today,
                    reviewDate = reviewDate?.ifBlank { null }
                )
            } else it
        }
        persist(current.copy(notes = updated))
        notifyUser("Note updated.")
        return true
    }

    fun toggleFavoriteNote(id: String) {
        val current = _dataState.value
        val updated = current.notes.map {
            if (it.id == id) it.copy(favorite = !it.favorite) else it
        }
        persist(current.copy(notes = updated))
    }

    fun togglePinNote(id: String) {
        val current = _dataState.value
        val updated = current.notes.map {
            if (it.id == id) it.copy(pinned = !it.pinned) else it
        }
        persist(current.copy(notes = updated))
    }

    fun duplicateNote(id: String) {
        val current = _dataState.value
        val source = current.notes.firstOrNull { it.id == id } ?: return
        val copy = source.copy(
            id = UUID.randomUUID().toString(),
            title = "${source.title} (Copy)",
            createdAt = StreakService.getTodayDate(),
            updatedAt = StreakService.getTodayDate()
        )
        persist(current.copy(notes = listOf(copy) + current.notes))
        notifyUser("Note duplicated.")
    }

    fun deleteNote(id: String) {
        val current = _dataState.value
        persist(current.copy(notes = current.notes.filterNot { it.id == id }))
        notifyUser("Note deleted.")
    }

    // --- Daily Reflection ---

    fun saveReflection(
        learned: String,
        difficult: String,
        improveTomorrow: String,
        proudOf: String
    ): Boolean {
        if (learned.isBlank() && difficult.isBlank() && improveTomorrow.isBlank() && proudOf.isBlank()) {
            notifyError("Please fill in at least one reflection insight.")
            return false
        }
        val current = _dataState.value
        val today = StreakService.getTodayDate()

        val existing = current.reflections.firstOrNull { it.date == today }
        val reflection = DailyReflection(
            id = existing?.id ?: UUID.randomUUID().toString(),
            date = today,
            learned = learned.trim(),
            difficult = difficult.trim(),
            improveTomorrow = improveTomorrow.trim(),
            proudOf = proudOf.trim()
        )

        val updatedReflections = listOf(reflection) + current.reflections.filterNot { it.date == today }

        val actionKey = "reflection_$today"
        val (updatedUser, xp) = ScoringService.tryAwardXP(
            current.user,
            actionKey,
            ScoringService.XP_DAILY_REFLECTION
        )

        persist(current.copy(user = updatedUser, reflections = updatedReflections))
        notifyUser("🪷 Daily reflection recorded! Rest and recharge.", xp)
        return true
    }

    // --- Settings, Reset & Backup ---

    fun updateSettings(settings: AppSettings) {
        val current = _dataState.value
        persist(current.copy(settings = settings, user = current.user.copy(name = settings.studentName)))
        notifyUser("Settings saved successfully.")
    }

    fun exportDataJson(): String {
        return storage.exportJsonString()
    }

    fun importDataJson(json: String): Boolean {
        val success = storage.importJsonString(json)
        if (success) {
            _dataState.value = storage.loadData()
            notifyUser("Data imported successfully.")
        } else {
            notifyError("Failed to import data. Please check JSON format.")
        }
        return success
    }

    fun resetToEmptyData() {
        val empty = storage.resetToEmpty()
        _dataState.value = empty
        notifyUser("All career data cleared.")
    }

    // --- Date Navigation & Daily Record System ---

    private val _selectedDate = MutableStateFlow(StreakService.getTodayDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun goToToday() {
        _selectedDate.value = StreakService.getTodayDate()
    }

    fun goToPreviousDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try {
            cal.time = sdf.parse(_selectedDate.value) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }
        cal.add(Calendar.DAY_OF_YEAR, -1)
        _selectedDate.value = sdf.format(cal.time)
    }

    fun goToNextDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try {
            cal.time = sdf.parse(_selectedDate.value) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }
        cal.add(Calendar.DAY_OF_YEAR, 1)
        _selectedDate.value = sdf.format(cal.time)
    }

    fun getDailyRecord(date: String): DailyProgressRecord {
        val current = _dataState.value
        return current.dailyRecords[date] ?: DailyProgressRecord(
            date = date,
            leetcodeCompleted = 0,
            leetcodeTarget = current.settings.dailyLeetCodeTarget,
            aptitudeCompleted = 0,
            aptitudeTarget = current.settings.dailyAptitudeTargetQuestions
        )
    }

    fun incrementLeetCode(date: String) {
        val current = _dataState.value
        val existing = getDailyRecord(date)
        val newCompleted = existing.leetcodeCompleted + 1
        var user = current.user

        // +50 XP per problem, keyed to date & problem index to prevent duplicate XP
        val actionKey = "lc_problem_${date}_$newCompleted"
        val (updatedUser, xp) = ScoringService.tryAwardXP(user, actionKey, ScoringService.XP_LEETCODE)
        user = updatedUser

        if (xp > 0) {
            notifyUser("💻 LeetCode Problem Solved! (+${ScoringService.XP_LEETCODE} XP)", xp)
        } else {
            notifyUser("LeetCode progress: $newCompleted/${existing.leetcodeTarget}")
        }

        val updatedRecord = existing.copy(
            leetcodeCompleted = newCompleted,
            leetcodeXpAwarded = existing.leetcodeXpAwarded || (newCompleted >= existing.leetcodeTarget),
            xpEarned = existing.xpEarned + xp
        )
        val updatedMap = current.dailyRecords + (date to updatedRecord)
        persist(current.copy(user = user, dailyRecords = updatedMap))
        checkAllGoalsCelebration(date)
    }

    fun decrementLeetCode(date: String) {
        val current = _dataState.value
        val existing = getDailyRecord(date)
        if (existing.leetcodeCompleted <= 0) return // No negative counters
        val actionKey = "lc_problem_${date}_${existing.leetcodeCompleted}"
        var user = current.user
        var xpDeducted = 0
        if (user.awardedActionIds.contains(actionKey)) {
            val newTotalXP = (user.totalXP - ScoringService.XP_LEETCODE).coerceAtLeast(0)
            val levelInfo = ScoringService.calculateLevelInfo(newTotalXP)
            user = user.copy(
                totalXP = newTotalXP,
                level = levelInfo.level,
                awardedActionIds = user.awardedActionIds - actionKey
            )
            xpDeducted = ScoringService.XP_LEETCODE
        }
        val newCompleted = existing.leetcodeCompleted - 1
        val updatedRecord = existing.copy(
            leetcodeCompleted = newCompleted,
            xpEarned = (existing.xpEarned - xpDeducted).coerceAtLeast(0)
        )
        val updatedMap = current.dailyRecords + (date to updatedRecord)
        if (xpDeducted > 0) {
            notifyUser("LeetCode problem undone (-$xpDeducted XP)")
        }
        persist(current.copy(user = user, dailyRecords = updatedMap))
    }

    fun incrementAptitude(date: String) {
        val current = _dataState.value
        val existing = getDailyRecord(date)
        val newCompleted = existing.aptitudeCompleted + 1
        var user = current.user

        // Keyed to date & problem index to prevent duplicate XP
        val actionKey = "apt_problem_${date}_$newCompleted"
        val (updatedUser, xp) = ScoringService.tryAwardXP(user, actionKey, ScoringService.XP_APTITUDE)
        user = updatedUser

        if (xp > 0) {
            notifyUser("🧠 Aptitude Problem Solved! (+${ScoringService.XP_APTITUDE} XP)", xp)
        } else {
            notifyUser("Aptitude progress: $newCompleted/${existing.aptitudeTarget}")
        }

        val updatedRecord = existing.copy(
            aptitudeCompleted = newCompleted,
            aptitudeXpAwarded = existing.aptitudeXpAwarded || (newCompleted >= existing.aptitudeTarget),
            xpEarned = existing.xpEarned + xp
        )
        val updatedMap = current.dailyRecords + (date to updatedRecord)
        persist(current.copy(user = user, dailyRecords = updatedMap))
        checkAllGoalsCelebration(date)
    }

    fun decrementAptitude(date: String) {
        val current = _dataState.value
        val existing = getDailyRecord(date)
        if (existing.aptitudeCompleted <= 0) return // No negative counters
        val actionKey = "apt_problem_${date}_${existing.aptitudeCompleted}"
        var user = current.user
        var xpDeducted = 0
        if (user.awardedActionIds.contains(actionKey)) {
            val newTotalXP = (user.totalXP - ScoringService.XP_APTITUDE).coerceAtLeast(0)
            val levelInfo = ScoringService.calculateLevelInfo(newTotalXP)
            user = user.copy(
                totalXP = newTotalXP,
                level = levelInfo.level,
                awardedActionIds = user.awardedActionIds - actionKey
            )
            xpDeducted = ScoringService.XP_APTITUDE
        }
        val newCompleted = existing.aptitudeCompleted - 1
        val updatedRecord = existing.copy(
            aptitudeCompleted = newCompleted,
            xpEarned = (existing.xpEarned - xpDeducted).coerceAtLeast(0)
        )
        val updatedMap = current.dailyRecords + (date to updatedRecord)
        if (xpDeducted > 0) {
            notifyUser("Aptitude question undone (-$xpDeducted XP)")
        }
        persist(current.copy(user = user, dailyRecords = updatedMap))
    }

    fun resetTodayRecord(date: String) {
        val current = _dataState.value
        val existing = getDailyRecord(date)
        var user = current.user
        var xpDeducted = 0

        for (i in 1..existing.leetcodeCompleted) {
            val lcKey = "lc_problem_${date}_$i"
            if (user.awardedActionIds.contains(lcKey)) {
                user = user.copy(
                    totalXP = (user.totalXP - ScoringService.XP_LEETCODE).coerceAtLeast(0),
                    awardedActionIds = user.awardedActionIds - lcKey
                )
                xpDeducted += ScoringService.XP_LEETCODE
            }
        }

        for (i in 1..existing.aptitudeCompleted) {
            val aptKey = "apt_problem_${date}_$i"
            if (user.awardedActionIds.contains(aptKey)) {
                user = user.copy(
                    totalXP = (user.totalXP - ScoringService.XP_APTITUDE).coerceAtLeast(0),
                    awardedActionIds = user.awardedActionIds - aptKey
                )
                xpDeducted += ScoringService.XP_APTITUDE
            }
        }

        val allGoalsKey = "all_goals_$date"
        if (user.awardedActionIds.contains(allGoalsKey)) {
            user = user.copy(
                totalXP = (user.totalXP - ScoringService.XP_ALL_DAILY_GOALS).coerceAtLeast(0),
                awardedActionIds = user.awardedActionIds - allGoalsKey
            )
            xpDeducted += ScoringService.XP_ALL_DAILY_GOALS
        }

        val levelInfo = ScoringService.calculateLevelInfo(user.totalXP)
        user = user.copy(level = levelInfo.level)

        val resetRecord = DailyProgressRecord(
            date = date,
            leetcodeCompleted = 0,
            leetcodeTarget = current.settings.dailyLeetCodeTarget,
            aptitudeCompleted = 0,
            aptitudeTarget = current.settings.dailyAptitudeTargetQuestions,
            leetcodeXpAwarded = false,
            aptitudeXpAwarded = false,
            allGoalsXpAwarded = false,
            xpEarned = 0
        )
        val updatedMap = current.dailyRecords + (date to resetRecord)
        persist(current.copy(user = user, dailyRecords = updatedMap))
        notifyUser("Reset progress for $date")
    }


    fun addQuickLinkedInPost(date: String) {
        val current = _dataState.value
        val weekId = StreakService.getCurrentWeekId()
        val activity = LinkedInActivity(
            id = UUID.randomUUID().toString(),
            type = LinkedInActivityType.CREATE_POST,
            title = "Shared career learning & technical insight",
            linkOrDetails = "Logged via Today Tracker",
            date = date,
            weekId = weekId
        )
        val updatedList = listOf(activity) + current.linkedInActivities
        val thisWeekCount = updatedList.count { it.weekId == weekId }
        var user = current.user
        if (thisWeekCount >= current.settings.weeklyLinkedInTarget) {
            val actionKey = "linkedin_week_$weekId"
            val (updatedUser, xp) = ScoringService.tryAwardXP(user, actionKey, ScoringService.XP_LINKEDIN_WEEKLY)
            user = updatedUser
            if (xp > 0) {
                notifyUser("🎯 Weekly LinkedIn Goal Met! (+${ScoringService.XP_LINKEDIN_WEEKLY} XP)", xp)
            } else {
                notifyUser("LinkedIn activity logged! ($thisWeekCount/${current.settings.weeklyLinkedInTarget} this week)")
            }
        } else {
            notifyUser("LinkedIn activity logged! ($thisWeekCount/${current.settings.weeklyLinkedInTarget} this week)")
        }
        persist(current.copy(user = user, linkedInActivities = updatedList))
    }

    fun addQuickGitHubContribution(date: String) {
        val current = _dataState.value
        val weekId = StreakService.getCurrentWeekId()
        val activity = GitHubActivity(
            id = UUID.randomUUID().toString(),
            type = GitHubActivityType.COMMIT,
            repoName = "career-quest",
            description = "Project commit and code contributions",
            link = "",
            date = date,
            weekId = weekId
        )
        val updatedList = listOf(activity) + current.gitHubActivities
        val thisWeekCount = updatedList.count { it.weekId == weekId }
        var user = current.user
        if (thisWeekCount >= current.settings.weeklyGitHubTarget) {
            val actionKey = "github_week_$weekId"
            val (updatedUser, xp) = ScoringService.tryAwardXP(user, actionKey, ScoringService.XP_GITHUB_WEEKLY)
            user = updatedUser
            if (xp > 0) {
                notifyUser("🚀 Weekly GitHub Goal Met! (+${ScoringService.XP_GITHUB_WEEKLY} XP)", xp)
            } else {
                notifyUser("GitHub contribution logged! ($thisWeekCount/${current.settings.weeklyGitHubTarget} this week)")
            }
        } else {
            notifyUser("GitHub contribution logged! ($thisWeekCount/${current.settings.weeklyGitHubTarget} this week)")
        }
        persist(current.copy(user = user, gitHubActivities = updatedList))
    }

    private fun checkAllGoalsCelebration(date: String) {
        val current = _dataState.value
        val rec = getDailyRecord(date)
        val todayTasks = current.tasks.filter { it.dueDate == date }
        val allTasksCompleted = todayTasks.isNotEmpty() && todayTasks.all { it.completed }
        val lcDone = rec.leetcodeCompleted >= rec.leetcodeTarget
        val aptDone = rec.aptitudeCompleted >= rec.aptitudeTarget

        if (lcDone && aptDone && (todayTasks.isEmpty() || allTasksCompleted) && !rec.allGoalsXpAwarded) {
            val actionKey = "all_goals_$date"
            val (updatedUser, xp) = ScoringService.tryAwardXP(current.user, actionKey, ScoringService.XP_ALL_DAILY_GOALS)
            val updatedRecord = rec.copy(allGoalsXpAwarded = true, xpEarned = rec.xpEarned + xp)
            val updatedMap = current.dailyRecords + (date to updatedRecord)
            persist(current.copy(user = updatedUser, dailyRecords = updatedMap))
            notifyUser("🎉 Incredible! All daily goals completed! (+${ScoringService.XP_ALL_DAILY_GOALS} XP Bonus)", xp)
        }
    }
}

