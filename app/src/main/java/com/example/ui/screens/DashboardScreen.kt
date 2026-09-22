package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.storage.CareerQuestData
import com.example.services.ScoringService
import com.example.services.StreakService
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    data: CareerQuestData,
    selectedDate: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onTodayDate: () -> Unit,
    onSelectDate: (String) -> Unit,
    onIncrementLeetCode: (String) -> Unit,
    onDecrementLeetCode: (String) -> Unit,
    onIncrementAptitude: (String) -> Unit,
    onDecrementAptitude: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onAddTask: (String, String, TaskCategory, TaskPriority, String) -> Boolean,
    onEditTask: (String, String, String, TaskCategory, TaskPriority, String) -> Boolean,
    onDeleteTask: (String) -> Unit,
    onAddLinkedInPost: (String) -> Unit,
    onAddGitHubContribution: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenReflection: () -> Unit,
    onResetToday: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val today = StreakService.getTodayDate()
    val isToday = selectedDate == today
    val weekId = StreakService.getCurrentWeekId()

    // Get or initialize record for selected date
    val dailyRecord = data.dailyRecords[selectedDate] ?: DailyProgressRecord(
        date = selectedDate,
        leetcodeCompleted = 0,
        leetcodeTarget = data.settings.dailyLeetCodeTarget,
        aptitudeCompleted = 0,
        aptitudeTarget = data.settings.dailyAptitudeTargetQuestions
    )

    val lcCompleted = dailyRecord.leetcodeCompleted
    val lcTarget = dailyRecord.leetcodeTarget.coerceAtLeast(1)
    val lcDone = lcCompleted >= lcTarget

    val aptCompleted = dailyRecord.aptitudeCompleted
    val aptTarget = dailyRecord.aptitudeTarget.coerceAtLeast(1)
    val aptDone = aptCompleted >= aptTarget

    // Tasks for the date
    val dayTasks = data.tasks.filter {
        it.dueDate == selectedDate || (isToday && (it.dueDate == today || it.dueDate.isBlank()))
    }
    val tasksCompletedCount = dayTasks.count { it.completed }
    val tasksTotalCount = dayTasks.size

    // Daily reflection status
    val dayReflection = data.reflections.firstOrNull { it.date == selectedDate }
    val reflectionDone = dayReflection != null

    // LinkedIn & GitHub weekly progress
    val linkedInThisWeek = data.linkedInActivities.count { it.weekId == weekId }
    val linkedInTarget = data.settings.weeklyLinkedInTarget.coerceAtLeast(1)

    val gitHubThisWeek = data.gitHubActivities.count { it.weekId == weekId }
    val gitHubTarget = data.settings.weeklyGitHubTarget.coerceAtLeast(1)

    // Dynamic Goals calculation
    // 1. LeetCode (1)
    // 2. Aptitude (1)
    // 3. Tasks (each task is a goal, min 1)
    // 4. Daily reflection (1)
    // 5. LinkedIn progress (1)
    // 6. GitHub progress (1)
    val totalGoalsCount = 2 + tasksTotalCount.coerceAtLeast(1) + 1 + 1 + 1
    var completedGoalsCount = 0
    if (lcDone) completedGoalsCount++
    if (aptDone) completedGoalsCount++
    completedGoalsCount += tasksCompletedCount
    if (reflectionDone) completedGoalsCount++
    if (linkedInThisWeek >= 1) completedGoalsCount++
    if (gitHubThisWeek >= 1) completedGoalsCount++

    val progressPercent = ((completedGoalsCount.toFloat() / totalGoalsCount.toFloat()) * 100).toInt().coerceIn(0, 100)
    val allGoalsCompleted = progressPercent == 100 || (lcDone && aptDone && (tasksTotalCount == 0 || tasksCompletedCount == tasksTotalCount))

    // Today's XP calculation
    val lcXP = lcCompleted * ScoringService.XP_LEETCODE
    val aptXP = aptCompleted * ScoringService.XP_APTITUDE
    val tasksXP = tasksCompletedCount * ScoringService.XP_TODO_TASK
    val totalTodayXP = lcXP + aptXP + tasksXP

    // User Level & Streak
    val levelInfo = ScoringService.calculateLevelInfo(data.user.totalXP)

    // Task Dialog State
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var showHeroBanner by remember { mutableStateOf(true) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Date Picker Dialog setup
    val calendar = Calendar.getInstance()
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.parse(selectedDate)?.let { calendar.time = it }
    } catch (_: Exception) {}

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onSelectDate(sdf.format(pickedCal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. TOP HEADER & DATE NAVIGATION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xF0061A2D),
                                    Color(0xE6140A2B),
                                    Color(0xCC051524)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    GlassGoldBorder,
                                    DivineGold,
                                    PurpleAccent.copy(alpha = 0.5f),
                                    DivineGold,
                                    GlassGoldBorder
                                )
                            ),
                            RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Greeting & Date Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            val greeting = when {
                                hour < 12 -> "Good Morning 🌅"
                                hour < 17 -> "Good Afternoon ☀️"
                                else -> "Good Evening 🪷"
                            }
                            Text(
                                text = greeting,
                                color = DivineGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDisplayDate(selectedDate),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Calendar Selector Button
                        IconButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x33061A2D))
                                .border(1.2.dp, DivineGold, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select Date",
                                tint = DivineGold
                            )
                        }
                    }

                    // ‹ Previous | Today | Next › bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x66020B14))
                            .border(1.dp, GlassGoldBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onPreviousDate) {
                            Text("‹ Prev", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onTodayDate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isToday) DivineGold else Color.White.copy(alpha = 0.15f),
                                contentColor = if (isToday) PeacockBlueDark else Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isToday) "• Today •" else "Jump to Today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        TextButton(onClick = onNextDate) {
                            Text("Next ›", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Badges: 🔥 7 Day Streak | ⭐ Level 4 • Problem Solver | 1,240 XP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Streak Badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xD92E084D))
                                .border(1.dp, GoldYellowWarm, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🔥", fontSize = 13.sp)
                            Text(
                                text = "${data.user.currentStreak}d Streak",
                                color = GoldYellowWarm,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Level Badge
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xD9061A2D))
                                .border(1.dp, DivineGold, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⭐", fontSize = 13.sp)
                            Text(
                                text = "Lvl ${levelInfo.level} • ${levelInfo.title}",
                                color = DivineGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Total XP
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xD9061A2D))
                                .border(1.dp, DivineGold, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${data.user.totalXP} XP",
                                color = DivineGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 2. KRISHNA FLUTE ARTISTIC HERO BANNER (Collapsible)
            if (showHeroBanner) {
                item {
                    PaddingBox {
                        KrishnaFluteHeroBanner(
                            onDismissOrCollapse = { showHeroBanner = false }
                        )
                    }
                }
            }

            // 3. DAILY PROGRESS (Large, visually prominent glassmorphism)
            item {
                PaddingBox {
                    HeritageOrnamentalCard(borderColor = DivineGold) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LotusIcon(size = 18.dp, tint = DivineGold)
                                    Text(
                                        text = "TODAY'S PROGRESS",
                                        color = DivineGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    IconButton(
                                        onClick = { showResetConfirmDialog = true },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset Today",
                                            tint = DivineGold.copy(alpha = 0.7f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "$completedGoalsCount / $totalGoalsCount goals completed",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "$progressPercent%",
                                color = if (allGoalsCompleted) DivineGold else GoldYellowWarm,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Progress Bar with gold glow
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (allGoalsCompleted) DivineGold else GoldYellowWarm,
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )

                        // 100% completion celebration golden glitter
                        if (allGoalsCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FullGoldenGlitterOverlay()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("✨", fontSize = 16.sp)
                                Text(
                                    text = "100% Complete! Divine Focus Achieved! (+100 XP)",
                                    color = DivineGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 4. LEETCODE TRACKER (Simple Counter: 1/2  −  +)
            item {
                PaddingBox {
                    HeritageOrnamentalCard(borderColor = TurquoiseAccent.copy(alpha = 0.5f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x3338BDF8))
                                        .border(1.2.dp, TurquoiseAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💻", fontSize = 18.sp)
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "LeetCode",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "+${ScoringService.XP_LEETCODE} XP/prob",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DivineGold
                                        )
                                    }
                                    Text(
                                        text = if (lcDone) "✓ Goal Completed" else "Daily coding practice",
                                        fontSize = 12.sp,
                                        color = if (lcDone) EmeraldMint else Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Simple Counter: 1/2   −   +
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xD9061A2D))
                                    .border(1.2.dp, DivineGold, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                // Fraction 1/2
                                Text(
                                    text = "$lcCompleted/$lcTarget",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (lcDone) EmeraldMint else DivineGold,
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                // − button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x2EFFFFFF))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        .clickable { onDecrementLeetCode(selectedDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                // + button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(listOf(DivineGold, GoldYellowWarm))
                                        )
                                        .border(1.dp, DivineGold, CircleShape)
                                        .clickable { onIncrementLeetCode(selectedDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PeacockBlueDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (lcCompleted.toFloat() / lcTarget).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (lcDone) EmeraldMint else TurquoiseAccent,
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )
                    }
                }
            }

            // 5. APTITUDE TRACKER (Simple Counter: 1/2  −  +)
            item {
                PaddingBox {
                    HeritageOrnamentalCard(borderColor = DivineGold.copy(alpha = 0.5f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33FBBF24))
                                        .border(1.2.dp, DivineGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🧠", fontSize = 18.sp)
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Aptitude",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "+${ScoringService.XP_APTITUDE} XP/Q",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DivineGold
                                        )
                                    }
                                    Text(
                                        text = if (aptDone) "✓ Goal Completed" else "Quant & logical reasoning",
                                        fontSize = 12.sp,
                                        color = if (aptDone) EmeraldMint else Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Simple Counter: 1/2   −   +
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xD9061A2D))
                                    .border(1.2.dp, DivineGold, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                // Fraction 1/2
                                Text(
                                    text = "$aptCompleted/$aptTarget",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (aptDone) EmeraldMint else DivineGold,
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                // − button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x2EFFFFFF))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        .clickable { onDecrementAptitude(selectedDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                // + button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(listOf(DivineGold, GoldYellowWarm))
                                        )
                                        .border(1.dp, DivineGold, CircleShape)
                                        .clickable { onIncrementAptitude(selectedDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PeacockBlueDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (aptCompleted.toFloat() / aptTarget).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (aptDone) EmeraldMint else GoldYellowWarm,
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )
                    }
                }
            }

            // 6. TODAY'S TASKS SECTION
            item {
                PaddingBox {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✅", fontSize = 16.sp)
                            Text(
                                text = "TODAY'S TO-DO",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DivineGold
                            )
                            Text(
                                text = "(+10 XP/task)",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { showAddTaskDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DivineGold,
                                contentColor = PeacockBlueDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (dayTasks.isEmpty()) {
                item {
                    PaddingBox {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xD9061A2D))
                                .border(1.dp, GlassGoldBorder, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks planned for this day. Tap '+ Add Task' above!",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(dayTasks, key = { it.id }) { task ->
                    PaddingBox {
                        TodayTaskItemRow(
                            task = task,
                            onToggle = { onToggleTask(task.id) },
                            onEdit = { taskToEdit = task },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }

            // 7. LINKEDIN & GITHUB WEEKLY TRACKERS
            item {
                PaddingBox {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // LinkedIn Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xD9061A2D))
                                .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💼", fontSize = 16.sp)
                                    Text("LINKEDIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DivineGold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+${ScoringService.XP_LINKEDIN_WEEKLY} XP / target",
                                    fontSize = 10.sp,
                                    color = GoldYellowWarm,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$linkedInThisWeek / $linkedInTarget Posts This Week",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (linkedInThisWeek.toFloat() / linkedInTarget).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = TurquoiseAccent,
                                    trackColor = Color.White.copy(alpha = 0.15f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onAddLinkedInPost(selectedDate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseSoft, contentColor = PeacockBlueDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("+ Log Post", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // GitHub Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xD9061A2D))
                                .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🐙", fontSize = 16.sp)
                                    Text("GITHUB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DivineGold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+${ScoringService.XP_GITHUB_WEEKLY} XP / target",
                                    fontSize = 10.sp,
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$gitHubThisWeek / $gitHubTarget Contribution",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (gitHubThisWeek.toFloat() / gitHubTarget).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = EmeraldMint,
                                    trackColor = Color.White.copy(alpha = 0.15f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onAddGitHubContribution(selectedDate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint, contentColor = PeacockBlueDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("+ Log Activity", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 8. TODAY'S XP CARD
            item {
                PaddingBox {
                    HeritageOrnamentalCard(borderColor = DivineGold) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S XP EARNED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DivineGold
                                )
                                Text(
                                    text = "+$totalTodayXP XP",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldYellowWarm
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                XPBreakdownPill("LeetCode", "+$lcXP XP", lcDone)
                                XPBreakdownPill("Aptitude", "+$aptXP XP", aptDone)
                                XPBreakdownPill("Tasks", "+$tasksXP XP", tasksCompletedCount > 0)
                            }
                        }
                    }
                }
            }

            // 9. MOTIVATIONAL SECTION WITH CELEBRATION
            item {
                PaddingBox {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xE6061A2D),
                                        Color(0xE6140A2B)
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                if (allGoalsCompleted) DivineGold else GlassGoldBorder,
                                RoundedCornerShape(18.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (allGoalsCompleted) {
                                Text("🪷 ✨ 🪷", fontSize = 24.sp)
                                Text(
                                    text = "Divine Dedication! All Goals Complete.",
                                    color = DivineGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Your steadfast effort builds unwavering mastery. +100 XP Bonus Awarded!",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                CelebrationParticleOverlay()
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LotusIcon(size = 18.dp, tint = DivineGold)
                                    Text(
                                        text = "MOTIVATION & WISDOM",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DivineGold,
                                        letterSpacing = 1.sp
                                    )
                                    LotusIcon(size = 18.dp, tint = DivineGold)
                                }
                                Text(
                                    text = "\"Karmanye vadhikaraste ma phaleshu kadachana\"",
                                    color = GoldYellowWarm,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Focus wholeheartedly on your practice and action. Consistent mastery is the highest virtue.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 10. DAILY REFLECTION BUTTON
            item {
                PaddingBox {
                    Button(
                        onClick = onOpenReflection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xD9061A2D),
                            contentColor = DivineGold
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.2.dp,
                                DivineGold,
                                RoundedCornerShape(14.dp)
                            ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            text = if (reflectionDone) "✓ Reflection Recorded Today" else "🪷 Write Daily Reflection (+5 XP)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DivineGold
                        )
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AddTodayTaskDialog(
                dueDate = selectedDate,
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, desc, cat, prio ->
                    onAddTask(title, desc, cat, prio, selectedDate)
                    showAddTaskDialog = false
                }
            )
        }

        // Edit Task Dialog
        taskToEdit?.let { task ->
            EditTodayTaskDialog(
                task = task,
                onDismiss = { taskToEdit = null },
                onSave = { title, desc, cat, prio ->
                    onEditTask(task.id, title, desc, cat, prio, task.dueDate)
                    taskToEdit = null
                }
            )
        }

        // Reset Today Confirmation Dialog
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                containerColor = Color(0xF2061A2D),
                title = {
                    Text("Reset Today's Counters?", color = DivineGold, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Do you want to reset LeetCode and Aptitude progress for this date back to 0?",
                        color = Color.White.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showResetConfirmDialog = false
                        onResetToday(selectedDate)
                    }) {
                        Text("Reset", color = LotusPink, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            )
        }
    }
}

@Composable
fun TodayTaskItemRow(
    task: TaskItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xD9061A2D))
            .border(
                1.dp,
                if (task.completed) DivineGold.copy(alpha = 0.4f) else GlassGoldBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = DivineGold,
                    checkmarkColor = PeacockBlueDark,
                    uncheckedColor = DivineGold.copy(alpha = 0.5f)
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.completed) Color.White.copy(alpha = 0.5f) else Color.White,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.category.displayName,
                        fontSize = 10.sp,
                        color = TurquoiseAccent,
                        fontWeight = FontWeight.Medium
                    )
                    Text("•", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                    Text(
                        text = "${task.priority.displayName} Priority",
                        fontSize = 10.sp,
                        color = when (task.priority) {
                            TaskPriority.HIGH -> LotusPink
                            TaskPriority.MEDIUM -> DivineGold
                            TaskPriority.LOW -> EmeraldMint
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DivineGold.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun XPBreakdownPill(label: String, xp: String, active: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0x33FBBF24) else Color(0x1AFFFFFF))
            .border(1.dp, if (active) DivineGold else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (active) DivineGold else Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = xp,
            fontSize = 11.sp,
            color = if (active) GoldYellowWarm else Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AddTodayTaskDialog(
    dueDate: String,
    onDismiss: () -> Unit,
    onAdd: (String, String, TaskCategory, TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.CODING) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Today's Task",
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, description, selectedCategory, selectedPriority)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PeacockBlueDark)
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTodayTaskDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onSave: (String, String, TaskCategory, TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Task",
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onSave(title, description, task.category, task.priority)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PeacockBlueDark)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDisplayDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val outSdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        outSdf.format(date)
    } catch (_: Exception) {
        dateStr
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        content()
    }
}

