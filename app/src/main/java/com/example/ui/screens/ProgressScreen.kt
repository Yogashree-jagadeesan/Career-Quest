package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.storage.CareerQuestData
import com.example.services.ScoringService
import com.example.services.StreakService
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgressScreen(
    data: CareerQuestData
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly
    val today = StreakService.getTodayDate()
    val weekId = StreakService.getCurrentWeekId()

    // Daily Metrics
    val todayTasksCompleted = data.tasks.count { it.dueDate == today && it.completed }
    val todayLeetSolved = data.leetCodeRecords.count { it.date == today && it.solved }
    val todayAptQuestions = data.aptitudeSessions.filter { it.date == today }.sumOf { it.attempted }
    val todayReflection = data.reflections.any { it.date == today }

    // Weekly Metrics
    val weekTasks = data.tasks.count { it.completed }
    val weekLeet = data.leetCodeRecords.size
    val weekLinkedIn = data.linkedInActivities.count { it.weekId == weekId }
    val weekGitHub = data.gitHubActivities.count { it.weekId == weekId }
    val weekAptitude = data.aptitudeSessions.sumOf { it.attempted }

    // Monthly Metrics
    val totalSolvedLC = data.leetCodeRecords.count { it.solved }
    val totalAptitudeAttempted = data.aptitudeSessions.sumOf { it.attempted }
    val totalTasksCompleted = data.tasks.count { it.completed }
    val totalNotesWritten = data.notes.size

    val levelInfo = ScoringService.calculateLevelInfo(data.user.totalXP)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("progress_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeritageOrnamentalCard(borderColor = DivineGold) {
                Text(
                    "Career Growth Analytics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DivineGold
                )
                Text(
                    "Track your mastery across coding, aptitude, projects, and career branding",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = DivineGold
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Daily", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 0) DivineGold else Color.White.copy(alpha = 0.6f)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Weekly", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 1) DivineGold else Color.White.copy(alpha = 0.6f)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Monthly / All Time", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 2) DivineGold else Color.White.copy(alpha = 0.6f)) }
                    )
                }
            }
        }

        // Selected Timeframe Breakdown
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD9061A2D))
                    .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (selectedTab) {
                        0 -> {
                            Text("Today's Snapshot", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DivineGold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricBox("LeetCode", "$todayLeetSolved", TurquoiseAccent, Modifier.weight(1f))
                                MetricBox("Aptitude Qs", "$todayAptQuestions", GoldYellowWarm, Modifier.weight(1f))
                                MetricBox("Tasks Done", "$todayTasksCompleted", EmeraldMint, Modifier.weight(1f))
                                MetricBox("Reflection", if (todayReflection) "Done" else "Pending", LotusPink, Modifier.weight(1f))
                            }
                        }
                        1 -> {
                            Text("Weekly Achievements", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DivineGold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricBox("LinkedIn", "$weekLinkedIn / ${data.settings.weeklyLinkedInTarget}", TurquoiseAccent, Modifier.weight(1f))
                                MetricBox("GitHub", "$weekGitHub / ${data.settings.weeklyGitHubTarget}", EmeraldMint, Modifier.weight(1f))
                                MetricBox("LeetCode", "$weekLeet", GoldYellowWarm, Modifier.weight(1f))
                                MetricBox("Aptitude", "$weekAptitude", LotusPink, Modifier.weight(1f))
                            }
                        }
                        2 -> {
                            Text("Overall Mastery Record", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DivineGold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricBox("Total XP", "${data.user.totalXP}", DivineGold, Modifier.weight(1f))
                                MetricBox("Problems", "$totalSolvedLC", TurquoiseAccent, Modifier.weight(1f))
                                MetricBox("Aptitude Qs", "$totalAptitudeAttempted", GoldYellowWarm, Modifier.weight(1f))
                                MetricBox("Tasks", "$totalTasksCompleted", EmeraldMint, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Visual Canvas Bar Chart
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD9061A2D))
                    .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Weekly Effort Distribution",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DivineGold
                    )
                    Text("Daily tasks and practice activity across the last 7 days", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))

                    Spacer(modifier = Modifier.height(16.dp))

                    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                    val dayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

                    val last7DaysData = remember(data.dailyRecords, data.tasks) {
                        (6 downTo 0).map { offset ->
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
                            val dateStr = sdf.format(cal.time)
                            val dayName = dayFormat.format(cal.time)
                            val record = data.dailyRecords[dateStr]
                            val lcFrac = if (record != null && record.leetcodeTarget > 0) {
                                (record.leetcodeCompleted.toFloat() / record.leetcodeTarget).coerceIn(0f, 1f)
                            } else 0f
                            val aptFrac = if (record != null && record.aptitudeTarget > 0) {
                                (record.aptitudeCompleted.toFloat() / record.aptitudeTarget).coerceIn(0f, 1f)
                            } else 0f
                            val dayTasks = data.tasks.filter { it.dueDate == dateStr }
                            val taskFrac = if (dayTasks.isNotEmpty()) {
                                dayTasks.count { it.completed }.toFloat() / dayTasks.size
                            } else 0f
                            val avgFrac = if (record != null || dayTasks.isNotEmpty()) {
                                ((lcFrac + aptFrac + (if (dayTasks.isNotEmpty()) taskFrac else 0.5f)) / 2.5f).coerceIn(0.12f, 1f)
                            } else {
                                0.15f
                            }
                            Pair(dayName, avgFrac)
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val barWidth = (size.width / (last7DaysData.size * 2))
                        val maxH = size.height - 20f

                        last7DaysData.forEachIndexed { i, pair ->
                            val frac = pair.second
                            val x = i * (size.width / last7DaysData.size) + (barWidth / 2)
                            val barH = maxH * frac
                            val y = maxH - barH

                            // Draw bar with gold glow
                            drawRoundRect(
                                color = if (i == last7DaysData.lastIndex) DivineGold else TurquoiseAccent,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        last7DaysData.forEach { pair ->
                            Text(pair.first, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                }
            }
        }

        // Skill Progress Bars
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD9061A2D))
                    .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Skill Progression", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DivineGold)

                    SkillProgressItem(
                        skill = "Problem Solving & DSA",
                        current = totalSolvedLC,
                        target = 100,
                        color = PeacockBlue
                    )
                    SkillProgressItem(
                        skill = "Aptitude Readiness",
                        current = totalAptitudeAttempted,
                        target = 200,
                        color = TurquoiseAccent
                    )
                    SkillProgressItem(
                        skill = "Git & Open Source Building",
                        current = data.gitHubActivities.size,
                        target = 20,
                        color = EmeraldMint
                    )
                    SkillProgressItem(
                        skill = "Professional Branding (LinkedIn)",
                        current = data.linkedInActivities.size,
                        target = 20,
                        color = LotusPink
                    )
                    SkillProgressItem(
                        skill = "Learning Consistency & Notes",
                        current = totalNotesWritten,
                        target = 30,
                        color = GoldYellowWarm
                    )
                }
            }
        }
    }
}

@Composable
fun SkillProgressItem(
    skill: String,
    current: Int,
    target: Int,
    color: Color
) {
    val fraction = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val percent = (fraction * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(skill, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text("$current / $target ($percent%)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
