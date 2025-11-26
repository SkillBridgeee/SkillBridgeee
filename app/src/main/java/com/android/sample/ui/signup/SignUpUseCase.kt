package com.android.sample.ui.signup

import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.google.firebase.auth.FirebaseAuthException

/** Data class representing the input for sign-up operation. */
data class SignUpRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val levelOfEducation: String,
    val description: String,
    val address: String,
    val location: Location? = null
)

/** Sealed class representing the result of a sign-up operation. */
sealed class SignUpResult {
  /** Sign-up completed successfully (for Google Sign-In users who don't need email verification) */
  object Success : SignUpResult()

  /** Verification email sent - user needs to verify before logging in */
  data class VerificationEmailSent(val email: String) : SignUpResult()

  /** Sign-up failed with an error */
  data class Error(val message: String) : SignUpResult()
}

/**
 * Use case that encapsulates the sign-up business logic.
 *
 * This separates the complex sign-up flow (Firebase Auth + Profile creation) from the ViewModel,
 * making the code more testable and maintainable.
 *
 * Responsibilities:
 * - Handle authentication (new users or already authenticated via Google)
 * - Create user profiles in Firestore
 * - Map Firebase exceptions to user-friendly error messages
 * - Handle the two-step process: auth → profile creation
 */
class SignUpUseCase(
    private val authRepository: AuthenticationRepository,
    private val profileRepository: ProfileRepository
) {

  /**
   * Executes the sign-up flow.
   *
   * @param request The sign-up data from the user
   * @return SignUpResult indicating success or failure with error message
   */
  suspend fun execute(request: SignUpRequest): SignUpResult {
    return try {
      // Check if user is already authenticated (e.g., via Google Sign-In)
      val currentUser = authRepository.getCurrentUser()

      if (currentUser != null) {
        // User already authenticated - just create profile
        createProfileForAuthenticatedUser(currentUser.uid, request)
      } else {
        // New user - create auth account then profile
        createNewUserWithProfile(request)
      }
    } catch (t: Throwable) {
      SignUpResult.Error(t.message ?: "Unknown error")
    }
  }

  /** Creates a profile for an already authenticated user (e.g., Google Sign-In). */
  private suspend fun createProfileForAuthenticatedUser(
      userId: String,
      request: SignUpRequest
  ): SignUpResult {
    return try {
      val profile = buildProfile(userId, request)
      profileRepository.addProfile(profile)
      SignUpResult.Success
    } catch (e: Exception) {
      SignUpResult.Error("Profile creation failed: ${e.message}")
    }
  }

  /** Creates a new Firebase Auth account, creates profile, and sends verification email. */
  private suspend fun createNewUserWithProfile(request: SignUpRequest): SignUpResult {
    val authResult = authRepository.signUpWithEmail(request.email, request.password)

    return authResult.fold(
        onSuccess = { firebaseUser ->
          // Auth successful - create profile first, then send verification email
          try {
            // Step 1: Create the profile before signing out
            val profile = buildProfile(firebaseUser.uid, request)
            try {
              profileRepository.addProfile(profile)
            } catch (profileException: Exception) {
              // Profile creation failed - sign out and return error
              authRepository.signOut()
              return SignUpResult.Error(
                  "Account created but profile creation failed: ${profileException.message}")
            }

            // Step 2: Send verification email
            val sendResult = authRepository.sendEmailVerification()

            sendResult.fold(
                onSuccess = {
                  // Sign out the user - they need to verify before logging in
                  // Profile is already created, so when they verify and log in, it will exist
                  authRepository.signOut()
                  SignUpResult.VerificationEmailSent(request.email)
                },
                onFailure = { exception ->
                  // Verification email failed to send
                  // Note: The Firebase Auth user and profile remain created. They can try to resend
                  // from login.
                  authRepository.signOut()
                  SignUpResult.Error(
                      "Account created but verification email couldn't be sent: ${exception.message}")
                })
          } catch (e: Exception) {
            authRepository.signOut()
            SignUpResult.Error("Failed to complete sign up: ${e.message}")
          }
        },
        onFailure = { exception ->
          // Firebase Auth account creation failed
          SignUpResult.Error(mapAuthException(exception))
        })
  }

  /** Builds a Profile object from the sign-up request. */
  private fun buildProfile(userId: String, request: SignUpRequest): Profile {
    val fullName =
        listOf(request.name.trim(), request.surname.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    // Use the selected location if available, otherwise create a Location with just the address
    // name
    val location = request.location ?: Location(name = request.address.trim())

    return Profile(
        userId = userId,
        name = fullName,
        email = request.email.trim(),
        levelOfEducation = request.levelOfEducation.trim(),
        description = request.description.trim(),
        location = location)
  }

  /** Maps Firebase authentication exceptions to user-friendly error messages. */
  private fun mapAuthException(exception: Throwable): String {
    return if (exception is FirebaseAuthException) {
      when (exception.errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
        "ERROR_INVALID_EMAIL" -> "Invalid email format"
        "ERROR_WEAK_PASSWORD" -> "Password is too weak"
        else -> exception.message ?: "Sign up failed"
      }
    } else {
      exception.message ?: "Sign up failed"
    }
  }
}
