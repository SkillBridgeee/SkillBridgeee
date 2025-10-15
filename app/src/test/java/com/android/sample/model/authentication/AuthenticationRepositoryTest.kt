package com.android.sample.model.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
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
}
