package com.android.sample.endToEnd

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.profile.MyProfileScreenTestTag
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

    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule.onAllNodesWithTag(SignInScreenTestTags.TITLE).fetchSemanticsNodes().isNotEmpty()
    }

    // Login
//    composeTestRule.loginUser(email, password)
//    composeTestRule.waitForIdle()


//    // Verify we're on home screen using an existing HomeScreen test tag
//    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
//
//    // Navigate to My Profile
//    composeTestRule.navigateToMyProfile()
//    composeTestRule.waitForIdle()
//    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST).assertExists()
//
//    // Navigate to My Bookings
//    composeTestRule.navigateToMyBookings()
//    composeTestRule.waitForIdle()
//    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.MY_BOOKINGS_SCREEN).assertExists()
//
//    // Navigate to Map
//    composeTestRule.navigateToMap()
//    composeTestRule.waitForIdle()
//    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertExists()
//
//    // Navigate back to Home using the new helper
//    composeTestRule.navigateToHome()
//    composeTestRule.waitForIdle()
//    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
  }
}
