package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.utils.AppTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenTestFUN : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateAppContent() }
  }

  @Test
  fun testBottomComponentExists() {
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_HOME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_MAP).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).assertIsDisplayed()
  }

  @Test
  fun testWelcomeSection() {
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Welcome back, ${profileRepository.getCurrentUserName()}!")
        .assertIsDisplayed()
  }

  @Test
  fun testExploreSkill() {
    composeTestRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.ALL_SUBJECT_LIST).assertIsDisplayed()

    // Scroll the list
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.ALL_SUBJECT_LIST)
        .performScrollToIndex(MainSubject.entries.size - 1)

    // Check if last MainSubject is displayed
    composeTestRule.onNodeWithText(MainSubject.entries[6].name).assertIsDisplayed()
  }
}
