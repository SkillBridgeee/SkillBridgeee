package com.android.sample.screen

import com.android.sample.model.map.Location
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

class MyProfileViewModelTest {

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

  // -------- Fake repository ------------------------------------------------------

  private class FakeRepo(private var storedProfile: Profile? = null) : ProfileRepository {
    var updatedProfile: Profile? = null
    var updateCalled = false
    var getProfileCalled = false

    override fun getNewUid(): String = "fake"

    override suspend fun getProfile(userId: String): Profile {
      getProfileCalled = true
      return storedProfile ?: error("not found")
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

    override suspend fun getProfileById(userId: String) = storedProfile ?: error("not found")

    override suspend fun getSkillsForUser(userId: String) =
        emptyList<com.android.sample.model.skill.Skill>()
  }

  // -------- Helpers ------------------------------------------------------

  private fun makeProfile(
      id: String = "1",
      name: String = "Kendrick",
      email: String = "kdot@example.com",
      location: Location = Location(name = "Compton"),
      desc: String = "Rap tutor"
  ) = Profile(id, name, email, location = location, description = desc)

  private fun newVm(repo: ProfileRepository = FakeRepo()) = MyProfileViewModel(repo)

  // -------- Tests --------------------------------------------------------

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadProfile_populatesUiState() = runTest {
    val profile = makeProfile()
    val repo = FakeRepo(profile)
    val vm = newVm(repo)

    vm.loadProfile(profile.userId)
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals(profile.name, ui.name)
    assertEquals(profile.email, ui.email)
    assertEquals(profile.location, ui.location)
    assertEquals(profile.description, ui.description)
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
  fun setLocation_updatesLocation_andErrorIfBlank() {
    val vm = newVm()

    vm.setLocation("Paris")
    assertEquals("Paris", vm.uiState.value.location?.name)
    assertNull(vm.uiState.value.invalidLocationMsg)

    vm.setLocation("")
    assertNull(vm.uiState.value.location)
    assertEquals("Location cannot be empty", vm.uiState.value.invalidLocationMsg)
  }

  @Test
  fun setDescription_updatesDesc_andErrorIfBlank() {
    val vm = newVm()

    vm.setDescription("Music mentor")
    assertEquals("Music mentor", vm.uiState.value.description)
    assertNull(vm.uiState.value.invalidDescMsg)

    vm.setDescription("")
    assertEquals("Description cannot be empty", vm.uiState.value.invalidDescMsg)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun editProfile_doesNotUpdate_whenInvalid() = runTest {
    val repo = FakeRepo()
    val vm = newVm(repo)

    // no name, invalid by default
    vm.editProfile("1")
    advanceUntilIdle()

    assertFalse(repo.updateCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun editProfile_updatesRepository_whenValid() = runTest {
    val repo = FakeRepo()
    val vm = newVm(repo)

    vm.setName("Kendrick Lamar")
    vm.setEmail("kdot@gmail.com")
    vm.setLocation("Compton")
    vm.setDescription("Hip-hop tutor")

    vm.editProfile("123")
    advanceUntilIdle()

    assertTrue(repo.updateCalled)
    val updated = repo.updatedProfile!!
    assertEquals("Kendrick Lamar", updated.name)
    assertEquals("kdot@gmail.com", updated.email)
    assertEquals("Compton", updated.location.name)
    assertEquals("Hip-hop tutor", updated.description)
  }

  @Test
  fun setError_setsAllErrorMessages_whenFieldsInvalid() {
    val vm = newVm()
    vm.setError()

    val ui = vm.uiState.value
    assertEquals("Name cannot be empty", ui.invalidNameMsg)
    assertEquals("Email is not in the right format", ui.invalidEmailMsg)
    assertEquals("Location cannot be empty", ui.invalidLocationMsg)
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
  }

  @Test
  fun isValid_returnsTrue_onlyWhenAllFieldsAreCorrect() {
    val vm = newVm()

    vm.setName("Test")
    vm.setEmail("test@mail.com")
    vm.setLocation("Paris")
    vm.setDescription("Teacher")

    assertTrue(vm.uiState.value.isValid)

    vm.setEmail("wrong")
    assertFalse(vm.uiState.value.isValid)
  }
}
