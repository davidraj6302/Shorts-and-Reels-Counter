package com.maibu.reelshortscounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maibu.reelshortscounter.ui.AnalyticsScreen
import com.maibu.reelshortscounter.ui.HomeScreen
import com.maibu.reelshortscounter.ui.PrivacyPolicyScreen
import com.maibu.reelshortscounter.ui.SettingsScreen
import com.maibu.reelshortscounter.ui.TrackerScreen
import com.maibu.reelshortscounter.ui.theme.ScrollSenseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ScrollSenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state
            ScrollSenseTheme(appTheme = state.theme, isDarkMode = state.isDarkMode) {
                MainContainer(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
        viewModel.refreshData()
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Tracker : Screen("tracker", "Tracker", Icons.Default.TrackChanges)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", Icons.Default.Settings)
}

@Composable
fun MainContainer(viewModel: ScrollSenseViewModel) {
    val navController = rememberNavController()
    val navItems = listOf(Screen.Home, Screen.Tracker, Screen.Analytics, Screen.Settings)
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        onClick = {
                            currentRoute = screen.route
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel) }
            composable(Screen.Tracker.route) { TrackerScreen(viewModel) }
            composable(Screen.Analytics.route) { AnalyticsScreen(viewModel) }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    viewModel = viewModel, 
                    navController = navController
                ) 
            }
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
