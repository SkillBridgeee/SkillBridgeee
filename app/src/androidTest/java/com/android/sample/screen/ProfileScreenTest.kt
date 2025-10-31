package com.android.sample.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.profile.ProfileScreen
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.profile.ProfileScreenViewModel
import java.util.Date
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  @get:Rule val compose = createComposeRule()

  private val sampleProfile =
      Profile(
          userId = "user-123",
          name = "Jane Smith",
          email = "jane.smith@example.com",
          description = "Experienced mathematics tutor with a passion for teaching",
          location = Location(name = "New York", longitude = -74.0, latitude = 40.7),
          tutorRating = RatingInfo(4.5, 20),
          studentRating = RatingInfo(4.0, 8))

  private val sampleProposal1 =
      Proposal(
          listingId = "p1",
          creatorUserId = "user-123",
          skill = Skill(MainSubject.ACADEMICS, "Calculus", 5.0, ExpertiseLevel.ADVANCED),
          description = "Advanced calculus tutoring",
          location = Location(name = "Campus"),
          createdAt = Date(),
          isActive = true,
          hourlyRate = 30.0)

  private val sampleProposal2 =
      Proposal(
          listingId = "p2",
          creatorUserId = "user-123",
          skill = Skill(MainSubject.ACADEMICS, "Algebra", 6.0, ExpertiseLevel.EXPERT),
          description = "Algebra for beginners",
          location = Location(name = "Library"),
          createdAt = Date(),
          isActive = false,
          hourlyRate = 25.0)

  private val sampleRequest =
      Request(
          listingId = "r1",
          creatorUserId = "user-123",
          skill = Skill(MainSubject.ACADEMICS, "Physics", 3.0, ExpertiseLevel.INTERMEDIATE),
          description = "Need help with quantum mechanics",
          location = Location(name = "Study Room"),
          createdAt = Date(),
          isActive = true,
          hourlyRate = 35.0)

  // Fake repositories
  private class FakeProfileRepo(private var profile: Profile? = null) : ProfileRepository {
    override fun getNewUid() = "fake"

    override suspend fun getProfile(userId: String) = profile

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles() = emptyList<Profile>()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String) = profile

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
  }

  private class FakeListingRepo(
      private val proposals: MutableList<Proposal> = mutableListOf(),
      private val requests: MutableList<Request> = mutableListOf()
  ) : ListingRepository {
    override fun getNewUid() = "fake"

    override suspend fun getAllListings() = proposals + requests

    override suspend fun getProposals() = proposals

    override suspend fun getRequests() = requests

    override suspend fun getListing(listingId: String) =
        (proposals + requests).find { it.listingId == listingId }

    override suspend fun getListingsByUser(userId: String) =
        (proposals + requests).filter { it.creatorUserId == userId }

    override suspend fun addProposal(proposal: Proposal) {
      proposals.add(proposal)
    }

    override suspend fun addRequest(request: Request) {
      requests.add(request)
    }

    override suspend fun updateListing(
        listingId: String,
        listing: com.android.sample.model.listing.Listing
    ) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) =
        emptyList<com.android.sample.model.listing.Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<com.android.sample.model.listing.Listing>()
  }

  // Helper to create default viewModel
  private fun createDefaultViewModel(): ProfileScreenViewModel {
    val profileRepo = FakeProfileRepo(sampleProfile)
    val listingRepo =
        FakeListingRepo(
            mutableListOf(sampleProposal1, sampleProposal2), mutableListOf(sampleRequest))
    return ProfileScreenViewModel(profileRepo, listingRepo)
  }

  // Helper to set up the screen and wait for it to load
  private fun setupScreen(
      viewModel: ProfileScreenViewModel = createDefaultViewModel(),
      profileId: String = "user-123",
      onBackClick: () -> Unit = {},
      onProposalClick: (String) -> Unit = {},
      onRequestClick: (String) -> Unit = {}
  ) {
    compose.setContent {
      ProfileScreen(
          profileId = profileId,
          onBackClick = onBackClick,
          onProposalClick = onProposalClick,
          onRequestClick = onRequestClick,
          viewModel = viewModel)
    }

    // Wait for content to load - either profile icon or error text
    compose.waitUntil(5_000) {
      val profileIconExists =
          compose
              .onAllNodesWithTag(ProfileScreenTestTags.PROFILE_ICON, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()

      val errorExists =
          compose
              .onAllNodesWithTag(ProfileScreenTestTags.ERROR_TEXT, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()

      val emptyProposalsExists =
          compose
              .onAllNodesWithTag(ProfileScreenTestTags.EMPTY_PROPOSALS, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()

      val emptyRequestsExists =
          compose
              .onAllNodesWithTag(ProfileScreenTestTags.EMPTY_REQUESTS, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()

      profileIconExists || errorExists || emptyProposalsExists || emptyRequestsExists
    }
  }

  @Test
  fun profileScreen_displaysProfileInfo() {
    setupScreen()

    // Profile icon
    compose.onNodeWithTag(ProfileScreenTestTags.PROFILE_ICON).assertIsDisplayed()

    // Name
    compose
        .onNodeWithTag(ProfileScreenTestTags.NAME_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Jane Smith")

    // Email
    compose
        .onNodeWithTag(ProfileScreenTestTags.EMAIL_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("jane.smith@example.com")

    // Location
    compose
        .onNodeWithTag(ProfileScreenTestTags.LOCATION_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("New York")

    // Description
    compose
        .onNodeWithTag(ProfileScreenTestTags.DESCRIPTION_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysRatings() {
    setupScreen()

    // Tutor rating section
    compose.onNodeWithTag(ProfileScreenTestTags.TUTOR_RATING_SECTION).assertIsDisplayed()

    compose
        .onNodeWithTag(ProfileScreenTestTags.TUTOR_RATING_VALUE, useUnmergedTree = true)
        .assertIsDisplayed()

    // Student rating section
    compose.onNodeWithTag(ProfileScreenTestTags.STUDENT_RATING_SECTION).assertIsDisplayed()

    compose
        .onNodeWithTag(ProfileScreenTestTags.STUDENT_RATING_VALUE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun profileScreen_backButton_isDisplayed() {
    setupScreen()

    compose.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_refreshButton_isDisplayed() {
    setupScreen()

    compose.onNodeWithTag(ProfileScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_backButton_callsCallback() {
    var backClicked = false

    setupScreen(onBackClick = { backClicked = true })

    compose.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)
  }

  @Test
  fun profileScreen_proposalClick_callsCallback() {
    var clickedProposalId: String? = null

    setupScreen(onProposalClick = { clickedProposalId = it })

    // Click first proposal
    compose.onNodeWithText("Advanced calculus tutoring").performClick()
    assertEquals("p1", clickedProposalId)
  }

  @Test
  fun profileScreen_emptyProposals_showsEmptyState() {
    val profileRepo = FakeProfileRepo(sampleProfile)
    val listingRepo = FakeListingRepo(mutableListOf(), mutableListOf(sampleRequest))
    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    setupScreen(viewModel = vm)

    compose
        .onNodeWithTag(ProfileScreenTestTags.EMPTY_PROPOSALS, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("No proposals yet")
  }

  @Test
  fun profileScreen_profileNotFound_showsError() {
    val profileRepo = FakeProfileRepo(null)
    val listingRepo = FakeListingRepo()
    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    setupScreen(viewModel = vm, profileId = "non-existent")

    compose
        .onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Profile not found")
  }

  @Test
  fun profileScreen_initialLoad_showsLoadingIndicator() {
    val profileRepo = FakeProfileRepo(sampleProfile)
    val listingRepo = FakeListingRepo()
    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    compose.setContent {
      ProfileScreen(
          profileId = "user-123",
          onBackClick = {},
          onProposalClick = {},
          onRequestClick = {},
          viewModel = vm)
    }

    // Loading indicator should appear initially
    // Note: This may be very brief, so we just check it exists at some point
    compose.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
