package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.DailyReflection
import com.example.data.models.SurpriseReward
import com.example.ui.theme.*

@Composable
fun SurpriseRewardDialog(
    reward: SurpriseReward,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, GoldYellowWarm, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GoldYellowLight.copy(alpha = 0.5f),
                                Color.White
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GoldYellowWarm)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(reward.icon, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = reward.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PeacockBlueDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reward.description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldYellowLight)
                        .border(1.dp, GoldYellowWarm, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐ +${reward.bonusXP} BONUS XP", fontWeight = FontWeight.Bold, color = GoldTrim, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Claim Reward ✨", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailyReflectionDialog(
    existingReflection: DailyReflection?,
    onDismiss: () -> Unit,
    onSave: (learned: String, difficult: String, improveTomorrow: String, proudOf: String) -> Unit
) {
    var learned by remember { mutableStateOf(existingReflection?.learned ?: "") }
    var difficult by remember { mutableStateOf(existingReflection?.difficult ?: "") }
    var improveTomorrow by remember { mutableStateOf(existingReflection?.improveTomorrow ?: "") }
    var proudOf by remember { mutableStateOf(existingReflection?.proudOf ?: "") }

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
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LotusIcon(size = 28.dp)
                    Column {
                        Text("Daily Reflection", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PeacockBlueDark)
                        Text("Pause, reflect, and cultivate wisdom (+5 XP)", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = learned,
                    onValueChange = { learned = it },
                    label = { Text("What did I learn today?") },
                    placeholder = { Text("Key insights, algorithms, topics...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = difficult,
                    onValueChange = { difficult = it },
                    label = { Text("What was difficult?") },
                    placeholder = { Text("Tricky bugs, formulas, distractions...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = improveTomorrow,
                    onValueChange = { improveTomorrow = it },
                    label = { Text("What should I improve tomorrow?") },
                    placeholder = { Text("Pacing, consistency, revisions...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = proudOf,
                    onValueChange = { proudOf = it },
                    label = { Text("What am I proud of today?") },
                    placeholder = { Text("Consistency, milestones, discipline...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            onSave(learned, difficult, improveTomorrow, proudOf)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Reflection")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = PeacockBlueDark) },
        text = { Text(message, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) LotusPink else PeacockBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
