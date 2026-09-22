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
import com.example.data.models.LinkedInActivity
import com.example.data.models.LinkedInActivityType
import com.example.data.storage.CareerQuestData
import com.example.services.ScoringService
import com.example.services.StreakService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*

@Composable
fun LinkedInScreen(
    data: CareerQuestData,
    onAddActivity: (type: LinkedInActivityType, title: String, details: String) -> Unit,
    onDeleteActivity: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<LinkedInActivity?>(null) }

    val weekId = StreakService.getCurrentWeekId()
    val thisWeekActivities = data.linkedInActivities.filter { it.weekId == weekId }
    val thisWeekCount = thisWeekActivities.size
    val target = data.settings.weeklyLinkedInTarget
    val isGoalMet = thisWeekCount >= target

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("linkedin_screen"),
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
                            "LinkedIn Career Presence",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "Weekly Target: $target activities / week",
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

                // Progress Banner
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
                            "This Week's Progress",
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
                            "🎉 Goal Met! (+${ScoringService.XP_LINKEDIN_WEEKLY} Bonus XP)",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldMint,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "${target - thisWeekCount} more needed",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Activity History (${data.linkedInActivities.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark
            )
        }

        if (data.linkedInActivities.isEmpty()) {
            item {
                EmptyStateView(
                    message = "No LinkedIn activities logged yet. Build your technical voice!",
                    iconSymbol = "✨",
                    actionText = "Log First Activity",
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(data.linkedInActivities, key = { it.id }) { item ->
                LinkedInActivityCard(
                    activity = item,
                    onDelete = { activityToDelete = item }
                )
            }
        }
    }

    if (showAddDialog) {
        AddLinkedInDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, title, details ->
                onAddActivity(type, title, details)
                showAddDialog = false
            }
        )
    }

    if (activityToDelete != null) {
        ConfirmationDialog(
            title = "Delete LinkedIn Activity",
            message = "Remove '${activityToDelete?.title}' from your activity history?",
            onConfirm = {
                activityToDelete?.let { onDeleteActivity(it.id) }
                activityToDelete = null
            },
            onDismiss = { activityToDelete = null }
        )
    }
}

@Composable
fun LinkedInActivityCard(
    activity: LinkedInActivity,
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
                        .background(TurquoiseSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        activity.type.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeacockBlue
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PeacockBlueDark
            )

            if (activity.linkOrDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.linkOrDetails,
                    fontSize = 12.sp,
                    color = TextSecondary
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
fun AddLinkedInDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: LinkedInActivityType, title: String, details: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(LinkedInActivityType.CREATE_POST) }
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

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
                Text("Log LinkedIn Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PeacockBlueDark)

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
                        LinkedInActivityType.entries.forEach { type ->
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title / Topic *") },
                    placeholder = { Text("e.g. Shared blog on Room DB architecture") },
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
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Post Link or Reflections (optional)") },
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
                        onClick = { onConfirm(selectedType, title, details) },
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
