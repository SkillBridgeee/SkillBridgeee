package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsHelper
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.components.LocationInputFieldTestTags
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
    composeTestRule.setContent { CreateAppContent() }
    composeTestRule.navigateToNewListing()
  }

  @Test
  fun testAllComponentsAreDisplayedAndErrorMsg() {

    // Check all components
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD).assertIsDisplayed()
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

    /////// ERROR MESSAGE CHECK
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).performClick()

    // (for CI)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(NewListingScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_LISTING_TYPE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .assertIsDisplayed()

    // Scroll down
    composeTestRule
        .onNodeWithText(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .performScrollTo()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testCI5() {
    // Important en CI :
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).performClick()

    composeTestRule
        .onNodeWithText(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .performScrollTo()

    composeTestRule.waitForIdle()
    // --- WAIT FOR VALIDATION ERRORS ---
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // --- ASSERT ERRORS ---
    composeTestRule
        .onNodeWithText(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testChooseSubjectListingTypeAndLocation() {

    ////// Subject
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

    //////  Listing Type
    composeTestRule.clickOn(NewListingScreenTestTag.LISTING_TYPE_FIELD)

    composeTestRule.onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_DROPDOWN).assertIsDisplayed()

    // Check if all subjects are displayed
    for (i in 0 until ListingType.entries.size) {
      composeTestRule
          .onNodeWithTag("${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_$i")
          .assertIsDisplayed()
    }
    composeTestRule.clickOn("${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_0")
    composeTestRule
        .onNodeWithTag(NewListingScreenTestTag.LISTING_TYPE_FIELD)
        .assertTextContains(ListingType.entries[0].name)

    ////// Location

    composeTestRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("Pari")

    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      composeTestRule
          .onAllNodesWithTag(LocationInputFieldTestTags.SUGGESTION, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithTag(LocationInputFieldTestTags.SUGGESTION)
        .filter(hasText("Paris"))
        .onFirst()
        .performClick()

    // composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .assertTextContains("Paris")
  }

  @Test
  fun testTextInput() {
    val newListing =
        Proposal(
            title = "Piano Lessons",
            description = "Description",
            hourlyRate = 12.0,
            skill = Skill(mainSubject = MainSubject.MUSIC, skill = "PIANO"),
            location = Location(name = "Paris"),
        )

    // Fill all the Listing Info in the screen
    composeTestRule.fillNewListing(newListing)
    // Save the newSkill
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).performClick()
    // Check if the user is back to the home Page
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()

    val lastListing = listingRepository.getLastListingCreated()
    if (lastListing != null) {
      assert(lastListing.title == newListing.title)
      assert(lastListing.description == newListing.description)
      assert(lastListing.hourlyRate == newListing.hourlyRate)
      assert(lastListing.location.name == newListing.location.name)
      assert(lastListing.skill.mainSubject == newListing.skill.mainSubject)
      assert(lastListing.skill.skill == newListing.skill.skill)
    } else {
      assert(false)
    }
  }
}
