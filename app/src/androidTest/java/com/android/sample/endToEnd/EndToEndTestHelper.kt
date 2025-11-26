package com.android.sample.utils

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.listing.Listing
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.components.TopAppBarTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.ui.signup.SignUpScreenTestTags

@OptIn(ExperimentalTestApi::class)
class EndToEndTestHelper(private val composeTestRule: ComposeTestRule) {

  fun navigateToNewListing() {
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).performClick()
  }

  fun navigateToMyProfile() {
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).performClick()
  }

  fun navigateToMyBookings() {
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).performClick()
  }

  fun navigateToMap() {
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_MAP).performClick()
  }

  fun clickTopAppBarBack() {
    composeTestRule.onNodeWithTag(TopAppBarTestTags.BACK_BUTTON).performClick()
  }

  private fun enterText(testTag: String, text: String) {
    composeTestRule.onNodeWithTag(testTag).performClick().performTextInput(text)
  }

  private fun clickOn(testTag: String) {
    composeTestRule.onNodeWithTag(testTag = testTag).performClick()
  }

  private fun scrollAndEnterText(testTag: String, text: String) {
    composeTestRule.onNodeWithTag(testTag).performScrollTo().performClick().performTextInput(text)
  }

  private fun scrollAndClickOn(
      clickTag: String,
      scrollToTag: String? = null,
      useContentDesc: Boolean = false
  ) {
    if (scrollToTag != null) {
      composeTestRule.onNodeWithTag(scrollToTag).performScrollTo()
    }

    if (useContentDesc) {
      composeTestRule.onNodeWithContentDescription(clickTag).performClick()
    } else {
      composeTestRule.onNodeWithTag(clickTag).performScrollTo().performClick()
    }
  }

  private fun multipleChooseExposeMenu(multipleTestTag: String, differentChoiceTestTag: String) {
    composeTestRule.onNodeWithTag(multipleTestTag).performClick()
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(differentChoiceTestTag, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(differentChoiceTestTag).performClick()
  }

  fun fillNewListing(newListing: Listing) {
    enterText(NewListingScreenTestTag.INPUT_COURSE_TITLE, newListing.title)
    enterText(NewListingScreenTestTag.INPUT_DESCRIPTION, newListing.description)
    enterText(NewListingScreenTestTag.INPUT_PRICE, newListing.hourlyRate.toString())

    multipleChooseExposeMenu(
        NewListingScreenTestTag.LISTING_TYPE_FIELD,
        "${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_${newListing.type.ordinal}")

    scrollAndClickOn(
        clickTag = NewListingScreenTestTag.BUTTON_USE_MY_LOCATION,
        scrollToTag = NewListingScreenTestTag.INPUT_LOCATION_FIELD)
    composeTestRule.waitForIdle()

    multipleChooseExposeMenu(
        NewListingScreenTestTag.SUBJECT_FIELD,
        "${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_${newListing.skill.mainSubject.ordinal}")

    multipleChooseExposeMenu(
        NewListingScreenTestTag.SUB_SKILL_FIELD,
        "${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_0")
  }

  fun signUpNewUser(
      name: String,
      surname: String,
      address: String,
      levelOfEducation: String,
      description: String,
      email: String,
      password: String
  ) {
    clickOn(SignInScreenTestTags.SIGNUP_LINK)
    composeTestRule.waitForIdle()

    scrollAndEnterText(SignUpScreenTestTags.NAME, name)
    scrollAndEnterText(SignUpScreenTestTags.SURNAME, surname)
    scrollAndClickOn(
        clickTag = SignUpScreenTestTags.PIN_CONTENT_DESC,
        scrollToTag = SignUpScreenTestTags.ADDRESS,
        useContentDesc = true)
    composeTestRule.waitForIdle()
    scrollAndEnterText(SignUpScreenTestTags.LEVEL_OF_EDUCATION, levelOfEducation)
    scrollAndEnterText(SignUpScreenTestTags.DESCRIPTION, description)
    scrollAndEnterText(SignUpScreenTestTags.EMAIL, email)
    scrollAndEnterText(SignUpScreenTestTags.PASSWORD, password)

    scrollAndClickOn(SignUpScreenTestTags.SIGN_UP)
    composeTestRule.waitForIdle()
  }

  fun loginUser(email: String, password: String) {
    composeTestRule.waitUntil {
      composeTestRule
          .onAllNodesWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertExists()

    enterText(SignInScreenTestTags.EMAIL_INPUT, email)
    enterText(SignInScreenTestTags.PASSWORD_INPUT, password)

    clickOn(SignInScreenTestTags.SIGN_IN_BUTTON)
    composeTestRule.waitForIdle()
  }

  fun signUpAndLogin(
      name: String,
      surname: String,
      address: String,
      levelOfEducation: String,
      description: String,
      email: String,
      password: String
  ) {
    signUpNewUser(name, surname, address, levelOfEducation, description, email, password)
    composeTestRule.waitForIdle()

    if (composeTestRule
        .onAllNodesWithTag(SignUpScreenTestTags.TITLE)
        .fetchSemanticsNodes()
        .isNotEmpty()) {
      composeTestRule.onNodeWithTag(SignUpScreenTestTags.NAME).performClick()
      clickTopAppBarBack()
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertExists()

    loginUser(email, password)
  }
}
