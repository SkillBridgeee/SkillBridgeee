package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.utils.AppTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyProfileScreenTestAppTest : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateAppContent() }
    composeTestRule.navigateToMyProfile()
  }

  @Test
  fun testGoodScreen() {
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST).assertIsDisplayed()
  }
}
