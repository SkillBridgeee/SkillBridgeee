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
    viewModel = NewSkillViewModel(repo)
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
}
