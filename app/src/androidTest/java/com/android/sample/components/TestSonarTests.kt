package com.android.sample.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.profile.CoveragePreviewContainer
import org.junit.Rule
import org.junit.Test

class CoverageDebugSectionTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun coverageDebugSection_initialState() {
    composeTestRule.setContent { CoveragePreviewContainer() }

    composeTestRule.onNodeWithText("Counter: 0").assertExists()
    composeTestRule.onNodeWithText("Zero state").assertExists()
    composeTestRule.onNodeWithText("Message: Idle").assertExists()
  }

  @Test
  fun coverageDebugSection_incrementButton_changesState() {
    composeTestRule.setContent { CoveragePreviewContainer() }

    composeTestRule.onNodeWithText("Increment").performClick()
    composeTestRule.onNodeWithText("Counter: 1").assertExists()
    composeTestRule.onNodeWithText("Low range").assertExists()
    composeTestRule.onNodeWithText("Message: Odd").assertExists()

    composeTestRule.onNodeWithText("Increment").performClick()
    composeTestRule.onNodeWithText("Counter: 2").assertExists()
    composeTestRule.onNodeWithText("Message: Even").assertExists()
  }

  @Test
  fun coverageDebugSection_resetButton_resetsState() {
    composeTestRule.setContent { CoveragePreviewContainer() }

    composeTestRule.onNodeWithText("Increment").performClick()
    composeTestRule.onNodeWithText("Reset").performClick()

    composeTestRule.onNodeWithText("Counter: 0").assertExists()
    composeTestRule.onNodeWithText("Zero state").assertExists()
    composeTestRule.onNodeWithText("Message: Reset").assertExists()
  }

  @Test
  fun coverageDebugSection_switch_togglesState() {
    composeTestRule.setContent { CoveragePreviewContainer() }

    val switch = composeTestRule.onAllNodes(isToggleable()).onFirst()

    switch.performClick()
    composeTestRule.onNodeWithText("Message: Disabled").assertExists()

    switch.performClick()
    composeTestRule.onNodeWithText("Message: Enabled").assertExists()
  }

  @Test
  fun coverageDebugSection_highRangeBranch() {
    composeTestRule.setContent { CoveragePreviewContainer() }

    repeat(6) { composeTestRule.onNodeWithText("Increment").performClick() }

    composeTestRule.onNodeWithText("High range").assertExists()
    composeTestRule.onNodeWithText("Counter: 6").assertExists()
  }
}
