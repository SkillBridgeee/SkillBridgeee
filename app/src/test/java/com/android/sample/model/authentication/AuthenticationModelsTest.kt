package com.android.sample.model.authentication

import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class AuthenticationModelsTest {

  @Test
  fun authResult_Success_holdsUser() {
    val mockUser = mockk<FirebaseUser>()
    val result = AuthResult.Success(mockUser)

    assertEquals(mockUser, result.user)
  }

  @Test
  fun authResult_Error_holdsMessage() {
    val errorMessage = "Authentication failed"
    val result = AuthResult.Error(errorMessage)

    assertEquals(errorMessage, result.message)
  }

  @Test
  fun authenticationUiState_defaultValues() {
    val state = AuthenticationUiState()

    assertEquals("", state.email)
    assertEquals("", state.password)
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNull(state.message)
    assertFalse(state.showSuccessMessage)
  }

  @Test
  fun authenticationUiState_isSignInButtonEnabled_withEmptyFields() {
    val state = AuthenticationUiState(email = "", password = "")

    assertFalse(state.isSignInButtonEnabled)
  }

  @Test
  fun authenticationUiState_isSignInButtonEnabled_withOnlyEmail() {
    val state = AuthenticationUiState(email = "test@example.com", password = "")

    assertFalse(state.isSignInButtonEnabled)
  }

  @Test
  fun authenticationUiState_isSignInButtonEnabled_withOnlyPassword() {
    val state = AuthenticationUiState(email = "", password = "password123")

    assertFalse(state.isSignInButtonEnabled)
  }

  @Test
  fun authenticationUiState_isSignInButtonEnabled_withBothFields() {
    val state = AuthenticationUiState(email = "test@example.com", password = "password123")

    assertTrue(state.isSignInButtonEnabled)
  }

  @Test
  fun authenticationUiState_isSignInButtonEnabled_disabledWhileLoading() {
    val state =
        AuthenticationUiState(
            email = "test@example.com", password = "password123", isLoading = true)

    assertFalse(state.isSignInButtonEnabled)
  }

  @Test
  fun authenticationUiState_withCustomValues() {
    val state =
        AuthenticationUiState(
            email = "custom@example.com",
            password = "custompass",
            isLoading = true,
            error = "Custom error",
            message = "Custom message",
            showSuccessMessage = true)

    assertEquals("custom@example.com", state.email)
    assertEquals("custompass", state.password)
    assertTrue(state.isLoading)
    assertEquals("Custom error", state.error)
    assertEquals("Custom message", state.message)
    assertTrue(state.showSuccessMessage)
  }

  @Test
  fun authenticationUiState_copy_updatesSpecificFields() {
    val originalState =
        AuthenticationUiState(email = "original@example.com", password = "originalpass")

    val updatedState = originalState.copy(email = "updated@example.com")

    assertEquals("updated@example.com", updatedState.email)
    assertEquals("originalpass", updatedState.password)
  }
}
