package com.android.sample.model.authentication

import org.junit.Assert.*
import org.junit.Test

class AuthModelsTest {

  @Test
  fun testAuthUser_creation() {
    val user =
        AuthUser(
            uid = "test-uid",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg")

    assertEquals("test-uid", user.uid)
    assertEquals("test@example.com", user.email)
    assertEquals("Test User", user.displayName)
    assertEquals("https://example.com/photo.jpg", user.photoUrl)
  }

  @Test
  fun testAuthUser_withNullValues() {
    val user = AuthUser(uid = "test-uid", email = null, displayName = null, photoUrl = null)

    assertEquals("test-uid", user.uid)
    assertNull(user.email)
    assertNull(user.displayName)
    assertNull(user.photoUrl)
  }

  @Test
  fun testAuthResult_Success() {
    val user = AuthUser("uid", "email", "name", null)
    val result = AuthResult.Success(user)

    assertEquals(user, result.user)
  }

  @Test
  fun testAuthResult_Error() {
    val exception = Exception("Test error")
    val result = AuthResult.Error(exception)

    assertEquals(exception, result.exception)
    assertEquals("Test error", result.exception.message)
  }

  @Test
  fun testUserRole_enumValues() {
    assertEquals("Learner", UserRole.LEARNER.displayName)
    assertEquals("Tutor", UserRole.TUTOR.displayName)
  }

  @Test
  fun testUserRole_enumCount() {
    val roles = UserRole.values()
    assertEquals(2, roles.size)
    assertTrue(roles.contains(UserRole.LEARNER))
    assertTrue(roles.contains(UserRole.TUTOR))
  }

  @Test
  fun testAuthUiState_defaultValues() {
    val state = AuthUiState()

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
  fun testAuthUiState_withCustomValues() {
    val state =
        AuthUiState(
            isLoading = true,
            error = "Test error",
            message = "Test message",
            email = "test@example.com",
            password = "password123",
            selectedRole = UserRole.TUTOR,
            showSuccessMessage = true,
            isSignInButtonEnabled = true,
            name = "Test User",
            isSignUpButtonEnabled = true)

    assertTrue(state.isLoading)
    assertEquals("Test error", state.error)
    assertEquals("Test message", state.message)
    assertEquals("test@example.com", state.email)
    assertEquals("password123", state.password)
    assertEquals(UserRole.TUTOR, state.selectedRole)
    assertTrue(state.showSuccessMessage)
    assertTrue(state.isSignInButtonEnabled)
    assertEquals("Test User", state.name)
    assertTrue(state.isSignUpButtonEnabled)
  }

  @Test
  fun testAuthUiState_copyMethod() {
    val originalState = AuthUiState()
    val copiedState = originalState.copy(isLoading = true, email = "new@example.com")

    // Original state unchanged
    assertFalse(originalState.isLoading)
    assertEquals("", originalState.email)

    // Copied state has new values
    assertTrue(copiedState.isLoading)
    assertEquals("new@example.com", copiedState.email)

    // Other values remain the same
    assertEquals(originalState.password, copiedState.password)
    assertEquals(originalState.selectedRole, copiedState.selectedRole)
  }
}
