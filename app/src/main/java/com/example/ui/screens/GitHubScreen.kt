package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.models.GitHubActivity
import com.example.data.models.GitHubActivityType
import com.example.data.storage.CareerQuestData
import com.example.services.ScoringService
import com.example.services.StreakService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*

@Composable
fun GitHubScreen(
    data: CareerQuestData,
    onAddActivity: (type: GitHubActivityType, repoName: String, desc: String, link: String) -> Unit,
    onDeleteActivity: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<GitHubActivity?>(null) }

    val weekId = StreakService.getCurrentWeekId()
    val thisWeekActivities = data.gitHubActivities.filter { it.weekId == weekId }
    val thisWeekCount = thisWeekActivities.size
    val target = data.settings.weeklyGitHubTarget
    val isGoalMet = thisWeekCount >= target

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("github_screen"),
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
                            "GitHub Code Contributions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "Weekly Target: $target activity / week",
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
                        Text("Log Activity", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isGoalMet) EmeraldMintLight else TurquoiseSoft)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "This Week's Contribution",
                            fontSize = 11.sp,
                            color = if (isGoalMet) EmeraldMint else PeacockBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "$thisWeekCount / $target completed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isGoalMet) EmeraldMint else PeacockBlue
                        )
                    }

                    if (isGoalMet) {
                        Text(
                            "🦚 Target Reached! (+${ScoringService.XP_GITHUB_WEEKLY} Bonus XP)",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldMint,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "Commit & push code this week!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Contribution Timeline (${data.gitHubActivities.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark
            )
        }

        if (data.gitHubActivities.isEmpty()) {
            item {
                EmptyStateView(
                    message = "No GitHub activities tracked yet. Build, push, and contribute!",
                    iconSymbol = "🚀",
                    actionText = "Record First Commit",
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(data.gitHubActivities, key = { it.id }) { item ->
                GitHubActivityCard(
                    activity = item,
                    onDelete = { activityToDelete = item }
                )
            }
        }
    }

    if (showAddDialog) {
        AddGitHubDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, repo, desc, link ->
                onAddActivity(type, repo, desc, link)
                showAddDialog = false
            }
        )
    }

    if (activityToDelete != null) {
        ConfirmationDialog(
            title = "Delete GitHub Record",
            message = "Remove '${activityToDelete?.repoName}' from your contributions?",
            onConfirm = {
                activityToDelete?.let { onDeleteActivity(it.id) }
                activityToDelete = null
            },
            onDismiss = { activityToDelete = null }
        )
    }
}

@Composable
fun GitHubActivityCard(
    activity: GitHubActivity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CreamCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldYellowLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        activity.type.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTrim
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.repoName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark
            )

            if (activity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (activity.link.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.link,
                    fontSize = 11.sp,
                    color = TurquoiseAccent
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(activity.date, fontSize = 11.sp, color = TextMuted)
                Text(activity.weekId, fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun AddGitHubDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: GitHubActivityType, repoName: String, desc: String, link: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(GitHubActivityType.COMMIT) }
    var repoName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

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
                Text("Log GitHub Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PeacockBlueDark)

                Text("Activity Type", fontSize = 12.sp, color = TextSecondary)
                var expandedDropdown by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(selectedType.displayName, color = PeacockBlueDark)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        GitHubActivityType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = repoName,
                    onValueChange = { repoName = it },
                    label = { Text("Repository Name *") },
                    placeholder = { Text("e.g. android-career-quest") },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Commit / PR / Contribution Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
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
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Repository / PR URL (optional)") },
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
                        onClick = { onConfirm(selectedType, repoName, description, link) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
