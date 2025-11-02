package com.android.sample.model.authentication

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class UserSessionManagerTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun `getCurrentUserId executes without exception`() {
    // Given/When/Then - verify the method can be called without throwing
    UserSessionManager.getCurrentUserId()
  }

  @Test
  fun `authState StateFlow is accessible and has valid value`() {
    // Given/When
    val authState = UserSessionManager.authState

    // Then
    assertNotNull(authState)
    assertNotNull(authState.value)
    assertTrue(
        authState.value is AuthState.Loading ||
            authState.value is AuthState.Authenticated ||
            authState.value is AuthState.Unauthenticated)
  }

  @Test
  fun `currentUser StateFlow is accessible`() {
    // Given/When
    val currentUser = UserSessionManager.currentUser

    // Then
    assertNotNull(currentUser)
  }

  @Test
  fun `UserSessionManager singleton is accessible`() {
    // Given/When
    val instance1 = UserSessionManager
    val instance2 = UserSessionManager

    // Then
    assertSame(instance1, instance2)
    assertNotNull(instance1)
  }

  @Test
  fun `multiple calls to getCurrentUserId are consistent`() {
    // Given/When
    val userId1 = UserSessionManager.getCurrentUserId()
    val userId2 = UserSessionManager.getCurrentUserId()
    val userId3 = UserSessionManager.getCurrentUserId()

    // Then - all calls should return the same value
    assertEquals(userId1, userId2)
    assertEquals(userId2, userId3)
  }

  @Test
  fun `AuthState Loading is object type`() {
    // Given/When
    val loadingState: AuthState = AuthState.Loading

    // Then
    assertNotNull(loadingState)
  }

  @Test
  fun `AuthState Authenticated has correct properties`() {
    // Given/When
    val authenticatedState = AuthState.Authenticated("user123", "test@example.com")

    // Then
    assertEquals("user123", authenticatedState.userId)
    assertEquals("test@example.com", authenticatedState.email)
  }

  @Test
  fun `AuthState Authenticated can have null email`() {
    // Given/When
    val authenticatedState = AuthState.Authenticated("user123", null)

    // Then
    assertEquals("user123", authenticatedState.userId)
    assertNull(authenticatedState.email)
  }

  @Test
  fun `AuthState Unauthenticated is object type`() {
    // Given/When
    val unauthenticatedState: AuthState = AuthState.Unauthenticated

    // Then
    assertNotNull(unauthenticatedState)
  }

  @Test
  fun `AuthState Authenticated equality works correctly`() {
    // Given
    val state1 = AuthState.Authenticated("user1", "email1@example.com")
    val state2 = AuthState.Authenticated("user1", "email1@example.com")
    val state3 = AuthState.Authenticated("user2", "email1@example.com")
    val state4 = AuthState.Authenticated("user1", "email2@example.com")

    // Then
    assertEquals(state1, state2)
    assertNotEquals(state1, state3)
    assertNotEquals(state1, state4)
  }

  @Test
  fun `AuthState singleton objects are identical`() {
    // Given/When
    val loading1 = AuthState.Loading
    val loading2 = AuthState.Loading
    val unauth1 = AuthState.Unauthenticated
    val unauth2 = AuthState.Unauthenticated

    // Then
    assertSame(loading1, loading2)
    assertSame(unauth1, unauth2)
  }

  @Test
  fun `AuthState Authenticated with different userIds are not equal`() {
    // Given
    val state1 = AuthState.Authenticated("user1", "test@example.com")
    val state2 = AuthState.Authenticated("user2", "test@example.com")

    // Then
    assertNotEquals(state1, state2)
  }

  @Test
  fun `AuthState Authenticated with different emails are not equal`() {
    // Given
    val state1 = AuthState.Authenticated("user1", "email1@example.com")
    val state2 = AuthState.Authenticated("user1", "email2@example.com")

    // Then
    assertNotEquals(state1, state2)
  }

  @Test
  fun `AuthState different types are not equal`() {
    // Given
    val loading = AuthState.Loading
    val authenticated = AuthState.Authenticated("user1", "test@example.com")
    val unauthenticated = AuthState.Unauthenticated

    // Then
    assertNotEquals(loading, authenticated)
    assertNotEquals(loading, unauthenticated)
    assertNotEquals(authenticated, unauthenticated)
  }

  @Test
  fun `AuthState Authenticated can be created with various userId formats`() {
    // Given
    val testUserIds =
        listOf("simple-id", "user@domain", "12345", "uid-with-dashes", "special!chars#123")

    // When/Then
    testUserIds.forEach { userId ->
      val state = AuthState.Authenticated(userId, "test@example.com")
      assertEquals(userId, state.userId)
    }
  }

  @Test
  fun `AuthState Authenticated toString contains userId`() {
    // Given
    val state = AuthState.Authenticated("test-user", "test@example.com")

    // When
    val stringRepresentation = state.toString()

    // Then
    assertTrue(stringRepresentation.contains("test-user"))
  }

  @Test
  fun `logout executes without exception`() {
    // Given/When/Then - verify the method can be called without throwing
    UserSessionManager.logout()
  }

  @Test
  fun `logout clears current user ID`() {
    // Given - logout is called
    UserSessionManager.logout()

    // When
    val userId = UserSessionManager.getCurrentUserId()

    // Then - user ID should be null after logout
    assertNull(userId)
  }

  @Test
  fun `logout updates auth state`() = runTest {
    // Given
    UserSessionManager.logout()

    // When
    testDispatcher.scheduler.advanceUntilIdle()
    val authState = UserSessionManager.authState.value

    // Then - auth state should be Unauthenticated after logout
    assertTrue(authState is AuthState.Unauthenticated)
  }

  @Test
  fun `logout clears current user flow`() = runTest {
    // Given
    UserSessionManager.logout()

    // When
    testDispatcher.scheduler.advanceUntilIdle()
    val currentUser = UserSessionManager.currentUser.value

    // Then - current user should be null after logout
    assertNull(currentUser)
  }

  @Test
  fun `multiple logout calls do not cause errors`() {
    // Given/When - calling logout multiple times
    UserSessionManager.logout()
    UserSessionManager.logout()
    UserSessionManager.logout()

    // Then - verify no exception is thrown and state is consistent
    assertNull(UserSessionManager.getCurrentUserId())
    assertTrue(UserSessionManager.authState.value is AuthState.Unauthenticated)
  }
}
