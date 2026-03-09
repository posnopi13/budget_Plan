package com.babytime.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.babytime.app.ui.screen.*
import com.babytime.app.viewmodel.BabyViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "홈", Icons.Filled.Home)
    object Statistics : Screen("statistics", "통계", Icons.Filled.BarChart)
    object Status : Screen("status", "아기상태", Icons.Filled.Favorite)
    object Settings : Screen("settings", "설정", Icons.Filled.Settings)
}

private val bottomNavItems = listOf(Screen.Home, Screen.Statistics, Screen.Status, Screen.Settings)

@Composable
fun BabyTimeNavigation(viewModel: BabyViewModel) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) { HomeScreen(viewModel, paddingValues) }
            composable(Screen.Statistics.route) { StatisticsScreen(viewModel, paddingValues) }
            composable(Screen.Status.route) { BabyStatusScreen(viewModel, paddingValues) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel, paddingValues) }
        }
    }
}
