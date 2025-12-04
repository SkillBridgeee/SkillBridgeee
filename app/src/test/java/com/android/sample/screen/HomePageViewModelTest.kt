package com.android.sample.screen

import android.app.Application
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.HomePage.MainPageViewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = MainPageViewModelTest.TestApp::class)
class MainPageViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  // Custom test application that initializes Firebase
  class TestApp : Application() {
    override fun onCreate() {
      super.onCreate()
      try {
        FirebaseApp.initializeApp(this)
      } catch (_: IllegalStateException) {
        // Firebase already initialized, ignore
      }
    }
  }

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ---------- Fake Repositories ----------

  private open class FakeProfileRepository(private val profiles: List<Profile>) :
      ProfileRepository {
    override fun getNewUid() = "fake"

    override fun getCurrentUserId() = "test-user-id"

    override suspend fun getProfile(userId: String): Profile? =
        profiles.find { it.userId == userId }

    override suspend fun getAllProfiles(): List<Profile> = profiles

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getProfileById(userId: String) = getProfile(userId) ?: error("not found")

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      /* no-op for this test */
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      /* no-op for this test */
    }
  }

  private open class FakeListingRepository(private val listings: List<Proposal>) :
      ListingRepository {
    override fun getNewUid() = "fake"

    override suspend fun getAllListings() = listings // List<Proposal> is fine (covariant)

    override suspend fun getProposals() = listings

    override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

    override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun getListing(listingId: String) = null

    override suspend fun getListingsByUser(userId: String) =
        emptyList<com.android.sample.model.listing.Listing>()

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

  // ---------- Helpers ----------

  private fun profile(id: String, name: String) =
      Profile(userId = id, name = name, email = "$name@mail.com", description = "")

  private fun proposal(userId: String) =
      Proposal(listingId = "l-$userId", creatorUserId = userId, skill = Skill(), description = "")

  // ---------- Tests ----------

  @Test
  fun `load populates tutor list based on proposals`() = runTest {
    // Clear session to avoid Firebase Auth issues
    com.android.sample.model.authentication.UserSessionManager.clearSession()

    val profiles = listOf(profile("u1", "Alice"), profile("u2", "Bob"))

    val proposals = listOf(proposal("u1"), proposal("u2"))

    val vm = MainPageViewModel(FakeProfileRepository(profiles), FakeListingRepository(proposals))

    advanceUntilIdle()
    val state = vm.uiState.first()

    // We no longer expose tutors; we expose proposals directly
    Assert.assertEquals(2, state.proposals.size)
    val listingIds = state.proposals.map { it.listingId }.toSet()
    Assert.assertEquals(setOf("l-u1", "l-u2"), listingIds)
  }

  @Test
  fun `default welcome message when no logged user`() = runTest {
    // Ensure no user is logged in
    com.android.sample.model.authentication.UserSessionManager.clearSession()

    val vm =
        MainPageViewModel(FakeProfileRepository(emptyList()), FakeListingRepository(emptyList()))

    advanceUntilIdle()
    val state = vm.uiState.first()

    Assert.assertEquals("Welcome back!", state.welcomeMessage)
  }

  @Test
  fun `gracefully handles repository failure`() = runTest {
    // Clear session to ensure clean state
    com.android.sample.model.authentication.UserSessionManager.clearSession()

    val failingListings =
        object : FakeListingRepository(emptyList()) {
          override suspend fun getProposals(): List<Proposal> {
            throw IllegalStateException("Test crash")
          }
        }

    val vm = MainPageViewModel(FakeProfileRepository(emptyList()), failingListings)

    advanceUntilIdle()
    val state = vm.uiState.first()

    // On failure we fall back to the default HomeUiState()
    Assert.assertTrue(state.proposals.isEmpty())
    Assert.assertEquals("Welcome back!", state.welcomeMessage)
  }
}
