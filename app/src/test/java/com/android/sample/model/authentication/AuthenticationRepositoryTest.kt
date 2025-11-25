package com.android.sample.model.authentication

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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

    every { mockAuthResult.user } returns mockUser
    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signUpWithEmail_failure_returnsError() = runTest {
    val exception = Exception("Email already in use")

    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(exception)

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signUpWithEmail_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns null
    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("Sign up failed: No user created", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithEmail_success_returnsUser() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockUser.isEmailVerified } returns true
    every { mockUser.reload() } returns Tasks.forResult(null)
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signInWithEmail_failure_returnsError() = runTest {
    val exception = Exception("Invalid credentials")

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(exception)

    val result = repository.signInWithEmail("test@example.com", "wrongpassword")

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signInWithEmail_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns null
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("Sign in failed: No user", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithCredential_success_returnsUser() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()
    val mockCredential = mockk<AuthCredential>()

    every { mockAuthResult.user } returns mockUser
    every { mockAuth.signInWithCredential(any()) } returns Tasks.forResult(mockAuthResult)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signInWithCredential_failure_returnsError() = runTest {
    val mockCredential = mockk<AuthCredential>()
    val exception = Exception("Credential error")

    every { mockAuth.signInWithCredential(any()) } returns Tasks.forException(exception)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signInWithCredential_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()
    val mockCredential = mockk<AuthCredential>()

    every { mockAuthResult.user } returns null
    every { mockAuth.signInWithCredential(any()) } returns Tasks.forResult(mockAuthResult)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals("Sign in failed: No user", result.exceptionOrNull()?.message)
  }

  @Test
  fun signUpWithEmail_taskCanceled_returnsFailure() = runTest {
    val exception = Exception("Task was cancelled")

    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(exception)

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signInWithEmail_taskCanceled_returnsFailure() = runTest {
    val exception = Exception("Task was cancelled")

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(exception)

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signInWithCredential_taskCanceled_returnsFailure() = runTest {
    val mockCredential = mockk<AuthCredential>()
    val exception = Exception("Task was cancelled")

    every { mockAuth.signInWithCredential(any()) } returns Tasks.forException(exception)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  @Test
  fun signUpWithEmail_withDifferentEmails_callsCorrectMethod() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val email1 = "user1@example.com"
    val password1 = "password1"
    repository.signUpWithEmail(email1, password1)

    verify { mockAuth.createUserWithEmailAndPassword(email1, password1) }

    val email2 = "user2@example.com"
    val password2 = "password2"
    repository.signUpWithEmail(email2, password2)

    verify { mockAuth.createUserWithEmailAndPassword(email2, password2) }
  }

  @Test
  fun signInWithEmail_withDifferentCredentials_callsCorrectMethod() = runTest {
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val email1 = "user1@example.com"
    val password1 = "password1"
    repository.signInWithEmail(email1, password1)

    verify { mockAuth.signInWithEmailAndPassword(email1, password1) }

    val email2 = "user2@example.com"
    val password2 = "password2"
    repository.signInWithEmail(email2, password2)

    verify { mockAuth.signInWithEmailAndPassword(email2, password2) }
  }

  @Test
  fun signOut_multipleTimesDoesNotThrow() {
    repository.signOut()
    repository.signOut()
    repository.signOut()

    verify(exactly = 3) { mockAuth.signOut() }
  }

  @Test
  fun getCurrentUser_calledMultipleTimes_returnsConsistentResult() {
    val mockUser = mockk<FirebaseUser>()
    every { mockAuth.currentUser } returns mockUser

    val result1 = repository.getCurrentUser()
    val result2 = repository.getCurrentUser()
    val result3 = repository.getCurrentUser()

    assertEquals(mockUser, result1)
    assertEquals(mockUser, result2)
    assertEquals(mockUser, result3)
  }

  @Test
  fun isUserSignedIn_afterSignOut_returnsFalse() {
    val mockUser = mockk<FirebaseUser>()
    every { mockAuth.currentUser } returns mockUser andThen null

    val beforeSignOut = repository.isUserSignedIn()
    repository.signOut()
    val afterSignOut = repository.isUserSignedIn()

    assertTrue(beforeSignOut)
    assertFalse(afterSignOut)
  }

  // -------- Error Normalization Tests --------------------------------------------------------

  @Test
  fun signUpWithEmail_normalizesEmailAlreadyInUseError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
    every { firebaseException.message } returns "The email address is already in use"

    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("This email is already registered", result.exceptionOrNull()?.message)
  }

  @Test
  fun signUpWithEmail_normalizesInvalidEmailError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_INVALID_EMAIL"
    every { firebaseException.message } returns "The email address is badly formatted"

    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signUpWithEmail("invalid-email", "password123")

    assertTrue(result.isFailure)
    assertEquals("Invalid email format", result.exceptionOrNull()?.message)
  }

  @Test
  fun signUpWithEmail_normalizesWeakPasswordError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_WEAK_PASSWORD"
    every { firebaseException.message } returns "Password should be at least 6 characters"

    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signUpWithEmail("test@example.com", "123")

    assertTrue(result.isFailure)
    assertEquals(
        "Password is too weak. Use at least 6 characters", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithEmail_normalizesWrongPasswordError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_WRONG_PASSWORD"
    every { firebaseException.message } returns "The password is invalid"

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signInWithEmail("test@example.com", "wrongpassword")

    assertTrue(result.isFailure)
    assertEquals("Incorrect password", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithEmail_normalizesUserNotFoundError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_USER_NOT_FOUND"
    every { firebaseException.message } returns "There is no user record"

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signInWithEmail("nonexistent@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("No account found with this email", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithEmail_normalizesUserDisabledError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_USER_DISABLED"
    every { firebaseException.message } returns "The user account has been disabled"

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("This account has been disabled", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithEmail_normalizesTooManyRequestsError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_TOO_MANY_REQUESTS"
    every { firebaseException.message } returns "Too many unsuccessful login attempts"

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signInWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("Too many attempts. Please try again later", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithCredential_normalizesInvalidCredentialError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_INVALID_CREDENTIAL"
    every { firebaseException.message } returns "The supplied auth credential is malformed"

    val mockCredential = mockk<AuthCredential>()
    every { mockAuth.signInWithCredential(any()) } returns Tasks.forException(firebaseException)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals("Invalid credentials. Please try again", result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithCredential_normalizesAccountExistsWithDifferentCredentialError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"
    every { firebaseException.message } returns "An account already exists with the same email"

    val mockCredential = mockk<AuthCredential>()
    every { mockAuth.signInWithCredential(any()) } returns Tasks.forException(firebaseException)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals(
        "An account already exists with a different sign-in method",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun signInWithCredential_normalizesCredentialAlreadyInUseError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_CREDENTIAL_ALREADY_IN_USE"
    every { firebaseException.message } returns "This credential is already associated"

    val mockCredential = mockk<AuthCredential>()
    every { mockAuth.signInWithCredential(any()) } returns Tasks.forException(firebaseException)

    val result = repository.signInWithCredential(mockCredential)

    assertTrue(result.isFailure)
    assertEquals(
        "This credential is already associated with a different account",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun signUpWithEmail_normalizesUnknownFirebaseAuthError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_UNKNOWN"
    every { firebaseException.message } returns "Some unknown Firebase error"

    every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.signUpWithEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    // Should fall back to original message for unknown error codes
    assertEquals("Some unknown Firebase error", result.exceptionOrNull()?.message)
  }

  // -------- Email Verification Tests (NEW) -----------------------------------------------

  @Test
  fun signInWithEmail_unverifiedUser_signsOutAndReturnsError() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockUser.isEmailVerified } returns false
    every { mockUser.reload() } returns Tasks.forResult(null)
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.signInWithEmail("test@example.com", "password123")

    // Should sign out unverified user
    verify { mockAuth.signOut() }
    // Should return failure with verification message
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("verify your email") == true)
  }

  @Test
  fun signInWithEmail_verifiedUser_returnsSuccess() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockUser.isEmailVerified } returns true
    every { mockUser.reload() } returns Tasks.forResult(null)
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.signInWithEmail("test@example.com", "password123")

    // Should NOT sign out verified user
    verify(exactly = 0) { mockAuth.signOut() }
    // Should return success
    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun signInWithEmail_reloadsUserBeforeCheckingVerification() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockUser.isEmailVerified } returns true
    every { mockUser.reload() } returns Tasks.forResult(null)
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    repository.signInWithEmail("test@example.com", "password123")

    // Should reload user to get latest verification status
    verify { mockUser.reload() }
  }

  @Test
  fun sendEmailVerification_success_returnsSuccess() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)

    every { mockAuth.currentUser } returns mockUser
    every { mockUser.sendEmailVerification() } returns Tasks.forResult(null)

    val result = repository.sendEmailVerification()

    assertTrue(result.isSuccess)
    verify { mockUser.sendEmailVerification() }
  }

  @Test
  fun sendEmailVerification_noUserSignedIn_returnsFailure() = runTest {
    every { mockAuth.currentUser } returns null

    val result = repository.sendEmailVerification()

    assertTrue(result.isFailure)
    assertEquals("No user is currently signed in", result.exceptionOrNull()?.message)
  }

  @Test
  fun sendEmailVerification_failure_returnsNormalizedError() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_TOO_MANY_REQUESTS"
    every { firebaseException.message } returns "Too many requests"

    every { mockAuth.currentUser } returns mockUser
    every { mockUser.sendEmailVerification() } returns Tasks.forException(firebaseException)

    val result = repository.sendEmailVerification()

    assertTrue(result.isFailure)
    assertEquals("Too many attempts. Please try again later", result.exceptionOrNull()?.message)
  }

  @Test
  fun resendVerificationEmail_success_sendsEmailAndSignsOut() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockUser.isEmailVerified } returns false
    every { mockUser.reload() } returns Tasks.forResult(null)
    every { mockUser.sendEmailVerification() } returns Tasks.forResult(null)
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.resendVerificationEmail("test@example.com", "password123")

    assertTrue(result.isSuccess)
    verify { mockUser.sendEmailVerification() }
    verify { mockAuth.signOut() }
  }

  @Test
  fun resendVerificationEmail_alreadyVerified_returnsError() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns mockUser
    every { mockUser.isEmailVerified } returns true
    every { mockUser.reload() } returns Tasks.forResult(null)
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.resendVerificationEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("already verified") == true)
    verify { mockAuth.signOut() }
  }

  @Test
  fun resendVerificationEmail_noUserReturned_returnsFailure() = runTest {
    val mockAuthResult = mockk<AuthResult>()

    every { mockAuthResult.user } returns null
    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forResult(mockAuthResult)

    val result = repository.resendVerificationEmail("test@example.com", "password123")

    assertTrue(result.isFailure)
    assertEquals("Failed to sign in", result.exceptionOrNull()?.message)
  }

  @Test
  fun resendVerificationEmail_signInFails_returnsNormalizedError() = runTest {
    val firebaseException = mockk<FirebaseAuthException>(relaxed = true)
    every { firebaseException.errorCode } returns "ERROR_WRONG_PASSWORD"
    every { firebaseException.message } returns "Wrong password"

    every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns
        Tasks.forException(firebaseException)

    val result = repository.resendVerificationEmail("test@example.com", "wrongpassword")

    assertTrue(result.isFailure)
    assertEquals("Incorrect password", result.exceptionOrNull()?.message)
  }

  @Test
  fun isEmailVerified_verifiedUser_returnsTrue() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)

    every { mockAuth.currentUser } returns mockUser
    every { mockUser.isEmailVerified } returns true
    every { mockUser.reload() } returns Tasks.forResult(null)

    val result = repository.isEmailVerified()

    assertTrue(result)
    verify { mockUser.reload() }
  }

  @Test
  fun isEmailVerified_unverifiedUser_returnsFalse() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)

    every { mockAuth.currentUser } returns mockUser
    every { mockUser.isEmailVerified } returns false
    every { mockUser.reload() } returns Tasks.forResult(null)

    val result = repository.isEmailVerified()

    assertFalse(result)
  }

  @Test
  fun isEmailVerified_noUserSignedIn_returnsFalse() = runTest {
    every { mockAuth.currentUser } returns null

    val result = repository.isEmailVerified()

    assertFalse(result)
  }

  @Test
  fun isEmailVerified_reloadFails_returnsFalse() = runTest {
    val mockUser = mockk<FirebaseUser>(relaxed = true)

    every { mockAuth.currentUser } returns mockUser
    every { mockUser.reload() } returns Tasks.forException(Exception("Network error"))

    val result = repository.isEmailVerified()

    assertFalse(result)
  }
}
