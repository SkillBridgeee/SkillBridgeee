package com.android.sample.screen

import com.android.sample.model.authentication.FirebaseTestRule
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.profile.MyProfileViewModel
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MyProfileViewModelTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // -------- Fake repositories ------------------------------------------------------

  private open class FakeProfileRepo(private var storedProfile: Profile? = null) :
      ProfileRepository {
    var updatedProfile: Profile? = null
    var updateCalled = false
    var getProfileCalled = false

    override fun getNewUid(): String = "fake"

    override suspend fun getProfile(userId: String): Profile {
      getProfileCalled = true
      return storedProfile ?: error("Profile not found")
    }

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {
      updateCalled = true
      updatedProfile = profile
    }

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> = emptyList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String) =
        storedProfile ?: error("Profile not found")

    override suspend fun getSkillsForUser(userId: String) =
        emptyList<com.android.sample.model.skill.Skill>()
  }

  private class FakeLocationRepo(
      private val results: List<Location> =
          listOf(Location(name = "Paris"), Location(name = "Rome"))
  ) : LocationRepository {
    var lastQuery: String? = null
    var searchCalled = false

    override suspend fun search(query: String): List<Location> {
      lastQuery = query
      searchCalled = true
      return if (query.isNotBlank()) results else emptyList()
    }
  }

  // Minimal fake ListingRepository to satisfy the ViewModel dependency
  private class FakeListingRepo : ListingRepository {
    override fun getNewUid(): String = "fake-listing-id"

    override suspend fun getAllListings(): List<Listing> = emptyList()

    override suspend fun getProposals(): List<Proposal> = emptyList()

    override suspend fun getRequests(): List<Request> = emptyList()

    override suspend fun getListing(listingId: String): Listing? = null

    override suspend fun getListingsByUser(userId: String): List<Listing> = emptyList()

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill): List<Listing> =
        emptyList()

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
        emptyList()
  }

  // -------- Helpers ------------------------------------------------------

  private fun makeProfile(
      id: String = "1",
      name: String = "Kendrick",
      email: String = "kdot@example.com",
      location: Location = Location(name = "Compton"),
      desc: String = "Rap tutor"
  ) = Profile(id, name, email, location = location, description = desc)

  private fun newVm(
      repo: ProfileRepository = FakeProfileRepo(),
      locRepo: LocationRepository = FakeLocationRepo(),
      listingRepo: ListingRepository = FakeListingRepo(),
      userId: String = "testUid"
  ) = MyProfileViewModel(repo, locRepo, listingRepo, userId)

  // -------- Tests --------------------------------------------------------

  @Test
  fun loadProfile_populatesUiState() = runTest {
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo)

    vm.loadProfile()
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals(profile.name, ui.name)
    assertEquals(profile.email, ui.email)
    assertEquals(profile.location, ui.selectedLocation)
    assertEquals(profile.description, ui.description)
    assertFalse(ui.isLoading)
    assertNull(ui.loadError)
    assertTrue(repo.getProfileCalled)
  }

  @Test
  fun setName_updatesName_and_setsErrorIfBlank() {
    val vm = newVm()

    vm.setName("K Dot")
    assertEquals("K Dot", vm.uiState.value.name)
    assertNull(vm.uiState.value.invalidNameMsg)

    vm.setName("")
    assertEquals("Name cannot be empty", vm.uiState.value.invalidNameMsg)
  }

  @Test
  fun setEmail_validatesFormat_andRequired() {
    val vm = newVm()

    vm.setEmail("")
    assertEquals("Email cannot be empty", vm.uiState.value.invalidEmailMsg)

    vm.setEmail("invalid-email")
    assertEquals("Email is not in the right format", vm.uiState.value.invalidEmailMsg)

    vm.setEmail("good@mail.com")
    assertNull(vm.uiState.value.invalidEmailMsg)
  }

  @Test
  fun setLocation_updatesLocation_andClearsError() {
    val vm = newVm()

    vm.setLocation(Location(name = "Paris"))
    val ui = vm.uiState.value
    assertEquals("Paris", ui.selectedLocation?.name)
    assertNull(ui.invalidLocationMsg)
  }

  @Test
  fun setDescription_updatesDesc_and_setsErrorIfBlank() {
    val vm = newVm()

    vm.setDescription("Music mentor")
    assertEquals("Music mentor", vm.uiState.value.description)
    assertNull(vm.uiState.value.invalidDescMsg)

    vm.setDescription("")
    assertEquals("Description cannot be empty", vm.uiState.value.invalidDescMsg)
  }

  @Test
  fun setError_setsAllErrorMessages_whenFieldsInvalid() {
    val vm = newVm()
    vm.setError()

    val ui = vm.uiState.value
    assertEquals("Name cannot be empty", ui.invalidNameMsg)
    assertEquals("Email cannot be empty", ui.invalidEmailMsg)
    assertEquals("Location cannot be empty", ui.invalidLocationMsg)
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
  }

  @Test
  fun isValid_returnsTrue_onlyWhenAllFieldsAreCorrect() {
    val vm = newVm()

    vm.setName("Test")
    vm.setEmail("test@mail.com")
    vm.setLocation(Location(name = "Paris"))
    vm.setDescription("Teacher")

    assertTrue(vm.uiState.value.isValid)

    vm.setEmail("wrong")
    assertFalse(vm.uiState.value.isValid)
  }

  @Test
  fun setLocationQuery_updatesQuery_andFetchesResults() = runTest {
    val locRepo = FakeLocationRepo()
    val vm = newVm(locRepo = locRepo)

    vm.setLocationQuery("Par")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals("Par", ui.locationQuery)
    assertTrue(locRepo.searchCalled)
    assertEquals(2, ui.locationSuggestions.size)
    assertEquals("Paris", ui.locationSuggestions[0].name)
  }

  @Test
  fun setLocationQuery_emptyQuery_setsError_andClearsSuggestions() = runTest {
    val locRepo = FakeLocationRepo()
    val vm = newVm(locRepo = locRepo)

    vm.setLocationQuery("")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals("Location cannot be empty", ui.invalidLocationMsg)
    assertTrue(ui.locationSuggestions.isEmpty())
  }

  @Test
  fun editProfile_doesNotUpdate_whenInvalid() = runTest {
    val repo = FakeProfileRepo()
    val vm = newVm(repo)

    // invalid by default
    vm.editProfile()
    advanceUntilIdle()

    assertFalse(repo.updateCalled)
  }

  @Test
  fun editProfile_updatesRepository_whenValid() = runTest {
    val repo = FakeProfileRepo()
    val vm = newVm(repo)

    vm.setName("Kendrick Lamar")
    vm.setEmail("kdot@gmail.com")
    vm.setLocation(Location(name = "Compton"))
    vm.setDescription("Hip-hop tutor")

    vm.editProfile()
    advanceUntilIdle()

    assertTrue(repo.updateCalled)
    val updated = repo.updatedProfile!!
    assertEquals("Kendrick Lamar", updated.name)
    assertEquals("kdot@gmail.com", updated.email)
    assertEquals("Compton", updated.location.name)
    assertEquals("Hip-hop tutor", updated.description)
  }

  @Test
  fun editProfile_handlesRepositoryException_gracefully() = runTest {
    val failingRepo =
        object : FakeProfileRepo() {
          override suspend fun updateProfile(userId: String, profile: Profile) {
            throw RuntimeException("Update failed")
          }
        }
    val vm = newVm(failingRepo)

    vm.setName("Good")
    vm.setEmail("good@mail.com")
    vm.setLocation(Location(name = "LA"))
    vm.setDescription("Mentor")

    // Should not crash
    vm.editProfile()
    advanceUntilIdle()

    assertTrue(true)
  }

  @Test
  fun loadProfile_withUserId_loadsCorrectProfile() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo)

    // When - load profile with specific userId
    vm.loadProfile("specificUserId")
    advanceUntilIdle()

    // Then - profile should be loaded
    val ui = vm.uiState.value
    assertEquals(profile.name, ui.name)
    assertEquals(profile.email, ui.email)
    assertEquals(profile.location, ui.selectedLocation)
    assertEquals(profile.description, ui.description)
    assertTrue(repo.getProfileCalled)
  }

  @Test
  fun loadProfile_storesUserIdInState() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo, userId = "originalUserId")

    // When - load profile with different userId
    vm.loadProfile("differentUserId")
    advanceUntilIdle()

    // Then - UI state should have the new userId
    val ui = vm.uiState.value
    assertEquals("differentUserId", ui.userId)
  }

  @Test
  fun loadProfile_withoutParameter_usesDefaultUserId() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo, userId = "defaultUserId")

    // When - load profile without parameter
    vm.loadProfile()
    advanceUntilIdle()

    // Then - UI state should have the default userId
    val ui = vm.uiState.value
    assertEquals("defaultUserId", ui.userId)
  }

  @Test
  fun editProfile_usesUserIdFromState() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo, userId = "originalUserId")

    // Load profile with different userId
    vm.loadProfile("targetUserId")
    advanceUntilIdle()

    // Set valid data
    vm.setName("New Name")
    vm.setEmail("new@email.com")
    vm.setLocation(Location(name = "New Location"))
    vm.setDescription("New Description")

    // When - edit profile
    vm.editProfile()
    advanceUntilIdle()

    // Then - should update with userId from state, not original VM userId
    val updated = repo.updatedProfile
    assertNotNull(updated)
    assertEquals("targetUserId", updated?.userId)
    assertEquals("New Name", updated?.name)
  }

  @Test
  fun loadUserListings_handlesRepositoryException_setsListingsError() = runTest {
    // Listing repo that throws to exercise the catch branch
    val failingListingRepo =
        object : ListingRepository by FakeListingRepo() {
          override suspend fun getListingsByUser(userId: String): List<Listing> {
            throw RuntimeException("Listings fetch failed")
          }
        }

    val repo = FakeProfileRepo(makeProfile())
    val vm = newVm(repo = repo, listingRepo = failingListingRepo)

    // Trigger listings load
    vm.loadUserListings("ownerId")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertTrue(ui.listings.isEmpty())
    assertFalse(ui.listingsLoading)
    assertEquals("Failed to load listings.", ui.listingsLoadError)
  }

  @Test
  fun setError_setsEmailFormatError_whenEmailMalformed_and_setsOtherErrors() {
    val vm = newVm()

    // Set malformed email and leave other fields empty
    vm.setEmail("not-an-email")
    vm.setError()

    val ui = vm.uiState.value
    assertEquals("Email is not in the right format", ui.invalidEmailMsg)
    assertEquals("Name cannot be empty", ui.invalidNameMsg)
    assertEquals("Location cannot be empty", ui.invalidLocationMsg)
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
  }

  @Test
  fun isValid_false_whenMissingLocationOrDescription_and_true_afterSettingBoth() {
    val vm = newVm()

    vm.setName("Test")
    vm.setEmail("test@mail.com")
    // no location, no description -> invalid
    assertFalse(vm.uiState.value.isValid)

    vm.setDescription("Teacher")
    // still missing location -> invalid
    assertFalse(vm.uiState.value.isValid)

    vm.setLocation(Location(name = "Paris"))
    // now all required fields present and valid -> valid
    assertTrue(vm.uiState.value.isValid)
  }
}
