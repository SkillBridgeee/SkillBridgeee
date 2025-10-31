package com.android.sample.ui.signup

import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.user.ProfileRepository
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SignUpUseCaseTest {

  private lateinit var mockAuthRepository: AuthenticationRepository
  private lateinit var mockProfileRepository: ProfileRepository
  private lateinit var signUpUseCase: SignUpUseCase

  @Before
  fun setUp() {
    mockAuthRepository = mockk()
    mockProfileRepository = mockk()
    signUpUseCase = SignUpUseCase(mockAuthRepository, mockProfileRepository)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  private fun createTestRequest(
      name: String = "John",
      surname: String = "Doe",
      email: String = "john@example.com",
      password: String = "password123!",
      levelOfEducation: String = "CS",
      description: String = "Student",
      address: String = "123 Main St"
  ): SignUpRequest {
    return SignUpRequest(
        name = name,
        surname = surname,
        email = email,
        password = password,
        levelOfEducation = levelOfEducation,
        description = description,
        address = address)
  }

  // Tests for already authenticated users (Google Sign-In flow)

  @Test
  fun execute_authenticatedUser_createsProfileOnly() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "google-user-123"
    every { mockAuthRepository.getCurrentUser() } returns mockUser
    coEvery { mockProfileRepository.addProfile(any()) } returns Unit

    val request = createTestRequest(email = "google@gmail.com")
    val result = signUpUseCase.execute(request)

    // Should create profile
    coVerify(exactly = 1) { mockProfileRepository.addProfile(any()) }
    // Should NOT create auth account
    coVerify(exactly = 0) { mockAuthRepository.signUpWithEmail(any(), any()) }
    // Should return success
    assertTrue(result is SignUpResult.Success)
  }

  @Test
  fun execute_authenticatedUser_profileCreationFails_returnsError() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "google-user-456"
    every { mockAuthRepository.getCurrentUser() } returns mockUser
    coEvery { mockProfileRepository.addProfile(any()) } throws
        Exception("Database connection failed")

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals(
        "Profile creation failed: Database connection failed",
        (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_authenticatedUser_usesCorrectUserId() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val expectedUid = "google-uid-789"
    every { mockUser.uid } returns expectedUid
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request = createTestRequest(name = "Jane", surname = "Smith")
    signUpUseCase.execute(request)

    assertEquals(expectedUid, capturedProfile.captured.userId)
    assertEquals("Jane Smith", capturedProfile.captured.name)
  }

  @Test
  fun execute_authenticatedUser_buildsProfileCorrectly() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "google-user"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request =
        createTestRequest(
            name = "  Alice  ",
            surname = "  Johnson  ",
            email = "alice@example.com",
            levelOfEducation = "  Math, PhD  ",
            description = "  Professor  ",
            address = "  456 Oak Ave  ")

    signUpUseCase.execute(request)

    val profile = capturedProfile.captured
    assertEquals("Alice Johnson", profile.name) // Names trimmed and joined
    assertEquals("alice@example.com", profile.email)
    assertEquals("Math, PhD", profile.levelOfEducation)
    assertEquals("Professor", profile.description)
    assertEquals("456 Oak Ave", profile.location.name)
  }

  // Tests for new users (regular email/password flow)

  @Test
  fun execute_newUser_createsAuthAndProfile() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "new-user-123"
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns Result.success(mockUser)
    coEvery { mockProfileRepository.addProfile(any()) } returns Unit

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    // Should create auth account
    coVerify(exactly = 1) { mockAuthRepository.signUpWithEmail("john@example.com", "password123!") }
    // Should create profile
    coVerify(exactly = 1) { mockProfileRepository.addProfile(any()) }
    // Should return success
    assertTrue(result is SignUpResult.Success)
  }

  @Test
  fun execute_newUser_authFails_doesNotCreateProfile() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(Exception("Email already in use"))

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    // Should NOT create profile since auth failed
    coVerify(exactly = 0) { mockProfileRepository.addProfile(any()) }
    // Should return error
    assertTrue(result is SignUpResult.Error)
    assertEquals("Email already in use", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_newUser_profileCreationFails_returnsSpecificError() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "user-with-profile-issue"
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns Result.success(mockUser)
    coEvery { mockProfileRepository.addProfile(any()) } throws
        Exception("Firestore permission denied")

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals(
        "Account created but profile failed: Firestore permission denied",
        (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_newUser_usesFirebaseUidAsUserId() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val expectedUid = "firebase-uid-abc"
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns expectedUid
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns Result.success(mockUser)

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request = createTestRequest()
    signUpUseCase.execute(request)

    assertEquals(expectedUid, capturedProfile.captured.userId)
  }

  // Tests for Firebase exception mapping

  @Test
  fun execute_firebaseAuthException_emailAlreadyInUse_returnsFriendlyMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
    every { mockException.message } returns
        "The email address is already in use by another account."
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(mockException)

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("This email is already registered", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_firebaseAuthException_invalidEmail_returnsFriendlyMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_INVALID_EMAIL"
    every { mockException.message } returns "The email address is badly formatted."
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(mockException)

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Invalid email format", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_firebaseAuthException_weakPassword_returnsFriendlyMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_WEAK_PASSWORD"
    every { mockException.message } returns "Password should be at least 6 characters"
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(mockException)

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Password is too weak", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_firebaseAuthException_unknownError_returnsOriginalMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_UNKNOWN"
    every { mockException.message } returns "Something went wrong"
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(mockException)

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Something went wrong", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_firebaseAuthException_nullMessage_returnsDefaultMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockException = mockk<FirebaseAuthException>(relaxed = true)
    every { mockException.errorCode } returns "ERROR_UNKNOWN"
    every { mockException.message } returns null
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(mockException)

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Sign up failed", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_nonFirebaseException_returnsMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(Exception("Network timeout"))

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Network timeout", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_nonFirebaseException_nullMessage_returnsDefaultMessage() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null
    val exceptionWithNullMessage =
        object : Exception() {
          override val message: String? = null
        }
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns
        Result.failure(exceptionWithNullMessage)

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Sign up failed", (result as SignUpResult.Error).message)
  }

  // Tests for unexpected exceptions

  @Test
  fun execute_unexpectedException_returnsError() = runTest {
    every { mockAuthRepository.getCurrentUser() } throws RuntimeException("Unexpected crash")

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Unexpected crash", (result as SignUpResult.Error).message)
  }

  @Test
  fun execute_unexpectedException_nullMessage_returnsUnknownError() = runTest {
    val throwableWithNullMessage =
        object : Throwable() {
          override val message: String? = null
        }
    every { mockAuthRepository.getCurrentUser() } throws throwableWithNullMessage

    val request = createTestRequest()
    val result = signUpUseCase.execute(request)

    assertTrue(result is SignUpResult.Error)
    assertEquals("Unknown error", (result as SignUpResult.Error).message)
  }

  // Tests for profile building logic

  @Test
  fun buildProfile_trimsAndCombinesNames() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "test-user"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request = createTestRequest(name = "  John  ", surname = "  Doe  ")
    signUpUseCase.execute(request)

    assertEquals("John Doe", capturedProfile.captured.name)
  }

  @Test
  fun buildProfile_handlesEmptySpacesBetweenNames() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "test-user"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request = createTestRequest(name = "Mary Jane", surname = "Watson")
    signUpUseCase.execute(request)

    // Only filters empty strings, so "Mary Jane" and "Watson" both remain
    assertEquals("Mary Jane Watson", capturedProfile.captured.name)
  }

  @Test
  fun buildProfile_trimsAllFields() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "test-user"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request =
        createTestRequest(
            name = "  Alice  ",
            surname = "  Smith  ",
            email = "  alice@test.com  ",
            levelOfEducation = "  PhD  ",
            description = "  Researcher  ",
            address = "  123 Lab St  ")

    signUpUseCase.execute(request)

    val profile = capturedProfile.captured
    assertEquals("alice@test.com", profile.email)
    assertEquals("PhD", profile.levelOfEducation)
    assertEquals("Researcher", profile.description)
    assertEquals("123 Lab St", profile.location.name)
  }

  @Test
  fun buildProfile_emptyDescription_storesEmptyString() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "test-user"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request = createTestRequest(description = "")
    signUpUseCase.execute(request)

    assertEquals("", capturedProfile.captured.description)
  }

  @Test
  fun buildProfile_emptyAddress_storesEmptyString() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "test-user"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request = createTestRequest(address = "")
    signUpUseCase.execute(request)

    assertEquals("", capturedProfile.captured.location.name)
  }

  // Tests for complete flow scenarios

  @Test
  fun execute_completeSuccessfulFlow_regularUser() = runTest {
    every { mockAuthRepository.getCurrentUser() } returns null

    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "complete-user-123"
    coEvery { mockAuthRepository.signUpWithEmail("complete@example.com", "SecurePass123!") } returns
        Result.success(mockUser)

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request =
        createTestRequest(
            name = "Complete",
            surname = "User",
            email = "complete@example.com",
            password = "SecurePass123!",
            levelOfEducation = "Masters",
            description = "Full stack developer",
            address = "789 Dev Street")

    val result = signUpUseCase.execute(request)

    // Verify result
    assertTrue(result is SignUpResult.Success)

    // Verify auth was called with correct params
    coVerify { mockAuthRepository.signUpWithEmail("complete@example.com", "SecurePass123!") }

    // Verify profile was created with correct data
    val profile = capturedProfile.captured
    assertEquals("complete-user-123", profile.userId)
    assertEquals("Complete User", profile.name)
    assertEquals("complete@example.com", profile.email)
    assertEquals("Masters", profile.levelOfEducation)
    assertEquals("Full stack developer", profile.description)
    assertEquals("789 Dev Street", profile.location.name)
  }

  @Test
  fun execute_completeSuccessfulFlow_googleUser() = runTest {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "google-complete-123"
    every { mockAuthRepository.getCurrentUser() } returns mockUser

    val capturedProfile = slot<com.android.sample.model.user.Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    val request =
        createTestRequest(
            name = "Google",
            surname = "User",
            email = "google@gmail.com",
            password = "", // Password ignored for Google users
            levelOfEducation = "Bachelors",
            description = "Mobile developer",
            address = "321 Mobile Ave")

    val result = signUpUseCase.execute(request)

    // Verify result
    assertTrue(result is SignUpResult.Success)

    // Verify auth was NOT called for Google user
    coVerify(exactly = 0) { mockAuthRepository.signUpWithEmail(any(), any()) }

    // Verify profile was created
    val profile = capturedProfile.captured
    assertEquals("google-complete-123", profile.userId)
    assertEquals("Google User", profile.name)
    assertEquals("google@gmail.com", profile.email)
    assertEquals("Bachelors", profile.levelOfEducation)
    assertEquals("Mobile developer", profile.description)
    assertEquals("321 Mobile Ave", profile.location.name)
  }
}
