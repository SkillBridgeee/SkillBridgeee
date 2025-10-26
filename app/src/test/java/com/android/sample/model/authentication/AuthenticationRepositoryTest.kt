package com.android.sample.model.authentication

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthenticationRepositoryTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private lateinit var mockAuth: FirebaseAuth
  private lateinit var repository: AuthenticationRepository

  @Before
  fun setUp() {
    mockAuth = mockk(relaxed = true)
    repository = AuthenticationRepository(mockAuth)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun signOut_callsFirebaseAuthSignOut() {
    repository.signOut()

    verify { mockAuth.signOut() }
  }

  @Test
  fun getCurrentUser_returnsCurrentUser() {
    val mockUser = mockk<FirebaseUser>()
    every { mockAuth.currentUser } returns mockUser

    val result = repository.getCurrentUser()

    assertEquals(mockUser, result)
  }

  @Test
  fun getCurrentUser_returnsNull_whenNoUserSignedIn() {
    every { mockAuth.currentUser } returns null

    val result = repository.getCurrentUser()

    assertNull(result)
  }

  @Test
  fun isUserSignedIn_returnsTrue_whenUserSignedIn() {
    val mockUser = mockk<FirebaseUser>()
    every { mockAuth.currentUser } returns mockUser

    val result = repository.isUserSignedIn()

    assertTrue(result)
  }

  @Test
  fun isUserSignedIn_returnsFalse_whenNoUserSignedIn() {
    every { mockAuth.currentUser } returns null

    val result = repository.isUserSignedIn()

    assertFalse(result)
  }

  @Test
  fun signUpWithEmail_success_returnsUser() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()
    val mockTask = mockk<Task<AuthResult>>()

    every { mockAuthResult.user } returns mockUser
    coEvery { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns null
    coEvery { mockTask.isCanceled } returns false
    coEvery { mockTask.result } returns mockAuthResult

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signUpWithEmail_failure_returnsError() = runTest {
    val mockTask = mockk<Task<AuthResult>>()
    val exception = Exception("Email already in use")

    coEvery { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns exception
    coEvery { mockTask.isCanceled } returns false

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signUpWithEmail_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()
    val mockTask = mockk<Task<AuthResult>>()

    every { mockAuthResult.user } returns null
    coEvery { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns null
    coEvery { mockTask.isCanceled } returns false
    coEvery { mockTask.result } returns mockAuthResult

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("Sign up failed: No user created", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithEmail_success_returnsUser() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()
    val mockTask = mockk<Task<AuthResult>>()

    every { mockAuthResult.user } returns mockUser
    coEvery { mockAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns null
    coEvery { mockTask.isCanceled } returns false
    coEvery { mockTask.result } returns mockAuthResult

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signInWithEmail_failure_returnsError() = runTest {
    val mockTask = mockk<Task<AuthResult>>()
    val exception = Exception("Invalid credentials")

    coEvery { mockAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns exception
    coEvery { mockTask.isCanceled } returns false

    val result = repository.signInWithEmail("test@example.com", "wrongpassword")

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signInWithEmail_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()
    val mockTask = mockk<Task<AuthResult>>()

    every { mockAuthResult.user } returns null
    coEvery { mockAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns null
    coEvery { mockTask.isCanceled } returns false
    coEvery { mockTask.result } returns mockAuthResult

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("Sign in failed: No user", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithCredential_success_returnsUser() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()
    val mockTask = mockk<Task<AuthResult>>()
    val mockCredential = mockk<AuthCredential>()

    every { mockAuthResult.user } returns mockUser
    coEvery { mockAuth.signInWithCredential(any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns null
    coEvery { mockTask.isCanceled } returns false
    coEvery { mockTask.result } returns mockAuthResult

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signInWithCredential_failure_returnsError() = runTest {
    val mockTask = mockk<Task<AuthResult>>()
    val mockCredential = mockk<AuthCredential>()
    val exception = Exception("Credential error")

    coEvery { mockAuth.signInWithCredential(any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns exception
    coEvery { mockTask.isCanceled } returns false

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signInWithCredential_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()
    val mockTask = mockk<Task<AuthResult>>()
    val mockCredential = mockk<AuthCredential>()

    every { mockAuthResult.user } returns null
    coEvery { mockAuth.signInWithCredential(any()) } returns mockTask
    coEvery { mockTask.isComplete } returns true
    coEvery { mockTask.exception } returns null
    coEvery { mockTask.isCanceled } returns false
    coEvery { mockTask.result } returns mockAuthResult

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals("Sign in failed: No user", result.exceptionOrNull()?.message)
  }
}
