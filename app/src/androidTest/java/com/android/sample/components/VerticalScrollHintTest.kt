package com.android.sample.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.components.VERTICAL_SCROLL_HINT_BOX_TAG
import com.android.sample.ui.components.VERTICAL_SCROLL_HINT_ICON_TAG
import com.android.sample.ui.components.VerticalScrollHint
import org.junit.Rule
import org.junit.Test

class VerticalScrollHintTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun verticalScrollHint_visible_showsBoxAndArrow() {
    composeTestRule.setContent { MaterialTheme { VerticalScrollHint(visible = true) } }

    composeTestRule.onNodeWithTag(VERTICAL_SCROLL_HINT_BOX_TAG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(VERTICAL_SCROLL_HINT_ICON_TAG).assertIsDisplayed()
  }

  @Test
  fun verticalScrollHint_notVisible_hidesBoxAndArrow() {
    composeTestRule.setContent { MaterialTheme { VerticalScrollHint(visible = false) } }

    composeTestRule.onNodeWithTag(VERTICAL_SCROLL_HINT_BOX_TAG).assertDoesNotExist()
    composeTestRule.onNodeWithTag(VERTICAL_SCROLL_HINT_ICON_TAG).assertDoesNotExist()
  }
}
