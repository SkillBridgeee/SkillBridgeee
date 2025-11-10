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
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class NavGraphCoverageTest {

  companion object {
    private const val TAG = "NavGraphCoverageTest"

    @BeforeClass
    @JvmStatic
    fun globalSignIn() {
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: IllegalStateException) {}

      // Create & sign-in a persistent test Google user and create a minimal app profile.
      try {
        TestAuthHelpers.signInAsGoogleUserBlocking(
            email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
      } catch (e: Exception) {
        Log.w(TAG, "globalSignIn failed", e)
      }
    }

    @AfterClass
    @JvmStatic
    fun globalTearDown() {
      try {
        Firebase.auth.currentUser?.delete()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to delete global test user in @AfterClass", e)
      }
      try {
        Firebase.auth.signOut()
      } catch (_: Exception) {}
    }
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun initRepositories() {
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

    // Connect to Firebase emulators (safe to call repeatedly)
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {}

    // Ensure the class-scoped user is present for Activity's auth listener.
    try {
      Firebase.auth.signOut()
      TestAuthHelpers.signInAsGoogleUserBlocking(
          email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
    } catch (_: Exception) {}

    // Allow the activity / auth listener to react and, if routed to SignUp, complete it.
    composeTestRule.waitForIdle()
    val detectStart = System.currentTimeMillis()
    val detectTimeout = 5_000L
    while (System.currentTimeMillis() - detectStart < detectTimeout) {
      val current = RouteStackManager.getCurrentRoute()
      if (current == NavRoutes.HOME) break
      if (current?.startsWith(NavRoutes.SIGNUP_BASE) == true) {
        // Complete the signup UI (email should be pre-filled for Google signups)
        TestUiHelpers.signUpThroughUi(
            composeTestRule = composeTestRule,
            password = "P@ssw0rd!",
            name = "Class",
            surname = "User",
            levelOfEducation = "Test",
            description = "Class-level test user",
            timeoutMs = 8_000L)
        break
      }
      Thread.sleep(200)
    }
  }

  /**
   * Wait helper: the class-level Google sign-in means tests should not need to click the "GitHub"
   * button. Wait for the app to reach HOME instead.
   */
  private fun waitForHome(timeoutMs: Long = 5_000L) {
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun compose_all_nav_destinations_to_exercise_animated_lambdas() {
    // Ensure signed in and at home (replaces previous GitHub click)
    waitForHome(timeoutMs = 15_000)

    // Home assertions
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()

    // Navigate using bottom nav (use test tags for reliability)
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

    // FAB (Add)
    composeTestRule.onNodeWithContentDescription("Add").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Create Your Lessons !").assertExists()
  }

  @Test
  fun skills_navigation_opens_subject_list() {
    // Ensure signed in and at home (replaces previous GitHub click)
    waitForHome(timeoutMs = 15_000)

    // Wait until HOME route is registered
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)

    // Click the first subject card on the Home screen
    composeTestRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).onFirst().performClick()
    composeTestRule.waitForIdle()

    // Wait until SKILLS route is registered
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS)

    // Verify SubjectListScreen is displayed (search bar present)
    composeTestRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).assertExists()
  }
}
