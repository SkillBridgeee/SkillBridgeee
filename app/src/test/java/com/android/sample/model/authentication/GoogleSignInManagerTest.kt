package com.android.sample.model.authentication

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class GoogleSignInManagerTest {

  @Mock private lateinit var mockGoogleSignInHelper: GoogleSignInHelper

  private lateinit var googleSignInManager: GoogleSignInManager

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    googleSignInManager = GoogleSignInManagerImpl(mockGoogleSignInHelper)
  }

  @Test
  fun signInWithGoogle_withValidHelper_callsHelper() {
    val onResult: (AuthResult) -> Unit = mock()

    googleSignInManager.signInWithGoogle(onResult)

    verify(mockGoogleSignInHelper).signInWithGoogle()
  }

  @Test
  fun signInWithGoogle_withNullHelper_returnsError() {
    val managerWithNullHelper = GoogleSignInManagerImpl(null)
    var capturedResult: AuthResult? = null

    managerWithNullHelper.signInWithGoogle { result -> capturedResult = result }

    assertTrue(capturedResult is AuthResult.Error)
    assertEquals(
        "Google Sign-In not available", (capturedResult as AuthResult.Error).exception.message)
  }

  @Test
  fun isAvailable_withValidHelper_returnsTrue() {
    val result = googleSignInManager.isAvailable()

    assertTrue(result)
  }

  @Test
  fun isAvailable_withNullHelper_returnsFalse() {
    val managerWithNullHelper = GoogleSignInManagerImpl(null)

    val result = managerWithNullHelper.isAvailable()

    assertFalse(result)
  }
}
