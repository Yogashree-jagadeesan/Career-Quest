package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ScreenRoute(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val isPrimaryBottomNav: Boolean = false
) {
    DASHBOARD("dashboard", "Today", Icons.Default.Home, isPrimaryBottomNav = true),
    PROGRESS("progress", "Progress", Icons.Default.BarChart, isPrimaryBottomNav = true),
    TODOS("todos", "To-Do", Icons.Default.CheckCircle, isPrimaryBottomNav = true),
    SETTINGS("settings", "Settings", Icons.Default.Settings, isPrimaryBottomNav = true),

    NOTES("notes", "Notes", Icons.Default.EditNote),
    LEETCODE("leetcode", "LeetCode", Icons.Default.Code),
    LINKEDIN("linkedin", "LinkedIn", Icons.Default.Share),
    GITHUB("github", "GitHub", Icons.Default.Source),
    APTITUDE("aptitude", "Aptitude", Icons.Default.Psychology),
    ACHIEVEMENTS("achievements", "Achievements", Icons.Default.EmojiEvents),
    STREAKS("streaks", "Streaks", Icons.Default.LocalFireDepartment);


    companion object {
        fun fromRoute(route: String?): ScreenRoute {
            return entries.firstOrNull { it.route == route } ?: DASHBOARD
        }
    }
}
