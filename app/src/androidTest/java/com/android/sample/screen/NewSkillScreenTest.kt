package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.newSkill.NewSkillScreen
import com.android.sample.ui.newSkill.NewSkillScreenTestTag
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

  private fun createTestNavController(): TestNavHostController {
    val navController = TestNavHostController(composeRule.activity)
    composeRule.runOnUiThread { navController.navigatorProvider.addNavigator(ComposeNavigator()) }
    return navController
  }

  // ========== Rendering Tests ==========

  @Test
  fun allFieldsRender() {

    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user-123", createTestNavController())
      }
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
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user-456", createTestNavController())
      }
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
}
