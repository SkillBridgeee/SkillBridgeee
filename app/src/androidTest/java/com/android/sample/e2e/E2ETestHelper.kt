package com.android.sample.e2e

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Helper object containing utility functions for end-to-end tests. Provides common functionality
 * for authentication, user management, and test setup.
 */
object E2ETestHelper {

  /**
   * Creates a test profile for a user in Firestore. This simulates an existing user who has already
   * signed up.
   *
   * @param userId The Firebase user ID
   * @param email The user's email address
   * @param name The user's name
   * @param surname The user's surname (optional)
   */
  suspend fun createTestProfile(userId: String, email: String, name: String, surname: String = "") {
    val fullName = if (surname.isNotEmpty()) "$name $surname" else name

    val profile =
        Profile(
            userId = userId,
            name = fullName,
            email = email,
            levelOfEducation = "Bachelor",
            location = Location(latitude = 0.0, longitude = 0.0),
            hourlyRate = "25",
            description = "Test user for E2E testing",
            tutorRating = RatingInfo(averageRating = 5.0, totalRatings = 0),
            studentRating = RatingInfo(averageRating = 5.0, totalRatings = 0))

    ProfileRepositoryProvider.repository.addProfile(profile)
  }

  /**
   * Cleans up test data by deleting a user profile from Firestore.
   *
   * @param userId The Firebase user ID to clean up
   */
  suspend fun cleanupTestProfile(userId: String) {
    try {
      val firestore = FirebaseFirestore.getInstance()
      firestore.collection("profiles").document(userId).delete().await()
    } catch (_: Exception) {
      // Ignore cleanup errors
    }
  }

  /**
   * Cleans up a Firebase Auth user.
   *
   * @param user The FirebaseUser to delete
   */
  suspend fun cleanupFirebaseUser(user: FirebaseUser?) {
    try {
      user?.delete()?.await()
    } catch (_: Exception) {
      // Ignore cleanup errors
    }
  }

  /** Signs out the current user from Firebase Auth. */
  fun signOutCurrentUser() {
    FirebaseAuth.getInstance().signOut()
  }

  /**
   * Waits for a semantic node to be displayed.
   *
   * @param composeTestRule The ComposeTestRule for the test
   * @param node The SemanticsNodeInteraction to wait for
   * @param timeoutMillis Maximum time to wait in milliseconds (default: 10 seconds)
   */
  @Suppress("unused")
  fun waitForNode(
      composeTestRule: ComposeTestRule,
      node: SemanticsNodeInteraction,
      timeoutMillis: Long = 10_000L
  ) {
    composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
      try {
        node.assertExists()
        true
      } catch (_: AssertionError) {
        false
      }
    }
  }

  /**
   * Gets the current Firebase Auth user.
   *
   * @return The current FirebaseUser or null if not authenticated
   */
  fun getCurrentUser(): FirebaseUser? {
    return FirebaseAuth.getInstance().currentUser
  }

  /**
   * Checks if the current user is authenticated.
   *
   * @return true if a user is authenticated, false otherwise
   */
  fun isUserAuthenticated(): Boolean {
    return getCurrentUser() != null
  }

  /**
   * Gets the target application context.
   *
   * @return The application context
   */
  @Suppress("unused") fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext

  /**
   * Initializes Firebase emulators for testing. This is automatically done in TestRunner, but can
   * be called again if needed.
   */
  @Suppress("unused")
  fun initializeFirebaseEmulators() {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Use emulators
    try {
      firestore.useEmulator("10.0.2.2", 8080)
      auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {
      // Already initialized, ignore
    }
  }

  /** Clears all Firestore data in the emulator. WARNING: Only use in test environments! */
  @Suppress("unused")
  suspend fun clearFirestoreData() {
    try {
      val firestore = FirebaseFirestore.getInstance()
      // Delete all profiles
      val profiles = firestore.collection("profiles").get().await()
      profiles.documents.forEach { it.reference.delete().await() }
    } catch (_: Exception) {
      // Ignore errors during cleanup
    }
  }

  /**
   * Creates a Google Sign-In test account and authenticates with Firebase. This creates a real
   * Firebase user that can be used for E2E testing.
   *
   * IMPORTANT: This function simulates a Google-authenticated user. Google users are automatically
   * email-verified, so we need to handle this.
   *
   * @param email The email for the test account
   * @param displayName The display name for the test account (not used in current implementation)
   * @return The authenticated FirebaseUser
   */
  suspend fun createAndAuthenticateGoogleUser(
      email: String,
      @Suppress("UNUSED_PARAMETER") displayName: String
  ): FirebaseUser {
    val auth = FirebaseAuth.getInstance()

    // Create a temporary email/password account that simulates a Google-signed-in user
    try {
      val result = auth.createUserWithEmailAndPassword(email, "TestPassword123!").await()
      val user = result.user!!

      // Google users are automatically verified, so we send a verification email
      // In the Firebase emulator, this marks the email as verified
      try {
        user.sendEmailVerification().await()
        // Reload to get the updated verification status
        user.reload().await()
      } catch (_: Exception) {
        // Ignore verification errors in emulator
      }

      return user
    } catch (_: Exception) {
      // If account already exists, sign in
      val result = auth.signInWithEmailAndPassword(email, "TestPassword123!").await()
      val user = result.user!!

      // Ensure email is verified
      if (!user.isEmailVerified) {
        try {
          user.sendEmailVerification().await()
          user.reload().await()
        } catch (_: Exception) {
          // Ignore verification errors
        }
      }

      return user
    }
  }
}
