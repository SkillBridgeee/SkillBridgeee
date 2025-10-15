package com.android.sample.model.authentication

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
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

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var mockContext: Context
  private val mockAuthService = mockk<AuthenticationService>()

  private lateinit var viewModel: AuthenticationViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Use ApplicationProvider for proper Android context in Robolectric
    mockContext = ApplicationProvider.getApplicationContext()

    // Mock the provider to return our mock service
    mockkObject(AuthenticationServiceProvider)
    every { AuthenticationServiceProvider.getAuthenticationService(any()) } returns mockAuthService

    // Set up default mock behaviors for all methods
    coEvery { mockAuthService.signInWithEmailAndPassword(any(), any()) } returns
        AuthResult.Success(AuthUser("uid", "test@example.com", "Test User", null))
    coEvery { mockAuthService.signUpWithEmailAndPassword(any(), any(), any()) } returns
        AuthResult.Success(AuthUser("uid", "test@example.com", "Test User", null))
    coEvery { mockAuthService.sendPasswordResetEmail(any()) } returns true
    coEvery { mockAuthService.signOut() } returns Unit
    every { mockAuthService.isUserSignedIn() } returns false
    every { mockAuthService.getCurrentUser() } returns null

    viewModel = AuthenticationViewModel(mockContext)
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
    assertEquals(UserRole.LEARNER, state.selectedRole)
    assertFalse(state.showSuccessMessage)
    assertFalse(state.isSignInButtonEnabled)
    assertEquals("", state.name)
    assertFalse(state.isSignUpButtonEnabled)
  }

  @Test
  fun updateEmail_updatesStateAndButtonStates() = runTest {
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    val state = viewModel.uiState.first()

    assertEquals("test@example.com", state.email)
    assertTrue(state.isSignInButtonEnabled)
  }

  @Test
  fun updatePassword_updatesStateAndButtonStates() = runTest {
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    val state = viewModel.uiState.first()

    assertEquals("password123", state.password)
    assertTrue(state.isSignInButtonEnabled)
  }

  @Test
  fun updateSelectedRole_updatesState() = runTest {
    viewModel.updateSelectedRole(UserRole.TUTOR)

    val state = viewModel.uiState.first()

    assertEquals(UserRole.TUTOR, state.selectedRole)
  }

  @Test
  fun updateName_updatesStateAndSignUpButton() = runTest {
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")
    viewModel.updateName("Test User")

    val state = viewModel.uiState.first()

    assertEquals("Test User", state.name)
    assertTrue(state.isSignUpButtonEnabled)
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
  fun signUpButtonEnabled_onlyWhenAllFieldsProvided() = runTest {
    // Initially disabled
    var state = viewModel.uiState.first()
    assertFalse(state.isSignUpButtonEnabled)

    // Still disabled with partial fields
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")
    state = viewModel.uiState.first()
    assertFalse(state.isSignUpButtonEnabled)

    // Enabled with all required fields
    viewModel.updateName("Test User")
    state = viewModel.uiState.first()
    assertTrue(state.isSignUpButtonEnabled)
  }

  @Test
  fun showSuccessMessage_updatesState() = runTest {
    viewModel.showSuccessMessage(true)

    val state = viewModel.uiState.first()

    assertTrue(state.showSuccessMessage)
  }

  @Test
  fun signIn_callsServiceWithCurrentState() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val successResult = AuthResult.Success(AuthUser("uid", email, "Test User", null))

    viewModel.updateEmail(email)
    viewModel.updatePassword(password)

    coEvery { mockAuthService.signInWithEmailAndPassword(email, password) } returns successResult

    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { mockAuthService.signInWithEmailAndPassword(email, password) }
  }

  @Test
  fun signInWithEmailAndPassword_withValidCredentials_succeeds() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val successResult = AuthResult.Success(AuthUser("uid", email, "Test User", null))

    coEvery { mockAuthService.signInWithEmailAndPassword(email, password) } returns successResult

    viewModel.signInWithEmailAndPassword(email, password)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertNull(state.error)
    assertTrue(state.showSuccessMessage)
    assertEquals(successResult, authResult)
  }

  @Test
  fun signInWithEmailAndPassword_withInvalidEmail_showsError() = runTest {
    val email = "invalid-email"
    val password = "password123"

    viewModel.signInWithEmailAndPassword(email, password)

    val state = viewModel.uiState.first()

    assertEquals("Please enter a valid email and password (min 6 characters)", state.error)
    assertFalse(state.isSignInButtonEnabled)
  }

  @Test
  fun signInWithEmailAndPassword_withShortPassword_showsError() = runTest {
    val email = "test@example.com"
    val password = "123"

    viewModel.signInWithEmailAndPassword(email, password)

    val state = viewModel.uiState.first()

    assertEquals("Please enter a valid email and password (min 6 characters)", state.error)
  }

  @Test
  fun signInWithEmailAndPassword_withAuthError_showsError() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val errorResult = AuthResult.Error(Exception("Auth failed"))

    coEvery { mockAuthService.signInWithEmailAndPassword(email, password) } returns errorResult

    viewModel.signInWithEmailAndPassword(email, password)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertEquals("Auth failed", state.error)
    assertFalse(state.showSuccessMessage)
    assertEquals(errorResult, authResult)
  }

  @Test
  fun signUpWithEmailAndPassword_withValidData_succeeds() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val name = "Test User"
    val successResult = AuthResult.Success(AuthUser("uid", email, name, null))

    coEvery { mockAuthService.signUpWithEmailAndPassword(email, password, name) } returns
        successResult

    viewModel.signUpWithEmailAndPassword(email, password, name)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertNull(state.error)
    assertEquals(successResult, authResult)
  }

  @Test
  fun signUpWithEmailAndPassword_withInvalidData_showsError() = runTest {
    val email = "invalid-email"
    val password = "123"
    val name = ""

    viewModel.signUpWithEmailAndPassword(email, password, name)

    val state = viewModel.uiState.first()

    assertEquals("Please enter valid email, password (min 6 characters), and name", state.error)
  }

  @Test
  fun signUp_callsServiceWithCurrentState() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val name = "Test User"
    val successResult = AuthResult.Success(AuthUser("uid", email, name, null))

    viewModel.updateEmail(email)
    viewModel.updatePassword(password)
    viewModel.updateName(name)

    coEvery { mockAuthService.signUpWithEmailAndPassword(email, password, name) } returns
        successResult

    viewModel.signUp()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { mockAuthService.signUpWithEmailAndPassword(email, password, name) }
  }

  @Test
  fun sendPasswordReset_withValidEmail_succeeds() = runTest {
    val email = "test@example.com"
    viewModel.updateEmail(email)

    coEvery { mockAuthService.sendPasswordResetEmail(email) } returns true

    viewModel.sendPasswordReset()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()

    assertEquals("Password reset email sent!", state.message)
    assertNull(state.error)
  }

  @Test
  fun sendPasswordReset_withEmptyEmail_showsError() = runTest {
    viewModel.sendPasswordReset()

    val state = viewModel.uiState.first()

    assertEquals("Please enter your email address first", state.error)
  }

  @Test
  fun sendPasswordResetEmail_withInvalidEmail_showsError() = runTest {
    val email = "invalid-email"

    viewModel.sendPasswordResetEmail(email)

    val state = viewModel.uiState.first()

    assertEquals("Please enter a valid email address", state.error)
  }

  @Test
  fun sendPasswordResetEmail_withServiceFailure_showsError() = runTest {
    val email = "test@example.com"

    coEvery { mockAuthService.sendPasswordResetEmail(email) } returns false

    viewModel.sendPasswordResetEmail(email)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()

    assertEquals("Failed to send password reset email", state.error)
    assertNull(state.message)
  }

  @Test
  fun handleGoogleSignInResult_withSuccess_updatesState() = runTest {
    val successResult = AuthResult.Success(AuthUser("uid", "test@example.com", "Test User", null))

    viewModel.handleGoogleSignInResult(successResult)

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertNull(state.error)
    assertEquals(successResult, authResult)
  }

  @Test
  fun handleGoogleSignInResult_withError_updatesState() = runTest {
    val errorResult = AuthResult.Error(Exception("Google sign-in failed"))

    viewModel.handleGoogleSignInResult(errorResult)

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    assertFalse(state.isLoading)
    assertEquals("Google sign-in failed", state.error)
    assertEquals(errorResult, authResult)
  }

  @Test
  fun signOut_clearsStateAndCallsService() = runTest {
    // Set some initial state
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    viewModel.signOut()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    val authResult = viewModel.authResult.first()

    // State should be reset to defaults
    assertEquals("", state.email)
    assertEquals("", state.password)
    assertNull(authResult)

    coVerify { mockAuthService.signOut() }
  }

  @Test
  fun clearError_removesError() = runTest {
    viewModel.setError("Test error")
    viewModel.clearError()

    val state = viewModel.uiState.first()

    assertNull(state.error)
  }

  @Test
  fun clearMessage_removesMessage() = runTest {
    // First set a message by sending password reset
    val email = "test@example.com"
    coEvery { mockAuthService.sendPasswordResetEmail(email) } returns true

    viewModel.sendPasswordResetEmail(email)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.clearMessage()

    val state = viewModel.uiState.first()

    assertNull(state.message)
  }

  @Test
  fun isUserSignedIn_delegatesToService() {
    every { mockAuthService.isUserSignedIn() } returns true

    val result = viewModel.isUserSignedIn()

    assertTrue(result)
    verify { mockAuthService.isUserSignedIn() }
  }

  @Test
  fun getCurrentUser_delegatesToService() {
    val expectedUser = AuthUser("uid", "email", "name", null)
    every { mockAuthService.getCurrentUser() } returns expectedUser

    val result = viewModel.getCurrentUser()

    assertEquals(expectedUser, result)
    verify { mockAuthService.getCurrentUser() }
  }

  @Test
  fun setError_updatesErrorState() = runTest {
    val errorMessage = "Custom error message"

    viewModel.setError(errorMessage)

    val state = viewModel.uiState.first()

    assertEquals(errorMessage, state.error)
  }

  @Test
  fun loadingState_disablesButtons() = runTest {
    viewModel.updateEmail("test@example.com")
    viewModel.updatePassword("password123")

    // Simulate loading state during sign-in
    coEvery { mockAuthService.signInWithEmailAndPassword(any(), any()) } coAnswers
        {
          // Check state while loading
          val state = viewModel.uiState.first()
          assertTrue(state.isLoading)
          assertFalse(state.isSignInButtonEnabled)

          AuthResult.Success(AuthUser("uid", "email", "name", null))
        }

    viewModel.signIn()
    testDispatcher.scheduler.advanceUntilIdle()
  }
}
