package com.android.sample.model.authentication

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationViewModelExceptionTest {

  private lateinit var context: Context
  private lateinit var mockRepository: AuthenticationRepository
  private lateinit var mockCredentialHelper: CredentialAuthHelper
  private lateinit var testDispatcher: TestDispatcher

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    context = mockk(relaxed = true)
    mockRepository = mockk(relaxed = true)
    mockCredentialHelper = mockk(relaxed = true)

    // Mock FirebaseAuth to return null user by default
    mockkStatic(FirebaseAuth::class)
    val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns null
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkStatic(FirebaseAuth::class)
  }

  @Test
  fun authStateListener_whenProfileRepositoryThrowsException_returnsRequiresSignUp() = runTest {
    // Given - a user exists but profile repository throws exception
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "user-123"
    every { mockUser.email } returns "test@example.com"

    // Mock profile repository to throw exception
    val mockProfileRepo = mockk<com.android.sample.model.user.ProfileRepository>()
    coEvery { mockProfileRepo.getProfile("user-123") } throws
        Exception("Database connection failed")

    // Create a mock FirebaseAuth that we can control
    val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns null

    // Capture the auth state listener so we can trigger it manually
    val authListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    every { mockFirebaseAuth.addAuthStateListener(capture(authListenerSlot)) } just Runs

    // Create ViewModel (this registers the listener)
    val viewModelWithException =
        AuthenticationViewModel(context, mockRepository, mockCredentialHelper, mockProfileRepo)

    testDispatcher.scheduler.advanceUntilIdle()

    // Manually trigger the auth state listener with the mock user
    // This simulates what happens when Firebase detects an authentication change
    every { mockFirebaseAuth.currentUser } returns mockUser
    authListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)

    testDispatcher.scheduler.advanceUntilIdle()

    // Then - should return RequiresSignUp instead of crashing
    val authResult = viewModelWithException.authResult.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("test@example.com", (authResult as AuthResult.RequiresSignUp).email)
    assertEquals(mockUser, authResult.user)
  }

  @Test
  fun init_whenExistingUserAndProfileRepositoryThrowsException_setsRequiresSignUp() = runTest {
    // Given - Firebase already has a current user but profile fetch fails
    val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns "existing-user-123"
    every { mockUser.email } returns "existing@example.com"

    // Mock Firebase to return existing user
    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns mockUser

    // Mock profile repository to throw exception during init
    val mockProfileRepo = mockk<com.android.sample.model.user.ProfileRepository>()
    coEvery { mockProfileRepo.getProfile("existing-user-123") } throws Exception("Network timeout")

    // When - ViewModel is initialized with existing user
    val viewModelWithExistingUser =
        AuthenticationViewModel(context, mockRepository, mockCredentialHelper, mockProfileRepo)

    testDispatcher.scheduler.advanceUntilIdle()

    // Then - should set RequiresSignUp state instead of crashing
    val authResult = viewModelWithExistingUser.authResult.first()

    assertTrue(authResult is AuthResult.RequiresSignUp)
    assertEquals("existing@example.com", (authResult as AuthResult.RequiresSignUp).email)
    assertEquals(mockUser, authResult.user)
  }
}
