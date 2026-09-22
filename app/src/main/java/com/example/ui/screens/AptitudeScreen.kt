package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.AptitudeCategory
import com.example.data.models.AptitudeSession
import com.example.data.storage.CareerQuestData
import com.example.services.StreakService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AptitudeScreen(
    data: CareerQuestData,
    onAddSession: (
        category: AptitudeCategory, topic: String, attempted: Int,
        correct: Int, timeSpent: Int, date: String
    ) -> Unit,
    onDeleteSession: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<AptitudeSession?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<AptitudeCategory?>(null) }

    val totalAttempted = data.aptitudeSessions.sumOf { it.attempted }
    val totalCorrect = data.aptitudeSessions.sumOf { it.correct }
    val totalIncorrect = data.aptitudeSessions.sumOf { it.incorrect }
    val overallAccuracy = if (totalAttempted > 0) (totalCorrect.toDouble() / totalAttempted * 100.0) else 0.0

    // Topic performance breakdown
    val topicGroups = data.aptitudeSessions.groupBy { it.topic }
    val strongTopics = topicGroups.filter { (_, sessions) ->
        val att = sessions.sumOf { it.attempted }
        val corr = sessions.sumOf { it.correct }
        att >= 10 && (corr.toDouble() / att * 100.0) >= 85.0
    }.keys.toList()

    val weakTopics = topicGroups.filter { (_, sessions) ->
        val att = sessions.sumOf { it.attempted }
        val corr = sessions.sumOf { it.correct }
        att >= 10 && (corr.toDouble() / att * 100.0) < 75.0
    }.keys.toList()

    val filteredSessions = if (selectedCategoryFilter == null) {
        data.aptitudeSessions
    } else {
        data.aptitudeSessions.filter { it.category == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("aptitude_screen"),
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
                            "Aptitude & Reasoning",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "Quantitative, Logical, Verbal & Data Interpretation",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Session", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats overview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Attempted",
                        value = totalAttempted.toString(),
                        color = PeacockBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Correct",
                        value = totalCorrect.toString(),
                        color = EmeraldMint,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Incorrect",
                        value = totalIncorrect.toString(),
                        color = LotusPink,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Accuracy",
                        value = String.format(Locale.getDefault(), "%.1f%%", overallAccuracy),
                        color = GoldYellowWarm,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }

        // Strong and Weak Topics analysis
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CreamCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Topic Mastery Analysis", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PeacockBlueDark)

                    // Strong Topics
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("💪 Strong Topics (≥85% Accuracy)", fontSize = 12.sp, color = EmeraldMint, fontWeight = FontWeight.SemiBold)
                        if (strongTopics.isEmpty()) {
                            Text("Complete more practice sessions to uncover strong topics.", fontSize = 11.sp, color = TextMuted)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                strongTopics.forEach { t ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EmeraldMintLight)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(t, fontSize = 11.sp, color = EmeraldMint, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Weak Topics
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🎯 Topics to Focus On (<75% Accuracy)", fontSize = 12.sp, color = LotusPinkRose, fontWeight = FontWeight.SemiBold)
                        if (weakTopics.isEmpty()) {
                            Text("No weak topics identified yet! Great accuracy.", fontSize = 11.sp, color = TextMuted)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                weakTopics.forEach { t ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LotusPinkLight)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(t, fontSize = 11.sp, color = LotusPinkRose, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${data.aptitudeSessions.size})") }
                    )
                }
                items(AptitudeCategory.entries) { cat ->
                    val count = data.aptitudeSessions.count { it.category == cat }
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text("${cat.displayName} ($count)") }
                    )
                }
            }
        }

        // Practice History Sessions
        if (filteredSessions.isEmpty()) {
            item {
                EmptyStateView(
                    message = "No practice sessions recorded yet. Sharpen your placement readiness!",
                    iconSymbol = "🧠",
                    actionText = "Record Session",
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(filteredSessions, key = { it.id }) { session ->
                AptitudeSessionCard(
                    session = session,
                    onDelete = { sessionToDelete = session }
                )
            }
        }
    }

    if (showAddDialog) {
        AddAptitudeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, top, att, corr, time, date ->
                onAddSession(cat, top, att, corr, time, date)
                showAddDialog = false
            }
        )
    }

    if (sessionToDelete != null) {
        ConfirmationDialog(
            title = "Delete Practice Session",
            message = "Remove this session for '${sessionToDelete?.topic}'?",
            onConfirm = {
                sessionToDelete?.let { onDeleteSession(it.id) }
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x33061A2D))
            .border(1.dp, GlassGoldBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AptitudeSessionCard(
    session: AptitudeSession,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xD9061A2D))
            .border(1.2.dp, GlassGoldBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3338BDF8))
                        .border(1.dp, TurquoiseAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        session.category.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TurquoiseAccent
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (session.accuracy >= 80.0) Color(0x3334D399) else Color(0x33F43F5E)
                            )
                            .border(1.dp, if (session.accuracy >= 80.0) EmeraldMint else LotusPink, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        val accStr = String.format(Locale.getDefault(), "%.1f%% Accuracy", session.accuracy)
                        Text(
                            accStr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (session.accuracy >= 80.0) EmeraldMint else LotusPink
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.topic,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DivineGold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${session.attempted} attempted", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Text("${session.correct} correct", fontSize = 12.sp, color = EmeraldMint, fontWeight = FontWeight.SemiBold)
                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Text("${session.incorrect} incorrect", fontSize = 12.sp, color = LotusPink)
                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Text("${session.timeSpentMinutes} mins", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(session.date, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun AddAptitudeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        category: AptitudeCategory, topic: String, attempted: Int,
        correct: Int, timeSpent: Int, date: String
    ) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AptitudeCategory.QUANTITATIVE) }
    var topic by remember { mutableStateOf("Profit & Loss") }
    var attemptedStr by remember { mutableStateOf("15") }
    var correctStr by remember { mutableStateOf("13") }
    var timeMinutesStr by remember { mutableStateOf("20") }
    var date by remember { mutableStateOf(StreakService.getTodayDate()) }

    val topicPresets = when (selectedCategory) {
        AptitudeCategory.QUANTITATIVE -> listOf(
            "Percentages", "Profit & Loss", "Ratio", "Average",
            "Time & Work", "Time, Speed & Distance", "Probability",
            "Permutation & Combination", "Number System"
        )
        AptitudeCategory.LOGICAL_REASONING -> listOf(
            "Series", "Coding-Decoding", "Blood Relations",
            "Directions", "Seating Arrangement", "Puzzles", "Syllogisms"
        )
        AptitudeCategory.VERBAL -> listOf(
            "Grammar", "Vocabulary", "Reading Comprehension", "Sentence Correction"
        )
        AptitudeCategory.DATA_INTERPRETATION -> listOf(
            "Tables", "Charts", "Graphs"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Record Aptitude Practice", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PeacockBlueDark)

                Text("Category", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AptitudeCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                topic = topicPresets.first()
                            },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Topic", fontSize = 12.sp, color = TextSecondary)
                var expandedDropdown by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(topic, color = PeacockBlueDark)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        topicPresets.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    topic = t
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = attemptedStr,
                        onValueChange = { attemptedStr = it },
                        label = { Text("Attempted *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1E1E),
                            unfocusedTextColor = Color(0xFF1E1E1E),
                            cursorColor = PeacockBlue,
                            focusedBorderColor = PeacockBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    OutlinedTextField(
                        value = correctStr,
                        onValueChange = { correctStr = it },
                        label = { Text("Correct *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1E1E),
                            unfocusedTextColor = Color(0xFF1E1E1E),
                            cursorColor = PeacockBlue,
                            focusedBorderColor = PeacockBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeMinutesStr,
                        onValueChange = { timeMinutesStr = it },
                        label = { Text("Time (mins)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1E1E),
                            unfocusedTextColor = Color(0xFF1E1E1E),
                            cursorColor = PeacockBlue,
                            focusedBorderColor = PeacockBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1E1E),
                            unfocusedTextColor = Color(0xFF1E1E1E),
                            cursorColor = PeacockBlue,
                            focusedBorderColor = PeacockBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val att = attemptedStr.toIntOrNull() ?: 10
                            val corr = correctStr.toIntOrNull() ?: 8
                            val time = timeMinutesStr.toIntOrNull() ?: 20
                            onConfirm(selectedCategory, topic, att, corr, time, date)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save (+30 XP)")
                    }
                }
            }
        }
    }
}
