package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.services.ScoringService
import com.example.services.StreakService
import com.example.ui.components.*
import com.example.ui.navigation.ScreenRoute
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CareerQuestViewModel
import com.example.ui.viewmodel.UiToastMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerQuestApp(
    viewModel: CareerQuestViewModel = viewModel()
) {
    val navController = rememberNavController()
    val data by viewModel.dataState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val surpriseReward by viewModel.activeSurpriseReward.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoute.DASHBOARD.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showReflectionDialog by remember { mutableStateOf(false) }
    var floatingXp by remember { mutableStateOf<Pair<Int, String>?>(null) }

    LaunchedEffect(data.settings.enableBackgroundMusic) {
        com.example.services.BansuriBackgroundPlayer.setEnabled(data.settings.enableBackgroundMusic)
    }

    DisposableEffect(Unit) {
        onDispose {
            com.example.services.BansuriBackgroundPlayer.stop()
        }
    }

    val performPalaceNavigation: (ScreenRoute) -> Unit = { screen ->
        if (currentRoute != screen.route) {
            navController.navigate(screen.route) {
                popUpTo(ScreenRoute.DASHBOARD.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Collect Toast/Snackbar events and trigger floating +XP animation
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { event ->
            when (event) {
                is UiToastMessage.Success -> {
                    if (event.xpAwarded > 0) {
                        floatingXp = Pair(event.xpAwarded, event.message)
                    }
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiToastMessage.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "⚠️ ${event.message}",
                        duration = SnackbarDuration.Short
                    )
                }
                is UiToastMessage.Reward -> {
                    // Dialog handles this via activeSurpriseReward
                }
            }
        }
    }

    val currentScreen = ScreenRoute.fromRoute(currentRoute)
    val today = StreakService.getTodayDate()
    val todayReflection = data.reflections.firstOrNull { it.date == today }
    val levelInfo = ScoringService.calculateLevelInfo(data.user.totalXP)

    KrishnaAppBackground {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xF2061A2D),
                    drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                    modifier = Modifier
                        .width(300.dp)
                        .border(1.5.dp, GlassGoldBorder, RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                ) {
                    // Drawer Header with Krishna-inspired colors
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(PeacockBlueDark, PurpleNight)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LotusIcon(size = 30.dp, tint = DivineGold)
                                Text(
                                    "Career Quest",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DivineGold
                                )
                            }
                            Text(
                                data.settings.studentName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TurquoiseSoft
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Level ${levelInfo.level}", fontSize = 12.sp, color = DivineGold, fontWeight = FontWeight.Bold)
                                Text("•", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                                Text("${data.user.totalXP} XP", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Drawer Navigation Items
                    ScreenRoute.entries.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) DivineGold else Color.White.copy(alpha = 0.7f)
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DivineGold else Color.White
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                performPalaceNavigation(screen)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33FBBF24),
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PeacockEyeIcon(size = 22.dp)
                                Text(
                                    text = "Career Quest",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DivineGold
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = DivineGold)
                            }
                        },
                        actions = {
                            // Quick Level & Streak Pills with Glassmorphism
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xD90A2E46))
                                    .border(1.dp, DivineGold, RoundedCornerShape(14.dp))
                                    .clickable { performPalaceNavigation(ScreenRoute.PROGRESS) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Lvl ${levelInfo.level}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DivineGold)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xD91E0B36))
                                    .border(1.dp, GoldYellowWarm, RoundedCornerShape(14.dp))
                                    .clickable { performPalaceNavigation(ScreenRoute.DASHBOARD) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("🔥 ${data.user.currentStreak}d", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldYellowWarm)
                            }

                            IconButton(onClick = { showReflectionDialog = true }) {
                                LotusIcon(size = 24.dp)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xE6051524),
                            titleContentColor = DivineGold
                        )
                    )
                },
                bottomBar = {
                    TraditionalPalaceDoorwayNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { screen -> performPalaceNavigation(screen) }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ScreenRoute.DASHBOARD.route
                    ) {
                        composable(ScreenRoute.DASHBOARD.route) {
                            DashboardScreen(
                                data = data,
                                selectedDate = selectedDate,
                                onPreviousDate = { viewModel.goToPreviousDate() },
                                onNextDate = { viewModel.goToNextDate() },
                                onTodayDate = { viewModel.goToToday() },
                                onSelectDate = { viewModel.setSelectedDate(it) },
                                onIncrementLeetCode = { viewModel.incrementLeetCode(it) },
                                onDecrementLeetCode = { viewModel.decrementLeetCode(it) },
                                onIncrementAptitude = { viewModel.incrementAptitude(it) },
                                onDecrementAptitude = { viewModel.decrementAptitude(it) },
                                onToggleTask = { viewModel.toggleTaskComplete(it) },
                                onAddTask = { title, desc, cat, prio, due ->
                                    viewModel.addTask(title, desc, cat, prio, due)
                                },
                                onEditTask = { id, title, desc, cat, prio, due ->
                                    viewModel.editTask(id, title, desc, cat, prio, due)
                                },
                                onDeleteTask = { viewModel.deleteTask(it) },
                                onAddLinkedInPost = { viewModel.addQuickLinkedInPost(it) },
                                onAddGitHubContribution = { viewModel.addQuickGitHubContribution(it) },
                                onNavigate = { routeStr ->
                                    val match = ScreenRoute.entries.firstOrNull { it.route == routeStr }
                                    if (match != null) performPalaceNavigation(match)
                                    else navController.navigate(routeStr)
                                },
                                onOpenReflection = { showReflectionDialog = true },
                                onResetToday = { viewModel.resetTodayRecord(it) }
                            )
                        }

                        composable(ScreenRoute.LEETCODE.route) {
                            LeetCodeScreen(
                                data = data,
                                onAddRecord = { name, num, url, diff, top, date, time, att, notes, appr, learned, mistake, better, tc, sc ->
                                    viewModel.addLeetCodeRecord(name, num, url, diff, top, date, time, att, notes, appr, learned, mistake, better, tc, sc)
                                },
                                onDeleteRecord = { viewModel.deleteLeetCodeRecord(it) }
                            )
                        }

                        composable(ScreenRoute.APTITUDE.route) {
                            AptitudeScreen(
                                data = data,
                                onAddSession = { cat, top, att, corr, time, date ->
                                    viewModel.addAptitudeSession(cat, top, att, corr, time, date)
                                },
                                onDeleteSession = { viewModel.deleteAptitudeSession(it) }
                            )
                        }

                        composable(ScreenRoute.PROGRESS.route) {
                            ProgressScreen(data = data)
                        }

                        composable(ScreenRoute.STREAKS.route) {
                            StreaksScreen(data = data)
                        }

                        composable(ScreenRoute.NOTES.route) {
                            NotesScreen(
                                data = data,
                                onAddNote = { title, content, cat, tags, review ->
                                    viewModel.addNote(title, content, cat, tags, review)
                                },
                                onEditNote = { id, title, content, cat, tags, review ->
                                    viewModel.editNote(id, title, content, cat, tags, review)
                                },
                                onToggleFavorite = { viewModel.toggleFavoriteNote(it) },
                                onTogglePin = { viewModel.togglePinNote(it) },
                                onDuplicateNote = { viewModel.duplicateNote(it) },
                                onDeleteNote = { viewModel.deleteNote(it) }
                            )
                        }

                        composable(ScreenRoute.SETTINGS.route) {
                            SettingsScreen(
                                data = data,
                                onSaveSettings = { viewModel.updateSettings(it) },
                                onExportJson = { viewModel.exportDataJson() },
                                onImportJson = { viewModel.importDataJson(it) },
                                onResetToEmpty = { viewModel.resetToEmptyData() }
                            )
                        }

                        composable(ScreenRoute.LINKEDIN.route) {
                            LinkedInScreen(
                                data = data,
                                onAddActivity = { type, title, details ->
                                    viewModel.addLinkedInActivity(type, title, details)
                                },
                                onDeleteActivity = { viewModel.deleteLinkedInActivity(it) }
                            )
                        }

                        composable(ScreenRoute.GITHUB.route) {
                            GitHubScreen(
                                data = data,
                                onAddActivity = { type, repoName, desc, link ->
                                    viewModel.addGitHubActivity(type, repoName, desc, link)
                                },
                                onDeleteActivity = { viewModel.deleteGitHubActivity(it) }
                            )
                        }

                        composable(ScreenRoute.TODOS.route) {
                            TodosScreen(
                                data = data,
                                onToggleTask = { viewModel.toggleTaskComplete(it) },
                                onAddTask = { title, desc, cat, prio, due ->
                                    viewModel.addTask(title, desc, cat, prio, due)
                                },
                                onEditTask = { id, title, desc, cat, prio, due ->
                                    viewModel.editTask(id, title, desc, cat, prio, due)
                                },
                                onDeleteTask = { viewModel.deleteTask(it) }
                            )
                        }

                        composable(ScreenRoute.ACHIEVEMENTS.route) {
                            AchievementsScreen(data = data)
                        }
                    }

                    // Floating +XP Notification Animation overlay
                    floatingXp?.let { (xp, label) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            FloatingXpBadge(
                                xp = xp,
                                label = label,
                                onDismiss = { floatingXp = null }
                            )
                        }
                    }
                }
            }
        }
    }

    // Surprise Reward Dialog
    surpriseReward?.let { reward ->
        SurpriseRewardDialog(
            reward = reward,
            onDismiss = { viewModel.dismissSurpriseReward() }
        )
    }

    // Daily Reflection Dialog
    if (showReflectionDialog) {
        DailyReflectionDialog(
            existingReflection = todayReflection,
            onDismiss = { showReflectionDialog = false },
            onSave = { learned, difficult, improve, proud ->
                viewModel.saveReflection(learned, difficult, improve, proud)
                showReflectionDialog = false
            }
        )
    }
}
