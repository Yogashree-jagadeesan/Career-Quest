package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.storage.CareerQuestData
import com.example.services.DayActivitySummary
import com.example.services.DayStatus
import com.example.services.StreakService
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StreaksScreen(
    data: CareerQuestData
) {
    val cal = remember { Calendar.getInstance() }
    var currentYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH) + 1) }
    var selectedDaySummary by remember { mutableStateOf<DayActivitySummary?>(null) }

    val monthDays = remember(currentYear, currentMonth, data) {
        StreakService.getMonthlyCalendarDays(
            year = currentYear,
            month = currentMonth,
            tasks = data.tasks,
            leetCode = data.leetCodeRecords,
            aptitude = data.aptitudeSessions,
            notes = data.notes,
            reflections = data.reflections
        )
    }

    val monthName = remember(currentMonth) {
        val c = Calendar.getInstance()
        c.set(Calendar.MONTH, currentMonth - 1)
        SimpleDateFormat("MMMM", Locale.getDefault()).format(c.time)
    }

    // Days in week leading blank padding
    val firstDayOfWeekOffset = remember(currentYear, currentMonth) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, currentYear)
        c.set(Calendar.MONTH, currentMonth - 1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("streaks_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeritageOrnamentalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Streak & Consistency Garden",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "Small daily efforts compound into extraordinary technical mastery",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, LotusPink.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = LotusPinkLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥 Current Streak", fontSize = 12.sp, color = LotusPinkRose, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${data.user.currentStreak} Days", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = LotusPinkRose)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, GoldYellowWarm.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GoldYellowLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐ Longest Streak", fontSize = 12.sp, color = GoldTrim, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${data.user.longestStreak} Days", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GoldTrim)
                        }
                    }
                }
            }
        }

        // Monthly Interactive Calendar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CreamCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentMonth == 1) {
                                    currentMonth = 12
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = PeacockBlue)
                        }

                        Text(
                            "$monthName $currentYear",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )

                        IconButton(
                            onClick = {
                                if (currentMonth == 12) {
                                    currentMonth = 1
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = PeacockBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of week header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                            Text(d, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid
                    val totalSlots = firstDayOfWeekOffset + monthDays.size
                    val rows = (totalSlots + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (r in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                for (c in 0 until 7) {
                                    val index = r * 7 + c
                                    val dayIndex = index - firstDayOfWeekOffset
                                    if (dayIndex in monthDays.indices) {
                                        val daySummary = monthDays[dayIndex]
                                        CalendarDayCell(
                                            daySummary = daySummary,
                                            onClick = { selectedDaySummary = daySummary },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f).height(44.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldMint))
                            Text("Goals Met", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldYellowWarm))
                            Text("Partial", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFCBD5E1)))
                            Text("Missed", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    if (selectedDaySummary != null) {
        DayDetailDialog(
            day = selectedDaySummary!!,
            onDismiss = { selectedDaySummary = null }
        )
    }
}

@Composable
fun CalendarDayCell(
    daySummary: DayActivitySummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (daySummary.status) {
        DayStatus.COMPLETED -> EmeraldMint
        DayStatus.PARTIAL -> GoldYellowWarm
        DayStatus.MISSED -> Color(0xFFE2E8F0)
        DayStatus.FUTURE -> Color.Transparent
    }

    val isInteractive = daySummary.status != DayStatus.FUTURE

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isInteractive) { onClick() }
            .background(
                if (daySummary.status == DayStatus.COMPLETED) EmeraldMintLight.copy(alpha = 0.5f)
                else if (daySummary.status == DayStatus.PARTIAL) GoldYellowLight.copy(alpha = 0.5f)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${daySummary.dayNumber}",
                fontSize = 12.sp,
                fontWeight = if (daySummary.status == DayStatus.COMPLETED) FontWeight.Bold else FontWeight.Normal,
                color = if (daySummary.status == DayStatus.FUTURE) TextMuted.copy(alpha = 0.4f) else PeacockBlueDark
            )
            if (indicatorColor != Color.Transparent) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(indicatorColor))
            }
        }
    }
}

@Composable
fun DayDetailDialog(
    day: DayActivitySummary,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Activity on ${day.date}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text("Day Status: ${day.status.name}", fontSize = 12.sp, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldYellowLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("+${day.totalXPEarned} XP", fontWeight = FontWeight.Bold, color = GoldTrim, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = CreamCardBorder)

                // Tasks
                Text("Tasks Completed (${day.tasksCompleted.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PeacockBlue)
                if (day.tasksCompleted.isEmpty()) {
                    Text("No tasks completed on this day.", fontSize = 12.sp, color = TextMuted)
                } else {
                    day.tasksCompleted.forEach { t ->
                        Text("✓ ${t.title}", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                // LeetCode
                Text("LeetCode Solved (${day.leetCodeRecords.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PeacockBlue)
                if (day.leetCodeRecords.isEmpty()) {
                    Text("No LeetCode problems recorded.", fontSize = 12.sp, color = TextMuted)
                } else {
                    day.leetCodeRecords.forEach { lc ->
                        Text("💻 #${lc.problemNumber} ${lc.problemName} (${lc.difficulty.name})", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                // Aptitude
                Text("Aptitude Practice (${day.aptitudeSessions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PeacockBlue)
                if (day.aptitudeSessions.isEmpty()) {
                    Text("No aptitude sessions recorded.", fontSize = 12.sp, color = TextMuted)
                } else {
                    day.aptitudeSessions.forEach { a ->
                        Text("🧠 ${a.topic}: ${a.correct}/${a.attempted} (${a.accuracy.toInt()}%)", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                // Reflection
                Text("Daily Reflection", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PeacockBlue)
                if (day.reflection == null) {
                    Text("No reflection recorded for this day.", fontSize = 12.sp, color = TextMuted)
                } else {
                    Text("🪷 Learned: ${day.reflection.learned}", fontSize = 12.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}
