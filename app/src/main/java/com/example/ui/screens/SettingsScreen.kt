package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.AppSettings
import com.example.data.storage.CareerQuestData
import com.example.services.ScoringService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    data: CareerQuestData,
    onSaveSettings: (AppSettings) -> Unit,
    onExportJson: () -> String,
    onImportJson: (String) -> Boolean,
    onResetToEmpty: () -> Unit
) {
    val context = LocalContext.current
    var studentName by remember { mutableStateOf(data.settings.studentName) }
    var leetCodeTarget by remember { mutableStateOf(data.settings.dailyLeetCodeTarget.toString()) }
    var linkedInTarget by remember { mutableStateOf(data.settings.weeklyLinkedInTarget.toString()) }
    var gitHubTarget by remember { mutableStateOf(data.settings.weeklyGitHubTarget.toString()) }
    var aptitudeTarget by remember { mutableStateOf(data.settings.dailyAptitudeTargetQuestions.toString()) }
    var enableAnimations by remember { mutableStateOf(data.settings.enableAnimations) }
    var enableMotivation by remember { mutableStateOf(data.settings.enableMotivationalMessages) }
    var enableMusic by remember { mutableStateOf(data.settings.enableBackgroundMusic) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeritageOrnamentalCard(borderColor = DivineGold) {
                Text(
                    "Settings & Customization",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DivineGold
                )
                Text(
                    "Personalize your targets, configure XP rewards, and manage local backups",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Student Profile & Daily Targets
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
                    Text("Profile & Goals", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DivineGold)

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Name", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DivineGold,
                            unfocusedBorderColor = GlassGoldBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = leetCodeTarget,
                            onValueChange = { leetCodeTarget = it },
                            label = { Text("LeetCode / day", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DivineGold,
                                unfocusedBorderColor = GlassGoldBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = aptitudeTarget,
                            onValueChange = { aptitudeTarget = it },
                            label = { Text("Aptitude Qs / day", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DivineGold,
                                unfocusedBorderColor = GlassGoldBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = linkedInTarget,
                            onValueChange = { linkedInTarget = it },
                            label = { Text("LinkedIn / week", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DivineGold,
                                unfocusedBorderColor = GlassGoldBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = gitHubTarget,
                            onValueChange = { gitHubTarget = it },
                            label = { Text("GitHub / week", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DivineGold,
                                unfocusedBorderColor = GlassGoldBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Krishna Bansuri Ambient Music", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DivineGold)
                            Text("Soft peaceful Vrindavan flute melody", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                        Switch(
                            checked = enableMusic,
                            onCheckedChange = { enableMusic = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DivineGold,
                                checkedTrackColor = PeacockBlue
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Motivational Guidance", fontSize = 13.sp, color = Color.White)
                        Switch(
                            checked = enableMotivation,
                            onCheckedChange = { enableMotivation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DivineGold,
                                checkedTrackColor = PeacockBlue
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val newSettings = AppSettings(
                                studentName = studentName.ifBlank { "Student" },
                                dailyLeetCodeTarget = leetCodeTarget.toIntOrNull() ?: 2,
                                weeklyLinkedInTarget = linkedInTarget.toIntOrNull() ?: 2,
                                weeklyGitHubTarget = gitHubTarget.toIntOrNull() ?: 1,
                                dailyAptitudeTargetQuestions = aptitudeTarget.toIntOrNull() ?: 15,
                                enableAnimations = enableAnimations,
                                enableMotivationalMessages = enableMotivation,
                                enableBackgroundMusic = enableMusic
                            )
                            onSaveSettings(newSettings)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DivineGold, contentColor = PeacockBlueDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // XP System Reference Table
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD9061A2D))
                    .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⭐ XP & Leveling Rules", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DivineGold)
                    Text("Every positive effort contributes to your career level", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))

                    Spacer(modifier = Modifier.height(4.dp))

                    XpRuleRow("Solve LeetCode Problem", "+${ScoringService.XP_LEETCODE} XP")
                    XpRuleRow("Complete Aptitude Practice", "+${ScoringService.XP_APTITUDE} XP")
                    XpRuleRow("Complete To-Do Task", "+${ScoringService.XP_TODO_TASK} XP")
                    XpRuleRow("Save Technical Learning Note", "+${ScoringService.XP_LEARNING_NOTE} XP")
                    XpRuleRow("Record Daily Reflection", "+${ScoringService.XP_DAILY_REFLECTION} XP")
                    XpRuleRow("All Daily Goals Achieved Bonus", "+${ScoringService.XP_ALL_DAILY_GOALS} XP")
                    XpRuleRow("Weekly LinkedIn Target (2/2)", "+${ScoringService.XP_LINKEDIN_WEEKLY} XP")
                    XpRuleRow("Weekly GitHub Target (1/1)", "+${ScoringService.XP_GITHUB_WEEKLY} XP")
                    XpRuleRow("7-Day Streak Milestone Bonus", "+${ScoringService.XP_STREAK_7} XP")
                    XpRuleRow("30-Day Streak Milestone Bonus", "+${ScoringService.XP_STREAK_30} XP")
                }
            }
        }

        // Backup, Export, Import & Reset
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD9061A2D))
                    .border(1.2.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Data Persistence & Backup", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DivineGold)
                    Text("Your career progress is saved securely in local storage.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportedJsonText = onExportJson()
                                showExportDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DivineGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DivineGold)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                importJsonText = ""
                                showImportDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TurquoiseAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TurquoiseAccent)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import JSON", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showClearAllConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LotusPink, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Export Career Quest Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PeacockBlueDark)
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
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
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("CareerQuestData", exportedJsonText)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }
                        OutlinedButton(
                            onClick = { showExportDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Import Career Quest Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PeacockBlueDark)
                    Text("Paste valid JSON data to restore your profile", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        placeholder = { Text("Paste JSON here...") },
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
                        Button(
                            onClick = {
                                val ok = onImportJson(importJsonText)
                                if (ok) showImportDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Import")
                        }
                        OutlinedButton(
                            onClick = { showImportDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showClearAllConfirm) {
        ConfirmationDialog(
            title = "Clear All Career Data",
            message = "This will delete all tasks, notes, practice records, and reset your XP to 0. Are you sure?",
            confirmText = "Clear All",
            isDestructive = true,
            onConfirm = {
                onResetToEmpty()
                showClearAllConfirm = false
            },
            onDismiss = { showClearAllConfirm = false }
        )
    }
}

@Composable
fun XpRuleRow(action: String, xp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(action, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x33FBBF24))
                .border(1.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(xp, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DivineGold)
        }
    }
}
