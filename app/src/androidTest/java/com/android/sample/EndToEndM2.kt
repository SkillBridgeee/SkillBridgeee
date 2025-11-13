package com.android.sample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.FirestoreProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.newListing.NewSkillScreenTestTag
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.ui.subject.SubjectListTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Helpers (inspired by SignUpScreenTest)

private const val DEFAULT_TIMEOUT_MS = 10_000L // Reduced from 30_000

private fun waitForTag(
    rule: ComposeContentTestRule,
    tag: String,
    timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
  rule.waitUntil(timeoutMs) {
    rule.onAllNodes(hasTestTag(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
  }
}

private fun waitForText(
    rule: ComposeContentTestRule,
    tag: String,
    timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
  rule.waitUntil(timeoutMs) {
    rule.onAllNodes(hasText(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
  }
}

private fun ComposeContentTestRule.nodeByTag(tag: String) =
    onNodeWithTag(tag, useUnmergedTree = false)

private fun ComposeContentTestRule.nodeByText(text: String) =
    onNodeWithText(text, useUnmergedTree = false)

@RunWith(AndroidJUnit4::class)
class EndToEndM2 {


    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

  @Test
  fun userSignsInAndDiscoversApp() {

    //--------User Sign-Up, Sign-In and Profile Update Flow--------//
    val testEmail = "guillaume.lepinuus@epfl.ch"
    val testPassword = "testPassword123!"

    waitForTag(compose, SignInScreenTestTags.SIGN_IN_BUTTON)

    // Create user
    compose.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed().performClick()

    waitForTag(compose, SignUpScreenTestTags.NAME)

    // Fill sign-up form

    compose
        .onNodeWithTag(SignUpScreenTestTags.NAME)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("Lepin")
    compose
        .onNodeWithTag(SignUpScreenTestTags.SURNAME)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("Guillaume")
    compose
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("London Street 1")
    compose
        .onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("CS, 3rd year")
    compose
        .onNodeWithTag(SignUpScreenTestTags.DESCRIPTION)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("Gay")

    compose
        .onNodeWithTag(SignUpScreenTestTags.EMAIL)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(testEmail)
    compose
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(testPassword)

    compose.onNodeWithTag(SignUpScreenTestTags.PASSWORD).performImeAction()

    compose.waitForIdle()

    compose.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    compose.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    // Wait for navigation to home screen

    compose.onNodeWithContentDescription("Back").performClick()
    waitForTag(compose, SignInScreenTestTags.SIGN_IN_BUTTON)

    // Now sign in with the created user
    compose
        .onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(testEmail)

    compose
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(testPassword)

    compose.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsEnabled().performClick()

    // Verify navigation to home screen
    waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)
    compose.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()

    // Go to my profile
    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed().performClick()

    waitForTag(compose, MyProfileScreenTestTag.PROFILE_ICON)
    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()

    waitForTag(compose, MyProfileScreenTestTag.INPUT_PROFILE_NAME)
    waitForText(compose, "Lepin Guillaume")

    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
        .assertIsDisplayed()
        .assertTextContains("Lepin Guillaume")

    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .assertIsDisplayed()
        .assertTextContains("Gay")

    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsNotEnabled()

    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(" Man")

    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsEnabled().performClick()

    waitForText(compose, "Gay Man")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .assertIsDisplayed()
        .assertTextContains("Gay Man")
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .performClick()
        .performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .performTextInput("Gay")

      compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON)
          .assertIsEnabled()
          .performClick()

    waitForText(compose, "Gay")

    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed().performClick()

    waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)

    //--------End of User Sign-Up, Sign-In and Profile Update Flow--------//

    //--------User Discovers the Home Page of the app and creates a new listing--------//

    compose.onNodeWithTag(HomeScreenTestTags.FAB_ADD)
        .assertIsDisplayed()
        .performClick()

    waitForTag(compose, NewSkillScreenTestTag.INPUT_COURSE_TITLE)

    compose.onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
        .assertIsDisplayed()
        .performClick()
    compose.onNodeWithText("PROPOSAL")
        .assertIsDisplayed()
        .performClick()

    compose.onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
        .assertTextContains("PROPOSAL")

    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("Math Class")

    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .assertTextContains("Math Class")

    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("Learn math with me")

    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .assertTextContains("Learn math with me")

    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE)
        .assertIsDisplayed()
        .performClick()
        .performTextInput("50")
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE)
        .assertTextContains("50")

    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD)
        .performClick()

    compose.onNodeWithText("ACADEMICS").performClick()
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD)
        .assertTextContains("ACADEMICS")

    compose.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD)
        .performClick()

    compose.onNodeWithText("MATHEMATICS").performClick()

    compose.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD)
        .assertTextContains("MATHEMATICS")


    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION)
        .performClick()
        .performTextInput("epfl")



    waitForText(compose, "EPFL")
    compose.onNodeWithText("EPFL")
        .assertIsDisplayed()
        .performClick()



      compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION)
          .assertTextContains("EPFL")

    compose.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL)
        .performClick()



    waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)
    compose.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()

      compose.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)[0]
          .assertIsDisplayed()
          .performClick()

    waitForText(compose, "Learn math with me")

    compose.onAllNodesWithText("Learn math with me")[0]
        .assertIsDisplayed()

    compose.onNodeWithTag(SubjectListTestTags.CATEGORY_SELECTOR)
        .performClick()

    compose.onNodeWithText("Chemistry")
        .assertIsDisplayed()
        .performClick()

      compose.onNodeWithText("Learn math with me")
          .assertIsNotDisplayed()

      compose.onNodeWithTag(SubjectListTestTags.CATEGORY_SELECTOR)
          .performClick()

      compose.onNodeWithText("All")
            .assertIsDisplayed()
            .performClick()

      compose.onAllNodesWithText("Learn math with me")[0]
          .assertIsDisplayed()


  }
}
