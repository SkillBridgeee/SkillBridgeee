package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.screens.newSkill.NewSkillScreen
import com.android.sample.ui.screens.newSkill.NewSkillScreenTestTag
import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// ---------- Fake Repositories ----------

class FakeListingRepository : ListingRepository {
  val proposals = mutableListOf<Proposal>()
  val requests = mutableListOf<Request>()
  private var uidCounter = 0

  override fun getNewUid(): String = "listing-${uidCounter++}"

  override suspend fun getAllListings(): List<Listing> = proposals + requests

  override suspend fun getProposals(): List<Proposal> = proposals

  override suspend fun getRequests(): List<Request> = requests

  override suspend fun getListing(listingId: String): Listing? {
    return proposals.find { it.listingId == listingId }
        ?: requests.find { it.listingId == listingId }
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    return (proposals + requests).filter { it.creatorUserId == userId }
  }

  override suspend fun addProposal(proposal: Proposal) {
    proposals.add(proposal)
  }

  override suspend fun addRequest(request: Request) {
    requests.add(request)
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    // Not implemented for tests
  }

  override suspend fun deleteListing(listingId: String) {
    proposals.removeIf { it.listingId == listingId }
    requests.removeIf { it.listingId == listingId }
  }

  override suspend fun deactivateListing(listingId: String) {
    // Not implemented for tests
  }

  override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill): List<Listing> =
      emptyList()

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
      emptyList()
}

class FakeLocationRepository : LocationRepository {
  val searchResults = mutableMapOf<String, List<Location>>()

  override suspend fun search(query: String): List<Location> {
    return searchResults[query] ?: emptyList()
  }
}

// ---------- helpers ----------

private fun ComposeContentTestRule.nodeByTag(tag: String) =
    onNodeWithTag(tag, useUnmergedTree = false)

// ---------- tests ----------
class NewSkillScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var fakeListingRepository: FakeListingRepository
  private lateinit var fakeLocationRepository: FakeLocationRepository

  @Before
  fun setUp() {
    fakeListingRepository = FakeListingRepository()
    fakeLocationRepository = FakeLocationRepository()
  }

  // ========== Rendering Tests ==========

  @Test
  fun allFieldsRender() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    // Check title
    composeRule.nodeByTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()

    // Check all input fields render
    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).assertIsDisplayed()
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).assertIsDisplayed()
    composeRule.nodeByTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .assertIsDisplayed()

    // Check button renders
    composeRule.nodeByTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).assertIsDisplayed()
  }

  @Test
  fun buttonText_changesBasedOnListingType() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    // Initially shows "Create Listing"
    composeRule.onNodeWithText("Create Listing").assertIsDisplayed()

    // Select PROPOSAL
    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("PROPOSAL").performClick()
    composeRule.waitForIdle()

    // Button should show "Create Proposal"
    composeRule.onNodeWithText("Create Proposal").assertIsDisplayed()

    // Select REQUEST
    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("REQUEST").performClick()
    composeRule.waitForIdle()

    // Button should show "Create Request"
    composeRule.onNodeWithText("Create Request").assertIsDisplayed()
  }

  // ========== Input Tests ==========

  @Test
  fun titleInput_acceptsText() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    val testTitle = "Advanced Mathematics"
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput(testTitle)
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertTextContains(testTitle)
  }

  @Test
  fun descriptionInput_acceptsText() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    val testDescription = "Expert tutor with 5 years experience"
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput(testDescription)
    composeRule.waitForIdle()

    composeRule
        .nodeByTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .assertTextContains(testDescription)
  }

  @Test
  fun priceInput_acceptsText() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    val testPrice = "25.50"
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput(testPrice)
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).assertTextContains(testPrice)
  }

  // ========== Dropdown Tests ==========

  @Test
  fun listingTypeDropdown_showsOptions() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()

    // Check both options are displayed
    composeRule.onNodeWithText("PROPOSAL").assertIsDisplayed()
    composeRule.onNodeWithText("REQUEST").assertIsDisplayed()
  }

  @Test
  fun listingTypeDropdown_selectsProposal() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("PROPOSAL").performClick()
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).assertTextContains("PROPOSAL")
  }

  @Test
  fun listingTypeDropdown_selectsRequest() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("REQUEST").performClick()
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).assertTextContains("REQUEST")
  }

  @Test
  fun subjectDropdown_showsAllSubjects() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    composeRule.waitForIdle()

    // Check all subjects are present
    MainSubject.entries.forEach { subject ->
      composeRule.onNodeWithText(subject.name).assertIsDisplayed()
    }
  }

  @Test
  fun subjectDropdown_selectsSubject() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("ACADEMICS").performClick()
    composeRule.waitForIdle()

    composeRule.nodeByTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertTextContains("ACADEMICS")
  }

  // ========== Validation Tests ==========
  @Test
  fun emptyPrice_showsError() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    // Click submit without filling price
    composeRule.nodeByTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    // Error should appear - use unmerged tree to find nested error message
    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Price cannot be empty", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun invalidPrice_showsError() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    // Enter invalid price
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("abc")
    composeRule.waitForIdle()

    // Error should appear - use unmerged tree to find nested error message
    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("Price must be a positive number", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun negativePrice_showsError() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    // Enter negative price
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("-10")
    composeRule.waitForIdle()

    // Error should appear - use unmerged tree to find nested error message
    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("Price must be a positive number", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun missingSubject_showsError() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    // Click submit without selecting subject
    composeRule.nodeByTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    // Error should appear - use unmerged tree to find nested error message
    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("You must choose a subject", useUnmergedTree = true)
        .assertIsDisplayed()
  }
  // ========== Integration Tests ==========

  @Test
  fun completeProposalForm_callsRepository() {
    val fakeRepo = FakeListingRepository()
    val vm =
        NewSkillViewModel(
            listingRepository = fakeRepo,
            locationRepository = fakeLocationRepository,
            userId = "test-user-123")

    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user-123") }
    }
    composeRule.waitForIdle()

    // Fill in all fields for Proposal
    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("PROPOSAL").performClick()
    composeRule.waitForIdle()

    composeRule
        .nodeByTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .performTextInput("Math Tutoring")
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput("Expert tutor")
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("30.00")

    composeRule.nodeByTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("ACADEMICS").performClick()
    composeRule.waitForIdle()

    // Set location programmatically through ViewModel since location search is complex
    vm.setLocation(Location(46.5196535, 6.6322734, "Lausanne"))
    composeRule.waitForIdle()

    // Submit
    composeRule.nodeByTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    // Verify proposal was added
    assert(fakeRepo.proposals.size == 1)
    assert(fakeRepo.proposals[0].skill.skill == "Math Tutoring")
    assert(fakeRepo.proposals[0].description == "Expert tutor")
    assert(fakeRepo.proposals[0].hourlyRate == 30.00)
  }

  @Test
  fun completeRequestForm_callsRepository() {
    val fakeRepo = FakeListingRepository()
    val vm =
        NewSkillViewModel(
            listingRepository = fakeRepo,
            locationRepository = fakeLocationRepository,
            userId = "test-user-456")

    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user-456") }
    }
    composeRule.waitForIdle()

    // Fill in all fields for Request
    composeRule.nodeByTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("REQUEST").performClick()
    composeRule.waitForIdle()

    composeRule
        .nodeByTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .performTextInput("Need Math Help")
    composeRule
        .nodeByTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .performTextInput("Looking for tutor")
    composeRule.nodeByTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("25.00")

    composeRule.nodeByTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText("ACADEMICS").performClick()
    composeRule.waitForIdle()

    // Set location programmatically
    vm.setLocation(Location(46.2044, 6.1432, "Geneva"))
    composeRule.waitForIdle()

    // Submit
    composeRule.nodeByTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    // Verify request was added
    assert(fakeRepo.requests.size == 1)
    assert(fakeRepo.requests[0].skill.skill == "Need Math Help")
    assert(fakeRepo.requests[0].description == "Looking for tutor")
    assert(fakeRepo.requests[0].hourlyRate == 25.00)
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
