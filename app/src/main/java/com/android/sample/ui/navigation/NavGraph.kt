package com.android.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.sample.ui.screens.HomePlaceholder
import com.android.sample.ui.screens.PianoSkill2Screen
import com.android.sample.ui.screens.PianoSkillScreen
import com.android.sample.ui.screens.ProfilePlaceholder
import com.android.sample.ui.screens.SettingsPlaceholder
import com.android.sample.ui.screens.SkillsPlaceholder

/**
 * AppNavGraph - Main navigation configuration for the SkillBridge app
 *
 * This file defines all navigation routes and their corresponding screen composables. Each route is
 * registered with the NavHost and includes route tracking via RouteStackManager.
 *
 * Usage:
 * - Call AppNavGraph(navController) from your main activity/composable
 * - Navigation is handled through the provided NavHostController
 *
 * Adding a new screen:
 * 1. Add route constant to NavRoutes object
 * 2. Import the new screen composable
 * 3. Add composable() block with LaunchedEffect for route tracking
 * 4. Pass navController parameter if screen needs navigation
 *
 * Removing a screen:
 * 1. Delete the composable() block
 * 2. Remove unused import
 * 3. Remove route constant from NavRoutes (if no longer needed)
 *
 * Note: All screens automatically register with RouteStackManager for back navigation tracking
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
  NavHost(navController = navController, startDestination = NavRoutes.HOME) {
    composable(NavRoutes.PIANO_SKILL) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PIANO_SKILL) }
      PianoSkillScreen(navController = navController)
    }

    composable(NavRoutes.PIANO_SKILL_2) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PIANO_SKILL_2) }
      PianoSkill2Screen()
    }

    composable(NavRoutes.SKILLS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SKILLS) }
      SkillsPlaceholder(navController)
    }

    composable(NavRoutes.PROFILE) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.PROFILE) }
      ProfilePlaceholder()
      // val vm: SubjectListViewModel = viewModel()
      //      val vm2: TutorProfileViewModel = viewModel()
      // SubjectListScreen(vm) { _: Profile -> }
      //      // TutorProfileScreen("test", vm2, navController)
    }

    composable(NavRoutes.HOME) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.HOME) }
      HomePlaceholder()
    }

    composable(NavRoutes.SETTINGS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.SETTINGS) }
      SettingsPlaceholder()
    }

    composable(NavRoutes.BOOKINGS) {
      LaunchedEffect(Unit) { RouteStackManager.addRoute(NavRoutes.BOOKINGS) }
    }
  }
}
