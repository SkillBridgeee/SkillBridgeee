package com.android.sample.ui.navigation

/**
 * RouteStackManager - Custom navigation stack manager for SkillBridge app
 *
 * A singleton that maintains a manual navigation stack to provide predictable back navigation
 * between screens, especially for parameterized routes and complex navigation flows.
 *
 * Key Features:
 * - Tracks navigation history with a maximum stack size of 20
 * - Prevents duplicate consecutive routes
 * - Distinguishes between main routes (bottom nav) and other screens
 * - Provides stack manipulation methods for custom back navigation
 *
 * Usage:
 * - Call addRoute() when navigating to a new screen
 * - Call popAndGetPrevious() to get the previous route for back navigation
 * - Use isMainRoute() to check if a route is a main bottom navigation route
 *
 * Integration:
 * - Used in AppNavGraph to track all route changes via LaunchedEffect
 * - Main routes are automatically defined (HOME, SKILLS, PROFILE, BOOKINGS)
 * - Works alongside NavHostController for enhanced navigation control
 *
 * Modifying main routes:
 * - Update the mainRoutes set to add/remove bottom navigation routes
 * - Ensure route constants match those defined in NavRoutes object
 */
object RouteStackManager {
  private const val MAX_STACK_SIZE = 20
  private val stack = ArrayDeque<String>()

  // Set of the app's main routes (bottom nav)
  private val mainRoutes =
      setOf(NavRoutes.HOME, NavRoutes.SKILLS, NavRoutes.PROFILE, NavRoutes.BOOKINGS)

  fun addRoute(route: String) {
    // prevent consecutive duplicates
    if (stack.lastOrNull() == route) return

    if (stack.size >= MAX_STACK_SIZE) {
      stack.removeFirst()
    }
    stack.addLast(route)
  }

  /** Pops the current route and returns the new current route (previous). */
  fun popAndGetPrevious(): String? {
    if (stack.isNotEmpty()) stack.removeLast()
    return stack.lastOrNull()
  }

  /** Remove and return the popped route (legacy if you still want it) */
  fun popRoute(): String? = if (stack.isNotEmpty()) stack.removeLast() else null

  fun getCurrentRoute(): String? = stack.lastOrNull()

  fun clear() = stack.clear()

  fun getAllRoutes(): List<String> = stack.toList()

  fun isMainRoute(route: String?): Boolean = route != null && mainRoutes.contains(route)
}
