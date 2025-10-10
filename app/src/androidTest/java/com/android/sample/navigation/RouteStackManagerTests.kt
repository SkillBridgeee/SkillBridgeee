package com.android.sample.navigation

import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * RouteStackManagerTest
 *
 * Unit tests for the RouteStackManager singleton.
 *
 * These tests verify:
 * - Stack operations (add, pop, clear)
 * - Prevention of consecutive duplicate routes
 * - Maximum stack size enforcement
 * - Main route detection logic
 * - Correct retrieval of current and previous routes
 */
class RouteStackManagerTest {

  @Before
  fun setup() {
    RouteStackManager.clear()
  }

  @After
  fun tearDown() {
    RouteStackManager.clear()
  }

  @Test
  fun addRoute_adds_new_route_to_stack() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    assertEquals(listOf(NavRoutes.HOME), RouteStackManager.getAllRoutes())
  }

  @Test
  fun addRoute_does_not_add_consecutive_duplicate_routes() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.HOME)
    assertEquals(1, RouteStackManager.getAllRoutes().size)
  }

  @Test
  fun addRoute_allows_duplicate_routes_if_not_consecutive() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.SKILLS)
    RouteStackManager.addRoute(NavRoutes.HOME)
    assertEquals(
        listOf(NavRoutes.HOME, NavRoutes.SKILLS, NavRoutes.HOME), RouteStackManager.getAllRoutes())
  }

  @Test
  fun popAndGetPrevious_returns_previous_route_and_removes_last() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.SKILLS)
    RouteStackManager.addRoute(NavRoutes.PROFILE)

    val previous = RouteStackManager.popAndGetPrevious()

    assertEquals(NavRoutes.SKILLS, previous)
    assertEquals(listOf(NavRoutes.HOME, NavRoutes.SKILLS), RouteStackManager.getAllRoutes())
  }

  @Test
  fun popAndGetPrevious_returns_null_when_stack_empty() {
    assertNull(RouteStackManager.popAndGetPrevious())
  }

  @Test
  fun popRoute_removes_and_returns_last_route() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.PROFILE)

    val popped = RouteStackManager.popRoute()

    assertEquals(NavRoutes.PROFILE, popped)
    assertEquals(listOf(NavRoutes.HOME), RouteStackManager.getAllRoutes())
  }

  @Test
  fun getCurrentRoute_returns_last_route_in_stack() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.SKILLS)

    assertEquals(NavRoutes.SKILLS, RouteStackManager.getCurrentRoute())
  }

  @Test
  fun clear_removes_all_routes() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.addRoute(NavRoutes.SETTINGS)

    RouteStackManager.clear()

    assertTrue(RouteStackManager.getAllRoutes().isEmpty())
  }

  @Test
  fun isMainRoute_returns_true_for_main_routes() {
    listOf(NavRoutes.HOME, NavRoutes.PROFILE, NavRoutes.SKILLS, NavRoutes.SETTINGS).forEach { route
      ->
      assertTrue("$route should be a main route", RouteStackManager.isMainRoute(route))
    }
  }

  @Test
  fun isMainRoute_returns_false_for_non_main_routes() {
    assertFalse(RouteStackManager.isMainRoute("piano_skill"))
    assertFalse(RouteStackManager.isMainRoute("proposal"))
    assertFalse(RouteStackManager.isMainRoute(null))
  }

  @Test
  fun addRoute_discards_oldest_when_stack_exceeds_limit() {
    val maxSize = 20
    // Add more than 20 routes
    repeat(maxSize + 5) { i -> RouteStackManager.addRoute("route_$i") }

    val routes = RouteStackManager.getAllRoutes()
    assertEquals(maxSize, routes.size)
    assertEquals("route_5", routes.first()) // first 5 were discarded
    assertEquals("route_24", routes.last()) // last added
  }

  @Test
  fun popAndGetPrevious_does_not_crash_when_called_repeatedly_on_small_stack() {
    RouteStackManager.addRoute(NavRoutes.HOME)
    RouteStackManager.popAndGetPrevious()
    RouteStackManager.popAndGetPrevious() // should not throw
    assertTrue(RouteStackManager.getAllRoutes().isEmpty())
  }
}
