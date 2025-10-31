package com.android.sample.model.authentication

import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for AuthResult sealed class. These tests verify that the data classes hold the correct
 * values, which is useful for:
 * 1. Documenting the API contract
 * 2. Catching accidental changes to data class properties
 * 3. Verifying edge cases (like empty email)
 */
class AuthResultTest {

  @Test
  fun authResultSuccess_containsUser() {
    val mockUser = mockk<FirebaseUser>()
    val result = AuthResult.Success(mockUser)

    assertEquals(mockUser, result.user)
  }

  @Test
  fun authResultError_containsMessage() {
    val errorMessage = "Authentication failed"
    val result = AuthResult.Error(errorMessage)

    assertEquals(errorMessage, result.message)
  }

  @Test
  fun authResultRequiresSignUp_containsEmailAndUser() {
    val mockUser = mockk<FirebaseUser>()
    val email = "test@gmail.com"
    val result = AuthResult.RequiresSignUp(email, mockUser)

    assertEquals(email, result.email)
    assertEquals(mockUser, result.user)
  }

  @Test
  fun authResultRequiresSignUp_withEmptyEmail_isValid() {
    val mockUser = mockk<FirebaseUser>()
    val result = AuthResult.RequiresSignUp("", mockUser)

    assertEquals("", result.email)
    assertEquals(mockUser, result.user)
  }
}
