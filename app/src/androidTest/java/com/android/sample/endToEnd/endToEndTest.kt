package com.android.sample.endToEnd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.MainActivity
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.utils.AppTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest : AppTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  companion object {
    private const val TEST_PASSWORD = "testPassword123!"
    private const val TEST_DESC = "Happy"
    private const val TEST_DESC_APPEND = " Man"
    private const val TEST_DESC_FULL = "Happy Man"
    private const val TEST_TITLE = "Math Class"
    private const val TEST_EMAIL = "guillaume.lepinuuuuusu@epfl.ch"
    private const val TEST_NAME = "Lepin"
    private const val TEST_SURNAME = "Guillaume"
    private const val TEST_FULL_NAME = "Lepin Guillaume"
    private const val TEST_LOCATION = "London Street 1"
    private const val TEST_EDUCATION = "CS, 3rd year"
    private const val TEST_PROPOSAL = "PROPOSAL"
    private const val TEST_PROPOSAL_DESCRIPTION = "Learn math with me"
    private const val TEST_PROPOSAL_PRICE = "50"
    private const val TEST_PROPOSAL_SUBJECT = "ACADEMICS"
    private const val TEST_BACK_BUTTON = "Back"
  }

  @Test
  fun userCanLoginAndNavigateThroughMainPages() {
    // Get credentials from the fake repository
    val email = "alice@example.com"
    val password = "TestPassword123!"

    composeTestRule.signUpAndLogin(
        name = TEST_NAME,
        surname = TEST_SURNAME,
        address = TEST_LOCATION,
        levelOfEducation = TEST_EDUCATION,
        description = TEST_DESC,
        email = TEST_EMAIL,
        password = TEST_PASSWORD)

    composeTestRule.waitUntil {
      composeTestRule
          .onAllNodesWithTag(HomeScreenTestTags.WELCOME_SECTION)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    // Verify navigation to home screen
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()

    // Navigate to My Profile
    composeTestRule.navigateToMyProfile()
    composeTestRule.waitUntil {
      composeTestRule
          .onAllNodesWithTag(MyProfileScreenTestTag.PROFILE_ICON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()

    // Navigate to My Bookings
    composeTestRule.navigateToMyBookings()
    composeTestRule.waitUntil {
      composeTestRule
          .onAllNodesWithTag(MyBookingsPageTestTag.EMPTY)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.EMPTY).assertIsDisplayed()

    // Navigate to Map
    //    composeTestRule.navigateToMap()

    // Go back to home page
    composeTestRule.navigateToHome()
    composeTestRule.waitUntil {
      composeTestRule
          .onAllNodesWithTag(HomeScreenTestTags.WELCOME_SECTION)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
  }
}

// package com.android.sample
//
// import androidx.compose.ui.test.assertIsDisplayed
// import androidx.compose.ui.test.assertIsEnabled
// import androidx.compose.ui.test.assertIsNotDisplayed
// import androidx.compose.ui.test.assertIsNotEnabled
// import androidx.compose.ui.test.assertTextContains
// import androidx.compose.ui.test.hasTestTag
// import androidx.compose.ui.test.hasText
// import androidx.compose.ui.test.junit4.ComposeContentTestRule
// import androidx.compose.ui.test.junit4.createAndroidComposeRule
// import androidx.compose.ui.test.onAllNodesWithTag
// import androidx.compose.ui.test.onNodeWithContentDescription
// import androidx.compose.ui.test.onNodeWithTag
// import androidx.compose.ui.test.onNodeWithText
// import androidx.compose.ui.test.performClick
// import androidx.compose.ui.test.performImeAction
// import androidx.compose.ui.test.performScrollTo
// import androidx.compose.ui.test.performTextClearance
// import androidx.compose.ui.test.performTextInput
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import com.android.sample.ui.HomePage.HomeScreenTestTags
// import com.android.sample.ui.bookings.MyBookingsPageTestTag
// import com.android.sample.ui.components.LocationInputFieldTestTags
// import com.android.sample.ui.login.SignInScreenTestTags
// import com.android.sample.ui.newListing.NewListingScreenTestTag
// import com.android.sample.ui.profile.MyProfileScreenTestTag
// import com.android.sample.ui.signup.SignUpScreenTestTags
// import com.android.sample.ui.subject.SubjectListTestTags
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
//
//// Helpers (inspired by SignUpScreenTest)
//
// private const val DEFAULT_TIMEOUT_MS = 10_000L // Reduced from 30_000
//
// private fun waitForTag(
//    rule: ComposeContentTestRule,
//    tag: String,
//    timeoutMs: Long = DEFAULT_TIMEOUT_MS
// ) {
//  rule.waitUntil(timeoutMs) {
//    rule.onAllNodes(hasTestTag(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
//  }
// }
//
// private fun waitForText(
//    rule: ComposeContentTestRule,
//    tag: String,
//    timeoutMs: Long = DEFAULT_TIMEOUT_MS
// ) {
//  rule.waitUntil(timeoutMs) {
//    rule.onAllNodes(hasText(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
//  }
// }
//
// private fun ComposeContentTestRule.nodeByTag(tag: String) =
//    onNodeWithTag(tag, useUnmergedTree = false)
//
// private fun ComposeContentTestRule.nodeByText(text: String) =
//    onNodeWithText(text, useUnmergedTree = false)
//
// @RunWith(AndroidJUnit4::class)
// class EndToEndM2 {
//
//  @get:Rule val compose = createAndroidComposeRule<MainActivity>()
//
//  companion object {
//    private val TEST_PASSWORD = "testPassword123!"
//    private val TEST_DESC = "Happy"
//    private val TEST_DESC_APPEND = " Man"
//    private val TEST_DESC_FULL = "Happy Man"
//    private val TEST_TITLE = "Math Class"
//    private val TEST_EMAIL = "guillaume.lepinuuuuusu@epfl.ch"
//    private val TEST_NAME = "Lepin"
//    private val TEST_SURNAME = "Guillaume"
//    private val TEST_FULL_NAME = "Lepin Guillaume"
//    private val TEST_LOCATION = "London Street 1"
//    private val TEST_EDUCATION = "CS, 3rd year"
//    private val TEST_PROPOSAL = "PROPOSAL"
//    private val TEST_PROPOSAL_DESCRIPTION = "Learn math with me"
//    private val TEST_PROPOSAL_PRICE = "50"
//    private val TEST_PROPOSAL_SUBJECT = "ACADEMICS"
//    private val TEST_BACK_BUTTON = "Back"
//  }
//
//  @Test
//  fun userSignsInAndDiscoversApp() {
//
//    compose.waitForIdle()
//
//    // --------User Sign-Up, Sign-In and Profile Update Flow--------//
//
//    waitForTag(compose, SignInScreenTestTags.SIGN_IN_BUTTON)
//
//    // Create user
//    compose.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed().performClick()
//
//    waitForTag(compose, SignUpScreenTestTags.NAME)
//
//    // Fill sign-up form
//
//    compose
//        .onNodeWithTag(SignUpScreenTestTags.NAME)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_NAME)
//    compose
//        .onNodeWithTag(SignUpScreenTestTags.SURNAME)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_SURNAME)
//    compose
//      .onNodeWithTag(
//            LocationInputFieldTestTags.INPUT_LOCATION,
//            useUnmergedTree = true)
//        .performTextInput(TEST_LOCATION)
//    compose
//        .onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_EDUCATION)
//    compose
//        .onNodeWithTag(SignUpScreenTestTags.DESCRIPTION)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_DESC)
//
//    compose
//        .onNodeWithTag(SignUpScreenTestTags.EMAIL)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_EMAIL)
//
//    compose.waitUntil(timeoutMillis = 10000) {
//      compose
//          .onAllNodes(hasTestTag(SignUpScreenTestTags.PASSWORD))
//          .fetchSemanticsNodes()
//          .isNotEmpty()
//    }
//
//    compose
//        .onNodeWithTag(SignUpScreenTestTags.PASSWORD)
//        .performScrollTo()
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_PASSWORD)
//
//    compose.onNodeWithTag(SignUpScreenTestTags.PASSWORD).performImeAction()
//
//    compose.waitForIdle()
//
//    compose.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()
//    compose.waitForIdle()
//    // Wait for navigation to home screen
//
//    compose.onNodeWithContentDescription(TEST_BACK_BUTTON).performClick()
//    waitForTag(compose, SignInScreenTestTags.SIGN_IN_BUTTON)
//
//    // Now sign in with the created user
//    compose
//        .onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_EMAIL)
//
//    compose
//        .onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_PASSWORD)
//
//    compose.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsEnabled().performClick()
//
//    // Verify navigation to home screen
//    waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)
//    compose.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
//
//    // Go to my profile
//    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed().performClick()
//
//    waitForTag(compose, MyProfileScreenTestTag.PROFILE_ICON)
//    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
//
//    waitForTag(compose, MyProfileScreenTestTag.INPUT_PROFILE_NAME)
//    waitForText(compose, TEST_FULL_NAME)
//
//    compose
//        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
//        .assertIsDisplayed()
//        .assertTextContains(TEST_FULL_NAME)
//
//    compose
//        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
//        .assertIsDisplayed()
//        .assertTextContains(TEST_DESC)
//
//    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsNotEnabled()
//
//    compose
//        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_DESC_APPEND)
//
//    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsEnabled().performClick()
//
//    waitForText(compose, TEST_DESC_FULL)
//    compose
//        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
//        .assertIsDisplayed()
//        .assertTextContains(TEST_DESC_FULL)
//    compose
//        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
//        .performClick()
//        .performTextClearance()
//    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput(TEST_DESC)
//
//    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsEnabled().performClick()
//
//    waitForText(compose, TEST_DESC)
//
//    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed().performClick()
//
//    waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)
//
//    // --------End of User Sign-Up, Sign-In and Profile Update Flow--------//
//
//    // --------User Discovers the Home Page of the app and creates a new listing--------//
//
//    compose.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed().performClick()
//
//    waitForTag(compose, NewListingScreenTestTag.INPUT_COURSE_TITLE)
//
//
// compose.onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD).assertIsDisplayed().performClick()
//    compose.onNodeWithText(TEST_PROPOSAL).assertIsDisplayed().performClick()
//
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD)
//        .assertTextContains(TEST_PROPOSAL)
//
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.INPUT_COURSE_TITLE)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_TITLE)
//
//
// compose.onNodeWithTag(NewListingScreenTestTag.INPUT_COURSE_TITLE).assertTextContains(TEST_TITLE)
//
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.INPUT_DESCRIPTION)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_PROPOSAL_DESCRIPTION)
//
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.INPUT_DESCRIPTION)
//        .assertTextContains(TEST_PROPOSAL_DESCRIPTION)
//
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.INPUT_PRICE)
//        .assertIsDisplayed()
//        .performClick()
//        .performTextInput(TEST_PROPOSAL_PRICE)
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.INPUT_PRICE)
//        .assertTextContains(TEST_PROPOSAL_PRICE)
//
//    compose.onNodeWithTag(NewListingScreenTestTag.SUBJECT_FIELD).performClick()
//
//    compose.onNodeWithText(TEST_PROPOSAL_SUBJECT).performClick()
//    compose
//        .onNodeWithTag(NewListingScreenTestTag.SUBJECT_FIELD)
//        .assertTextContains(TEST_PROPOSAL_SUBJECT)
//
//    compose.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD).performClick()
//
//    compose.onNodeWithContentDescription(TEST_BACK_BUTTON).assertIsDisplayed().performClick()
//
//    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed().performClick()
//    waitForTag(compose, MyProfileScreenTestTag.PROFILE_ICON)
//    compose.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_TAB).assertIsDisplayed().performClick()
//    waitForTag(compose, MyProfileScreenTestTag.LISTINGS_SECTION)
//    compose.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_SECTION).assertIsDisplayed()
//    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()
//    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_SECTION).assertIsDisplayed()
//
//    // Go back to home page
//    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed().performClick()
//
//    compose.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)[0].assertIsDisplayed().performClick()
//    waitForTag(compose, SubjectListTestTags.CATEGORY_SELECTOR)
//    compose.onNodeWithTag(SubjectListTestTags.LISTING_CARD).assertIsNotDisplayed()
//
//    // User goes to bookings
//    compose.onNodeWithContentDescription(TEST_BACK_BUTTON).assertIsDisplayed().performClick()
//    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertIsDisplayed().performClick()
//    waitForTag(compose, MyBookingsPageTestTag.EMPTY)
//    compose.onNodeWithTag(MyBookingsPageTestTag.EMPTY).assertIsDisplayed()
//  }
// }
