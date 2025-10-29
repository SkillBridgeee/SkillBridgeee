package com.android.sample.screen

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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyProfileViewModelTest {

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
      userId: String = "testUid"
  ) = MyProfileViewModel(repo, locRepo, userId)

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
}
