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
import kotlin.collections.get
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

  override suspend fun updateListing(listingId: String, listing: Listing) {}

  override suspend fun deleteListing(listingId: String) {
    proposals.removeIf { it.listingId == listingId }
    requests.removeIf { it.listingId == listingId }
  }

  override suspend fun deactivateListing(listingId: String) {}

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

// =====================
// === Stable Helpers ===
// =====================

private fun ComposeContentTestRule.waitForNode(
    tag: String,
    useUnmergedTree: Boolean = true,
    timeoutMillis: Long = 5000
) {
  waitUntil(timeoutMillis) {
    onAllNodesWithTag(tag, useUnmergedTree).fetchSemanticsNodes().isNotEmpty()
  }
}

private fun ComposeContentTestRule.openDropdown(fieldTag: String) {
  onNodeWithTag(fieldTag, useUnmergedTree = true).performClick()
  waitForIdle()
}

private fun ComposeContentTestRule.selectDropdownItemByText(text: String) {
  onNodeWithText(text, useUnmergedTree = true).assertExists().performClick()
  waitForIdle()
}

private fun ComposeContentTestRule.selectDropdownItemByTag(
    itemTag: String,
    index: Int = 0,
    timeoutMillis: Long = 5000
) {
  waitUntil(timeoutMillis) {
    onAllNodesWithTag(itemTag, useUnmergedTree = true).fetchSemanticsNodes().size > index
  }
  onAllNodesWithTag(itemTag, useUnmergedTree = true)[index].performClick()
  waitForIdle()
}

private fun ComposeContentTestRule.openAndSelect(
    fieldTag: String,
    dropdownTag: String,
    itemText: String? = null,
    itemTag: String? = null,
    index: Int = 0
) {
  openDropdown(fieldTag)
  waitForNode(dropdownTag)

  if (itemText != null) {
    selectDropdownItemByText(itemText)
  } else if (itemTag != null) {
    selectDropdownItemByTag(itemTag, index)
  }
}

// =====================
// ====== Tests ========
// =====================

class NewSkillScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val compose: ComposeContentTestRule
    get() = composeRule

  private lateinit var fakeListingRepository: FakeListingRepository
  private lateinit var fakeLocationRepository: FakeLocationRepository

  @Before
  fun setUp() {
    fakeListingRepository = FakeListingRepository()
    fakeLocationRepository = FakeLocationRepository()
  }

  // ----------------------------------------------------------
  // Rendering Tests
  // ----------------------------------------------------------
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

    composeRule.onNodeWithTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).assertIsDisplayed()
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

    composeRule.onNodeWithText("Create Listing").assertIsDisplayed()
    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD,
        dropdownTag = NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN,
        itemText = "PROPOSAL")
    composeRule.onNodeWithText("Create Proposal").assertIsDisplayed()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD,
        dropdownTag = NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN,
        itemText = "REQUEST")
    composeRule.onNodeWithText("Create Request").assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // Input Tests
  // ----------------------------------------------------------

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

    val text = "Advanced Mathematics"
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput(text)
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertTextContains(text)
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

    val text = "Expert tutor with 5 years experience"
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput(text)
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertTextContains(text)
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

    val text = "25.50"
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput(text)
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertTextContains(text)
  }

  // ----------------------------------------------------------
  // Dropdown Tests
  // ----------------------------------------------------------

  @Test
  fun listingTypeDropdown_showsOptions() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.openDropdown(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
    composeRule.waitForNode(NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN)

    composeRule.onNodeWithText("PROPOSAL").assertIsDisplayed()
    composeRule.onNodeWithText("REQUEST").assertIsDisplayed()
  }

  @Test
  fun listingTypeDropdown_selectsProposal() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD,
        dropdownTag = NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN,
        itemText = "PROPOSAL")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
        .assertTextContains("PROPOSAL")
  }

  @Test
  fun listingTypeDropdown_selectsRequest() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD,
        dropdownTag = NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN,
        itemText = "REQUEST")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
        .assertTextContains("REQUEST")
  }

  @Test
  fun subjectDropdown_showsAllSubjects() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.openDropdown(NewSkillScreenTestTag.SUBJECT_FIELD)
    composeRule.waitForNode(NewSkillScreenTestTag.SUBJECT_DROPDOWN)

    MainSubject.entries.forEach { composeRule.onNodeWithText(it.name).assertIsDisplayed() }
  }

  @Test
  fun subjectDropdown_selectsSubject() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemText = "ACADEMICS")

    composeRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertTextContains("ACADEMICS")
  }

  // ----------------------------------------------------------
  // Validation Tests
  // ----------------------------------------------------------

  @Test
  fun emptyPrice_showsError() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Price cannot be empty", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun invalidPrice_showsError() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("abc")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("Price must be a positive number", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun negativePrice_showsError() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("-10")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("Price must be a positive number", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun missingSubject_showsError() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme {
        NewSkillScreen(skillViewModel = vm, profileId = "test-user", createTestNavController())
      }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("You must choose a subject", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // Integration Tests
  // ----------------------------------------------------------

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

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD,
        dropdownTag = NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN,
        itemText = "PROPOSAL")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .performTextInput("Math Tutoring")
    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .performTextInput("Expert tutor")
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("30.00")

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUB_SKILL_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX,
        index = 0)

    vm.setLocation(Location(46.5196535, 6.6322734, "Lausanne"))
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    composeRule.runOnIdle {
      assert(fakeRepo.proposals.size == 1)
      val saved = fakeRepo.proposals[0]
      assert(saved.description == "Expert tutor")
      assert(saved.hourlyRate == 30.00)
      assert(saved.creatorUserId == "test-user-123")
      assert(saved.skill.mainSubject == MainSubject.ACADEMICS)
      assert(saved.skill.skill.isNotBlank())
    }
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

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD,
        dropdownTag = NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN,
        itemText = "REQUEST")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .performTextInput("Need Math Help")
    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .performTextInput("Looking for tutor")
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("25.00")

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUB_SKILL_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX,
        index = 0)

    vm.setLocation(Location(46.2044, 6.1432, "Geneva"))
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    composeRule.runOnIdle {
      assert(fakeRepo.requests.size == 1)
      val saved = fakeRepo.requests[0]
      assert(saved.description == "Looking for tutor")
      assert(saved.hourlyRate == 25.00)
      assert(saved.creatorUserId == "test-user-456")
      assert(saved.skill.mainSubject == MainSubject.ACADEMICS)
      assert(saved.skill.skill.isNotBlank())
    }
  }

  // ----------------------------------------------------------
  // Subject / Sub-Skill Extended Tests
  // ----------------------------------------------------------

  @Test
  fun subSkill_notVisible_untilSubjectSelected_thenVisible() {
    val vm =
        NewSkillViewModel(
            listingRepository = fakeListingRepository, locationRepository = fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule
        .onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD, useUnmergedTree = true)
        .assertCountEquals(0)

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD).assertIsDisplayed()
  }

  @Test
  fun subjectDropdown_open_selectItem_thenCloses() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.openDropdown(NewSkillScreenTestTag.SUBJECT_FIELD)
    composeRule.waitForNode(NewSkillScreenTestTag.SUBJECT_DROPDOWN)

    composeRule.selectDropdownItemByTag(
        NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX, index = 0)

    composeRule
        .onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN, useUnmergedTree = true)
        .assertCountEquals(0)
  }

  @Test
  fun subSkillDropdown_open_selectItem_thenCloses() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.openDropdown(NewSkillScreenTestTag.SUB_SKILL_FIELD)
    composeRule.waitForNode(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN)

    composeRule.selectDropdownItemByTag(
        itemTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX, index = 0)

    composeRule
        .onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN, useUnmergedTree = true)
        .assertCountEquals(0)
  }

  @Test
  fun showsError_whenNoSubject_onSave() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    val nodes =
        composeRule
            .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
            .fetchSemanticsNodes()

    org.junit.Assert.assertTrue(nodes.isNotEmpty())
  }

  @Test
  fun showsError_whenSubjectChosen_butNoSubSkill_onSave() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    val nodes =
        composeRule
            .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG, useUnmergedTree = true)
            .fetchSemanticsNodes()

    org.junit.Assert.assertTrue(nodes.isNotEmpty())
  }

  @Test
  fun selectingSubject_thenSubSkill_enablesCleanSave_noErrorsShown() {
    val vm = NewSkillViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewSkillScreen(skillViewModel = vm, profileId = "test-user") }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.openAndSelect(
        fieldTag = NewSkillScreenTestTag.SUB_SKILL_FIELD,
        dropdownTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN,
        itemTag = NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput("T")
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput("D")
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("1")

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    composeRule
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertCountEquals(0)

    composeRule
        .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG, useUnmergedTree = true)
        .assertCountEquals(0)
  }
}
