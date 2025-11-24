package com.android.sample.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.components.HORIZONTAL_SCROLL_HINT_BOX_TAG
import com.android.sample.ui.components.HORIZONTAL_SCROLL_HINT_ICON_TAG
import com.android.sample.ui.components.HorizontalScrollHint
import org.junit.Rule
import org.junit.Test

class HorizontalScrollHintTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun horizontalScrollHint_visible_showsBoxAndArrow() {
    composeTestRule.setContent { MaterialTheme { HorizontalScrollHint(visible = true) } }

    composeTestRule.onNodeWithTag(HORIZONTAL_SCROLL_HINT_BOX_TAG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HORIZONTAL_SCROLL_HINT_ICON_TAG).assertIsDisplayed()
  }

  @Test
  fun horizontalScrollHint_notVisible_hidesBoxAndArrow() {
    composeTestRule.setContent { MaterialTheme { HorizontalScrollHint(visible = false) } }

    composeTestRule.onNodeWithTag(HORIZONTAL_SCROLL_HINT_BOX_TAG).assertDoesNotExist()
    composeTestRule.onNodeWithTag(HORIZONTAL_SCROLL_HINT_ICON_TAG).assertDoesNotExist()
  }
}
