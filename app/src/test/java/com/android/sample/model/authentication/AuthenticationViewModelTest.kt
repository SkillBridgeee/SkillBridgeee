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
    val mockUser = mockk<FirebaseUser>()
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    coEvery { mockRepository.signInWithEmail(any(), any()) } returns Result.success(mockUser)

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

    every { mockCredentialHelper.getFirebaseCredential(any()) } returns mockk()
    coEvery { mockRepository.signInWithCredential(any()) } returns Result.success(mockFirebaseUser)
    coEvery { mockProfileRepository.getProfile("user-123") } returns null

    viewModelWithProfile.handleGoogleSignInResult(mockActivityResult)
    testDispatcher.scheduler.advanceUntilIdle()

    val authResult = viewModelWithProfile.authResult.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("", (authResult as AuthResult.RequiresSignUp).email)
  }
}
