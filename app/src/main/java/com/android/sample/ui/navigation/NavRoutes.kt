package com.android.sample.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
  const val LOGIN = "login"
  const val HOME = "home"
  const val PROFILE = "profile/{profileId}"
  const val SKILLS = "skills"
  const val BOOKINGS = "bookings"
  const val MAP = "map"

  // Secondary pages
  const val NEW_SKILL = "new_skill/{profileId}"
  const val MESSAGES = "messages"
  const val SIGNUP = "signup?email={email}"
  const val SIGNUP_BASE = "signup"

  fun createProfileRoute(profileId: String) = "profile/$profileId"

  fun createNewSkillRoute(profileId: String) = "new_skill/$profileId"

  fun createSignUpRoute(email: String? = null): String {
    return if (email != null) {
      // URL encode the email to handle special characters like @
      val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
      "signup?email=$encodedEmail"
    } else {
      "signup"
    }
  }
}
