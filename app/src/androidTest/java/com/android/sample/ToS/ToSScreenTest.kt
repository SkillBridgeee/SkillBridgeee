package com.android.sample.tos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.android.sample.ui.tos.ToSScreen
import com.android.sample.ui.tos.ToSTestTags
import org.junit.Rule
import org.junit.Test

class ToSScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun tosScreen_isDisplayed() {
    // Given: Setup the ToS screen
    composeTestRule.setContent { ToSScreen(onAcceptanceComplete = {}, onDecline = {}) }

    // Then: Verify the main screen container is displayed
    composeTestRule.onNodeWithTag(ToSTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun tosScreen_headerIsDisplayed() {
    // Given: Setup the ToS screen
    composeTestRule.setContent { ToSScreen(onAcceptanceComplete = {}, onDecline = {}) }

    // Then: Verify the header title is displayed
    composeTestRule.onNodeWithText("Terms of Service").assertIsDisplayed()
  }

  @Test
  fun tosScreen_subheaderIsDisplayed() {
    // Given: Setup the ToS screen
    composeTestRule.setContent { ToSScreen(onAcceptanceComplete = {}, onDecline = {}) }

    // Then: Verify the subheader text is displayed
    composeTestRule
        .onNodeWithText("Please read and accept our Terms of Service to continue")
        .assertIsDisplayed()
  }

  @Test
  fun tosScreen_allSectionTitlesAreDisplayed() {
    // Given: Setup the ToS screen
    composeTestRule.setContent { ToSScreen(onAcceptanceComplete = {}, onDecline = {}) }

    // Then: Verify all section titles are displayed
    val sectionTitles =
        listOf(
            "1. User Responsibilities",
            "2. Intellectual Property Rights",
            "3. Limitation of Liability",
            "4. Modification of Terms",
            "5. Dispute Resolution")

    sectionTitles.forEach { title ->
      // First scroll to make sure the element is visible
      composeTestRule.onNodeWithText(title).performScrollTo().assertIsDisplayed()
    }
  }

  @Test
  fun tosScreen_sectionContentIsDisplayed() {
    // Given: Setup the ToS screen
    composeTestRule.setContent { ToSScreen(onAcceptanceComplete = {}, onDecline = {}) }

    // Then: Verify some key content text is displayed
    val contentSnippets =
        listOf(
            "By using this application, you agree to abide",
            "All content included in this application is the property",
            "In no case shall SkillBridge",
            "SkillBridge reserves the right to modify",
            "Any disputes arising from these Terms")

    contentSnippets.forEach { snippet ->
      // Scroll to each content snippet before asserting visibility
      composeTestRule
          .onNodeWithText(snippet, substring = true)
          .performScrollTo()
          .assertIsDisplayed()
    }
  }

  @Test
  fun tosScreen_errorTextIsNotDisplayedByDefault() {
    // Given: Setup the ToS screen (error is hardcoded to false)
    composeTestRule.setContent { ToSScreen(onAcceptanceComplete = {}, onDecline = {}) }

    // Then: Verify error text is NOT displayed (since condition is false)
    composeTestRule.onNodeWithTag(ToSTestTags.ERROR_TEXT).assertDoesNotExist()
  }
}
