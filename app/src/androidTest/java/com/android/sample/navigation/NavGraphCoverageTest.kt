package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.MainActivity
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.testutils.TestAuthHelpers
import com.android.sample.testutils.TestUiHelpers
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.subject.SubjectListTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlin.text.clear
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavGraphCoverageTest {

  companion object {
    private const val TAG = "NavGraphCoverageTest"
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var testEmail: String

  @Before
  fun setUp() {
    // Sign out first to ensure clean state
    try {
      Firebase.auth.signOut()
    } catch (_: Exception) {}

    // Configure emulators
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {}

    // Initialize repositories
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    try {
      ProfileRepositoryProvider.init(ctx)
      ListingRepositoryProvider.init(ctx)
      BookingRepositoryProvider.init(ctx)
      RatingRepositoryProvider.init(ctx)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    RouteStackManager.clear()

    // Sign in with Google (this will trigger the signup flow)
    testEmail = "test.user.${System.currentTimeMillis()}@example.com"
    var userId: String? = null

    runBlocking {
      try {
        Log.d(TAG, "Signing in as Google user with email: $testEmail")
        TestAuthHelpers.signInAsGoogleUser(email = testEmail, displayName = "Test User")

        val currentUser = Firebase.auth.currentUser
        require(currentUser != null) { "User not signed in after signInAsGoogleUser" }
        userId = currentUser.uid
        Log.d(TAG, "User signed in: ${currentUser.uid}, email: ${currentUser.email}")
      } catch (e: Exception) {
        Log.e(TAG, "Sign in failed in setUp", e)
        throw e
      }
    }

    // Wait for navigation to signup screen
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      val route = RouteStackManager.getCurrentRoute()
      Log.d(TAG, "Waiting for SIGNUP, current route: $route")
      route?.startsWith(NavRoutes.SIGNUP_BASE) == true
    }

    // Complete signup through UI
    Log.d(TAG, "Completing signup through UI")
    TestUiHelpers.signUpThroughUi(
        composeTestRule = composeTestRule,
        password = "TestPassw0rd!",
        name = "Test",
        surname = "User",
        levelOfEducation = "CS, 3rd year",
        description = "Test user for navigation tests",
        addressQuery = "Test Location",
        timeoutMs = 15_000L)

    // Wait for signup to complete (check if profile exists in Firestore)
    runBlocking {
      try {
        Log.d(TAG, "Waiting for profile to be created")
        var profileCreated = false
        val startTime = System.currentTimeMillis()

        while (!profileCreated && System.currentTimeMillis() - startTime < 15_000) {
          try {
            val profile = ProfileRepositoryProvider.repository.getProfile(userId!!)
            if (profile != null) {
              Log.d(TAG, "Profile verified: ${profile.name}")
              profileCreated = true
            }
          } catch (_: Exception) {}

          if (!profileCreated) {
            delay(500)
          }
        }

        require(profileCreated) { "Profile not created after signup" }
      } catch (e: Exception) {
        Log.e(TAG, "Profile verification failed", e)
        throw e
      }
    }

    // Re-authenticate to trigger AuthStateListener
    runBlocking {
      try {
        Log.d(TAG, "Re-authenticating after signup")
        TestAuthHelpers.signInAsGoogleUser(email = testEmail, displayName = "Test User")

        val currentUser = Firebase.auth.currentUser
        require(currentUser != null) { "User not signed in after re-authentication" }
        Log.d(TAG, "Re-authenticated: ${currentUser.uid}")

        delay(1000) // Give AuthStateListener time to fire
      } catch (e: Exception) {
        Log.e(TAG, "Re-authentication failed", e)
        throw e
      }
    }

    // Wait for home screen
    waitForHome(timeoutMs = 15_000L)
  }

  @After
  fun tearDown() {
    try {
      Firebase.auth.currentUser?.delete()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to delete test user", e)
    }
    try {
      TestAuthHelpers.signOut()
    } catch (_: Exception) {}
  }

  private fun waitForHome(timeoutMs: Long = 10_000L) {
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      try {
        val route = RouteStackManager.getCurrentRoute()
        Log.d(TAG, "Waiting for HOME, current route: $route")
        route == NavRoutes.HOME
      } catch (e: Exception) {
        Log.d(TAG, "Error getting current route", e)
        false
      }
    }

    composeTestRule.waitForIdle()
    Log.d(TAG, "Reached HOME screen")
  }

  @Test
  fun compose_all_nav_destinations_to_exercise_animated_lambdas() {
    waitForHome(timeoutMs = 15_000)

    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_MAP).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertExists()

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.CARD_TITLE).assertExists()

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertExists()

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
  }

  @Test
  fun skills_navigation_opens_subject_list() {
    waitForHome(timeoutMs = 15_000)

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)

    composeTestRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).onFirst().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS)

    composeTestRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).assertExists()
  }
}
