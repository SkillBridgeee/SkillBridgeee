package com.android.sample.ui.navigation

/**
 * Defines the navigation routes for the application.
 *
 * This object centralizes all route constants, providing a single source of truth for navigation.
 * This makes the navigation system easier to maintain, as all route strings are in one place.
 *
 * ## How to use
 *
 * ### Adding a new screen:
 * 1. Add a new `const val` for the screen's route (e.g., `const val NEW_SCREEN = "new_screen"`).
 * 2. Add the new route to the `NavGraph.kt` file with its corresponding composable.
 * 3. If the screen should be in the bottom navigation bar, add it to the items list in
 *    `BottomNavBar.kt`.
 *
 * ### Removing a screen:
 * 1. Remove the `const val` for the screen's route.
 * 2. Remove the route and its composable from `NavGraph.kt`.
 * 3. If it was in the bottom navigation bar, remove it from the items list in `BottomNavBar.kt`.
 */
object NavRoutes {
  const val HOME = "home"
  const val PROFILE = "profile"
  const val SKILLS = "skills"
  const val SETTINGS = "settings"

  // Secondary pages
  const val PIANO_SKILL = "skills/piano"
  const val PIANO_SKILL_2 = "skills/piano2"
}
