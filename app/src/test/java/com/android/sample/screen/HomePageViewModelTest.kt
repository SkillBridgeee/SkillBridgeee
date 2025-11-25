package com.android.sample.screen

import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.HomePage.MainPageViewModel
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
@Config(manifest = Config.NONE)
class MainPageViewModelTest {

  private val dispatcher = StandardTestDispatcher()

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

  private class FakeListingRepository(private val listings: List<Proposal>) : ListingRepository {
    override fun getNewUid() = "fake"

    override suspend fun getAllListings() = listings

    override suspend fun getProposals() = listings

    override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

    override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun getListing(listingId: String) = null

    override suspend fun getListingsByUser(userId: String) = emptyList<Proposal>()

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
    val profiles = listOf(profile("u1", "Alice"), profile("u2", "Bob"))

    val proposals = listOf(proposal("u1"), proposal("u2"))

    val vm = MainPageViewModel(FakeProfileRepository(profiles), FakeListingRepository(proposals))

    advanceUntilIdle()
    val state = vm.uiState.first()

    Assert.assertEquals(2, state.tutors.size)
    Assert.assertEquals("Alice", state.tutors[0].name)
    Assert.assertEquals("Bob", state.tutors[1].name)
  }

  @Test
  fun `default welcome message when no logged user`() = runTest {
    val vm =
        MainPageViewModel(FakeProfileRepository(emptyList()), FakeListingRepository(emptyList()))

    advanceUntilIdle()
    val state = vm.uiState.first()

    Assert.assertEquals("Welcome back!", state.welcomeMessage)
  }

  @Test
  fun `gracefully handles repository failure`() = runTest {
    val failingProfiles =
        object : FakeProfileRepository(emptyList()) {
          override suspend fun getAllProfiles(): List<Profile> {
            throw IllegalStateException("Test crash")
          }
        }

    val vm = MainPageViewModel(failingProfiles, FakeListingRepository(emptyList()))

    advanceUntilIdle()
    val state = vm.uiState.first()

    Assert.assertTrue(state.tutors.isEmpty())
    Assert.assertEquals("Welcome back!", state.welcomeMessage)
  }
}
