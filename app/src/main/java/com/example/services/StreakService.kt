package com.example.services

import com.example.data.models.AptitudeSession
import com.example.data.models.DailyReflection
import com.example.data.models.LeetCodeRecord
import com.example.data.models.NoteItem
import com.example.data.models.TaskItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DayStatus {
    COMPLETED,
    PARTIAL,
    MISSED,
    FUTURE
}

data class DayActivitySummary(
    val date: String,
    val dayNumber: Int,
    val status: DayStatus,
    val tasksCompleted: List<TaskItem>,
    val leetCodeRecords: List<LeetCodeRecord>,
    val aptitudeSessions: List<AptitudeSession>,
    val notes: List<NoteItem>,
    val reflection: DailyReflection?,
    val totalXPEarned: Int
)

object StreakService {

    fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getCurrentWeekId(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.getDefault(), "%d-W%02d", year, week)
    }

    fun getMonthlyCalendarDays(
        year: Int,
        month: Int, // 1 to 12
        tasks: List<TaskItem>,
        leetCode: List<LeetCodeRecord>,
        aptitude: List<AptitudeSession>,
        notes: List<NoteItem>,
        reflections: List<DailyReflection>
    ): List<DayActivitySummary> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayStr = getTodayDate()

        val results = mutableListOf<DayActivitySummary>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = sdf.format(cal.time)

            val dayTasks = tasks.filter { it.dueDate == dateStr && it.completed }
            val dayLeet = leetCode.filter { it.date == dateStr }
            val dayApt = aptitude.filter { it.date == dateStr }
            val dayNotes = notes.filter { it.createdAt == dateStr }
            val dayReflect = reflections.firstOrNull { it.date == dateStr }

            val isFuture = dateStr > todayStr

            val status = if (isFuture) {
                DayStatus.FUTURE
            } else {
                val hasLeetCode = dayLeet.isNotEmpty()
                val hasTasks = dayTasks.isNotEmpty()
                val hasAptitude = dayApt.isNotEmpty()

                if (hasLeetCode && (hasTasks || hasAptitude)) {
                    DayStatus.COMPLETED
                } else if (hasLeetCode || hasTasks || hasAptitude || dayNotes.isNotEmpty() || dayReflect != null) {
                    DayStatus.PARTIAL
                } else {
                    DayStatus.MISSED
                }
            }

            var xpEarned = dayLeet.size * ScoringService.XP_LEETCODE +
                    dayApt.sumOf { it.attempted } * ScoringService.XP_APTITUDE +
                    dayTasks.size * ScoringService.XP_TODO_TASK +
                    dayNotes.size * ScoringService.XP_LEARNING_NOTE

            if (dayReflect != null) xpEarned += ScoringService.XP_DAILY_REFLECTION
            if (status == DayStatus.COMPLETED) xpEarned += ScoringService.XP_ALL_DAILY_GOALS

            results.add(
                DayActivitySummary(
                    date = dateStr,
                    dayNumber = day,
                    status = status,
                    tasksCompleted = dayTasks,
                    leetCodeRecords = dayLeet,
                    aptitudeSessions = dayApt,
                    notes = dayNotes,
                    reflection = dayReflect,
                    totalXPEarned = xpEarned
                )
            )
        }

        return results
    }

    fun calculateStreak(
        tasks: List<TaskItem>,
        leetCode: List<LeetCodeRecord>,
        aptitude: List<AptitudeSession>,
        notes: List<NoteItem>,
        reflections: List<DailyReflection>,
        dailyRecords: Map<String, com.example.data.models.DailyProgressRecord> = emptyMap()
    ): Pair<Int, Int> {
        val activeDates = mutableSetOf<String>()
        tasks.filter { it.completed }.forEach {
            if (it.dueDate.isNotBlank()) activeDates.add(it.dueDate)
            if (it.completedAt != null && it.completedAt > 0) {
                val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.completedAt))
                activeDates.add(d)
            }
        }
        leetCode.forEach { if (it.date.isNotBlank()) activeDates.add(it.date) }
        aptitude.forEach { if (it.date.isNotBlank()) activeDates.add(it.date) }
        notes.forEach { if (it.createdAt.isNotBlank()) activeDates.add(it.createdAt) }
        reflections.forEach { if (it.date.isNotBlank()) activeDates.add(it.date) }
        dailyRecords.forEach { (d, r) ->
            if (r.leetcodeCompleted > 0 || r.aptitudeCompleted > 0) activeDates.add(d)
        }

        if (activeDates.isEmpty()) return Pair(0, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var currentStreak = 0
        val checkCal = Calendar.getInstance()
        var checkDateStr = sdf.format(checkCal.time)

        if (!activeDates.contains(checkDateStr)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            checkDateStr = sdf.format(checkCal.time)
        }

        while (activeDates.contains(checkDateStr)) {
            currentStreak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            checkDateStr = sdf.format(checkCal.time)
        }

        val sortedDates = activeDates.sorted()
        var maxStreak = 0
        var tempStreak = 0
        var prevCal: Calendar? = null

        for (dStr in sortedDates) {
            val dCal = Calendar.getInstance()
            try {
                dCal.time = sdf.parse(dStr) ?: continue
            } catch (e: Exception) {
                continue
            }
            if (prevCal == null) {
                tempStreak = 1
            } else {
                val diffDays = ((dCal.timeInMillis - prevCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else if (diffDays > 1) {
                    tempStreak = 1
                }
            }
            if (tempStreak > maxStreak) maxStreak = tempStreak
            prevCal = dCal
        }

        return Pair(currentStreak, maxOf(currentStreak, maxStreak))
    }
}
