package com.android.sample.endToEnd

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsScreenTestTags
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.profile.MyProfileScreenTestTags
import com.android.sample.utils.AppTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun userCanLoginAndNavigateThroughMainPages() = runTest {
    // Set up the app content
    composeTestRule.setContent { CreateAppContent() }
    composeTestRule.waitForIdle()

    // Get credentials from the fake repository
    val email = "alice@example.com"
    val password = "TestPassword123!"

    // Login
    composeTestRule.loginUser(email, password)
    composeTestRule.waitForIdle()

    // Verify we're on home screen
    composeTestRule.onNodeWithTag(HomeScreenTestTags.HOME_SCREEN).assertExists()

    // Navigate to My Profile
    composeTestRule.navigateToMyProfile()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTags.PROFILE_SCREEN).assertExists()

    // Navigate to My Bookings
    composeTestRule.navigateToMyBookings()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyBookingsScreenTestTags.MY_BOOKINGS_SCREEN).assertExists()

    // Navigate to Map
    composeTestRule.navigateToMap()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertExists()

    // Navigate back to Home
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_HOME).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.HOME_SCREEN).assertExists()
  }
}
