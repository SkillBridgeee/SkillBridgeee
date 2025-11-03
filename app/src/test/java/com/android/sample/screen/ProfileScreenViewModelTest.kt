package com.android.sample.screen

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
import com.android.sample.ui.profile.ProfileScreenViewModel
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProfileScreenViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // -------- Fake Repositories ------------------------------------------------------

  private class FakeProfileRepository(private var storedProfile: Profile? = null) :
      ProfileRepository {
    var getProfileCalled = false

    override fun getNewUid(): String = "fake-uid"

    override suspend fun getProfile(userId: String): Profile? {
      getProfileCalled = true
      return storedProfile
    }

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> = emptyList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String) = storedProfile

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
  }

  private class FakeListingRepository(
      private val storedProposals: MutableList<Proposal> = mutableListOf(),
      private val storedRequests: MutableList<Request> = mutableListOf()
  ) : ListingRepository {
    var getListingsByUserCalled = false

    override fun getNewUid(): String = "fake-listing-uid"

    override suspend fun getAllListings() = storedProposals + storedRequests

    override suspend fun getProposals() = storedProposals

    override suspend fun getRequests() = storedRequests

    override suspend fun getListing(listingId: String) =
        (storedProposals + storedRequests).find { it.listingId == listingId }

    override suspend fun getListingsByUser(
        userId: String
    ): List<com.android.sample.model.listing.Listing> {
      getListingsByUserCalled = true
      return (storedProposals + storedRequests).filter { it.creatorUserId == userId }
    }

    override suspend fun addProposal(proposal: Proposal) {
      storedProposals.add(proposal)
    }

    override suspend fun addRequest(request: Request) {
      storedRequests.add(request)
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

  // -------- Helpers ------------------------------------------------------

  private fun makeProfile(
      id: String = "user-123",
      name: String = "John Doe",
      email: String = "john@example.com",
      location: Location = Location(name = "New York"),
      desc: String = "Experienced tutor",
      tutorRating: RatingInfo = RatingInfo(4.5, 10),
      studentRating: RatingInfo = RatingInfo(4.0, 5)
  ) =
      Profile(
          userId = id,
          name = name,
          email = email,
          location = location,
          description = desc,
          tutorRating = tutorRating,
          studentRating = studentRating)

  private fun makeProposal(
      id: String = "proposal-1",
      creatorId: String = "user-123",
      desc: String = "Math tutoring",
      rate: Double = 25.0
  ) =
      Proposal(
          listingId = id,
          creatorUserId = creatorId,
          description = desc,
          hourlyRate = rate,
          skill = Skill(MainSubject.ACADEMICS, "Algebra", 5.0, ExpertiseLevel.ADVANCED),
          location = Location(name = "Campus"),
          createdAt = Date())

  private fun makeRequest(
      id: String = "request-1",
      creatorId: String = "user-123",
      desc: String = "Need physics help",
      rate: Double = 30.0
  ) =
      Request(
          listingId = id,
          creatorUserId = creatorId,
          description = desc,
          hourlyRate = rate,
          skill = Skill(MainSubject.ACADEMICS, "Physics", 3.0, ExpertiseLevel.INTERMEDIATE),
          location = Location(name = "Library"),
          createdAt = Date())

  // -------- Tests --------------------------------------------------------

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun initialState_isLoading() {
    val vm = ProfileScreenViewModel(FakeProfileRepository(), FakeListingRepository())

    val state = vm.uiState.value
    assertTrue(state.isLoading)
    assertNull(state.profile)
    assertTrue(state.proposals.isEmpty())
    assertTrue(state.requests.isEmpty())
    assertNull(state.errorMessage)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_successfullyLoadsProfileAndListings() = runTest {
    val profile = makeProfile()
    val proposal1 = makeProposal("p1", profile.userId)
    val proposal2 = makeProposal("p2", profile.userId)
    val request1 = makeRequest("r1", profile.userId)

    val profileRepo = FakeProfileRepository(profile)
    val listingRepo =
        FakeListingRepository(mutableListOf(proposal1, proposal2), mutableListOf(request1))

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertEquals(profile, state.profile)
    assertEquals(2, state.proposals.size)
    assertEquals(1, state.requests.size)
    assertTrue(state.proposals.contains(proposal1))
    assertTrue(state.proposals.contains(proposal2))
    assertTrue(state.requests.contains(request1))
    assertTrue(profileRepo.getProfileCalled)
    assertTrue(listingRepo.getListingsByUserCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_profileNotFound_showsError() = runTest {
    val profileRepo = FakeProfileRepository(null)
    val listingRepo = FakeListingRepository()

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile("non-existent-user")
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNotNull(state.errorMessage)
    assertEquals("Profile not found", state.errorMessage)
    assertNull(state.profile)
    assertTrue(profileRepo.getProfileCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_emptyListings_returnsEmptyLists() = runTest {
    val profile = makeProfile()
    val profileRepo = FakeProfileRepository(profile)
    val listingRepo = FakeListingRepository()

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertEquals(profile, state.profile)
    assertTrue(state.proposals.isEmpty())
    assertTrue(state.requests.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_onlyProposals_separatesCorrectly() = runTest {
    val profile = makeProfile()
    val proposal1 = makeProposal("p1", profile.userId)
    val proposal2 = makeProposal("p2", profile.userId)

    val profileRepo = FakeProfileRepository(profile)
    val listingRepo = FakeListingRepository(mutableListOf(proposal1, proposal2))

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(2, state.proposals.size)
    assertTrue(state.requests.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_onlyRequests_separatesCorrectly() = runTest {
    val profile = makeProfile()
    val request1 = makeRequest("r1", profile.userId)
    val request2 = makeRequest("r2", profile.userId)

    val profileRepo = FakeProfileRepository(profile)
    val listingRepo = FakeListingRepository(storedRequests = mutableListOf(request1, request2))

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertTrue(state.proposals.isEmpty())
    assertEquals(2, state.requests.size)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun refresh_reloadsData() = runTest {
    val profile = makeProfile()
    val proposal = makeProposal("p1", profile.userId)

    val profileRepo = FakeProfileRepository(profile)
    val listingRepo = FakeListingRepository(mutableListOf(proposal))

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    // Reset flags
    profileRepo.getProfileCalled = false
    listingRepo.getListingsByUserCalled = false

    // Refresh
    vm.refresh(profile.userId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertEquals(profile, state.profile)
    assertTrue(profileRepo.getProfileCalled)
    assertTrue(listingRepo.getListingsByUserCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_differentUser_filtersListingsCorrectly() = runTest {
    val user2Profile = makeProfile("user-2", "User Two")

    val user1Proposal = makeProposal("p1", "user-1")
    val user2Proposal = makeProposal("p2", "user-2")
    val user2Request = makeRequest("r1", "user-2")

    val profileRepo = FakeProfileRepository(user2Profile)
    val listingRepo =
        FakeListingRepository(
            mutableListOf(user1Proposal, user2Proposal), mutableListOf(user2Request))

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile("user-2")
    advanceUntilIdle()

    val state = vm.uiState.value
    // Should only get user-2's listings
    assertEquals(1, state.proposals.size)
    assertEquals(1, state.requests.size)
    assertEquals(user2Proposal, state.proposals[0])
    assertEquals(user2Request, state.requests[0])
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_withRatings_displaysCorrectly() = runTest {
    val profile =
        makeProfile(tutorRating = RatingInfo(4.8, 25), studentRating = RatingInfo(3.5, 12))

    val profileRepo = FakeProfileRepository(profile)
    val listingRepo = FakeListingRepository()

    val vm = ProfileScreenViewModel(profileRepo, listingRepo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(4.8, state.profile?.tutorRating?.averageRating ?: 0.0, 0.01)
    assertEquals(25, state.profile?.tutorRating?.totalRatings)
    assertEquals(3.5, state.profile?.studentRating?.averageRating ?: 0.0, 0.01)
    assertEquals(12, state.profile?.studentRating?.totalRatings)
  }
}
