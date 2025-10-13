package com.android.sample.utils

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.After
import org.junit.Before

abstract class AppTest() {

  @Before open fun setUp() {}

  @After open fun tearDown() {}

  fun ComposeTestRule.enterText(testTag: String, text: String) {
    onNodeWithTag(testTag).performTextClearance()
    onNodeWithTag(testTag).performTextInput(text)
  }
}
