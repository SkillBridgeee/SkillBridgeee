@file:Suppress("DEPRECATION")

package com.android.sample.integration

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.*
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.signup.SignUpEvent
import com.android.sample.ui.signup.SignUpViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for the complete Google Sign-In flow with profile checking. These tests verify
 * the end-to-end flow from Google Sign-In through profile creation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GoogleSignInIntegrationTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
  @get:Rule val firebaseRule = FirebaseTestRule()

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var context: Context
  private lateinit var mockAuthRepository: AuthenticationRepository
  private lateinit var mockProfileRepository: ProfileRepository
  private lateinit var mockCredentialHelper: CredentialAuthHelper

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    context = ApplicationProvider.getApplicationContext()

    mockAuthRepository = mockk(relaxed = true)
    mockProfileRepository = mockk(relaxed = true)
    mockCredentialHelper = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun googleSignIn_newUser_requiresSignUpFlow() = runTest {
    // Step 1: User signs in with Google (no existing profile)
    val authViewModel =
        AuthenticationViewModel(
            context, mockAuthRepository, mockCredentialHelper, mockProfileRepository)

    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()

    // Setup Google Sign-In mocks
    every { mockActivityResult.data } returns mockIntent

    try {
      mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
      every {
        com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
      } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

      every { mockAccount.idToken } returns "test-token"
      every { mockAccount.email } returns "newuser@gmail.com"
      every { mockFirebaseUser.uid } returns "new-user-123"
      every { mockFirebaseUser.email } returns "newuser@gmail.com"

      every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
      coEvery { mockAuthRepository.signInWithCredential(any()) } returns
          Result.success(mockFirebaseUser)
      coEvery { mockProfileRepository.getProfile("new-user-123") } returns null // No profile exists

      // Execute Google Sign-In
      authViewModel.handleGoogleSignInResult(mockActivityResult)
      testDispatcher.scheduler.advanceUntilIdle()
    } finally {
      unmockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    }

    // Verify: Should return RequiresSignUp
    val authResult = authViewModel.authResult.first()
    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("newuser@gmail.com", (authResult as AuthResult.RequiresSignUp).email)

    // Step 2: User is redirected to sign-up screen with pre-filled email
    every { mockAuthRepository.getCurrentUser() } returns mockFirebaseUser

    val signUpUseCase =
        com.android.sample.ui.signup.SignUpUseCase(mockAuthRepository, mockProfileRepository)
    val signUpViewModel =
        SignUpViewModel(
            initialEmail = "newuser@gmail.com",
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase)

    // Verify: Email is pre-filled and isGoogleSignUp is true
    var state = signUpViewModel.state.first()
    assertEquals("newuser@gmail.com", state.email)
    assertTrue(state.isGoogleSignUp)

    // Step 3: User fills out profile information
    signUpViewModel.onEvent(SignUpEvent.NameChanged("John"))
    signUpViewModel.onEvent(SignUpEvent.SurnameChanged("Doe"))
    signUpViewModel.onEvent(SignUpEvent.LevelOfEducationChanged("Computer Science"))
    signUpViewModel.onEvent(SignUpEvent.DescriptionChanged("Love teaching programming"))

    // Verify: Form is valid (no password required for Google sign-up)
    state = signUpViewModel.state.first()
    assertTrue(state.canSubmit)

    // Step 4: User submits the form
    val capturedProfile = slot<Profile>()
    coEvery { mockProfileRepository.addProfile(capture(capturedProfile)) } returns Unit

    signUpViewModel.onEvent(SignUpEvent.Submit)
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify: Profile is created with correct data
    coVerify(exactly = 1) { mockProfileRepository.addProfile(any()) }
    coVerify(exactly = 0) {
      mockAuthRepository.signUpWithEmail(any(), any())
    } // No auth account created

    assertEquals("new-user-123", capturedProfile.captured.userId)
    assertEquals("newuser@gmail.com", capturedProfile.captured.email)
    assertEquals("John Doe", capturedProfile.captured.name)
    assertEquals("Computer Science", capturedProfile.captured.levelOfEducation)
    assertEquals("Love teaching programming", capturedProfile.captured.description)

    // Verify: Sign-up was successful
    state = signUpViewModel.state.first()
    assertTrue(state.submitSuccess)
  }

  @Test
  fun googleSignIn_existingUser_directLogin() = runTest {
    // Setup: User with existing profile
    val authViewModel =
        AuthenticationViewModel(
            context, mockAuthRepository, mockCredentialHelper, mockProfileRepository)

    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()
    val existingProfile = mockk<Profile>()

    // Setup Google Sign-In mocks
    every { mockActivityResult.data } returns mockIntent

    try {
      mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
      every {
        com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
      } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

      every { mockAccount.idToken } returns "test-token"
      every { mockAccount.email } returns "existinguser@gmail.com"
      every { mockFirebaseUser.uid } returns "existing-user-456"
      every { mockFirebaseUser.email } returns "existinguser@gmail.com"

      every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
      coEvery { mockAuthRepository.signInWithCredential(any()) } returns
          Result.success(mockFirebaseUser)
      coEvery { mockProfileRepository.getProfile("existing-user-456") } returns
          existingProfile // Profile exists

      // Execute Google Sign-In
      authViewModel.handleGoogleSignInResult(mockActivityResult)
      testDispatcher.scheduler.advanceUntilIdle()
    } finally {
      unmockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    }

    // Verify: Should return Success (direct login)
    val authResult = authViewModel.authResult.first()
    assertTrue(authResult is AuthResult.Success)
    assertEquals(mockFirebaseUser, (authResult as AuthResult.Success).user)
  }

  @Test
  fun googleSignIn_userAbandonsSignUp_signsOutOnNextAttempt() = runTest {
    // Step 1: First Google Sign-In attempt
    val mockFirebaseUser = mockk<FirebaseUser>()
    every { mockFirebaseUser.uid } returns "abandoning-user-789"
    every { mockFirebaseUser.email } returns "abandoner@gmail.com"
    every { mockAuthRepository.getCurrentUser() } returns mockFirebaseUser
    every { mockAuthRepository.signOut() } returns Unit

    val signUpUseCase1 =
        com.android.sample.ui.signup.SignUpUseCase(mockAuthRepository, mockProfileRepository)
    val signUpViewModel =
        SignUpViewModel(
            initialEmail = "abandoner@gmail.com",
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase1)

    // Verify: Email is pre-filled
    var state = signUpViewModel.state.first()
    assertEquals("abandoner@gmail.com", state.email)
    assertTrue(state.isGoogleSignUp)

    // Step 2: User navigates away without completing (onSignUpAbandoned is called)
    signUpViewModel.onSignUpAbandoned()

    // Verify: User is signed out
    verify(exactly = 1) { mockAuthRepository.signOut() }

    // Step 3: Next Google Sign-In attempt should treat them as a new user
    // (This would be tested in the AuthenticationViewModel, but we verify cleanup here)
    every { mockAuthRepository.getCurrentUser() } returns null

    val signUpUseCase2 =
        com.android.sample.ui.signup.SignUpUseCase(mockAuthRepository, mockProfileRepository)
    val signUpViewModel2 =
        SignUpViewModel(
            initialEmail = "abandoner@gmail.com",
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase2)

    state = signUpViewModel2.state.first()
    // Now isGoogleSignUp should be false because user is not authenticated
    assertFalse(state.isGoogleSignUp)
  }

  @Test
  fun googleSignIn_emailProtection_cannotBeChanged() = runTest {
    val mockFirebaseUser = mockk<FirebaseUser>()
    every { mockFirebaseUser.uid } returns "protected-user"
    every { mockAuthRepository.getCurrentUser() } returns mockFirebaseUser

    val signUpUseCase =
        com.android.sample.ui.signup.SignUpUseCase(mockAuthRepository, mockProfileRepository)
    val signUpViewModel =
        SignUpViewModel(
            initialEmail = "protected@gmail.com",
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase)

    val originalEmail = signUpViewModel.state.first().email
    assertEquals("protected@gmail.com", originalEmail)

    // Attempt to change email (should be blocked)
    signUpViewModel.onEvent(SignUpEvent.EmailChanged("hacker@evil.com"))

    // Verify: Email remains unchanged
    val finalEmail = signUpViewModel.state.first().email
    assertEquals("protected@gmail.com", finalEmail)
  }

  @Test
  fun googleSignIn_profileCreationFails_showsError() = runTest {
    val mockFirebaseUser = mockk<FirebaseUser>()
    every { mockFirebaseUser.uid } returns "failing-user"
    every { mockAuthRepository.getCurrentUser() } returns mockFirebaseUser
    coEvery { mockProfileRepository.addProfile(any()) } throws
        Exception("Database connection failed")

    val signUpUseCase =
        com.android.sample.ui.signup.SignUpUseCase(mockAuthRepository, mockProfileRepository)
    val signUpViewModel =
        SignUpViewModel(
            initialEmail = "failing@gmail.com",
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase)

    // Fill out form
    signUpViewModel.onEvent(SignUpEvent.NameChanged("Jane"))
    signUpViewModel.onEvent(SignUpEvent.SurnameChanged("Smith"))
    signUpViewModel.onEvent(SignUpEvent.LevelOfEducationChanged("Math"))
    signUpViewModel.onEvent(SignUpEvent.Submit)
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify: Error is shown
    val state = signUpViewModel.state.first()
    assertFalse(state.submitSuccess)
    assertFalse(state.submitting)
    assertTrue(state.error?.contains("Profile creation failed") == true)
  }

  @Test
  fun googleSignIn_completeFlow_thenSignOut_thenSignInAgain() = runTest {
    // This test simulates the complete happy path
    val mockFirebaseUser = mockk<FirebaseUser>()
    val mockProfile = mockk<Profile>()

    every { mockFirebaseUser.uid } returns "complete-flow-user"
    every { mockFirebaseUser.email } returns "complete@gmail.com"

    // First sign-in: No profile
    coEvery { mockProfileRepository.getProfile("complete-flow-user") } returns null
    every { mockAuthRepository.getCurrentUser() } returns mockFirebaseUser
    coEvery { mockProfileRepository.addProfile(any()) } returns Unit

    val signUpUseCase =
        com.android.sample.ui.signup.SignUpUseCase(mockAuthRepository, mockProfileRepository)
    val signUpViewModel =
        SignUpViewModel(
            initialEmail = "complete@gmail.com",
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase)

    // Complete signup
    signUpViewModel.onEvent(SignUpEvent.NameChanged("Complete"))
    signUpViewModel.onEvent(SignUpEvent.SurnameChanged("User"))
    signUpViewModel.onEvent(SignUpEvent.LevelOfEducationChanged("Engineering"))
    signUpViewModel.onEvent(SignUpEvent.Submit)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(signUpViewModel.state.first().submitSuccess)

    // Simulate sign-out
    every { mockAuthRepository.signOut() } returns Unit
    signUpViewModel.onSignUpAbandoned() // This shouldn't sign out because submitSuccess is true
    verify(exactly = 0) { mockAuthRepository.signOut() }

    // Second sign-in: Profile now exists
    coEvery { mockProfileRepository.getProfile("complete-flow-user") } returns mockProfile

    val authViewModel =
        AuthenticationViewModel(
            context, mockAuthRepository, mockCredentialHelper, mockProfileRepository)

    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()

    every { mockActivityResult.data } returns mockIntent
    try {
      mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
      every {
        com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
      } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

      every { mockAccount.idToken } returns "test-token"
      every { mockAccount.email } returns "complete@gmail.com"
      every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
      coEvery { mockAuthRepository.signInWithCredential(any()) } returns
          Result.success(mockFirebaseUser)

      authViewModel.handleGoogleSignInResult(mockActivityResult)
      testDispatcher.scheduler.advanceUntilIdle()
    } finally {
      unmockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    }

    // Should now successfully sign in
    val authResult = authViewModel.authResult.first()
    assertTrue(authResult is AuthResult.Success)
  }
}
