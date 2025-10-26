package com.android.sample

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.components.BottomNavBarTestTags
import com.android.sample.ui.components.TopAppBarTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.subject.SubjectListTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class End2EndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun initRepositories() {
    RouteStackManager.clear()
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    try {
      ProfileRepositoryProvider.init(ctx)
      ListingRepositoryProvider.init(ctx)
      BookingRepositoryProvider.init(
        ctx) // prevents IllegalStateException in ViewModel construction
      RatingRepositoryProvider.init(ctx)
    } catch (e: Exception) {
      // Initialization may fail in some CI/emulator setups; log and continue
      println("Repository init failed: ${e.message}")
    }
  }

  @Test
  fun userLogsInAsLearnerAndGoesToMainPage() {
    // In the login screen, click the GitHub login button to simulate login
    composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are now on the main page by checking for a main page element
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
    val node = composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).fetchSemanticsNode()

    val text = node.config[SemanticsProperties.Text].joinToString("")
    val textTest = text.equals("Home")
    assert(textTest)

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertDoesNotExist()

    // Verify the BottomAppBar is present at the bottom and displays the right things
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.BOTTOM_NAV_BAR).assertExists()

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()
  }

  /*@Test
  fun userLogsInAndViewsTutorProfile() {
    // In the login screen, click the GitHub login button to simulate login
    composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are now on the main page by checking for a main page element
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertDoesNotExist()

    // Verify the BottomAppBar is present at the bottom and displays the right things
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.BOTTOM_NAV_BAR).assertExists()

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()

    // User navigates to Profile tab
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).performClick()
    composeTestRule.waitForIdle()

    // Verify the BottomAppBar is present at the bottom and displays the right things
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.BOTTOM_NAV_BAR).assertExists()

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
    val node = composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).fetchSemanticsNode()

    val text = node.config[SemanticsProperties.Text].joinToString("")
    val textTest = text.equals("Profile")
    assert(textTest)

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertExists()

    // Verify we are on the profile page by checking for a profile page element
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.CARD_TITLE).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_LOCATION).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE).assertExists()

    // Go back to home
    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are now on the main page by checking for a main page element
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()
  }*/

  @Test
  fun userLogsInAsTutorAndGoesToSkills() {

    composeTestRule.onNodeWithTag(SignInScreenTestTags.ROLE_TUTOR).performClick()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are now on the main page by checking for a main page element
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertDoesNotExist()

    // Go to Skills tab
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).performClick()
    composeTestRule.waitForIdle()

    // Verify the BottomAppBar is present at the bottom and displays the right things
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.BOTTOM_NAV_BAR).assertExists()

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things

    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
    val node = composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).fetchSemanticsNode()

    val text = node.config[SemanticsProperties.Text].joinToString("")
    val textTest = text.equals("skills")
    assert(textTest)

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertExists()

    // Verify we are on the skills page by checking for a skills page element
    composeTestRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).assertExists()

    // Go back to home
    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are now on the main page by checking for a main page element
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()
  }

  @Test
  fun userLogsInAsTutorAndViewsBookings() {

    composeTestRule.onNodeWithTag(SignInScreenTestTags.ROLE_TUTOR).performClick()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are now on the main page by checking for a main page element
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertDoesNotExist()

    // Go to Bookings tab
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).performClick()
    composeTestRule.waitForIdle()

    // Verify the BottomAppBar is present at the bottom and displays the right things
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.BOTTOM_NAV_BAR).assertExists()

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()

    // Verify the TopAppBar is present at the top and displays the right things

    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
    val node = composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).fetchSemanticsNode()

    val text = node.config[SemanticsProperties.Text].joinToString("")
    val textTest = text.equals("My Bookings")
    assert(textTest)

    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).assertExists()

    // Verify we are on the bookings page by checking for a bookings page element
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.EMPTY_BOOKINGS).assertExists()

    // Go back to home
    composeTestRule.onNodeWithTag(TopAppBarTestTags.NAVIGATE_BACK).performClick()
    composeTestRule.waitForIdle()
  }
}
