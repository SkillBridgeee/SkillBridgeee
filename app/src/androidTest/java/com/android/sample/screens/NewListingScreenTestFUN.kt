package com.android.sample.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
  fun testCi() {
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD)
  }

  //  @Test
  //  fun testAllComponentsAreDisplayedAndErrorMsg() {
  //    // Check all components
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD).assertIsDisplayed()
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).assertIsDisplayed()
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
  //    composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
  //    composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_PRICE).assertIsDisplayed()
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.INPUT_LOCATION_FIELD).assertIsDisplayed()
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD).assertIsNotDisplayed()
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.BUTTON_USE_MY_LOCATION)
  //        .assertIsDisplayed()
  //    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
  //
  //    // CLick on Save button
  //    composeTestRule.clickOn(NewListingScreenTestTag.BUTTON_SAVE_LISTING)
  //
  //    composeTestRule.waitForIdle()
  //
  //    // Test Error msg
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.INVALID_LISTING_TYPE_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.INVALID_LOCATION_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
  //        .assertIsDisplayed()
  //  }
  //
  //  @Test
  //  fun testChooseSubjectListingTypeAndLocation() {
  //
  //    ////// Subject
  //    val mainSubjectChoose = 0
  //
  //    // CLick choose subject
  //    composeTestRule.clickOn(NewListingScreenTestTag.SUBJECT_FIELD)
  //    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()
  //
  //    // Check if all subjects are displayed
  //    for (i in 0 until MainSubject.entries.size) {
  //      composeTestRule
  //          .onNodeWithTag("${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_$i")
  //          .assertIsDisplayed()
  //    }
  //
  //    // Click on the choose Subject
  //    composeTestRule.clickOn(
  //        "${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_$mainSubjectChoose")
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.SUBJECT_FIELD)
  //        .assertTextContains(MainSubject.entries[mainSubjectChoose].name)
  //
  //    // Check subSubject
  //    composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD).assertIsDisplayed()
  //
  //    composeTestRule.clickOn(NewListingScreenTestTag.SUB_SKILL_FIELD)
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_DROPDOWN).assertIsDisplayed()
  //
  //    // Check if all subjects are displayed
  //    for (i in
  //        0 until SkillsHelper.getSkillsForSubject(MainSubject.entries[mainSubjectChoose]).size) {
  //      composeTestRule
  //          .onNodeWithTag("${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_$i")
  //          .assertIsDisplayed()
  //    }
  //
  //    composeTestRule.clickOn("${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_0")
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.SUB_SKILL_FIELD)
  //        .assertTextContains(
  //            SkillsHelper.getSkillsForSubject(MainSubject.entries[mainSubjectChoose])[0].name)
  //
  //    //////  Listing Type
  //    composeTestRule.clickOn(NewListingScreenTestTag.LISTING_TYPE_FIELD)
  //
  // composeTestRule.onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_DROPDOWN).assertIsDisplayed()
  //
  //    // Check if all subjects are displayed
  //    for (i in 0 until ListingType.entries.size) {
  //      composeTestRule
  //          .onNodeWithTag("${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_$i")
  //          .assertIsDisplayed()
  //    }
  //    composeTestRule.clickOn("${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_0")
  //    composeTestRule
  //        .onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD)
  //        .assertTextContains(ListingType.entries[0].name)
  //
  //    ////// Location
  //
  //    composeTestRule
  //        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
  //        .performTextInput("Pari")
  //
  //    composeTestRule.waitUntil(timeoutMillis = 20_000) {
  //      composeTestRule
  //          .onAllNodesWithTag(LocationInputFieldTestTags.SUGGESTION, useUnmergedTree = true)
  //          .fetchSemanticsNodes()
  //          .isNotEmpty()
  //    }
  //
  //    composeTestRule.waitForIdle()
  //
  //    composeTestRule
  //        .onAllNodesWithTag(LocationInputFieldTestTags.SUGGESTION)
  //        .filter(hasText("Paris"))
  //        .onFirst()
  //        .performClick()
  //
  //    // composeTestRule.waitForIdle()
  //
  //    composeTestRule
  //        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
  //        .assertTextContains("Paris")
  //  }
  //
  //  @Test
  //  fun testTextInput() {
  //
  //    val numMainSub = 0
  //    val mainSub = MainSubject.entries[numMainSub]
  //
  //    val numSubSkill = 0
  //    // Enter Title
  //    composeTestRule.enterText(NewListingScreenTestTag.INPUT_COURSE_TITLE, "Piano Lessons")
  //
  //    // Enter Desc
  //    composeTestRule.enterText(NewListingScreenTestTag.INPUT_DESCRIPTION, "Description")
  //
  //    // Enter Price
  //    composeTestRule.enterText(NewListingScreenTestTag.INPUT_PRICE, "12")
  //
  //    // Choose ListingType
  //    composeTestRule.multipleChooseExposeMenu(
  //        NewListingScreenTestTag.LISTING_TYPE_FIELD,
  //        "${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_$numMainSub")
  //
  //    // Choose Main subject
  //    composeTestRule.multipleChooseExposeMenu(
  //        NewListingScreenTestTag.SUBJECT_FIELD,
  //        "${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_0")
  //
  //    // Choose sub skill
  //    composeTestRule.multipleChooseExposeMenu(
  //        NewListingScreenTestTag.SUB_SKILL_FIELD,
  //        "${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_$numSubSkill")
  //
  //    // Enter Location
  //    composeTestRule.enterAndChooseLocation(
  //        enterText = "Pari",
  //        selectText = "Paris",
  //        inputLocationTestTag = LocationInputFieldTestTags.INPUT_LOCATION)
  //
  //    composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).performClick()
  //
  //    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
  //  }
}
