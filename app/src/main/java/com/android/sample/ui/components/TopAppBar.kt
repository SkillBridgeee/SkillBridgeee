package com.android.sample.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * TopBar composable
 *
 * Displays a top app bar with:
 * - The current screen's title
 * - A back arrow button if the user can navigate back
 *
 * @param navController The app's NavController, used to detect back stack state and navigate up.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
  // Observe the current navigation state
  val navBackStackEntry = navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry.value?.destination

  // Define the title based on the current route
  val title =
      when (currentDestination?.route) {
        "home" -> "Home"
        "skills" -> "Skills"
        "profile" -> "Profile"
        "settings" -> "Settings"
        else -> "SkillBridge"
      }

  // Determine if the back arrow should be visible
  val canNavigateBack = navController.previousBackStackEntry != null

  TopAppBar(
      title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
      navigationIcon = {
        // Show back arrow only if not on the root (e.g., Home)
        if (canNavigateBack) {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      })
}
