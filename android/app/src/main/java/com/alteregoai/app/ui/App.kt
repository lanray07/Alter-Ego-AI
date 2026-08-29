package com.alteregoai.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alteregoai.app.core.AppContainer

object Routes {
    const val HOME = "home"
    const val MISSIONS = "missions"
    const val CHAT = "chat"
    const val TIMELINE = "timeline"
    const val INSIGHTS = "insights"
    const val PROFILE = "profile"
    const val JOURNAL = "journal"
    const val SHARE = "share"
    const val PAYWALL = "paywall"
    const val SETTINGS = "settings"
}

@Composable
fun AlterEgoApp(state: AppUiState, viewModel: MainViewModel, container: AppContainer, currentTheme: String, onThemeChange: (String) -> Unit, onRemindersChange: (Boolean) -> Unit) {
    var initialReminderPrompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.profile?.id) {
        if (!initialReminderPrompted && state.profile?.notificationPreference == true) {
            initialReminderPrompted = true
            onRemindersChange(true)
        }
    }
    if (state.profile == null) {
        OnboardingScreen(state, viewModel)
    } else {
        MainShell(state, viewModel, container, currentTheme, onThemeChange, onRemindersChange)
    }
}

@Composable
private fun MainShell(state: AppUiState, viewModel: MainViewModel, container: AppContainer, currentTheme: String, onThemeChange: (String) -> Unit, onRemindersChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val route = backStackEntry?.destination?.route ?: Routes.HOME
    val rootRoutes = setOf(Routes.HOME, Routes.MISSIONS, Routes.CHAT, Routes.TIMELINE, Routes.INSIGHTS)
    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppTopBar(route, rootRoutes.contains(route), navController) },
        bottomBar = { if (rootRoutes.contains(route)) BottomNavigation(route, navController) }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            CinematicBackground()
            NavHost(navController, startDestination = Routes.HOME, modifier = Modifier.fillMaxSize().padding(padding)) {
                composable(Routes.HOME) { HomeScreen(state, viewModel, onNavigate = navController::navigate) }
                composable(Routes.MISSIONS) { MissionsScreen(state, viewModel) }
                composable(Routes.CHAT) { ChatScreen(state, viewModel) }
                composable(Routes.TIMELINE) { TimelineScreen(state, viewModel) }
                composable(Routes.INSIGHTS) { InsightsScreen(state, navController::navigate) }
                composable(Routes.PROFILE) { ProfileScreen(state) }
                composable(Routes.JOURNAL) { JournalScreen(state, viewModel) }
                composable(Routes.SHARE) { ShareCardsScreen(state, viewModel, container) }
                composable(Routes.PAYWALL) { PaywallScreen(container) }
                composable(Routes.SETTINGS) { SettingsScreen(state, container, currentTheme, onThemeChange, onRemindersChange, viewModel::updatePreferences, onViewPlans = { navController.navigate(Routes.PAYWALL) }, onDelete = viewModel::clearAllData, onExport = { com.alteregoai.app.core.ShareCardRenderer.shareText(it, exportSummary(state)) }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(route: String, isRoot: Boolean, navController: NavHostController) {
    val title = when (route) { Routes.HOME -> "Dashboard"; Routes.MISSIONS -> "Daily Missions"; Routes.CHAT -> "Future Self"; Routes.TIMELINE -> "Timeline"; Routes.INSIGHTS -> "Insights"; Routes.PROFILE -> "Future Self"; Routes.JOURNAL -> "Journal"; Routes.SHARE -> "Share Cards"; Routes.PAYWALL -> "Upgrade"; Routes.SETTINGS -> "Settings"; else -> "Alter Ego AI" }
    androidx.compose.material3.TopAppBar(title = { androidx.compose.material3.Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }, navigationIcon = { if (!isRoot) androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) { androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, actions = { if (isRoot) androidx.compose.material3.IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) { androidx.compose.material3.Icon(Icons.Default.Settings, "Settings") } })
}

@Composable
private fun BottomNavigation(route: String, navController: NavHostController) {
    fun navigate(itemRoute: String) { navController.navigate(itemRoute) { popUpTo(Routes.HOME) { saveState = true }; launchSingleTop = true; restoreState = true } }
    androidx.compose.material3.NavigationBar {
        with(this) {
            NavigationBarItem(selected = route == Routes.HOME, onClick = { navigate(Routes.HOME) }, icon = { androidx.compose.material3.Icon(navIcon(Routes.HOME), "Home") }, label = { androidx.compose.material3.Text("Home") })
            NavigationBarItem(selected = route == Routes.MISSIONS, onClick = { navigate(Routes.MISSIONS) }, icon = { androidx.compose.material3.Icon(navIcon(Routes.MISSIONS), "Missions") }, label = { androidx.compose.material3.Text("Missions") })
            NavigationBarItem(selected = route == Routes.CHAT, onClick = { navigate(Routes.CHAT) }, icon = { androidx.compose.material3.Icon(navIcon(Routes.CHAT), "AI") }, label = { androidx.compose.material3.Text("AI") })
            NavigationBarItem(selected = route == Routes.TIMELINE, onClick = { navigate(Routes.TIMELINE) }, icon = { androidx.compose.material3.Icon(navIcon(Routes.TIMELINE), "Timeline") }, label = { androidx.compose.material3.Text("Timeline") })
            NavigationBarItem(selected = route == Routes.INSIGHTS, onClick = { navigate(Routes.INSIGHTS) }, icon = { androidx.compose.material3.Icon(navIcon(Routes.INSIGHTS), "Insights") }, label = { androidx.compose.material3.Text("Insights") })
        }
    }
}

private fun navIcon(route: String): ImageVector = when (route) { Routes.HOME -> Icons.Default.Home; Routes.MISSIONS -> Icons.Default.Checklist; Routes.CHAT -> Icons.Default.ChatBubble; Routes.TIMELINE -> Icons.Default.AutoAwesomeMotion; else -> Icons.Default.Insights }

private fun exportSummary(state: AppUiState): String = """Alter Ego AI Data Export Summary
Profiles: ${if (state.profile == null) 0 else 1}
Alter Ego Profiles: ${if (state.alterEgo == null) 0 else 1}
Missions: ${state.missions.size}
Journals: ${state.journals.size}
Chat Messages: ${state.messages.size}
Timeline Snapshots: ${state.snapshots.size}
Achievements: ${state.achievements.size}
"""
