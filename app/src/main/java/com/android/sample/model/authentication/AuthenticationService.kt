package com.android.sample.model.authentication

import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository

/**
 * Service class that handles authentication operations and integrates with user profile management
 */
class AuthenticationService(
    private val authRepository: AuthenticationRepository,
    private val profileRepository: ProfileRepository
) {

  /** Sign in user with email and password */
  suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
    return authRepository.signInWithEmailAndPassword(email, password)
  }

  /** Sign up user with email and password and create profile */
  suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      name: String
  ): AuthResult {
    val authResult = authRepository.signUpWithEmailAndPassword(email, password, name)

    // If authentication successful, create user profile
    if (authResult is AuthResult.Success) {
      try {
        val profile =
            Profile(
                userId = authResult.user.uid, // Firebase UID as userId
                name = name,
                email = email)
        profileRepository.addProfile(profile)
      } catch (e: Exception) {
        // If profile creation fails, we might want to delete the auth user
        // For now, we'll return the auth success but log the error
        println("Failed to create profile for user ${authResult.user.uid}: ${e.message}")
      }
    }

    return authResult
  }

  /** Handle Google Sign-In result and create/update profile if needed */
  suspend fun handleGoogleSignInResult(idToken: String): AuthResult {
    val firebaseRepo =
        authRepository as? FirebaseAuthenticationRepository
            ?: return AuthResult.Error(Exception("Invalid repository type for Google Sign-In"))

    val authResult = firebaseRepo.handleGoogleSignInResult(idToken)

    // If authentication successful, create or update user profile
    if (authResult is AuthResult.Success) {
      try {
        val existingProfile = profileRepository.getProfile(authResult.user.uid)
        if (existingProfile.userId.isEmpty()) {
          // Create new profile for Google user
          val profile =
              Profile(
                  userId = authResult.user.uid, // Firebase UID as userId
                  name = authResult.user.displayName ?: "",
                  email = authResult.user.email ?: "")
          profileRepository.addProfile(profile)
        }
      } catch (e: Exception) {
        println(
            "Failed to create/update profile for Google user ${authResult.user.uid}: ${e.message}")
      }
    }

    return authResult
  }

  /** Sign out current user */
  suspend fun signOut() {
    authRepository.signOut()
  }

  /** Get current authenticated user */
  fun getCurrentUser(): AuthUser? {
    return authRepository.getCurrentUser()
  }

  /** Check if user is signed in */
  fun isUserSignedIn(): Boolean {
    return authRepository.isUserSignedIn()
  }

  /** Send password reset email */
  suspend fun sendPasswordResetEmail(email: String): Boolean {
    return authRepository.sendPasswordResetEmail(email)
  }

  /** Delete user account and profile */
  suspend fun deleteAccount(): Boolean {
    val currentUser = getCurrentUser()
    if (currentUser != null) {
      try {
        // Delete profile first
        profileRepository.deleteProfile(currentUser.uid)
        // Then delete auth account
        return authRepository.deleteAccount()
      } catch (e: Exception) {
        println("Failed to delete profile for user ${currentUser.uid}: ${e.message}")
        return false
      }
    }
    return false
  }

  /** Get user profile for current authenticated user */
  suspend fun getCurrentUserProfile(): Profile? {
    val currentUser = getCurrentUser()
    return if (currentUser != null) {
      try {
        profileRepository.getProfile(currentUser.uid)
      } catch (e: Exception) {
        null
      }
    } else {
      null
    }
  }

  /** Get the underlying auth repository (needed for Google Sign-In helper) */
  fun getAuthRepository(): AuthenticationRepository = authRepository
}
