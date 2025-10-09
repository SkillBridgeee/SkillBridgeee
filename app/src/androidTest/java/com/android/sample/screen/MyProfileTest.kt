package com.android.sample.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.utils.AppTest
import org.junit.Rule
import org.junit.Test

class MyProfileTest : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun headerTitle_isDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.HEADER_TITLE).assertIsDisplayed()
  }

  @Test
  fun profileIcon_isDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
  }

  @Test
  fun nameDisplay_isDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY).assertIsDisplayed()
  }

  @Test
  fun roleBadge_isDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE).assertIsDisplayed()
  }

  @Test
  fun cardTitle_isDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.CARD_TITLE).assertIsDisplayed()
  }

  @Test
  fun inputFields_areDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_BIO).assertIsDisplayed()
  }

  @Test
  fun saveButton_isDisplayed() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun nameField_acceptsInput_andNoError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    val testName = "John Doe"
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_NAME, testName)
    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
        .assertTextContains(testName)
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG).assertIsNotDisplayed()
  }

  @Test
  fun emailField_acceptsInput_andNoError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    val testEmail = "john.doe@email.com"
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL, testEmail)
    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .assertTextContains(testEmail)
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG).assertIsNotDisplayed()
  }

  @Test
  fun locationField_acceptsInput_andNoError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    val testLocation = "Paris"
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_LOCATION, testLocation)
    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_LOCATION)
        .assertTextContains(testLocation)
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG).assertIsNotDisplayed()
  }

  @Test
  fun bioField_acceptsInput_andNoError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    val testBio = "Développeur Android"
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_BIO, testBio)
    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_BIO)
        .assertTextContains(testBio)
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG).assertIsNotDisplayed()
  }

  @Test
  fun nameField_empty_showsError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_NAME, "")
    composeTestRule
        .onNodeWithTag(testTag = MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun emailField_empty_showsError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL, "")
    composeTestRule
        .onNodeWithTag(testTag = MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun emailField_invalidEmail_showsError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL, "John")
    composeTestRule
        .onNodeWithTag(testTag = MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun locationField_empty_showsError() {
    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_LOCATION, "")
    composeTestRule
        .onNodeWithTag(testTag = MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  //  @Test
  //  fun bioField_empty_showsError() {
  //    composeTestRule.setContent { MyProfileScreen(profileId = "test") }
  //    composeTestRule.enterText(MyProfileScreenTestTag.INPUT_PROFILE_BIO, "")
  //    composeTestRule
  //        .onNodeWithTag(testTag = MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //  }
}
