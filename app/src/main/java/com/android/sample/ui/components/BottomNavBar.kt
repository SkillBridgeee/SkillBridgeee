package com.android.sample.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.sample.ui.navigation.NavRoutes

/**
 * BottomNavBar
 *
 * This composable defines the app’s bottom navigation bar. It allows users to switch between key
 * screens (Home, Skills, Profile, Settings) by tapping icons at the bottom of the screen.
 *
 * How it works:
 * - The NavigationBar is part of Material3 design.
 * - Each [NavigationBarItem] represents a screen and has: → An icon → A text label → A route to
 *   navigate to when clicked
 * - The bar highlights the active route using [selected].
 * - Navigation is handled by the shared [NavHostController].
 *
 * How to add a new tab:
 * 1. Add a new route constant to [NavRoutes].
 * 2. Add a new [BottomNavItem] to the `items` list below.
 * 3. Add a corresponding `composable()` entry to [NavGraph].
 *
 * How to remove a tab:
 * - Simply remove it from the `items` list below.
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val items =
      listOf(
          BottomNavItem("Home", Icons.Default.Home, NavRoutes.HOME),
          BottomNavItem("Skills", Icons.Default.Star, NavRoutes.SKILLS),
          BottomNavItem("Profile", Icons.Default.Person, NavRoutes.PROFILE),
          BottomNavItem("Settings", Icons.Default.Settings, NavRoutes.SETTINGS))

  NavigationBar {
    items.forEach { item ->
      NavigationBarItem(
          selected = currentRoute == item.route,
          onClick = {
            navController.navigate(item.route) {
              popUpTo(NavRoutes.HOME) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          icon = { Icon(item.icon, contentDescription = item.label) },
          label = { Text(item.label) })
    }
  }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)
