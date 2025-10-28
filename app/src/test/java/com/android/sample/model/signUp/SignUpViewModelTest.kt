package com.android.sample.model.signUp

import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.signup.Role
import com.android.sample.ui.signup.SignUpEvent
import com.android.sample.ui.signup.SignUpViewModel
import com.google.firebase.auth.FirebaseAuthException
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

  @Test
  fun email_validation_rejects_multiple_at_signs() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.EmailChanged("user@@example.com"))
    assertFalse(vm.state.value.canSubmit)

    vm.onEvent(SignUpEvent.EmailChanged("user@exam@ple.com"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun email_validation_rejects_no_at_sign() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.EmailChanged("userexample.com"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun email_validation_rejects_empty_local_part() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.EmailChanged("@example.com"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun email_validation_rejects_empty_domain() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.EmailChanged("user@"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun email_validation_rejects_domain_without_dot() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.EmailChanged("user@example"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun password_validation_rejects_only_letters() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))

    vm.onEvent(SignUpEvent.PasswordChanged("abcdefghij"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun password_validation_rejects_only_digits() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))

    vm.onEvent(SignUpEvent.PasswordChanged("12345678"))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun password_validation_accepts_exactly_8_chars_with_letter_and_digit() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))

    vm.onEvent(SignUpEvent.PasswordChanged("abcdef12"))
    assertTrue(vm.state.value.canSubmit)
  }

  @Test
  fun name_validation_rejects_empty_after_trim() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.NameChanged("   "))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun surname_validation_rejects_empty_after_trim() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.SurnameChanged("   "))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun level_of_education_validation_rejects_empty_after_trim() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))

    vm.onEvent(SignUpEvent.LevelOfEducationChanged("   "))
    assertFalse(vm.state.value.canSubmit)
  }

  @Test
  fun description_is_optional_and_stored_trimmed() = runTest {
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.DescriptionChanged("  Some description  "))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertEquals("Some description", capturedProfile.captured.description)
  }

  @Test
  fun address_is_stored_in_location_name_trimmed() = runTest {
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("  123 Main Street  "))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertEquals("123 Main Street", capturedProfile.captured.location.name)
  }

  @Test
  fun email_is_stored_trimmed_in_profile() = runTest {
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("  ada@math.org  "))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertEquals("ada@math.org", capturedProfile.captured.email)
  }

  @Test
  fun level_of_education_is_stored_trimmed_in_profile() = runTest {
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val vm = SignUpViewModel(createMockAuthRepository(), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("  CS, 3rd year  "))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertEquals("CS, 3rd year", capturedProfile.captured.levelOfEducation)
  }

  @Test
  fun firebase_auth_error_email_already_in_use_shows_friendly_message() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
    every { mockException.message } returns
        "The email address is already in use by another account."

    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns Result.failure(mockException)

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
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
    assertEquals("This email is already registered", vm.state.value.error)
  }

  @Test
  fun firebase_auth_error_badly_formatted_email_shows_friendly_message() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_INVALID_EMAIL"
    every { mockException.message } returns "The email address is badly formatted."

    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns Result.failure(mockException)

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    // Use an email that passes ViewModel validation but Firebase might reject
    vm.onEvent(SignUpEvent.EmailChanged("user@example.com"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertFalse(vm.state.value.submitSuccess)
    assertNotNull(vm.state.value.error)
    assertEquals("Invalid email format", vm.state.value.error)
  }

  @Test
  fun firebase_auth_error_weak_password_shows_friendly_message() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_WEAK_PASSWORD"
    every { mockException.message } returns "Password is too weak"

    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns Result.failure(mockException)

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
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
    assertEquals("Password is too weak", vm.state.value.error)
  }

  @Test
  fun firebase_auth_generic_error_shows_error_message() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns
        Result.failure(Exception("Some other Firebase error"))

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
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
    assertEquals("Some other Firebase error", vm.state.value.error)
  }

  @Test
  fun firebase_auth_error_with_null_message_shows_default_error() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    val exceptionWithNullMessage =
        object : Exception() {
          override val message: String? = null
        }
    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } returns
        Result.failure(exceptionWithNullMessage)

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
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
    assertEquals("Sign up failed", vm.state.value.error)
  }

  @Test
  fun unexpected_throwable_in_submit_shows_error() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } throws
        RuntimeException("Unexpected error")

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
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
    assertEquals("Unexpected error", vm.state.value.error)
  }

  @Test
  fun unexpected_throwable_with_null_message_shows_unknown_error() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>()
    val throwableWithNullMessage =
        object : Throwable() {
          override val message: String? = null
        }
    coEvery { mockAuthRepo.signUpWithEmail(any(), any()) } throws throwableWithNullMessage

    val vm = SignUpViewModel(mockAuthRepo, createMockProfileRepository())
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
    assertEquals("Unknown error", vm.state.value.error)
  }

  @Test
  fun role_event_updates_state_correctly() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())

    assertEquals(Role.LEARNER, vm.state.value.role)

    vm.onEvent(SignUpEvent.RoleChanged(Role.TUTOR))
    assertEquals(Role.TUTOR, vm.state.value.role)

    vm.onEvent(SignUpEvent.RoleChanged(Role.LEARNER))
    assertEquals(Role.LEARNER, vm.state.value.role)
  }

  @Test
  fun all_field_events_update_state_correctly() = runTest {
    val vm = SignUpViewModel(createMockAuthRepository(), createMockProfileRepository())

    vm.onEvent(SignUpEvent.NameChanged("John"))
    assertEquals("John", vm.state.value.name)

    vm.onEvent(SignUpEvent.SurnameChanged("Doe"))
    assertEquals("Doe", vm.state.value.surname)

    vm.onEvent(SignUpEvent.AddressChanged("123 Main St"))
    assertEquals("123 Main St", vm.state.value.address)

    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS, 2nd"))
    assertEquals("CS, 2nd", vm.state.value.levelOfEducation)

    vm.onEvent(SignUpEvent.DescriptionChanged("A student"))
    assertEquals("A student", vm.state.value.description)

    vm.onEvent(SignUpEvent.EmailChanged("john@example.com"))
    assertEquals("john@example.com", vm.state.value.email)

    vm.onEvent(SignUpEvent.PasswordChanged("password123"))
    assertEquals("password123", vm.state.value.password)
  }

  @Test
  fun submit_when_invalid_does_not_call_repository() = runTest {
    val mockAuthRepo = mockk<AuthenticationRepository>(relaxed = true)
    val mockProfileRepo = mockk<ProfileRepository>(relaxed = true)

    val vm = SignUpViewModel(mockAuthRepo, mockProfileRepo)

    // Verify form is invalid
    assertFalse(vm.state.value.canSubmit)

    // Don't fill in required fields - form is invalid
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    // The ViewModel should check canSubmit and NOT call the repository when form is invalid
    coVerify(exactly = 0) { mockAuthRepo.signUpWithEmail(any(), any()) }
  }

  @Test
  fun profile_uses_firebase_uid_as_userId() = runTest {
    val mockRepo = mockk<ProfileRepository>(relaxed = true)
    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockRepo.addProfile(capture(capturedProfile)) } returns Unit

    val customUid = "custom-firebase-uid-xyz"
    val vm = SignUpViewModel(createMockAuthRepository(uid = customUid), mockRepo)
    vm.onEvent(SignUpEvent.NameChanged("Ada"))
    vm.onEvent(SignUpEvent.SurnameChanged("Lovelace"))
    vm.onEvent(SignUpEvent.AddressChanged("S1"))
    vm.onEvent(SignUpEvent.LevelOfEducationChanged("CS"))
    vm.onEvent(SignUpEvent.EmailChanged("ada@math.org"))
    vm.onEvent(SignUpEvent.PasswordChanged("abcde123"))
    vm.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    assertEquals(customUid, capturedProfile.captured.userId)
  }
}
