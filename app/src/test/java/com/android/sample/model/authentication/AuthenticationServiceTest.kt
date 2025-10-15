package com.android.sample.model.authentication

import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class AuthenticationServiceTest {

  @Mock private lateinit var authRepository: AuthenticationRepository

  @Mock private lateinit var profileRepository: ProfileRepository

  private lateinit var authenticationService: AuthenticationService

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    authenticationService = AuthenticationService(authRepository, profileRepository)
  }

  @Test
  fun signInWithEmailAndPassword_delegatesToRepository() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val expectedResult = AuthResult.Success(AuthUser("uid", email, "Test User", null))

    whenever(authRepository.signInWithEmailAndPassword(email, password)).thenReturn(expectedResult)

    val result = authenticationService.signInWithEmailAndPassword(email, password)

    assertEquals(expectedResult, result)
    verify(authRepository).signInWithEmailAndPassword(email, password)
  }

  @Test
  fun signUpWithEmailAndPassword_createsProfileOnSuccess() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val name = "Test User"
    val uid = "test-uid"
    val authUser = AuthUser(uid, email, name, null)
    val successResult = AuthResult.Success(authUser)

    whenever(authRepository.signUpWithEmailAndPassword(email, password, name))
        .thenReturn(successResult)

    val result = authenticationService.signUpWithEmailAndPassword(email, password, name)

    assertEquals(successResult, result)
    verify(authRepository).signUpWithEmailAndPassword(email, password, name)

    val expectedProfile = Profile(userId = uid, name = name, email = email)
    verify(profileRepository).addProfile(expectedProfile)
  }

  @Test
  fun signUpWithEmailAndPassword_handlesProfileCreationFailure() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val name = "Test User"
    val uid = "test-uid"
    val authUser = AuthUser(uid, email, name, null)
    val successResult = AuthResult.Success(authUser)

    whenever(authRepository.signUpWithEmailAndPassword(email, password, name))
        .thenReturn(successResult)
    whenever(profileRepository.addProfile(any()))
        .thenThrow(RuntimeException("Profile creation failed"))

    val result = authenticationService.signUpWithEmailAndPassword(email, password, name)

    // Should still return success even if profile creation fails
    assertEquals(successResult, result)
    verify(profileRepository).addProfile(any())
  }

  @Test
  fun signUpWithEmailAndPassword_returnsErrorOnAuthFailure() = runTest {
    val email = "test@example.com"
    val password = "password123"
    val name = "Test User"
    val errorResult = AuthResult.Error(Exception("Auth failed"))

    whenever(authRepository.signUpWithEmailAndPassword(email, password, name))
        .thenReturn(errorResult)

    val result = authenticationService.signUpWithEmailAndPassword(email, password, name)

    assertEquals(errorResult, result)
    verify(authRepository).signUpWithEmailAndPassword(email, password, name)
    verify(profileRepository, never()).addProfile(any())
  }

  @Test
  fun handleGoogleSignInResult_withValidRepository() = runTest {
    val idToken = "test-token"
    val uid = "google-uid"
    val authUser = AuthUser(uid, "google@example.com", "Google User", null)
    val successResult = AuthResult.Success(authUser)
    val emptyProfile = Profile(userId = "", name = "", email = "")

    val firebaseRepo = mock<FirebaseAuthenticationRepository>()
    val authService = AuthenticationService(firebaseRepo, profileRepository)

    whenever(firebaseRepo.handleGoogleSignInResult(idToken)).thenReturn(successResult)
    whenever(profileRepository.getProfile(uid)).thenReturn(emptyProfile)

    val result = authService.handleGoogleSignInResult(idToken)

    assertEquals(successResult, result)
    verify(firebaseRepo).handleGoogleSignInResult(idToken)

    val expectedProfile = Profile(userId = uid, name = "Google User", email = "google@example.com")
    verify(profileRepository).addProfile(expectedProfile)
  }

  @Test
  fun handleGoogleSignInResult_withExistingProfile() = runTest {
    val idToken = "test-token"
    val uid = "google-uid"
    val authUser = AuthUser(uid, "google@example.com", "Google User", null)
    val successResult = AuthResult.Success(authUser)
    val existingProfile =
        Profile(userId = uid, name = "Existing User", email = "existing@example.com")

    val firebaseRepo = mock<FirebaseAuthenticationRepository>()
    val authService = AuthenticationService(firebaseRepo, profileRepository)

    whenever(firebaseRepo.handleGoogleSignInResult(idToken)).thenReturn(successResult)
    whenever(profileRepository.getProfile(uid)).thenReturn(existingProfile)

    val result = authService.handleGoogleSignInResult(idToken)

    assertEquals(successResult, result)
    verify(profileRepository, never()).addProfile(any())
  }

  @Test
  fun handleGoogleSignInResult_withInvalidRepository() = runTest {
    val idToken = "test-token"

    val result = authenticationService.handleGoogleSignInResult(idToken)

    assertTrue(result is AuthResult.Error)
    assertEquals(
        "Invalid repository type for Google Sign-In",
        (result as AuthResult.Error).exception.message)
  }

  @Test
  fun signOut_delegatesToRepository() = runTest {
    authenticationService.signOut()

    verify(authRepository).signOut()
  }

  @Test
  fun getCurrentUser_delegatesToRepository() {
    val expectedUser = AuthUser("uid", "email", "name", null)
    whenever(authRepository.getCurrentUser()).thenReturn(expectedUser)

    val result = authenticationService.getCurrentUser()

    assertEquals(expectedUser, result)
    verify(authRepository).getCurrentUser()
  }

  @Test
  fun getCurrentUser_returnsNull() {
    whenever(authRepository.getCurrentUser()).thenReturn(null)

    val result = authenticationService.getCurrentUser()

    assertNull(result)
    verify(authRepository).getCurrentUser()
  }

  @Test
  fun isUserSignedIn_delegatesToRepository() {
    whenever(authRepository.isUserSignedIn()).thenReturn(true)

    val result = authenticationService.isUserSignedIn()

    assertTrue(result)
    verify(authRepository).isUserSignedIn()
  }

  @Test
  fun sendPasswordResetEmail_delegatesToRepository() = runTest {
    val email = "test@example.com"
    whenever(authRepository.sendPasswordResetEmail(email)).thenReturn(true)

    val result = authenticationService.sendPasswordResetEmail(email)

    assertTrue(result)
    verify(authRepository).sendPasswordResetEmail(email)
  }

  @Test
  fun deleteAccount_deletesProfileAndAccount() = runTest {
    val uid = "test-uid"
    val currentUser = AuthUser(uid, "test@example.com", "Test User", null)

    whenever(authRepository.getCurrentUser()).thenReturn(currentUser)
    whenever(authRepository.deleteAccount()).thenReturn(true)

    val result = authenticationService.deleteAccount()

    assertTrue(result)
    verify(profileRepository).deleteProfile(uid)
    verify(authRepository).deleteAccount()
  }

  @Test
  fun deleteAccount_withNoCurrentUser() = runTest {
    whenever(authRepository.getCurrentUser()).thenReturn(null)

    val result = authenticationService.deleteAccount()

    assertFalse(result)
    verify(profileRepository, never()).deleteProfile(any())
    verify(authRepository, never()).deleteAccount()
  }

  @Test
  fun deleteAccount_handlesProfileDeletionFailure() = runTest {
    val uid = "test-uid"
    val currentUser = AuthUser(uid, "test@example.com", "Test User", null)

    whenever(authRepository.getCurrentUser()).thenReturn(currentUser)
    whenever(profileRepository.deleteProfile(uid))
        .thenThrow(RuntimeException("Profile deletion failed"))

    val result = authenticationService.deleteAccount()

    assertFalse(result)
    verify(profileRepository).deleteProfile(uid)
    verify(authRepository, never()).deleteAccount()
  }

  @Test
  fun getCurrentUserProfile_returnsProfile() = runTest {
    val uid = "test-uid"
    val currentUser = AuthUser(uid, "test@example.com", "Test User", null)
    val expectedProfile = Profile(userId = uid, name = "Test User", email = "test@example.com")

    whenever(authRepository.getCurrentUser()).thenReturn(currentUser)
    whenever(profileRepository.getProfile(uid)).thenReturn(expectedProfile)

    val result = authenticationService.getCurrentUserProfile()

    assertEquals(expectedProfile, result)
    verify(profileRepository).getProfile(uid)
  }

  @Test
  fun getCurrentUserProfile_withNoCurrentUser() = runTest {
    whenever(authRepository.getCurrentUser()).thenReturn(null)

    val result = authenticationService.getCurrentUserProfile()

    assertNull(result)
    verify(profileRepository, never()).getProfile(any())
  }

  @Test
  fun getCurrentUserProfile_handlesException() = runTest {
    val uid = "test-uid"
    val currentUser = AuthUser(uid, "test@example.com", "Test User", null)

    whenever(authRepository.getCurrentUser()).thenReturn(currentUser)
    whenever(profileRepository.getProfile(uid)).thenThrow(RuntimeException("Profile fetch failed"))

    val result = authenticationService.getCurrentUserProfile()

    assertNull(result)
  }

  @Test
  fun getAuthRepository_returnsRepository() {
    val result = authenticationService.getAuthRepository()

    assertEquals(authRepository, result)
  }
}
