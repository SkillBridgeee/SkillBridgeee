package com.android.sample.ui.navigation

import org.junit.Assert.*
import org.junit.Test

class NavRoutesTest {

  @Test
  fun createSignUpRoute_withNullEmail_returnsBaseRoute() {
    val route = NavRoutes.createSignUpRoute(null)
    assertEquals("signup", route)
  }

  @Test
  fun createSignUpRoute_withEmail_encodesEmailCorrectly() {
    val route = NavRoutes.createSignUpRoute("test@example.com")

    // @ should be encoded as %40
    assertTrue(route.contains("test%40example.com"))
    assertTrue(route.startsWith("signup?email="))
  }

  @Test
  fun createSignUpRoute_withSpecialCharacters_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("user+test@example.com")

    // Both + and @ should be encoded
    assertTrue(route.contains("%40")) // @
    assertTrue(route.contains("%2B")) // +
  }

  @Test
  fun createSignUpRoute_withSpaces_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test user@example.com")

    // Spaces should be encoded
    assertTrue(route.contains("%20") || route.contains("+"))
  }

  @Test
  fun createNewSkillRoute_createsCorrectRoute() {
    val route = NavRoutes.createNewSkillRoute("profile123")
    assertEquals("new_skill/profile123", route)
  }

  @Test
  fun createProfileRoute_createsCorrectRoute() {
    val route = NavRoutes.createProfileRoute("user456")
    assertEquals("profile/user456", route)
  }

  @Test
  fun signupRoute_hasCorrectPattern() {
    assertEquals("signup?email={email}", NavRoutes.SIGNUP)
  }

  @Test
  fun signupBaseRoute_isCorrect() {
    assertEquals("signup", NavRoutes.SIGNUP_BASE)
  }

  @Test
  fun createSignUpRoute_withEmptyString_returnsRouteWithEmptyParam() {
    val route = NavRoutes.createSignUpRoute("")
    assertEquals("signup?email=", route)
  }

  @Test
  fun createSignUpRoute_withComplexEmail_encodesAll() {
    val email = "user.name+tag@sub-domain.example.com"
    val route = NavRoutes.createSignUpRoute(email)

    // Should contain encoded @ symbol
    assertTrue(route.contains("%40"))
    // Should start with signup?email=
    assertTrue(route.startsWith("signup?email="))
  }

  @Test
  fun homeRoute_isCorrect() {
    assertEquals("home", NavRoutes.HOME)
  }

  @Test
  fun loginRoute_isCorrect() {
    assertEquals("login", NavRoutes.LOGIN)
  }

  @Test
  fun skillsRoute_isCorrect() {
    assertEquals("skills", NavRoutes.SKILLS)
  }

  @Test
  fun bookingsRoute_isCorrect() {
    assertEquals("bookings", NavRoutes.BOOKINGS)
  }
}
