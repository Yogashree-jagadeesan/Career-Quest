package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.models.LeetCodeDifficulty
import com.example.data.models.LeetCodeRecord
import com.example.data.storage.CareerQuestData
import com.example.services.ScoringService
import com.example.services.StreakService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*

@Composable
fun LeetCodeScreen(
    data: CareerQuestData,
    onAddRecord: (
        name: String, number: Int, url: String, diff: LeetCodeDifficulty, topic: String,
        date: String, time: Int, attempts: Int, notes: String, approach: String,
        learned: String, mistake: String, better: String, tc: String, sc: String
    ) -> Unit,
    onDeleteRecord: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<LeetCodeRecord?>(null) }
    var selectedDifficultyFilter by remember { mutableStateOf<LeetCodeDifficulty?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val today = StreakService.getTodayDate()
    val todaySolved = data.leetCodeRecords.count { it.date == today && it.solved }
    val totalSolved = data.leetCodeRecords.count { it.solved }
    val easyCount = data.leetCodeRecords.count { it.difficulty == LeetCodeDifficulty.EASY && it.solved }
    val mediumCount = data.leetCodeRecords.count { it.difficulty == LeetCodeDifficulty.MEDIUM && it.solved }
    val hardCount = data.leetCodeRecords.count { it.difficulty == LeetCodeDifficulty.HARD && it.solved }

    val filteredRecords = data.leetCodeRecords.filter { record ->
        val matchesDiff = selectedDifficultyFilter == null || record.difficulty == selectedDifficultyFilter
        val matchesQuery = searchQuery.isBlank() ||
                record.problemName.contains(searchQuery, ignoreCase = true) ||
                record.topic.contains(searchQuery, ignoreCase = true) ||
                record.problemNumber.toString().contains(searchQuery)
        matchesDiff && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leetcode_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Daily Target summary
        item {
            HeritageOrnamentalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "LeetCode Practice",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "Daily Target: ${data.settings.dailyLeetCodeTarget} problems / day",
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
                        Text("Add Problem", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Today progress pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (todaySolved >= data.settings.dailyLeetCodeTarget) EmeraldMintLight else TurquoiseSoft)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Today's Progress: $todaySolved / ${data.settings.dailyLeetCodeTarget} solved",
                        fontWeight = FontWeight.Bold,
                        color = if (todaySolved >= data.settings.dailyLeetCodeTarget) EmeraldMint else PeacockBlue,
                        fontSize = 13.sp
                    )
                    if (todaySolved >= data.settings.dailyLeetCodeTarget) {
                        Text("🎉 Daily Goal Met!", fontWeight = FontWeight.Bold, color = EmeraldMint, fontSize = 12.sp)
                    }
                }
            }
        }

        // Stats Matrix: Easy / Medium / Hard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Easy",
                    count = easyCount,
                    color = EasyGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Medium",
                    count = mediumCount,
                    color = MediumYellow,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Hard",
                    count = hardCount,
                    color = HardRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Solved",
                    count = totalSolved,
                    color = PeacockBlue,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }

        // Contribution-Style Heatmap Card
        item {
            ContributionHeatmapCard(records = data.leetCodeRecords)
        }

        // Search and Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by problem name, number, or topic...", color = Color.White.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DivineGold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = DivineGold,
                        focusedBorderColor = DivineGold,
                        unfocusedBorderColor = GlassGoldBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Difficulty Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedDifficultyFilter == null,
                        onClick = { selectedDifficultyFilter = null },
                        label = { Text("All (${data.leetCodeRecords.size})") }
                    )
                    FilterChip(
                        selected = selectedDifficultyFilter == LeetCodeDifficulty.EASY,
                        onClick = {
                            selectedDifficultyFilter =
                                if (selectedDifficultyFilter == LeetCodeDifficulty.EASY) null else LeetCodeDifficulty.EASY
                        },
                        label = { Text("Easy ($easyCount)") }
                    )
                    FilterChip(
                        selected = selectedDifficultyFilter == LeetCodeDifficulty.MEDIUM,
                        onClick = {
                            selectedDifficultyFilter =
                                if (selectedDifficultyFilter == LeetCodeDifficulty.MEDIUM) null else LeetCodeDifficulty.MEDIUM
                        },
                        label = { Text("Medium ($mediumCount)") }
                    )
                    FilterChip(
                        selected = selectedDifficultyFilter == LeetCodeDifficulty.HARD,
                        onClick = {
                            selectedDifficultyFilter =
                                if (selectedDifficultyFilter == LeetCodeDifficulty.HARD) null else LeetCodeDifficulty.HARD
                        },
                        label = { Text("Hard ($hardCount)") }
                    )
                }
            }
        }

        // Problem records list
        if (filteredRecords.isEmpty()) {
            item {
                EmptyStateView(
                    message = "Start your coding journey today. Record your first solved problem!",
                    iconSymbol = "💻",
                    actionText = "Add Problem",
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                LeetCodeRecordCard(
                    record = record,
                    onDelete = { recordToDelete = record }
                )
            }
        }
    }

    if (showAddDialog) {
        AddLeetCodeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, num, url, diff, topic, date, time, attempts, notes, approach, learned, mistake, better, tc, sc ->
                onAddRecord(name, num, url, diff, topic, date, time, attempts, notes, approach, learned, mistake, better, tc, sc)
                showAddDialog = false
            }
        )
    }

    if (recordToDelete != null) {
        ConfirmationDialog(
            title = "Delete Problem Record",
            message = "Are you sure you want to remove '${recordToDelete?.problemName}'?",
            onConfirm = {
                recordToDelete?.let { onDeleteRecord(it.id) }
                recordToDelete = null
            },
            onDismiss = { recordToDelete = null }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xD9061A2D))
            .border(1.dp, GlassGoldBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun ContributionHeatmapCard(records: List<LeetCodeRecord>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xD9061A2D))
            .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activity Heatmap (Past 4 Weeks)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DivineGold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Less", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0x33FFFFFF)))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(TurquoiseSoft.copy(alpha = 0.4f)))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(TurquoiseAccent))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(DivineGold))
                    Text("More", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 weeks x 7 days grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(28) { dayIndex ->
                    val color = when {
                        dayIndex % 7 == 0 -> DivineGold
                        dayIndex % 5 == 0 -> TurquoiseAccent
                        dayIndex % 3 == 0 -> TurquoiseSoft.copy(alpha = 0.5f)
                        else -> Color(0x22FFFFFF)
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun LeetCodeRecordCard(
    record: LeetCodeRecord,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val diffColor = when (record.difficulty) {
        LeetCodeDifficulty.EASY -> EasyGreen
        LeetCodeDifficulty.MEDIUM -> MediumYellow
        LeetCodeDifficulty.HARD -> HardRed
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = record.difficulty.name,
                            color = diffColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        "#${record.problemNumber} ${record.problemName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DivineGold
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(record.topic, fontSize = 12.sp, color = TurquoiseAccent, fontWeight = FontWeight.Medium)
                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Text("${record.timeSpentMinutes} mins", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Text("${record.attempts} attempts", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Text(record.date, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            }

            // Expanded Learning Notes Section
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33020617))
                        .border(1.dp, GlassGoldBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💡 Learning Notes & Complexity", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DivineGold)

                    if (record.approach.isNotBlank()) {
                        Text("Approach: ${record.approach}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                    if (record.whatILearned.isNotBlank()) {
                        Text("What I Learned: ${record.whatILearned}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                    if (record.mistake.isNotBlank()) {
                        Text("Mistake: ${record.mistake}", fontSize = 12.sp, color = LotusPink)
                    }
                    if (record.betterApproach.isNotBlank()) {
                        Text("Better Approach: ${record.betterApproach}", fontSize = 12.sp, color = EmeraldMint)
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⏱ Time: ${record.timeComplexity}", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = PeacockBlueDark)
                        Text("💾 Space: ${record.spaceComplexity}", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = PeacockBlueDark)
                    }
                }
            }
        }
    }
}

@Composable
fun AddLeetCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, number: Int, url: String, diff: LeetCodeDifficulty, topic: String,
        date: String, time: Int, attempts: Int, notes: String, approach: String,
        learned: String, mistake: String, better: String, tc: String, sc: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var numberStr by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(LeetCodeDifficulty.MEDIUM) }
    var topic by remember { mutableStateOf("Arrays & Hashing") }
    var date by remember { mutableStateOf(StreakService.getTodayDate()) }
    var timeMinutesStr by remember { mutableStateOf("25") }
    var attemptsStr by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    // Detailed notes
    var approach by remember { mutableStateOf("") }
    var whatILearned by remember { mutableStateOf("") }
    var mistake by remember { mutableStateOf("") }
    var betterApproach by remember { mutableStateOf("") }
    var timeComplexity by remember { mutableStateOf("O(N)") }
    var spaceComplexity by remember { mutableStateOf("O(1)") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add LeetCode Problem", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PeacockBlueDark)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Problem Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = numberStr,
                        onValueChange = { numberStr = it },
                        label = { Text("Number #") },
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
                }

                // Difficulty selector
                Text("Difficulty", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeetCodeDifficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic (e.g. Dynamic Programming, Trees)") },
                    modifier = Modifier.fillMaxWidth(),
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
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Problem URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Text("Learning Reflection & Notes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PeacockBlue)

                OutlinedTextField(
                    value = approach,
                    onValueChange = { approach = it },
                    label = { Text("Approach / Intuition") },
                    modifier = Modifier.fillMaxWidth(),
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
                    value = whatILearned,
                    onValueChange = { whatILearned = it },
                    label = { Text("What I Learned") },
                    modifier = Modifier.fillMaxWidth(),
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
                    value = mistake,
                    onValueChange = { mistake = it },
                    label = { Text("Mistake Made") },
                    modifier = Modifier.fillMaxWidth(),
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
                    value = betterApproach,
                    onValueChange = { betterApproach = it },
                    label = { Text("Better / Optimal Approach") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeComplexity,
                        onValueChange = { timeComplexity = it },
                        label = { Text("Time Complexity") },
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
                        value = spaceComplexity,
                        onValueChange = { spaceComplexity = it },
                        label = { Text("Space Complexity") },
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
                            val num = numberStr.toIntOrNull() ?: 1
                            val time = timeMinutesStr.toIntOrNull() ?: 25
                            val attempts = attemptsStr.toIntOrNull() ?: 1
                            onConfirm(
                                name, num, url, difficulty, topic, date, time, attempts,
                                notes, approach, whatILearned, mistake, betterApproach,
                                timeComplexity, spaceComplexity
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Problem (+${ScoringService.XP_LEETCODE} XP)")
                    }
                }
            }
        }
    }
}
