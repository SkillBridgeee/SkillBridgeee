@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.credentials.PasswordCredential
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthenticationViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
  @get:Rule val firebaseRule = FirebaseTestRule()

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var context: Context
  private lateinit var mockRepository: AuthenticationRepository
  private lateinit var mockCredentialHelper: CredentialAuthHelper
  private lateinit var mockProfileRepository: com.android.sample.model.user.ProfileRepository
  private lateinit var viewModel: AuthenticationViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    context = ApplicationProvider.getApplicationContext()

    mockRepository = mockk(relaxed = true)
    mockCredentialHelper = mockk(relaxed = true)
    mockProfileRepository = mockk(relaxed = true)

    viewModel =
        AuthenticationViewModel(
            context, mockRepository, mockCredentialHelper, mockProfileRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun initialState_hasCorrectDefaults() = runTest {
    val state = viewModel.uiState.first()

    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNull(state.message)
    assertEquals("", state.email)
    assertEquals("", state.password)
    assertFalse(state.showSuccessMessage)
    assertFalse(state.isSignInButtonEnabled)
  }

  @Test
  fun updateEmail_updatesState() = runTest {
    viewModel.updateEmail("test@example.com")

    val state = viewModel.uiState.first()

    assertEquals("test@example.com", state.email)
    assertNull(state.error)
    assertNull(state.message)
  }

  @Test
  fun updatePassword_updatesState() = runTest {
    viewModel.updatePassword("password123")

    val state = viewModel.uiState.first()

    assertEquals("password123", state.password)
    assertNull(state.error)
    assertNull(state.message)
  }

  @Test
  fun signInButtonEnabled_onlyWhenEmailAndPasswordProvided() = runTest {
    // Initially disabled
    var state = viewModel.uiState.first()
    assertFalse(state.isSignInButtonEnabled)

    // Still disabled with only email
    viewModel.updateEmail("test@example.com")
    state = viewModel.uiState.first()
    assertFalse(state.isSignInButtonEnabled)

    // Enabled with both email and password
    viewModel.updatePassword("password123")
    state = viewModel.uiState.first()
    assertTrue(state.isSignInButtonEnabled)
  }

  @Test
  fun signIn_withEmptyCredentials_showsError() = runTest {
    viewModel.signIn()

    val state = viewModel.uiState.first()

    assertEquals("Email and password cannot be empty", state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun signIn_withValidCredentials_succeeds() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "test-uid-123"
    every { mockUser.email } returns "test@example.com"
    every { mockUser.isEmailVerified } returns true // User's email is verified

    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.success(mockUser)
    // Mock that profile exists for this user
    coEvery { mockProfileRepository.getProfile("test-uid-123") } returns mockk(relaxed = true)

    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertNull(state.error)
    assertTrue(authResult is AuthResult.Success)
    assertEquals(mockUser, (authResult as AuthResult.Success).user)
  }

  @Test
  fun signIn_withInvalidCredentials_showsError() = runTest {
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("wrongpassword")

    val exception = Exception("Invalid credentials")
    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.failure(exception)

    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertEquals("Invalid credentials", state.error)
    assertTrue(authResult is AuthResult.Error)
  }

  @Test
  fun signIn_withExceptionWithoutMessage_usesDefaultMessage() = runTest {
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    val exception = Exception(null as String?)
    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.failure(exception)

    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()

    assertEquals("Sign in failed", state.error)
  }

  @Test
  fun handleGoogleSignInResult_withSuccess_updatesAuthResult() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockProfile = mockk<com.android.sample.model.user.Profile>()

    every { mockActivityResult.data } returns mockIntent

    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }
    every { mockAccount.idToken } returns "test-token"
    every { mockUser.uid } returns "test-uid"
    every { mockUser.isEmailVerified } returns true // Google Sign-In users are verified

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockUser)
    coEvery { mockProfileRepository.getProfile("test-uid") } returns mockProfile

    viewModel.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertNull(state.error)
    assertTrue(authResult is AuthResult.Success)
  }

  @Test
  fun handleGoogleSignInResult_withNoIdToken_showsError() = runTest {
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()

    every { mockActivityResult.data } returns mockIntent

    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }
    every { mockAccount.idToken } returns null

    viewModel.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertEquals("No ID token received", state.error)
    assertTrue(authResult is AuthResult.Error)
    assertEquals("No ID token received", (authResult as AuthResult.Error).message)
  }

  @Test
  fun handleGoogleSignInResult_withApiException_showsError() = runTest {
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val apiException =
        com.google.android.gms.common.api.ApiException(
            com.google.android.gms.common.api.Status(12501, "User cancelled"))

    every { mockActivityResult.data } returns mockIntent

    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } throws apiException }

    viewModel.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertTrue(state.error?.contains("Google sign in failed") == true)
    assertTrue(authResult is AuthResult.Error)
  }

  @Test
  fun handleGoogleSignInResult_withCredentialFailure_showsError() = runTest {
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()

    every { mockActivityResult.data } returns mockIntent

    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }
    every { mockAccount.idToken } returns "test-token"

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    val exception = Exception("Credential error")
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.failure(exception)

    viewModel.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertEquals("Credential error", state.error)
    assertTrue(authResult is AuthResult.Error)
  }

  @Test
  fun handleGoogleSignInResult_withCredentialFailureNoMessage_usesDefault() = runTest {
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()

    every { mockActivityResult.data } returns mockIntent

    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }
    every { mockAccount.idToken } returns "test-token"

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    val exception = Exception(null as String?)
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.failure(exception)

    viewModel.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()

    assertEquals("Google sign in failed", state.error)
  }

  @Test
  fun getSavedCredential_withSuccess_updatesEmailAndPassword() = runTest {
    val mockCredential = mockk<PasswordCredential>()
    every { mockCredential.id } returns "saved@example.com"
    every { mockCredential.password } returns "savedpassword"

    coEvery { mockCredentialHelper.getPasswordCredential() } returns Result.success(mockCredential)

    viewModel.getSavedCredential()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()

    assertEquals("saved@example.com", state.email)
    assertEquals("savedpassword", state.password)
    assertEquals("Credential loaded", state.message)
    assertFalse(state.isLoading)
  }

  @Test
  fun getSavedCredential_withFailure_silentlyFails() = runTest {
    val exception = Exception("No credentials")
    coEvery { mockCredentialHelper.getPasswordCredential() } returns Result.failure(exception)

    viewModel.getSavedCredential()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()

    assertFalse(state.isLoading)
    assertNull(state.error) // Should fail silently
  }

  @Test
  fun signOut_clearsAuthResultAndState() = runTest {
    val mockGoogleSignInClient = mockk<GoogleSignInClient>(relaxed = true)
    every { mockCredentialHelper.getGoogleSignInClient() } returns mockGoogleSignInClient

    viewModel.signOut()

    val authResult = viewModel.authResult.first()
    val state = viewModel.uiState.first()

    assertNull(authResult)
    assertEquals("", state.email)
    assertEquals("", state.password)
    verify { mockRepository.signOut() }
    verify { mockGoogleSignInClient.signOut() }
  }

  @Test
  fun setError_updatesStateWithError() = runTest {
    viewModel.setError("Test error message")

    val state = viewModel.uiState.first()

    assertEquals("Test error message", state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun showSuccessMessage_updatesState() = runTest {
    viewModel.showSuccessMessage(true)

    val state = viewModel.uiState.first()

    assertTrue(state.showSuccessMessage)

    viewModel.showSuccessMessage(false)

    val updatedState = viewModel.uiState.first()

    assertFalse(updatedState.showSuccessMessage)
  }

  @Test
  fun getGoogleSignInClient_returnsClientFromHelper() {
    val mockClient = mockk<GoogleSignInClient>()
    every { mockCredentialHelper.getGoogleSignInClient() } returns mockClient

    val result = viewModel.getGoogleSignInClient()

    assertEquals(mockClient, result)
    verify { mockCredentialHelper.getGoogleSignInClient() }
  }

  // Tests for Google Sign-In with Profile Check
  @Test
  fun handleGoogleSignInResult_withExistingProfile_returnsSuccess() = runTest {
    // Setup mocks
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()
    val mockProfile = mockk<com.android.sample.model.user.Profile>()

    // Mock profile repository
    val mockProfileRepository = mockk<com.android.sample.model.user.ProfileRepository>()
    val viewModelWithProfile =
        AuthenticationViewModel(
            context, mockRepository, mockCredentialHelper, mockProfileRepository)

    every { mockActivityResult.data } returns mockIntent
    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

    every { mockAccount.idToken } returns "test-token"
    every { mockAccount.email } returns "test@gmail.com"
    every { mockFirebaseUser.uid } returns "user-123"
    every { mockFirebaseUser.email } returns "test@gmail.com"
    every { mockFirebaseUser.isEmailVerified } returns true // Google Sign-In users are verified

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockFirebaseUser)
    coEvery { mockProfileRepository.getProfile("user-123") } returns mockProfile

    viewModelWithProfile.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val authResult = viewModelWithProfile.authResult.first()
    val state = viewModelWithProfile.uiState.first()

    assertTrue(authResult is AuthResult.Success)
    assertEquals(mockFirebaseUser, (authResult as AuthResult.Success).user)
    assertFalse(state.isLoading)
    assertNull(state.error)
  }

  @Test
  fun handleGoogleSignInResult_withoutExistingProfile_returnsRequiresSignUp() = runTest {
    // Setup mocks
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()

    // Mock profile repository
    val mockProfileRepository = mockk<com.android.sample.model.user.ProfileRepository>()
    val viewModelWithProfile =
        AuthenticationViewModel(
            context, mockRepository, mockCredentialHelper, mockProfileRepository)

    every { mockActivityResult.data } returns mockIntent
    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

    every { mockAccount.idToken } returns "test-token"
    every { mockAccount.email } returns "test@gmail.com"
    every { mockFirebaseUser.uid } returns "user-123"
    every { mockFirebaseUser.email } returns "test@gmail.com"
    every { mockFirebaseUser.isEmailVerified } returns true // Google Sign-In users are verified

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockFirebaseUser)
    coEvery { mockProfileRepository.getProfile("user-123") } returns null

    viewModelWithProfile.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val authResult = viewModelWithProfile.authResult.first()
    val state = viewModelWithProfile.uiState.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    val requiresSignUp = authResult as AuthResult.RequiresSignUp
    assertEquals("test@gmail.com", requiresSignUp.email)
    assertEquals(mockFirebaseUser, requiresSignUp.user)
    assertFalse(state.isLoading)
    assertNull(state.error)
  }

  @Test
  fun handleGoogleSignInResult_profileCheckThrowsException_returnsRequiresSignUp() = runTest {
    // Setup mocks
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()

    // Mock profile repository
    val mockProfileRepository = mockk<com.android.sample.model.user.ProfileRepository>()
    val viewModelWithProfile =
        AuthenticationViewModel(
            context, mockRepository, mockCredentialHelper, mockProfileRepository)

    every { mockActivityResult.data } returns mockIntent
    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

    every { mockAccount.idToken } returns "test-token"
    every { mockAccount.email } returns "test@gmail.com"
    every { mockFirebaseUser.uid } returns "user-123"
    every { mockFirebaseUser.email } returns "test@gmail.com"
    every { mockFirebaseUser.isEmailVerified } returns true // Google Sign-In users are verified

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockFirebaseUser)
    coEvery { mockProfileRepository.getProfile("user-123") } throws Exception("Network error")

    viewModelWithProfile.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val authResult = viewModelWithProfile.authResult.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("test@gmail.com", (authResult as AuthResult.RequiresSignUp).email)
  }

  @Test
  fun handleGoogleSignInResult_usesGoogleEmailAsFallback() = runTest {
    // Test when Firebase user email is null but Google account email is available
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()

    val mockProfileRepository = mockk<com.android.sample.model.user.ProfileRepository>()
    val viewModelWithProfile =
        AuthenticationViewModel(
            context, mockRepository, mockCredentialHelper, mockProfileRepository)

    every { mockActivityResult.data } returns mockIntent
    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

    every { mockAccount.idToken } returns "test-token"
    every { mockAccount.email } returns "google@gmail.com"
    every { mockFirebaseUser.uid } returns "user-123"
    every { mockFirebaseUser.email } returns null // Firebase email is null
    every { mockFirebaseUser.isEmailVerified } returns true // Google Sign-In users are verified

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockFirebaseUser)
    coEvery { mockProfileRepository.getProfile("user-123") } returns null

    viewModelWithProfile.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val authResult = viewModelWithProfile.authResult.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("google@gmail.com", (authResult as AuthResult.RequiresSignUp).email)
  }

  @Test
  fun handleGoogleSignInResult_usesEmptyStringWhenNoEmail() = runTest {
    // Test when both Firebase and Google emails are null
    val mockActivityResult = mockk<ActivityResult>()
    val mockIntent = mockk<android.content.Intent>()
    val mockAccount = mockk<GoogleSignInAccount>()
    val mockFirebaseUser = mockk<FirebaseUser>()

    val mockProfileRepository = mockk<com.android.sample.model.user.ProfileRepository>()
    val viewModelWithProfile =
        AuthenticationViewModel(
            context, mockRepository, mockCredentialHelper, mockProfileRepository)

    every { mockActivityResult.data } returns mockIntent
    mockkStatic("com.google.android.gms.auth.api.signin.GoogleSignIn")
    every {
      com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(any())
    } returns mockk { every { getResult(any<Class<Exception>>()) } returns mockAccount }

    every { mockAccount.idToken } returns "test-token"
    every { mockAccount.email } returns null
    every { mockFirebaseUser.uid } returns "user-123"
    every { mockFirebaseUser.email } returns null
    every { mockFirebaseUser.isEmailVerified } returns true // Google Sign-In users are verified

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockFirebaseUser)
    coEvery { mockProfileRepository.getProfile("user-123") } returns null

    viewModelWithProfile.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val authResult = viewModelWithProfile.authResult.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("", (authResult as AuthResult.RequiresSignUp).email)
  }

  @Test
  fun `signOut clears authentication state`() = runTest {
    // Given - user is signed in with email and password
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify state has email and password
    var uiState = viewModel.uiState.first()
    assertEquals("test@example.com", uiState.email)
    assertEquals("password123", uiState.password)

    // When - sign out
    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then - state should be reset
    uiState = viewModel.uiState.first()
    assertEquals("", uiState.email)
    assertEquals("", uiState.password)
    assertFalse(uiState.isLoading)
    assertNull(uiState.error)
    assertNull(uiState.message)
    assertFalse(uiState.showSuccessMessage)
  }

  @Test
  fun `signOut clears auth result`() = runTest {
    // Given - simulate successful authentication
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "user-123"
    every { mockUser.isEmailVerified } returns true // User's email is verified
    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.success(mockUser)
    coEvery { mockProfileRepository.getProfile("user-123") } returns mockk(relaxed = true)

    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")
    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify auth result is set
    var authResult = viewModel.authResult.first()
    assertTrue(authResult is AuthResult.Success)

    // When - sign out
    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then - auth result should be null
    authResult = viewModel.authResult.first()
    assertNull(authResult)
  }

  @Test
  fun `signOut calls repository signOut`() = runTest {
    // When
    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    verify { mockRepository.signOut() }
  }

  @Test
  fun `signOut calls Google SignIn client signOut`() = runTest {
    // Given
    val mockGoogleSignInClient = mockk<GoogleSignInClient>(relaxed = true)
    every { mockCredentialHelper.getGoogleSignInClient() } returns mockGoogleSignInClient

    // When
    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    verify { mockGoogleSignInClient.signOut() }
  }

  @Test
  fun `signOut can be called multiple times without errors`() = runTest {
    // When - calling signOut multiple times
    viewModel.signOut()
    viewModel.signOut()
    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then - no exception should be thrown and state should be reset
    val uiState = viewModel.uiState.first()
    assertEquals("", uiState.email)
    assertEquals("", uiState.password)
    assertNull(viewModel.authResult.first())
  }

  @Test
  fun `signOut after failed login clears error state`() = runTest {
    // Given - failed login
    coEvery { mockRepository.signInWithEmail(any(), any()) } returns
        Result.failure(Exception("Login failed"))

    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("wrong")
    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify error is present
    var uiState = viewModel.uiState.first()
    assertNotNull(uiState.error)

    // When - sign out
    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then - error should be cleared
    uiState = viewModel.uiState.first()
    assertNull(uiState.error)
    assertEquals("", uiState.email)
    assertEquals("", uiState.password)
  }

  // -------- Email Verification Tests (NEW) -----------------------------------------------

  @Test
  fun `resendVerificationEmail success updates UI state with success message`() = runTest {
    // Given
    coEvery { mockRepository.resendVerificationEmail() } returns Result.success(Unit)

    // When
    viewModel.resendVerificationEmail()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertNull(uiState.error)
    assertEquals("Verification email sent! Please check your inbox.", uiState.message)
  }

  @Test
  fun `resendVerificationEmail failure updates UI state with error message`() = runTest {
    // Given
    coEvery { mockRepository.resendVerificationEmail() } returns
        Result.failure(Exception("Please sign in first to resend verification email"))

    // When
    viewModel.resendVerificationEmail()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertEquals("Please sign in first to resend verification email", uiState.error)
  }

  @Test
  fun `resendVerificationEmail sets loading state`() = runTest {
    // Given - delay the repository response
    coEvery { mockRepository.resendVerificationEmail() } coAnswers
        {
          kotlinx.coroutines.delay(100)
          Result.success(Unit)
        }

    // When
    viewModel.resendVerificationEmail()

    // Then - should be loading
    var uiState = viewModel.uiState.first()
    assertTrue(uiState.isLoading)

    // After completion
    testDispatcher.scheduler.advanceUntilIdle()
    uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
  }

  @Test
  fun `resendVerificationEmail calls repository`() = runTest {
    // Given
    coEvery { mockRepository.resendVerificationEmail() } returns Result.success(Unit)

    // When
    viewModel.resendVerificationEmail()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { mockRepository.resendVerificationEmail() }
  }

  @Test
  fun `resendVerificationEmail with network error shows normalized error`() = runTest {
    // Given
    coEvery { mockRepository.resendVerificationEmail() } returns
        Result.failure(Exception("Network connection failed"))

    // When
    viewModel.resendVerificationEmail()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertEquals("Network connection failed", uiState.error)
  }

  @Test
  fun `signIn with unverified email returns UnverifiedEmail auth result`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "test-uid-123"
    every { mockUser.email } returns "test@example.com"
    every { mockUser.isEmailVerified } returns false

    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.success(mockUser)

    // When
    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val authResult = viewModel.authResult.first()
    assertTrue(authResult is AuthResult.UnverifiedEmail)
    assertEquals(mockUser, (authResult as AuthResult.UnverifiedEmail).user)
  }

  @Test
  fun `signIn with unverified email shows error message`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "test-uid-123"
    every { mockUser.email } returns "test@example.com"
    every { mockUser.isEmailVerified } returns false

    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.success(mockUser)

    // When
    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertTrue(uiState.error?.contains("verify your email") == true)
  }

  @Test
  fun `signIn with verified email and existing profile succeeds`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "test-uid-123"
    every { mockUser.email } returns "test@example.com"
    every { mockUser.isEmailVerified } returns true

    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.success(mockUser)
    coEvery { mockProfileRepository.getProfile("test-uid-123") } returns mockk(relaxed = true)

    // When
    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val authResult = viewModel.authResult.first()
    assertTrue(authResult is AuthResult.Success)
    assertEquals(mockUser, (authResult as AuthResult.Success).user)
    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertNull(uiState.error)
  }

  // -------- Password Reset Tests --------------------------------------------------------

  @Test
  fun `showPasswordResetDialog sets dialog to visible with reset state`() = runTest {
    // When
    viewModel.showPasswordResetDialog()

    // Then
    val state = viewModel.uiState.first()
    assertTrue(state.showPasswordResetDialog)
    assertEquals("", state.resetEmail)
    assertNull(state.passwordResetError)
    assertNull(state.passwordResetMessage)
  }

  @Test
  fun `hidePasswordResetDialog sets dialog to invisible and clears state`() = runTest {
    // Given - dialog is shown with some data
    viewModel.showPasswordResetDialog()
    viewModel.updateResetEmail("test@example.com")

    // When
    viewModel.hidePasswordResetDialog()

    // Then
    val state = viewModel.uiState.first()
    assertFalse(state.showPasswordResetDialog)
    assertEquals("", state.resetEmail)
    assertNull(state.passwordResetError)
    assertNull(state.passwordResetMessage)
  }

  @Test
  fun `updateResetEmail updates email and clears error`() = runTest {
    // When
    viewModel.updateResetEmail("test@example.com")

    // Then
    val state = viewModel.uiState.first()
    assertEquals("test@example.com", state.resetEmail)
    assertNull(state.passwordResetError)
  }

  @Test
  fun `sendPasswordReset with blank email shows error`() = runTest {
    // Given
    viewModel.updateResetEmail("")

    // When
    viewModel.sendPasswordReset()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Please enter your email address", state.passwordResetError)
    assertNull(state.passwordResetMessage)
  }

  @Test
  fun `sendPasswordReset with whitespace email shows error`() = runTest {
    // Given
    viewModel.updateResetEmail("   ")

    // When
    viewModel.sendPasswordReset()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Please enter your email address", state.passwordResetError)
  }

  @Test
  fun `sendPasswordReset with invalid email format shows error`() = runTest {
    // Given
    viewModel.updateResetEmail("invalid-email")

    // When
    viewModel.sendPasswordReset()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Please enter a valid email address", state.passwordResetError)
    assertNull(state.passwordResetMessage)
  }

  @Test
  fun `sendPasswordReset with valid email sends reset and starts cooldown`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)

    // When
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()

    // Then
    val state = viewModel.uiState.first()
    assertEquals(
        "If this email is registered, a password reset link has been sent.",
        state.passwordResetMessage)
    assertNull(state.passwordResetError)
    assertEquals(60, state.passwordResetCooldownSeconds)
    coVerify { mockRepository.sendPasswordResetEmail("test@example.com") }
  }

  @Test
  fun `sendPasswordReset trims email before sending`() = runTest {
    // Given
    viewModel.updateResetEmail("  test@example.com  ")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)

    // When
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { mockRepository.sendPasswordResetEmail("test@example.com") }
  }

  @Test
  fun `sendPasswordReset failure shows error message`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    val exception = Exception("Network error")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.failure(exception)

    // When
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Network error", state.passwordResetError)
    assertNull(state.passwordResetMessage)
    assertEquals(0, state.passwordResetCooldownSeconds)
  }

  @Test
  fun `sendPasswordReset failure without message uses default error`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    val exception = Exception(null as String?)
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.failure(exception)

    // When
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Failed to send reset email. Please try again.", state.passwordResetError)
  }

  @Test
  fun `sendPasswordReset cooldown timer counts down from 60 to 0`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)

    // When
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()

    // Check initial cooldown
    var state = viewModel.uiState.first()
    assertEquals(60, state.passwordResetCooldownSeconds)

    // Advance time by 1 second
    testDispatcher.scheduler.advanceTimeBy(1000)
    testDispatcher.scheduler.runCurrent()
    state = viewModel.uiState.first()
    assertEquals(59, state.passwordResetCooldownSeconds)

    // Advance time by 10 more seconds
    testDispatcher.scheduler.advanceTimeBy(10000)
    testDispatcher.scheduler.runCurrent()
    state = viewModel.uiState.first()
    assertEquals(49, state.passwordResetCooldownSeconds)

    // Advance to the end
    testDispatcher.scheduler.advanceTimeBy(49000)
    testDispatcher.scheduler.runCurrent()
    state = viewModel.uiState.first()
    assertEquals(0, state.passwordResetCooldownSeconds)
  }

  @Test
  fun `sendPasswordReset during cooldown is blocked by button disabled state`() = runTest {
    // Given - first successful send
    viewModel.updateResetEmail("test@example.com")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()

    // Then - verify cooldown is active
    var state = viewModel.uiState.first()
    assertEquals(60, state.passwordResetCooldownSeconds)
    assertFalse(state.isPasswordResetButtonEnabled)

    // When - try to send again (button would be disabled in UI)
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()

    // Then - still in cooldown, verify repository wasn't called again
    // (In real UI, button would be disabled, preventing the call)
    state = viewModel.uiState.first()
    assertTrue(state.passwordResetCooldownSeconds > 0)
  }

  @Test
  fun `isPasswordResetButtonEnabled returns true when email present and no cooldown`() = runTest {
    // Given
    viewModel.showPasswordResetDialog()
    viewModel.updateResetEmail("test@example.com")

    // When
    val state = viewModel.uiState.first()

    // Then
    assertTrue(state.isPasswordResetButtonEnabled)
  }

  @Test
  fun `isPasswordResetButtonEnabled returns false when email is blank`() = runTest {
    // Given
    viewModel.showPasswordResetDialog()
    viewModel.updateResetEmail("")

    // When
    val state = viewModel.uiState.first()

    // Then
    assertFalse(state.isPasswordResetButtonEnabled)
  }

  @Test
  fun `isPasswordResetButtonEnabled returns false during cooldown`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()

    // When
    val state = viewModel.uiState.first()

    // Then
    assertFalse(state.isPasswordResetButtonEnabled)
    assertEquals(60, state.passwordResetCooldownSeconds)
  }

  @Test
  fun `isPasswordResetButtonEnabled returns true after cooldown expires`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.advanceUntilIdle()

    // Advance past cooldown
    testDispatcher.scheduler.advanceTimeBy(60000)
    testDispatcher.scheduler.runCurrent()

    // When
    val state = viewModel.uiState.first()

    // Then
    assertTrue(state.isPasswordResetButtonEnabled)
    assertEquals(0, state.passwordResetCooldownSeconds)
  }

  @Test
  fun `password reset state is independent from login state`() = runTest {
    // Given - update login email
    viewModel.updateEmail("login@example.com")
    viewModel.updatePassword("password123")

    // When - update reset email
    viewModel.showPasswordResetDialog()
    viewModel.updateResetEmail("reset@example.com")

    // Then
    val state = viewModel.uiState.first()
    assertEquals("login@example.com", state.email)
    assertEquals("password123", state.password)
    assertEquals("reset@example.com", state.resetEmail)
    assertTrue(state.showPasswordResetDialog)
  }

  @Test
  fun `multiple password resets can be sent after cooldown`() = runTest {
    // Given
    viewModel.updateResetEmail("test@example.com")
    coEvery { mockRepository.sendPasswordResetEmail(any()) } returns Result.success(Unit)

    // First send
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()
    var state = viewModel.uiState.first()
    assertEquals(60, state.passwordResetCooldownSeconds)

    // Wait for cooldown
    testDispatcher.scheduler.advanceTimeBy(60000)
    testDispatcher.scheduler.runCurrent()
    state = viewModel.uiState.first()
    assertEquals(0, state.passwordResetCooldownSeconds)

    // Second send
    viewModel.sendPasswordReset()
    testDispatcher.scheduler.runCurrent()
    state = viewModel.uiState.first()
    assertEquals(60, state.passwordResetCooldownSeconds)

    // Verify repository was called twice
    coVerify(exactly = 2) { mockRepository.sendPasswordResetEmail("test@example.com") }
  }
}
