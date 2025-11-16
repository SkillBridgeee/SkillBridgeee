package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.SkillsHelper
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.utils.AppTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewListingScreenTestFUN : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateEveryThing() }
    composeTestRule.navigateToNewListing()
  }

  @Test
  fun testAllComponentsAreDisplayedAndErrorMsg() {
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_PRICE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_LOCATION_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD).assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.BUTTON_USE_MY_LOCATION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()

    // CLick on Save button
    composeTestRule.clickOn(NewListingScreenTestTag.BUTTON_SAVE_LISTING)

    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_LOCATION_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testChooseSubject() {

    val mainSubjectChoose = 0

    // CLick choose subject
    composeTestRule.clickOn(NewListingScreenTestTag.SUBJECT_FIELD)
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()

    // Check if all subjects are displayed
    for (i in 0 until MainSubject.entries.size) {
      composeTestRule
          .onNodeWithTag("${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_$i")
          .assertIsDisplayed()
    }

    // Click on the choose Subject
    composeTestRule.clickOn(
        "${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_$mainSubjectChoose")
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.SUBJECT_FIELD)
        .assertTextContains(MainSubject.entries[mainSubjectChoose].name)

    // Check subSubject
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD).assertIsDisplayed()

    composeTestRule.clickOn(NewListingScreenTestTag.SUB_SKILL_FIELD)
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_DROPDOWN).assertIsDisplayed()

    // Check if all subjects are displayed
    for (i in
        0 until SkillsHelper.getSkillsForSubject(MainSubject.entries[mainSubjectChoose]).size) {
      composeTestRule
          .onNodeWithTag("${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_$i")
          .assertIsDisplayed()
    }

    composeTestRule.clickOn("${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_0")
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD)
        .assertTextContains(
            SkillsHelper.getSkillsForSubject(MainSubject.entries[mainSubjectChoose])[0].name)
  }

  //  @Test
  //  fun testTextInput() {
  //    composeTestRule
  //      .enterText(NewListingScreenTestTag.CREATE_LESSONS_TITLE, "Piano Lessons")
  //
  //    composeTestRule
  //      .enterText(NewListingScreenTestTag.CREATE_LESSONS_TITLE, "Piano Lessons")
  //
  //    composeTestRule
  //      .enterText(NewListingScreenTestTag.CREATE_LESSONS_TITLE, "Piano Lessons")
  //
  //    composeTestRule
  //      .enterText(NewListingScreenTestTag.CREATE_LESSONS_TITLE, "Piano Lessons")
  //
  //    composeTestRule
  //      .enterText(NewListingScreenTestTag.CREATE_LESSONS_TITLE, "Piano Lessons")
  //  }
}
