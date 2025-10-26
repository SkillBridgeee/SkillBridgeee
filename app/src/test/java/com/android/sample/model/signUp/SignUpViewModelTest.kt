package com.android.sample.model.signUp

import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.signup.Role
import com.android.sample.ui.signup.SignUpEvent
import com.android.sample.ui.signup.SignUpViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

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
    unmockkAll()
  }

  private fun createMockAuthRepository(
      shouldSucceed: Boolean = true,
      uid: String = "firebase-uid-123"
  ): AuthenticationRepository {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    if (shouldSucceed) {
      val mockUser = mockk<FirebaseUser>()
      every { mockUser.uid } returns uid
      coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns Result.success(mockUser)
    } else {
      coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns
          Result.failure(Exception("Email already in use"))
    }
    return mockAuthRepo
  }

  private fun createMockProfileRepository(): ProfileRepository {
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    coEvery { mockRepo.addProfile(any()) } returns Unit
    return mockRepo
  }

  private fun createThrowingProfileRepository(): ProfileRepository {
    val mockRepo = mockk<ProfileRepository>()
    coEvery { mockRepo.addProfile(any()) } throws Exception("add boom")
    return mockRepo
  }

  @Test
  fun initial_state_sane() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    val s = vm.state.value
    assertEquals(Role.LEARNER, s.role)
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
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("A1"))
    vm.onEvent(SignUpEvent.SurnameChanged("Doe!"))
    vm.onEvent(SignUpEvent.EmailChanged("a@b.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.AddressChanged("Anywhere"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun name_validation_accepts_unicode_letters_and_spaces() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Élise"))
    vm.onEvent(SignUpEvent.SurnameChanged("Müller Schmidt"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("passw0rd"))
    vm.onEvent(SignUpEvent.AddressChanged("Street"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("Math"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun email_validation_common_cases_and_trimming() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    // missing tld
    vm.onEvent(SignUpEvent.EmailChanged("a@b"))
    assertFalse(vm.state.value.canSubmit)
    // uppercase/subdomain + trim spaces
    vm.onEvent(SignUpEvent.EmailChanged("  USER@MAIL.Example.ORG  "))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun password_requires_min_8_and_mixed_classes() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Alan"))
    vm.onEvent(SignUpEvent.SurnameChanged("Turing"))
    vm.onEvent(SignUpEvent.AddressChanged("S2"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("Math"))
    vm.onEvent(SignUpEvent.EmailChanged("alan@code.org"))

    vm.onEvent(SignUpEvent.PasswordChanged("1234567")) // too short
    assertFalse(vm.state.value.canSubmit)
    vm.onEvent(SignUpEvent.PasswordChanged("abcdefgh")) // no digit
    assertFalse(vm.state.value.canSubmit)
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123")) // ok
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun address_and_level_must_be_non_blank_description_optional() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    // everything valid except address/level
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.DescriptionChanged("")) // optional
    assertFalse(vm.state.value.canSubmit)

    vm.onEvent(SignUpEvent.AddressChanged("X"))
    assertFalse(vm.state.value.canSubmit)
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun role_toggle_does_not_invalidate_valid_form() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    assertTrue(vm.state.value.canSubmit)

    vm.onEvent(SignUpEvent.RoleChanged(Role.TUTOR))
    assertEquals(Role.TUTOR, vm.state.value.role)
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun invalid_inputs_keep_can_submit_false_and_fixing_all_turns_true() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("A1"))
    vm.onEvent(SignUpEvent.SurnameChanged("Doe!"))
    vm.onEvent(SignUpEvent.AddressChanged(""))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged(""))
    vm.onEvent(SignUpEvent.EmailChanged("bad"))
    vm.onEvent(SignUpEvent.PasswordChanged("short1"))
    assertFalse(vm.state.value.canSubmit)

    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun full_name_is_trimmed_and_joined_with_single_space() = runTest {
    // Create a capturing mock to verify the profile data
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("   Ada   "))
    vm.onEvent(SignUpEvent.SurnameChanged("  Lovelace "))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertEquals("Ada Lovelace", capturedProfile.captured.name)
  }

  @Test
  fun submit_shows_submitting_then_success_and_stores_profile() = runTest {
    // Create a capturing mock to verify the profile data
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("Street 1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS, 3rd year"))
    vm.onEvent(SignUpEvent.DescriptionChanged("Writes algorithms"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    assertTrue(vm.state.value.canSubmit)

    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    val s = vm.state.value
    assertFalse(s.submitting)
    assertTrue(s.submitSuccess)
    assertNull(s.error)

    // Verify profile was added
    coVerify { mockRepo.addProfile(any()) }
    assertEquals("ada@math.org", capturedProfile.captured.email)
    assertEquals("firebase-uid-123", capturedProfile.captured.userId)
  }

  @Test
  fun submitting_flag_true_while_repo_is_slow() = runTest {
    // Create a slow mock repository using delay
    val mockRepo = mockk<ProfileRepository>()
    coEvery { mockRepo.addProfile(any()) } coAnswers { kotlinx.coroutines.delay(200) }

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Alan"))
    vm.onEvent(SignUpEvent.SurnameChanged("Turing"))
    vm.onEvent(SignUpEvent.AddressChanged("S2"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("Math"))
    vm.onEvent(SignUpEvent.EmailChanged("alan@code.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcdef12"))

    vm.onEvent(SignUpEvent.Submit)
    runCurrent()
    assertTrue(vm.state.value.submitting)
    advanceUntilIdle()
    assertFalse(vm.state.value.submitting)
    assertTrue(vm.state.value.submitSuccess)
  }

  @Test
  fun submit_failure_surfaces_error_and_validate_clears_it() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createThrowingProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Alan"))
    vm.onEvent(SignUpEvent.SurnameChanged("Turing"))
    vm.onEvent(SignUpEvent.AddressChanged("S2"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("Math"))
    vm.onEvent(SignUpEvent.EmailChanged("alan@code.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcdef12"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()
    assertFalse(vm.state.value.submitSuccess)
    assertNotNull(vm.state.value.error)

    vm.onEvent(SignUpEvent.EmailChanged("alan@computing.org"))
    assertNull(vm.state.value.error)
  }

  @Test
  fun changing_any_field_after_success_keeps_success_true_until_next_submit() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()
    assertTrue(vm.state.value.submitSuccess)

    // Change a field -> validate runs, success flag remains true (until next submit call resets it)
    vm.onEvent(SignUpEvent.AddressChanged("S2"))
    assertTrue(vm.state.value.submitSuccess)
  }

  @Test
  fun firebase_auth_failure_shows_error() = runTest {
    val mockProfileRepo = createMockProfileRepository()
    val vm = SignUpViewModel(createMockAuthRepository(shouldSucceed = false), mockProfileRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("existing@email.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertFalse(vm.state.value.submitSuccess)
    assertNotNull(vm.state.value.error)
    assertTrue(
        vm.state.value.error!!.contains("Email already in use") ||
            vm.state.value.error!!.contains("already registered"))

    // Verify profile repository was never called since auth failed
    coVerify(exactly = 0) { mockProfileRepo.addProfile(any()) }
  }

  @Test
  fun profile_creation_failure_after_auth_success_shows_specific_error() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createThrowingProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertFalse(vm.state.value.submitSuccess)
    assertNotNull(vm.state.value.error)
    assertTrue(vm.state.value.error!!.contains("Account created but profile failed"))
  }
}
