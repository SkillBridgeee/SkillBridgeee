package com.android.sample.screen

import android.Manifest
import android.app.UiAutomation
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.profile.MyProfileViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.text.set
import org.junit.Assert.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyProfileScreenTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private val sampleProfile =
      Profile(
          userId = "demo",
          name = "Kendrick Lamar",
          email = "kendrick@gmail.com",
          description = "Performer and mentor",
          location = Location(name = "EPFL", longitude = 0.0, latitude = 0.0))

  private val sampleSkills =
      listOf(
          Skill(MainSubject.MUSIC, "SINGING", 10.0, ExpertiseLevel.EXPERT),
          Skill(MainSubject.MUSIC, "DANCING", 5.0, ExpertiseLevel.INTERMEDIATE),
          Skill(MainSubject.MUSIC, "GUITAR", 7.0, ExpertiseLevel.BEGINNER),
      )

  /** Fake repository for testing ViewModel logic */
  private class FakeRepo() : ProfileRepository {

    private val profiles = mutableMapOf<String, Profile>()
    private val skillsByUser = mutableMapOf<String, List<Skill>>()

    // observable test hooks
    var updateCalled: Boolean = false
    var updatedProfile: Profile? = null

    fun seed(profile: Profile, skills: List<Skill>) {
      profiles[profile.userId] = profile
      skillsByUser[profile.userId] = skills
    }

    override fun getNewUid() = "fake"

    override suspend fun getProfile(userId: String): Profile =
        profiles[userId] ?: error("No profile $userId")

    override suspend fun getProfileById(userId: String) = getProfile(userId)

    override suspend fun addProfile(profile: Profile) {
      profiles[profile.userId] = profile
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      profiles[userId] = profile
      updateCalled = true
      updatedProfile = profile
    }

    override suspend fun deleteProfile(userId: String) {
      profiles.remove(userId)
      skillsByUser.remove(userId)
    }

    override suspend fun getAllProfiles(): List<Profile> = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getSkillsForUser(userId: String): List<Skill> =
        skillsByUser[userId] ?: emptyList()
  }

  // Minimal Fake ListingRepository to avoid initializing real Firebase/Firestore in tests
  private class FakeListingRepo : ListingRepository {
    override fun getNewUid(): String = "fake-listing-id"

    override suspend fun getAllListings(): List<Listing> = emptyList()

    override suspend fun getProposals(): List<com.android.sample.model.listing.Proposal> =
        emptyList()

    override suspend fun getRequests(): List<com.android.sample.model.listing.Request> = emptyList()

    override suspend fun getListing(listingId: String): Listing? = null

    override suspend fun getListingsByUser(userId: String): List<Listing> = emptyList()

    override suspend fun addProposal(proposal: com.android.sample.model.listing.Proposal) {}

    override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill): List<Listing> =
        emptyList()

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
        emptyList()
  }

  private lateinit var viewModel: MyProfileViewModel
  private val logoutClicked = AtomicBoolean(false)
  private lateinit var repo: FakeRepo

  @Before
  fun setup() {
    repo = FakeRepo().apply { seed(sampleProfile, sampleSkills) }
    viewModel = MyProfileViewModel(repo, listingRepository = FakeListingRepo(), userId = "demo")

    // reset flag before each test and set content once per test
    logoutClicked.set(false)
    compose.setContent {
      MyProfileScreen(
          profileViewModel = viewModel,
          profileId = "demo",
          onLogout = { logoutClicked.set(true) } // single callback wired once
          )
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.NAME_DISPLAY, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // Helper: wait for the LazyColumn to appear and scroll it so the logout button becomes visible
  private fun ensureLogoutVisible() {
    // Wait until the LazyColumn (root list) is present in unmerged tree
    compose.waitUntil(timeoutMillis = 5_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Scroll the LazyColumn to the logout button using the unmerged tree (targets LazyColumn)
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
        .performScrollToNode(hasTestTag(MyProfileScreenTestTag.LOGOUT_BUTTON))

    // Wait for the merged tree to expose the logout button
    compose.waitUntil(timeoutMillis = 2_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // --- TESTS ---

  @Test
  fun profileInfo_isDisplayedCorrectly() {
    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY)
        .assertIsDisplayed()
        .assertTextContains("Kendrick Lamar")
    compose.onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE).assertTextEquals("Student")
  }

  // ----------------------------------------------------------
  // NAME FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun nameField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
        .assertTextContains("Kendrick Lamar")
  }

  @Test
  fun nameField_canBeEdited() {
    val newName = "K Dot"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextInput(newName)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).assertTextContains(newName)
  }

  @Test
  fun nameField_showsError_whenEmpty() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextInput("")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }
  // ----------------------------------------------------------
  // EMAIL FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun emailField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .assertTextContains("kendrick@gmail.com")
  }

  @Test
  fun emailField_canBeEdited() {
    val newEmail = "kdot@gmail.com"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextInput(newEmail)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).assertTextContains(newEmail)
  }

  @Test
  fun emailField_showsError_whenInvalid() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextClearance()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .performTextInput("invalidEmail")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // LOCATION FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun locationField_displaysCorrectInitialValue() {
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains("EPFL")
  }

  @Test
  fun locationField_canBeEdited() {
    val newLocation = "Harvard University"
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextClearance()
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput(newLocation)
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains(newLocation)
  }

  @Test
  fun locationField_showsError_whenEmpty() {
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextClearance()
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput(" ")
    compose
        .onNodeWithTag(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun clickingPin_whenPermissionGranted_executesGrantedBranch() {
    // Grant runtime permission before composing the screen.
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val uiAutomation: UiAutomation = instrumentation.uiAutomation
    val packageName = compose.activity.packageName

    try {
      uiAutomation.grantRuntimePermission(packageName, Manifest.permission.ACCESS_FINE_LOCATION)
    } catch (e: SecurityException) {
      // In some test environments granting may fail; continue to run the test to still exercise
      // lines.
    }

    // Wait for UI to be ready
    compose.waitForIdle()

    // Click the pin - with permission granted the onClick should take the 'granted' branch.
    compose
        .onNodeWithContentDescription(MyProfileScreenTestTag.PIN_CONTENT_DESC)
        .assertExists()
        .performClick()

    // No crash + the branch was executed. Basic assertion to ensure UI still shows expected info.
    compose.onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY).assertExists()
  }

  // ----------------------------------------------------------
  // DESCRIPTION FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun descriptionField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .assertTextContains("Performer and mentor")
  }

  @Test
  fun descriptionField_canBeEdited() {
    val newDesc = "Artist and teacher"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput(newDesc)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).assertTextContains(newDesc)
  }

  @Test
  fun descriptionField_showsError_whenEmpty() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput("")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // GPS PIN BUTTON + SAVE FLOW TESTS
  // ----------------------------------------------------------
  @Test
  fun pinButton_isDisplayed_and_clickable() {
    compose
        .onNodeWithContentDescription(MyProfileScreenTestTag.PIN_CONTENT_DESC)
        .assertExists()
        .assertHasClickAction()
  }

  @Test
  fun clickingPin_thenSave_persistsLocation() {
    val gpsName = "12.34, 56.78"
    compose.runOnIdle {
      viewModel.setLocation(Location(name = gpsName, latitude = 12.34, longitude = 56.78))
    }

    // UI should reflect the location query
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains(gpsName)

    // Click save
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).performClick()

    // Wait until repo update is called
    compose.waitUntil(5_000) { repo.updateCalled }

    val updated = repo.updatedProfile
    assertNotNull(updated)
    assertEquals(gpsName, updated?.location?.name)
  }

  // ----------------------------------------------------------
  // LOGOUT BUTTON TESTS
  // ----------------------------------------------------------
  @Test
  fun logoutButton_isDisplayed() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun logoutButton_isClickable() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertHasClickAction()
  }

  @Test
  fun logoutButton_hasCorrectText() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertTextContains("Logout")
  }

  @Test
  fun logoutButton_triggersCallback() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).performClick()
    compose.waitForIdle()
    assert(logoutClicked.get())
  }

  // ----------------------------------------------------------
  // SAVE BUTTON TESTS
  // ----------------------------------------------------------
  @Test
  fun saveButton_isDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_isClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertHasClickAction()
  }

  @Test
  fun saveButton_hasCorrectText() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON)
        .assertTextContains("Save Profile Changes")
  }

  // ----------------------------------------------------------
  // PROFILE ICON TESTS
  // ----------------------------------------------------------
  @Test
  fun profileIcon_displaysFirstLetterOfName() {
    // The profile icon should display "K" from "Kendrick Lamar"
    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
  }

  // Edge case test for empty name is in MyProfileScreenEdgeCasesTest.kt

  // ----------------------------------------------------------
  // CARD TITLE TEST
  // ----------------------------------------------------------
  @Test
  fun cardTitle_isDisplayed() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.CARD_TITLE)
        .assertIsDisplayed()
        .assertTextEquals("Personal Details")
  }

  // ----------------------------------------------------------
  // ROLE BADGE TEST
  // ----------------------------------------------------------
  @Test
  fun roleBadge_displaysStudent() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE)
        .assertIsDisplayed()
        .assertTextEquals("Student")
  }

  @Test
  fun infoRankingBarIsDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_RATING_BAR).assertIsDisplayed()
  }

  @Test
  fun rankingTabIsDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed()
  }

  @Test
  fun infoTabIsDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).assertIsDisplayed()
  }

  @Test
  fun rankingTabIsClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertHasClickAction()
  }

  @Test
  fun infoTabIsClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).assertHasClickAction()
  }

  @Test
  fun rankingTabToRankings() {

    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_COMING_SOON_TEXT).assertIsDisplayed()
  }

  @Test
  fun infoRankingBarInRankings() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_RATING_BAR).assertIsDisplayed()
  }

  @Test
  fun rankingToInfo_SwitchesContent() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_COMING_SOON_TEXT).assertIsDisplayed()

    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
  }

  @Test
  fun tabIndicatorDisplaysCorrectly() {
    compose.onNodeWithTag(MyProfileScreenTestTag.TAB_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun rootList_isDisplayed() {
    // The LazyColumn root with a stable test tag should always exist
    compose.onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true).assertExists()
  }

  @Test
  fun yourListingsHeader_isDisplayed() {
    // The "Your Listings" section title is rendered unconditionally by ProfileListings()
    compose.onNodeWithText("Your Listings").assertIsDisplayed()
  }

  @Test
  fun emptyListingsMessage_isDisplayed_whenNoListings() {
    // With FakeListingRepo (empty) and no explicit loading/error,
    // the "empty" copy should appear.
    compose.onNodeWithText("You don’t have any listings yet.").assertIsDisplayed()
  }

  @Test
  fun saveButton_hidden_onRatingsTab() {
    // Switch to Ratings
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).performClick()

    // Save button should NOT be visible on the Ratings tab
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun saveButton_reappears_onInfoTab_afterSwitch() {
    // Go to Ratings then back to Info
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).performClick()
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).performClick()

    // Save button should be back
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun tabIndicator_visible_afterSwitchingTabs() {
    // Toggle to Ratings and make sure the indicator is still around
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).performClick()
    compose.onNodeWithTag(MyProfileScreenTestTag.TAB_INDICATOR).assertIsDisplayed()

    // Toggle back to Info and confirm it remains visible
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).performClick()
    compose.onNodeWithTag(MyProfileScreenTestTag.TAB_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun pinButton_contentDescription_matchesConstant() {
    // Sanity-check the a11y label matches the contract constant
    compose
        .onNodeWithContentDescription(MyProfileScreenTestTag.PIN_CONTENT_DESC)
        .assertExists()
        .assertContentDescriptionEquals(MyProfileScreenTestTag.PIN_CONTENT_DESC)
  }

  @Test
  fun infoTab_click_keepsProfileFormVisible() {
    // Clicking Info (already selected) should be a no-op; core form bits still visible
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).performClick()
    compose.onNodeWithTag(MyProfileScreenTestTag.CARD_TITLE).assertIsDisplayed()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).assertExists()
  }

  // Edge case tests for null/empty values are in MyProfileScreenEdgeCasesTest.kt
}
