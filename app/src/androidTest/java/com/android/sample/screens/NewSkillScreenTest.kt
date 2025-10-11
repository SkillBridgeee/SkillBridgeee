package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.sample.ui.screens.newSkill.NewSkillScreen
import com.android.sample.ui.screens.newSkill.NewSkillScreenTestTag
import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import org.junit.Rule
import org.junit.Test

class NewSkillScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topAppBarTitle_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.TOP_APP_BAR_TITLE).assertIsDisplayed()
  }

  @Test
  fun navBackButton_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.NAV_BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createLessonsTitle_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
  }

  @Test
  fun inputCourseTitle_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
  }

  @Test
  fun inputDescription_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
  }

  @Test
  fun inputPrice_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertIsDisplayed()
  }

  @Test
  fun subjectField_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
  }

  @Test
  fun subjectDropdown_showsItems_whenClicked() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()
    // le premier item (les items partagent le même tag) doit être visible
    composeTestRule
        .onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun titleField_acceptsInput_andNoError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    val testTitle = "Cours Kotlin"
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .performTextInput(testTitle)
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .assertTextContains(testTitle)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_TITLE_MSG).assertIsNotDisplayed()
  }

  @Test
  fun descriptionField_acceptsInput_andNoError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    val testDesc = "Description du cours"
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .performTextInput(testDesc)
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .assertTextContains(testDesc)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_DESC_MSG).assertIsNotDisplayed()
  }

  @Test
  fun priceField_acceptsInput_andNoError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    val testPrice = "25"
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput(testPrice)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertTextContains(testPrice)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG).assertIsNotDisplayed()
  }

  @Test
  fun titleField_empty_showsError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput(" ")
    composeTestRule
        .onNodeWithTag(testTag = NewSkillScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun descriptionField_empty_showsError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextClearance()
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput(" ")
    composeTestRule
        .onNodeWithTag(testTag = NewSkillScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun priceField_invalid_showsError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("abc")
    composeTestRule
        .onNodeWithTag(testTag = NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun setError_showsAllFieldErrors() {
    val vm = NewSkillViewModel()
    composeTestRule.setContent { NewSkillScreen(skillViewModel = vm, profileId = "test") }

    composeTestRule.runOnIdle { vm.setError() }

    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
