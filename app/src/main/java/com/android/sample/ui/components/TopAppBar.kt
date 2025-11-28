package com.android.sample.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager

/**
 * TopAppBar - Reusable top navigation bar component for SkillBridge app
 *
 * A Material3 TopAppBar that displays the current screen title and handles back navigation.
 * Integrates with both NavController and RouteStackManager for intelligent navigation behavior.
 *
 * Features:
 * - Dynamic title based on current route (Home, Skills, Profile, Settings, or default
 *   "SkillBridge")
 * - Smart back button visibility (hidden on Home screen, shown when navigation is possible)
 * - Dual navigation logic: main routes navigate to Home, secondary screens use custom stack
 * - Preserves navigation state with launchSingleTop and restoreState
 *
 * Navigation Behavior:
 * - Main routes (bottom nav): Back button goes to Home and resets stack
 * - Secondary screens: Back button uses RouteStackManager's previous route
 * - Fallback to NavController.navigateUp() if stack is empty
 *
 * Usage:
 * - Place in main activity/screen layout above content area
 * - Pass NavHostController from parent composable
 * - Works automatically with routes defined in NavRoutes object
 *
 * Modifying titles:
 * 1. Add new route case to the title when() expression
 * 2. Ensure route constant exists in NavRoutes object
 * 3. Update RouteStackManager.mainRoutes if it's a main route
 *
 * Note: Requires @OptIn(ExperimentalMaterial3Api::class) for TopAppBar usage
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  val currentRoute = currentDestination?.route

  if (currentRoute == NavRoutes.LOGIN) return

  val title =
      when (currentRoute) {
        NavRoutes.HOME -> "Home"
        NavRoutes.PROFILE -> "Profile"
        NavRoutes.MAP -> "Map"
        NavRoutes.BOOKINGS -> "My Bookings"
        NavRoutes.LISTING -> "Listing Details"
        else -> "SkillBridge"
      }

  // show back arrow if we have either NavController's previous or our stack knows of a previous
  // do not show it while on the home page
  val canNavigateBack =
      currentRoute != NavRoutes.HOME &&
          (navController.previousBackStackEntry != null ||
              RouteStackManager.getCurrentRoute() != null)

  TopAppBar(
      modifier = Modifier,
      title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
      navigationIcon = {
        if (canNavigateBack) {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      })
}
