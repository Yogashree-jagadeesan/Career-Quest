package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Achievement
import com.example.data.storage.CareerQuestData
import com.example.services.AchievementService
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    data: CareerQuestData
) {
    val unlockedCount = data.achievements.count { it.unlocked }
    val totalCount = data.achievements.size

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .testTag("achievements_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            HeritageOrnamentalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Achievements & Rewards",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "Milestones honoring dedication, skill, and consistency",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldYellowLight)
                            .border(1.dp, GoldYellowWarm, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "$unlockedCount / $totalCount Unlocked",
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldTrim,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Surprise Rewards section header
        item(span = { GridItemSpan(2) }) {
            Text(
                "🎁 Milestones & Surprise Blessings",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Surprise Rewards list
        items(AchievementService.ALL_SURPRISE_REWARDS, key = { it.id }, span = { GridItemSpan(1) }) { reward ->
            val isClaimed = data.user.unlockedRewardIds.contains(reward.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isClaimed) GoldYellowWarm else CreamCardBorder,
                        RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isClaimed) GoldYellowLight.copy(alpha = 0.4f) else Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(reward.icon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        reward.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PeacockBlueDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        reward.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isClaimed) EmeraldMintLight else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (isClaimed) "Claimed (+${reward.bonusXP} XP)" else "+${reward.bonusXP} XP (Locked)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimed) EmeraldMint else TextMuted
                        )
                    }
                }
            }
        }

        // Badges section header
        item(span = { GridItemSpan(2) }) {
            Text(
                "🏆 Quest Badges",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PeacockBlueDark,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Achievement Items
        items(data.achievements, key = { it.id }, span = { GridItemSpan(1) }) { ach ->
            AchievementCard(achievement = ach)
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val progressFraction = (achievement.current.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (achievement.unlocked) GoldYellowWarm else CreamCardBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) Color.White else Color(0xFFF8FAFC)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.unlocked) GoldYellowLight else Color(0xFFE2E8F0)
                    )
                    .border(
                        1.5.dp,
                        if (achievement.unlocked) GoldYellowWarm else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconSymbol,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (achievement.unlocked) PeacockBlueDark else TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = achievement.description,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (achievement.unlocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldMintLight)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Unlocked ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldMint
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PeacockBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${achievement.current} / ${achievement.target}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
