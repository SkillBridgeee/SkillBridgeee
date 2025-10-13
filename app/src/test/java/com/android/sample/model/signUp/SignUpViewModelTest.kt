package com.android.sample.model.signUp

import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class CapturingRepo : ProfileRepository {
  val added = mutableListOf<Profile>()
  private var uid = 1

  override fun getNewUid(): String = "test-$uid".also { uid++ }

  override suspend fun getProfile(userId: String): Profile = added.first { it.userId == userId }

  override suspend fun addProfile(profile: Profile) {
    added += profile
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles(): List<Profile> = added.toList()

  override suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile> = emptyList()

  override suspend fun getProfileById(userId: String): Profile {
    TODO("Not yet implemented")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    TODO("Not yet implemented")
  }
}

private class SlowRepo : ProfileRepository {
  override fun getNewUid(): String = "slow-1"

  override suspend fun getProfile(userId: String): Profile = error("unused")

  override suspend fun addProfile(profile: Profile) {
    delay(200)
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles(): List<Profile> = emptyList()

  override suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile> = emptyList()

  override suspend fun getProfileById(userId: String): Profile {
    TODO("Not yet implemented")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    TODO("Not yet implemented")
  }
}

private class ThrowingRepo : ProfileRepository {
  override fun getNewUid(): String = "x"

  override suspend fun getProfile(userId: String): Profile = error("unused")

  override suspend fun addProfile(profile: Profile) {
    error("add boom")
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles(): List<Profile> = emptyList()

  override suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile> = emptyList()

  override suspend fun getProfileById(userId: String): Profile {
    TODO("Not yet implemented")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    TODO("Not yet implemented")
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initial_state_sane() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    val s = vm.state.value
    assertEquals(_root_ide_package_.com.android.sample.ui.signup.Role.LEARNER, s.role)
    assertFalse(s.canSubmit)
    assertFalse(s.submitting)
    assertFalse(s.submitSuccess)
    assertNull(s.error)
    assertEquals("", s.name)
    assertEquals("", s.surname)
    assertEquals("", s.email)
    assertEquals("", s.password)
  }

  @Test
  fun name_validation_rejects_numbers_and_specials() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("A1"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Doe!"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("a@b.com"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("Anywhere"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun name_validation_accepts_unicode_letters_and_spaces() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Élise"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged(
            "Müller Schmidt"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged(
            "user@example.com"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("passw0rd"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("Street"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("Math"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun email_validation_common_cases_and_trimming() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Ada"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S1"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))

    // missing tld
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("a@b"))
    assertFalse(vm.state.value.canSubmit)
    // uppercase/subdomain + trim spaces
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged(
            "  USER@MAIL.Example.ORG  "))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun password_requires_min_8_and_mixed_classes() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Alan"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Turing"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S2"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("Math"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("alan@code.org"))

    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged(
            "1234567")) // too short
    assertFalse(vm.state.value.canSubmit)
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged(
            "abcdefgh")) // no digit
    assertFalse(vm.state.value.canSubmit)
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged(
            "abcde123")) // ok
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun address_and_level_must_be_non_blank_description_optional() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    // everything valid except address/level
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Ada"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.DescriptionChanged(
            "")) // optional
    assertFalse(vm.state.value.canSubmit)

    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("X"))
    assertFalse(vm.state.value.canSubmit)
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun role_toggle_does_not_invalidate_valid_form() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Ada"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S1"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    assertTrue(vm.state.value.canSubmit)

    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.RoleChanged(
            _root_ide_package_.com.android.sample.ui.signup.Role.TUTOR))
    assertEquals(_root_ide_package_.com.android.sample.ui.signup.Role.TUTOR, vm.state.value.role)
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun invalid_inputs_keep_can_submit_false_and_fixing_all_turns_true() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(CapturingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("A1"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Doe!"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged(""))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged(""))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("bad"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("short1"))
    assertFalse(vm.state.value.canSubmit)

    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Ada"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun full_name_is_trimmed_and_joined_with_single_space() = runTest {
    val repo = CapturingRepo()
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(repo)
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("   Ada   "))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("  Lovelace "))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S1"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.Submit)
    advanceUntilIdle()
    assertEquals("Ada Lovelace", repo.added.single().name)
  }

  @Test
  fun submit_shows_submitting_then_success_and_stores_profile() = runTest {
    val repo = CapturingRepo()
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(repo)
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Ada"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("Street 1"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged(
            "CS, 3rd year"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.DescriptionChanged(
            "Writes algorithms"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    assertTrue(vm.state.value.canSubmit)

    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.Submit)
    advanceUntilIdle()

    val s = vm.state.value
    assertFalse(s.submitting)
    assertTrue(s.submitSuccess)
    assertNull(s.error)
    assertEquals(1, repo.added.size)
    assertEquals("ada@math.org", repo.added[0].email)
  }

  @Test
  fun submitting_flag_true_while_repo_is_slow() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(SlowRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Alan"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Turing"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S2"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("Math"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("alan@code.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcdef12"))

    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.Submit)
    runCurrent()
    assertTrue(vm.state.value.submitting)
    advanceUntilIdle()
    assertFalse(vm.state.value.submitting)
    assertTrue(vm.state.value.submitSuccess)
  }

  @Test
  fun submit_failure_surfaces_error_and_validate_clears_it() = runTest {
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(ThrowingRepo())
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Alan"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Turing"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S2"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("Math"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("alan@code.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcdef12"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.Submit)
    advanceUntilIdle()
    assertFalse(vm.state.value.submitSuccess)
    assertNotNull(vm.state.value.error)

    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged(
            "alan@computing.org"))
    assertNull(vm.state.value.error)
  }

  @Test
  fun changing_any_field_after_success_keeps_success_true_until_next_submit() = runTest {
    val repo = CapturingRepo()
    val vm = _root_ide_package_.com.android.sample.ui.signup.SignUpViewModel(repo)
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.NameChanged("Ada"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S1"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(
        _root_ide_package_.com.android.sample.ui.signup.SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.Submit)
    advanceUntilIdle()
    assertTrue(vm.state.value.submitSuccess)

    // Change a field -> validate runs, success flag remains true (until next submit call resets it)
    vm.onEvent(_root_ide_package_.com.android.sample.ui.signup.SignUpEvent.AddressChanged("S2"))
    assertTrue(vm.state.value.submitSuccess)
  }
}
