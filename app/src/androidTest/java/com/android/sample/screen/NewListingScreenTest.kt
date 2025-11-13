package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.newListing.NewListingScreen
import com.android.sample.ui.newListing.NewSkillScreenTestTag
import com.android.sample.ui.screens.newSkill.NewListingViewModel
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

  override suspend fun search(query: String): List<Location> = searchResults[query] ?: emptyList()
}

// =============================
// === CI-Stable Test Helpers ===
// =============================

private const val STABLE_WAIT_TIMEOUT = 20_000L

private fun ComposeContentTestRule.stabilizeCompose(delayMillis: Long = 1_000) {
  mainClock.advanceTimeBy(delayMillis)
  waitForIdle()
}

private fun ComposeContentTestRule.waitForNodeStable(
    tag: String,
    useUnmergedTree: Boolean = true,
    timeoutMillis: Long = STABLE_WAIT_TIMEOUT
) {
  waitUntil(timeoutMillis) {
    onAllNodesWithTag(tag, useUnmergedTree).fetchSemanticsNodes().isNotEmpty()
  }
  stabilizeCompose()
}

private fun ComposeContentTestRule.openDropdownStable(fieldTag: String) {
  onNodeWithTag(fieldTag, useUnmergedTree = true).assertExists().performClick()

  stabilizeCompose()

  val dropdown =
      when (fieldTag) {
        NewSkillScreenTestTag.SUBJECT_FIELD -> NewSkillScreenTestTag.SUBJECT_DROPDOWN
        NewSkillScreenTestTag.SUB_SKILL_FIELD -> NewSkillScreenTestTag.SUB_SKILL_DROPDOWN
        NewSkillScreenTestTag.LISTING_TYPE_FIELD -> NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN
        else -> error("Unknown dropdown fieldTag")
      }

  waitForNodeStable(dropdown)
}

private fun ComposeContentTestRule.selectDropdownItemByTagStable(
    itemTagPrefix: String,
    index: Int,
    timeoutMillis: Long = STABLE_WAIT_TIMEOUT
) {
  val fullTag = "${itemTagPrefix}_$index"

  waitUntil(timeoutMillis) {
    onAllNodesWithTag(fullTag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
  }

  stabilizeCompose()

  // use onAllNodesWithTag and click the first matching node (should be the indexed one)
  val nodes = onAllNodesWithTag(fullTag, useUnmergedTree = true)
  nodes[0].assertExists().performClick()

  stabilizeCompose()
}

private fun ComposeContentTestRule.openAndSelectStable(
    fieldTag: String,
    itemText: String? = null,
    itemTagPrefix: String? = null,
    index: Int = 0
) {
  openDropdownStable(fieldTag)

  when {
    itemText != null -> {
      onNodeWithText(itemText, useUnmergedTree = true).assertExists().performClick()
      stabilizeCompose()
    }
    itemTagPrefix != null -> selectDropdownItemByTagStable(itemTagPrefix, index)
  }

  waitForIdle()
}

// =====================
// ====== Tests ========
// =====================

class NewSkillScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var fakeListingRepository: FakeListingRepository
  private lateinit var fakeLocationRepository: FakeLocationRepository

  @Before
  fun setUp() {
    fakeListingRepository = FakeListingRepository()
    fakeLocationRepository = FakeLocationRepository()
  }

  private fun createTestNavController(): NavHostController {
    val navController = NavHostController(composeRule.activity)
    composeRule.runOnUiThread { navController.navigatorProvider.addNavigator(ComposeNavigator()) }
    return navController
  }

  // Rendering Tests
  @Test
  fun allFieldsRender() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true).assertIsDisplayed()
    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).assertIsDisplayed()
  }

  @Test
  fun buttonText_changesBasedOnListingType() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithText("Create Listing").assertIsDisplayed()

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD, itemText = "PROPOSAL")
    composeRule.onNodeWithText("Create Proposal").assertIsDisplayed()

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD, itemText = "REQUEST")
    composeRule.onNodeWithText("Create Request").assertIsDisplayed()
  }

  // Input Tests
  @Test
  fun titleInput_acceptsText() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    val text = "Advanced Mathematics"
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput(text)
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertTextContains(text)
  }

  @Test
  fun descriptionInput_acceptsText() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    val text = "Expert tutor with 5 years experience"
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput(text)
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertTextContains(text)
  }

  @Test
  fun priceInput_acceptsText() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("25.50")
    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertTextContains("25.50")
  }

  // Dropdown Tests
  @Test
  fun listingTypeDropdown_showsOptions() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.openDropdownStable(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
    composeRule.waitForNodeStable(NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN)

    composeRule.onNodeWithText("PROPOSAL").assertIsDisplayed()
    composeRule.onNodeWithText("REQUEST").assertIsDisplayed()
  }

  @Test
  fun listingTypeDropdown_selectsProposal() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD, itemText = "PROPOSAL")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
        .assertTextContains("PROPOSAL")
  }

  @Test
  fun listingTypeDropdown_selectsRequest() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.LISTING_TYPE_FIELD, itemText = "REQUEST")

    composeRule
        .onNodeWithTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
        .assertTextContains("REQUEST")
  }

  @Test
  fun subjectDropdown_showsAllSubjects() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.openDropdownStable(NewSkillScreenTestTag.SUBJECT_FIELD)
    composeRule.waitForNodeStable(NewSkillScreenTestTag.SUBJECT_DROPDOWN)

    MainSubject.entries.forEach { composeRule.onNodeWithText(it.name).assertIsDisplayed() }
  }

  @Test
  fun subjectDropdown_selectsSubject() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD, itemText = "ACADEMICS")

    composeRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertTextContains("ACADEMICS")
  }

  // Validation Tests
  @Test
  fun emptyPrice_showsError() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, true).assertIsDisplayed()
    composeRule.onNodeWithText("Price cannot be empty", true).assertIsDisplayed()
  }

  @Test
  fun invalidPrice_showsError() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("abc")

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, true).assertIsDisplayed()
    composeRule.onNodeWithText("Price must be a positive number", true).assertIsDisplayed()
  }

  @Test
  fun negativePrice_showsError() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("-10")

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, true).assertIsDisplayed()
    composeRule.onNodeWithText("Price must be a positive number", true).assertIsDisplayed()
  }

  @Test
  fun missingSubject_showsError() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, true).assertIsDisplayed()

    composeRule.onNodeWithText("You must choose a subject", true).assertIsDisplayed()
  }

  @Test
  fun subSkill_notVisible_untilSubjectSelected_thenVisible() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onAllNodesWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD, true).assertCountEquals(0)

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        itemTagPrefix = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.onNodeWithTag(NewSkillScreenTestTag.SUB_SKILL_FIELD).assertIsDisplayed()
  }

  @Test
  fun subjectDropdown_open_selectItem_thenCloses() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    // ✅ FIXED: removed unsupported dropdownTag argument
    composeRule.openDropdownStable(fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD)

    composeRule.waitForNodeStable(NewSkillScreenTestTag.SUBJECT_DROPDOWN)

    composeRule.selectDropdownItemByTagStable(
        itemTagPrefix = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX, index = 0)

    composeRule.onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN, true).assertCountEquals(0)
  }

  @Test
  fun showsError_whenNoSubject_onSave() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    val nodes =
        composeRule
            .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, true)
            .fetchSemanticsNodes()

    org.junit.Assert.assertTrue(nodes.isNotEmpty())
  }

  @Test
  fun showsError_whenSubjectChosen_butNoSubSkill_onSave() {
    val vm = NewListingViewModel(fakeListingRepository, fakeLocationRepository)
    composeRule.setContent {
      SampleAppTheme { NewListingScreen(vm, "test-user", createTestNavController()) }
    }
    composeRule.waitForIdle()

    composeRule.openAndSelectStable(
        fieldTag = NewSkillScreenTestTag.SUBJECT_FIELD,
        itemTagPrefix = NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX,
        index = 0)

    composeRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
    composeRule.waitForIdle()

    val nodes =
        composeRule
            .onAllNodesWithTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG, true)
            .fetchSemanticsNodes()

    org.junit.Assert.assertTrue(nodes.isNotEmpty())
  }


}
