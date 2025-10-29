package com.android.sample.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.HomeScreenTestTags
import com.android.sample.MainActivity
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.subject.SubjectListTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavGraphCoverageTest {

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
  }

  @Test
  fun compose_all_nav_destinations_to_exercise_animated_lambdas() {
    // Login to reach main app
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Home assertions
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()

    // Navigate using bottom nav (use test tags for reliability)
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_MAP).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("map_screen_text").assertExists()

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
    // Login to reach main app
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

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
