package com.android.sample.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager

/**
 * BottomNavBar - Main navigation bar component for SkillBridge app
 *
 * A Material3 NavigationBar that provides tab-based navigation between main app sections.
 * Integrates with RouteStackManager to maintain proper navigation state and back stack handling.
 *
 * Features:
 * - Shows 4 main tabs: Home, Skills, Profile, Settings
 * - Highlights currently selected tab based on navigation state
 * - Resets route stack when switching tabs to prevent deep navigation issues
 * - Preserves tab state with saveState/restoreState for smooth UX
 * - Uses launchSingleTop to prevent duplicate destinations
 *
 * Usage:
 * - Place in main activity/screen as persistent bottom navigation
 * - Pass NavHostController from parent composable
 * - Navigation routes must match those defined in NavRoutes object
 *
 * Adding a new tab:
 * 1. Add new BottomNavItem to the items list with label, icon, and route
 * 2. Ensure corresponding route exists in NavRoutes
 * 3. Add route to RouteStackManager.mainRoutes if needed
 * 4. Import appropriate Material icon
 *
 * Note: Tab switching automatically clears and resets the navigation stack
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val items =
      listOf(
          BottomNavItem("Home", Icons.Default.Home, NavRoutes.HOME),
          BottomNavItem("Bookings", Icons.Default.Home, NavRoutes.BOOKINGS),
          BottomNavItem("Skills", Icons.Default.Star, NavRoutes.SKILLS),
          BottomNavItem("Profile", Icons.Default.Person, NavRoutes.PROFILE),
          BottomNavItem("Settings", Icons.Default.Settings, NavRoutes.SETTINGS))

  NavigationBar(modifier = Modifier) {
    items.forEach { item ->
      val itemModifier =
          when (item.route) {
            NavRoutes.HOME -> Modifier.testTag(MyBookingsPageTestTag.NAV_HOME)
            NavRoutes.BOOKINGS -> Modifier.testTag(MyBookingsPageTestTag.NAV_BOOKINGS)
            NavRoutes.PROFILE -> Modifier.testTag(MyBookingsPageTestTag.NAV_PROFILE)
            NavRoutes.MESSAGES -> Modifier.testTag(MyBookingsPageTestTag.NAV_MESSAGES)

            // Add NAV_MESSAGES mapping here if needed
            else -> Modifier
          }

      NavigationBarItem(
          modifier = itemModifier,
          selected = currentRoute == item.route,
          onClick = {
            RouteStackManager.clear()
            RouteStackManager.addRoute(item.route)
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
