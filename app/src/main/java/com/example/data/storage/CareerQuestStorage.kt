package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.Achievement
import com.example.data.models.AppSettings
import com.example.data.models.AptitudeCategory
import com.example.data.models.AptitudeSession
import com.example.data.models.DailyProgressRecord
import com.example.data.models.DailyReflection
import com.example.data.models.GitHubActivity
import com.example.data.models.GitHubActivityType
import com.example.data.models.LeetCodeDifficulty
import com.example.data.models.LeetCodeRecord
import com.example.data.models.LinkedInActivity
import com.example.data.models.LinkedInActivityType
import com.example.data.models.NoteCategory
import com.example.data.models.NoteItem
import com.example.data.models.TaskCategory
import com.example.data.models.TaskItem
import com.example.data.models.TaskPriority
import com.example.data.models.UserStats
import com.example.services.AchievementService
import com.example.services.StreakService
import org.json.JSONArray
import org.json.JSONObject

data class CareerQuestData(
    val user: UserStats = UserStats(),
    val tasks: List<TaskItem> = emptyList(),
    val leetCodeRecords: List<LeetCodeRecord> = emptyList(),
    val aptitudeSessions: List<AptitudeSession> = emptyList(),
    val linkedInActivities: List<LinkedInActivity> = emptyList(),
    val gitHubActivities: List<GitHubActivity> = emptyList(),
    val notes: List<NoteItem> = emptyList(),
    val reflections: List<DailyReflection> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val dailyRecords: Map<String, DailyProgressRecord> = emptyMap()
)


class CareerQuestStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("career_quest_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CAREER_QUEST_DATA = "career_quest_json_data"
    }

    fun loadData(): CareerQuestData {
        val jsonStr = prefs.getString(KEY_CAREER_QUEST_DATA, null)
        return if (jsonStr.isNullOrEmpty()) {
            resetToEmpty()
        } else {
            try {
                deserializeData(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
                resetToEmpty()
            }
        }
    }

    fun saveData(data: CareerQuestData) {
        val jsonStr = serializeData(data)
        prefs.edit().putString(KEY_CAREER_QUEST_DATA, jsonStr).apply()
    }

    fun exportJsonString(): String {
        val currentData = loadData()
        return serializeData(currentData)
    }

    fun importJsonString(jsonStr: String): Boolean {
        return try {
            val parsed = deserializeData(jsonStr)
            saveData(parsed)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetToEmpty(): CareerQuestData {
        val emptyData = CareerQuestData(
            user = UserStats(
                id = "user_student_1",
                name = "Student",
                totalXP = 0,
                level = 1,
                currentStreak = 0,
                longestStreak = 0,
                awardedActionIds = emptySet(),
                penalizedTaskIds = emptySet(),
                unlockedRewardIds = emptySet()
            ),
            tasks = emptyList(),
            leetCodeRecords = emptyList(),
            aptitudeSessions = emptyList(),
            linkedInActivities = emptyList(),
            gitHubActivities = emptyList(),
            notes = emptyList(),
            reflections = emptyList(),
            achievements = AchievementService.INITIAL_ACHIEVEMENTS.map {
                it.copy(current = 0, unlocked = false, unlockedDate = null)
            },
            settings = AppSettings(),
            dailyRecords = emptyMap()
        )

        saveData(emptyData)
        return emptyData
    }

    // --- Serialization Helpers ---

    private fun serializeData(data: CareerQuestData): String {
        val root = JSONObject()

        // User
        val userObj = JSONObject().apply {
            put("id", data.user.id)
            put("name", data.user.name)
            put("totalXP", data.user.totalXP)
            put("level", data.user.level)
            put("currentStreak", data.user.currentStreak)
            put("longestStreak", data.user.longestStreak)
            put("awardedActionIds", JSONArray(data.user.awardedActionIds))
            put("penalizedTaskIds", JSONArray(data.user.penalizedTaskIds))
            put("unlockedRewardIds", JSONArray(data.user.unlockedRewardIds))
        }
        root.put("user", userObj)

        // Tasks
        val tasksArr = JSONArray()
        data.tasks.forEach { t ->
            val o = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("description", t.description)
                put("category", t.category.name)
                put("priority", t.priority.name)
                put("dueDate", t.dueDate)
                put("completed", t.completed)
                put("completedAt", t.completedAt ?: 0L)
                put("xp", t.xp)
            }
            tasksArr.put(o)
        }
        root.put("tasks", tasksArr)

        // LeetCode
        val lcArr = JSONArray()
        data.leetCodeRecords.forEach { lc ->
            val o = JSONObject().apply {
                put("id", lc.id)
                put("problemName", lc.problemName)
                put("problemNumber", lc.problemNumber)
                put("url", lc.url)
                put("difficulty", lc.difficulty.name)
                put("topic", lc.topic)
                put("date", lc.date)
                put("timeSpentMinutes", lc.timeSpentMinutes)
                put("attempts", lc.attempts)
                put("solved", lc.solved)
                put("notes", lc.notes)
                put("approach", lc.approach)
                put("whatILearned", lc.whatILearned)
                put("mistake", lc.mistake)
                put("betterApproach", lc.betterApproach)
                put("timeComplexity", lc.timeComplexity)
                put("spaceComplexity", lc.spaceComplexity)
            }
            lcArr.put(o)
        }
        root.put("leetCodeRecords", lcArr)

        // Aptitude
        val aptArr = JSONArray()
        data.aptitudeSessions.forEach { a ->
            val o = JSONObject().apply {
                put("id", a.id)
                put("category", a.category.name)
                put("topic", a.topic)
                put("attempted", a.attempted)
                put("correct", a.correct)
                put("incorrect", a.incorrect)
                put("accuracy", a.accuracy)
                put("timeSpentMinutes", a.timeSpentMinutes)
                put("date", a.date)
            }
            aptArr.put(o)
        }
        root.put("aptitudeSessions", aptArr)

        // LinkedIn
        val liArr = JSONArray()
        data.linkedInActivities.forEach { li ->
            val o = JSONObject().apply {
                put("id", li.id)
                put("type", li.type.name)
                put("title", li.title)
                put("linkOrDetails", li.linkOrDetails)
                put("date", li.date)
                put("weekId", li.weekId)
            }
            liArr.put(o)
        }
        root.put("linkedInActivities", liArr)

        // GitHub
        val ghArr = JSONArray()
        data.gitHubActivities.forEach { gh ->
            val o = JSONObject().apply {
                put("id", gh.id)
                put("type", gh.type.name)
                put("repoName", gh.repoName)
                put("description", gh.description)
                put("link", gh.link)
                put("date", gh.date)
                put("weekId", gh.weekId)
            }
            ghArr.put(o)
        }
        root.put("gitHubActivities", ghArr)

        // Notes
        val notesArr = JSONArray()
        data.notes.forEach { n ->
            val o = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("content", n.content)
                put("category", n.category.name)
                put("tags", JSONArray(n.tags))
                put("createdAt", n.createdAt)
                put("updatedAt", n.updatedAt)
                put("favorite", n.favorite)
                put("pinned", n.pinned)
                put("reviewDate", n.reviewDate ?: "")
                put("linkedActivity", n.linkedActivity ?: "")
            }
            notesArr.put(o)
        }
        root.put("notes", notesArr)

        // Reflections
        val refArr = JSONArray()
        data.reflections.forEach { r ->
            val o = JSONObject().apply {
                put("id", r.id)
                put("date", r.date)
                put("learned", r.learned)
                put("difficult", r.difficult)
                put("improveTomorrow", r.improveTomorrow)
                put("proudOf", r.proudOf)
            }
            refArr.put(o)
        }
        root.put("reflections", refArr)

        // Achievements
        val achArr = JSONArray()
        data.achievements.forEach { ach ->
            val o = JSONObject().apply {
                put("id", ach.id)
                put("title", ach.title)
                put("iconSymbol", ach.iconSymbol)
                put("description", ach.description)
                put("target", ach.target)
                put("current", ach.current)
                put("unlocked", ach.unlocked)
                put("unlockedDate", ach.unlockedDate ?: "")
            }
            achArr.put(o)
        }
        root.put("achievements", achArr)

        // Settings
        val setObj = JSONObject().apply {
            put("studentName", data.settings.studentName)
            put("dailyLeetCodeTarget", data.settings.dailyLeetCodeTarget)
            put("weeklyLinkedInTarget", data.settings.weeklyLinkedInTarget)
            put("weeklyGitHubTarget", data.settings.weeklyGitHubTarget)
            put("dailyAptitudeTargetQuestions", data.settings.dailyAptitudeTargetQuestions)
            put("enableAnimations", data.settings.enableAnimations)
            put("enableMotivationalMessages", data.settings.enableMotivationalMessages)
            put("enableBackgroundMusic", data.settings.enableBackgroundMusic)
        }
        root.put("settings", setObj)

        // Daily Records
        val dailyObj = JSONObject()
        data.dailyRecords.forEach { (dateKey, rec) ->
            val dObj = JSONObject().apply {
                put("date", rec.date)
                put("leetcodeCompleted", rec.leetcodeCompleted)
                put("leetcodeTarget", rec.leetcodeTarget)
                put("aptitudeCompleted", rec.aptitudeCompleted)
                put("aptitudeTarget", rec.aptitudeTarget)
                put("completedTaskIds", JSONArray(rec.completedTaskIds))
                put("notesCreatedIds", JSONArray(rec.notesCreatedIds))
                put("xpEarned", rec.xpEarned)
                put("leetcodeXpAwarded", rec.leetcodeXpAwarded)
                put("aptitudeXpAwarded", rec.aptitudeXpAwarded)
                put("allGoalsXpAwarded", rec.allGoalsXpAwarded)
            }
            dailyObj.put(dateKey, dObj)
        }
        root.put("dailyRecords", dailyObj)

        return root.toString(2)

    }

    private fun deserializeData(jsonStr: String): CareerQuestData {
        val root = JSONObject(jsonStr)

        val uObj = root.optJSONObject("user") ?: JSONObject()
        val awardedSet = mutableSetOf<String>()
        val awardedArr = uObj.optJSONArray("awardedActionIds")
        if (awardedArr != null) {
            for (i in 0 until awardedArr.length()) awardedSet.add(awardedArr.getString(i))
        }
        val penalizedSet = mutableSetOf<String>()
        val penalizedArr = uObj.optJSONArray("penalizedTaskIds")
        if (penalizedArr != null) {
            for (i in 0 until penalizedArr.length()) penalizedSet.add(penalizedArr.getString(i))
        }
        val rewardSet = mutableSetOf<String>()
        val rewardArr = uObj.optJSONArray("unlockedRewardIds")
        if (rewardArr != null) {
            for (i in 0 until rewardArr.length()) rewardSet.add(rewardArr.getString(i))
        }

        val user = UserStats(
            id = uObj.optString("id", "user_student_1"),
            name = uObj.optString("name", "Student"),
            totalXP = uObj.optInt("totalXP", 0),
            level = uObj.optInt("level", 1),
            currentStreak = uObj.optInt("currentStreak", 0),
            longestStreak = uObj.optInt("longestStreak", 0),
            awardedActionIds = awardedSet,
            penalizedTaskIds = penalizedSet,
            unlockedRewardIds = rewardSet
        )

        val tasksList = mutableListOf<TaskItem>()
        val tasksArr = root.optJSONArray("tasks")
        if (tasksArr != null) {
            for (i in 0 until tasksArr.length()) {
                val o = tasksArr.getJSONObject(i)
                tasksList.add(
                    TaskItem(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        description = o.optString("description", ""),
                        category = TaskCategory.valueOf(o.optString("category", TaskCategory.CODING.name)),
                        priority = TaskPriority.valueOf(o.optString("priority", TaskPriority.MEDIUM.name)),
                        dueDate = o.optString("dueDate", ""),
                        completed = o.optBoolean("completed", false),
                        completedAt = o.optLong("completedAt", 0L).let { if (it > 0) it else null },
                        xp = o.optInt("xp", 10)
                    )
                )
            }
        }

        val lcList = mutableListOf<LeetCodeRecord>()
        val lcArr = root.optJSONArray("leetCodeRecords")
        if (lcArr != null) {
            for (i in 0 until lcArr.length()) {
                val o = lcArr.getJSONObject(i)
                lcList.add(
                    LeetCodeRecord(
                        id = o.optString("id"),
                        problemName = o.optString("problemName"),
                        problemNumber = o.optInt("problemNumber", 1),
                        url = o.optString("url", ""),
                        difficulty = LeetCodeDifficulty.valueOf(o.optString("difficulty", LeetCodeDifficulty.MEDIUM.name)),
                        topic = o.optString("topic", "Arrays"),
                        date = o.optString("date", StreakService.getTodayDate()),
                        timeSpentMinutes = o.optInt("timeSpentMinutes", 25),
                        attempts = o.optInt("attempts", 1),
                        solved = o.optBoolean("solved", true),
                        notes = o.optString("notes", ""),
                        approach = o.optString("approach", ""),
                        whatILearned = o.optString("whatILearned", ""),
                        mistake = o.optString("mistake", ""),
                        betterApproach = o.optString("betterApproach", ""),
                        timeComplexity = o.optString("timeComplexity", "O(N)"),
                        spaceComplexity = o.optString("spaceComplexity", "O(1)")
                    )
                )
            }
        }

        val aptList = mutableListOf<AptitudeSession>()
        val aptArr = root.optJSONArray("aptitudeSessions")
        if (aptArr != null) {
            for (i in 0 until aptArr.length()) {
                val o = aptArr.getJSONObject(i)
                val att = o.optInt("attempted", 10)
                val corr = o.optInt("correct", 8)
                aptList.add(
                    AptitudeSession(
                        id = o.optString("id"),
                        category = AptitudeCategory.valueOf(o.optString("category", AptitudeCategory.QUANTITATIVE.name)),
                        topic = o.optString("topic", "Percentages"),
                        attempted = att,
                        correct = corr,
                        incorrect = o.optInt("incorrect", att - corr),
                        accuracy = o.optDouble("accuracy", 80.0),
                        timeSpentMinutes = o.optInt("timeSpentMinutes", 20),
                        date = o.optString("date", StreakService.getTodayDate())
                    )
                )
            }
        }

        val liList = mutableListOf<LinkedInActivity>()
        val liArr = root.optJSONArray("linkedInActivities")
        if (liArr != null) {
            for (i in 0 until liArr.length()) {
                val o = liArr.getJSONObject(i)
                liList.add(
                    LinkedInActivity(
                        id = o.optString("id"),
                        type = LinkedInActivityType.valueOf(o.optString("type", LinkedInActivityType.CREATE_POST.name)),
                        title = o.optString("title"),
                        linkOrDetails = o.optString("linkOrDetails", ""),
                        date = o.optString("date", StreakService.getTodayDate()),
                        weekId = o.optString("weekId", StreakService.getCurrentWeekId())
                    )
                )
            }
        }

        val ghList = mutableListOf<GitHubActivity>()
        val ghArr = root.optJSONArray("gitHubActivities")
        if (ghArr != null) {
            for (i in 0 until ghArr.length()) {
                val o = ghArr.getJSONObject(i)
                ghList.add(
                    GitHubActivity(
                        id = o.optString("id"),
                        type = GitHubActivityType.valueOf(o.optString("type", GitHubActivityType.COMMIT.name)),
                        repoName = o.optString("repoName"),
                        description = o.optString("description", ""),
                        link = o.optString("link", ""),
                        date = o.optString("date", StreakService.getTodayDate()),
                        weekId = o.optString("weekId", StreakService.getCurrentWeekId())
                    )
                )
            }
        }

        val notesList = mutableListOf<NoteItem>()
        val notesArr = root.optJSONArray("notes")
        if (notesArr != null) {
            for (i in 0 until notesArr.length()) {
                val o = notesArr.getJSONObject(i)
                val tagsArr = o.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArr != null) {
                    for (j in 0 until tagsArr.length()) tags.add(tagsArr.getString(j))
                }
                notesList.add(
                    NoteItem(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        content = o.optString("content"),
                        category = NoteCategory.valueOf(o.optString("category", NoteCategory.CODING.name)),
                        tags = tags,
                        createdAt = o.optString("createdAt", StreakService.getTodayDate()),
                        updatedAt = o.optString("updatedAt", StreakService.getTodayDate()),
                        favorite = o.optBoolean("favorite", false),
                        pinned = o.optBoolean("pinned", false),
                        reviewDate = o.optString("reviewDate").let { if (it.isNotEmpty()) it else null },
                        linkedActivity = o.optString("linkedActivity").let { if (it.isNotEmpty()) it else null }
                    )
                )
            }
        }

        val refList = mutableListOf<DailyReflection>()
        val refArr = root.optJSONArray("reflections")
        if (refArr != null) {
            for (i in 0 until refArr.length()) {
                val o = refArr.getJSONObject(i)
                refList.add(
                    DailyReflection(
                        id = o.optString("id"),
                        date = o.optString("date"),
                        learned = o.optString("learned"),
                        difficult = o.optString("difficult"),
                        improveTomorrow = o.optString("improveTomorrow"),
                        proudOf = o.optString("proudOf")
                    )
                )
            }
        }

        val achList = mutableListOf<Achievement>()
        val achArr = root.optJSONArray("achievements")
        if (achArr != null && achArr.length() > 0) {
            for (i in 0 until achArr.length()) {
                val o = achArr.getJSONObject(i)
                achList.add(
                    Achievement(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        iconSymbol = o.optString("iconSymbol"),
                        description = o.optString("description"),
                        target = o.optInt("target", 1),
                        current = o.optInt("current", 0),
                        unlocked = o.optBoolean("unlocked", false),
                        unlockedDate = o.optString("unlockedDate").let { if (it.isNotEmpty()) it else null }
                    )
                )
            }
        } else {
            achList.addAll(AchievementService.INITIAL_ACHIEVEMENTS)
        }

        val setObj = root.optJSONObject("settings") ?: JSONObject()
        val settings = AppSettings(
            studentName = setObj.optString("studentName", "Student"),
            dailyLeetCodeTarget = setObj.optInt("dailyLeetCodeTarget", 2),
            weeklyLinkedInTarget = setObj.optInt("weeklyLinkedInTarget", 2),
            weeklyGitHubTarget = setObj.optInt("weeklyGitHubTarget", 1),
            dailyAptitudeTargetQuestions = setObj.optInt("dailyAptitudeTargetQuestions", 15),
            enableAnimations = setObj.optBoolean("enableAnimations", true),
            enableMotivationalMessages = setObj.optBoolean("enableMotivationalMessages", true),
            enableBackgroundMusic = setObj.optBoolean("enableBackgroundMusic", true)
        )

        val dailyRecordsMap = mutableMapOf<String, DailyProgressRecord>()
        val dailyObj = root.optJSONObject("dailyRecords")
        if (dailyObj != null) {
            val keys = dailyObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val dObj = dailyObj.optJSONObject(key)
                if (dObj != null) {
                    val taskIds = mutableListOf<String>()
                    val tArr = dObj.optJSONArray("completedTaskIds")
                    if (tArr != null) {
                        for (i in 0 until tArr.length()) taskIds.add(tArr.getString(i))
                    }
                    val noteIds = mutableListOf<String>()
                    val nArr = dObj.optJSONArray("notesCreatedIds")
                    if (nArr != null) {
                        for (i in 0 until nArr.length()) noteIds.add(nArr.getString(i))
                    }
                    dailyRecordsMap[key] = DailyProgressRecord(
                        date = dObj.optString("date", key),
                        leetcodeCompleted = dObj.optInt("leetcodeCompleted", 0),
                        leetcodeTarget = dObj.optInt("leetcodeTarget", 2),
                        aptitudeCompleted = dObj.optInt("aptitudeCompleted", 0),
                        aptitudeTarget = dObj.optInt("aptitudeTarget", 10),
                        completedTaskIds = taskIds,
                        notesCreatedIds = noteIds,
                        xpEarned = dObj.optInt("xpEarned", 0),
                        leetcodeXpAwarded = dObj.optBoolean("leetcodeXpAwarded", false),
                        aptitudeXpAwarded = dObj.optBoolean("aptitudeXpAwarded", false),
                        allGoalsXpAwarded = dObj.optBoolean("allGoalsXpAwarded", false)
                    )
                }
            }
        }

        return CareerQuestData(
            user = user,
            tasks = tasksList,
            leetCodeRecords = lcList,
            aptitudeSessions = aptList,
            linkedInActivities = liList,
            gitHubActivities = ghList,
            notes = notesList,
            reflections = refList,
            achievements = achList,
            settings = settings,
            dailyRecords = dailyRecordsMap
        )

    }
}
