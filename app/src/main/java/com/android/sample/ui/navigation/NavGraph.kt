package com.android.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.sample.ui.screens.HomePlaceholder
import com.android.sample.ui.screens.ProfilePlaceholder
import com.android.sample.ui.screens.SettingsPlaceholder
import com.android.sample.ui.screens.SkillsPlaceholder

/**
 * AppNavGraph
 *
 * This file defines the navigation graph for the app using Jetpack Navigation Compose. It maps
 * navigation routes (defined in [NavRoutes]) to the composable screens that should be displayed
 * when the user navigates to that route.
 *
 * How it works:
 * - [NavHost] acts as the navigation container.
 * - Each `composable()` inside NavHost represents one screen in the app.
 * - The [navController] is used to navigate between routes.
 *
 * Example usage: navController.navigate(NavRoutes.PROFILE)
 *
 * To add a new screen:
 * 1. Create a new composable screen (e.g., MyNewScreen.kt) inside ui/screens/.
 * 2. Add a new route constant to [NavRoutes] (e.g., const val MY_NEW_SCREEN = "my_new_screen").
 * 3. Add a new `composable()` entry below with your screen function.
 * 4. (Optional) Add your route to the bottom navigation bar if needed.
 *
 * This makes it easy to add, remove, or rename screens without breaking navigation.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
  NavHost(navController = navController, startDestination = NavRoutes.HOME) {
    composable(NavRoutes.HOME) { HomePlaceholder() }
    composable(NavRoutes.PROFILE) { ProfilePlaceholder() }
    composable(NavRoutes.SKILLS) { SkillsPlaceholder() }
    composable(NavRoutes.SETTINGS) { SettingsPlaceholder() }
  }
}
