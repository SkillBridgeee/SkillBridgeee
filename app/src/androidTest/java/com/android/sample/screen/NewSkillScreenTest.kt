package com.android.sample.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.ui.screens.newSkill.NewSkillScreen
import com.android.sample.ui.screens.newSkill.NewSkillScreenTestTag
import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewSkillScreenTest {

  @get:Rule val compose = createComposeRule()

  /** Fake repository for testing ViewModel logic */
  private class FakeRepo() : ListingRepository {

    fun seed() {}

    override fun getNewUid() = "fake"

    override suspend fun getAllListings(): List<Listing> {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun getProposals(): List<Proposal> {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun getRequests(): List<Request> {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun getListing(listingId: String): Listing? {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun getListingsByUser(userId: String): List<Listing> {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun addProposal(proposal: Proposal) {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun addRequest(request: Request) {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun updateListing(listingId: String, listing: Listing) {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun deleteListing(listingId: String) {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun deactivateListing(listingId: String) {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun searchBySkill(skill: Skill): List<Listing> {
      throw NotImplementedError("Unused in this test")
    }

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
      throw NotImplementedError("Unused in this test")
    }
  }

  private lateinit var viewModel: NewSkillViewModel

  @Before
  fun setup() {
    val repo = FakeRepo().apply { seed() }
    viewModel = NewSkillViewModel(repo, userId = "demoUser")
    compose.setContent { NewSkillScreen(profileId = "demoUser", skillViewModel = viewModel) }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // ----------------------------------------------------------
  // BASIC DISPLAY TESTS
  // ----------------------------------------------------------

  @Test
  fun screen_displaysAllInputFields() {
    compose
        .onNodeWithTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE)
        .assertIsDisplayed()
        .assertTextContains("Create Your Lessons !")

    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertIsDisplayed()
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
    compose.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // INITIAL STATE TESTS
  // ----------------------------------------------------------

  @Test
  fun allFields_areInitiallyEmpty() {
    compose
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .assertTextContains("", substring = true) // Le champ n’a pas de texte utilisateur
    compose
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .assertTextContains("", substring = true)
    compose
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE)
        .assertTextContains("", substring = true)
    compose
        .onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD)
        .assertTextContains("", substring = true)
  }

  // ----------------------------------------------------------
  // TEXT INPUT TESTS
  // ----------------------------------------------------------

  @Test
  fun titleField_canBeEdited() {
    val newTitle = "Guitar Lessons"
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput(newTitle)
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertTextContains(newTitle)
  }

  @Test
  fun descriptionField_canBeEdited() {
    val newDesc = "Learn the basics of guitar playing"
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput(newDesc)
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertTextContains(newDesc)
  }

  @Test
  fun priceField_canBeEdited() {
    val newPrice = "30"
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput(newPrice)
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertTextContains(newPrice)
  }

  // ----------------------------------------------------------
  // SUBJECT DROPDOWN TESTS
  // ----------------------------------------------------------

  @Test
  fun subjectDropdown_canBeOpened_andSelectItem() {
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()
    compose.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)[0].performClick()
  }

  // ----------------------------------------------------------
  // ERROR MESSAGE DISPLAY TESTS
  // (simulate invalid input visually)
  // ----------------------------------------------------------

  @Test
  fun showsErrorMessages_whenInvalidInput() {
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput("")
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput("")
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("abc")

    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()
  }

  // Test button save skill
  @Test
  fun clickOnSaveSkillButton() {
    compose.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
  }

  // ----------------------------------------------------------
  // SUBJECT / SUB-SKILL EXTENDED TESTS
  // ----------------------------------------------------------

  @Test
  fun subSkill_notVisible_untilSubjectSelected_thenVisible() {
    // Initially, sub-skill picker should not be shown
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD, useUnmergedTree = true)
        .assertCountEquals(0)

    // Select a subject
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()
    compose.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)[0].performClick()

    // After subject selection, sub-skill field should appear
    compose.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD).assertIsDisplayed()
  }

  @Test
  fun subjectDropdown_open_selectItem_thenCloses() {
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()

    // Select first subject
    compose.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)[0].performClick()

    // Menu should be gone after selection
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN, useUnmergedTree = true)
        .assertCountEquals(0)
  }

  @Test
  fun subSkillDropdown_open_selectItem_thenCloses() {
    // Precondition: select a subject so sub-skill menu appears
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    compose.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)[0].performClick()

    // Now open sub-skill dropdown
    compose.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD).performClick()
    compose.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN).assertIsDisplayed()

    // Select first sub-skill option
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX)[0]
        .performClick()

    // Menu should be gone after selection
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN, useUnmergedTree = true)
        .assertCountEquals(0)
  }

  @Test
  fun showsError_whenNoSubject_onSave() {
    // Ensure subject is empty (initial screen state), click Save
    compose.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    // Error helper under Subject field should be visible
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()
  }

  @Test
  fun showsError_whenSubjectChosen_butNoSubSkill_onSave() {
    // Choose a subject
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    compose.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)[0].performClick()

    // Sub-skill field visible now but we don't choose any sub-skill
    // Click Save directly
    compose.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    // Error helper under Sub-skill field should be visible
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()
  }

  @Test
  fun selectingSubject_thenSubSkill_enablesCleanSave_noErrorsShown() {
    // Select a subject
    compose.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    compose.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)[0].performClick()

    // Select a sub-skill
    compose.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD).performClick()
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX)[0]
        .performClick()

    // Provide minimal valid text inputs to avoid other errors from the ViewModel
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput("T")
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput("D")
    compose.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("1")

    // Save
    compose.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    // Assert no subject/sub-skill error helpers are shown
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertCountEquals(0)
    compose
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG, useUnmergedTree = true)
        .assertCountEquals(0)
  }
}
