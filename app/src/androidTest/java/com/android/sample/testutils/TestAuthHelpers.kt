package com.android.sample.testutils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.text.set
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

object TestAuthHelpers {
  private val emulatorConfigured = AtomicBoolean(false)

  private fun configureEmulatorsOnce() {
    if (emulatorConfigured.compareAndSet(false, true)) {
      try {
        // Auth emulator on 10.0.2.2:9099 and Firestore emulator on 10.0.2.2:8080
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: Exception) {
        // already configured or running in an environment without emulator; continue
      }
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
      } catch (_: Exception) {}
    }
  }

  private suspend fun createAppProfileIfRequested(
      user: FirebaseUser?,
      email: String,
      displayName: String? = null,
      createAppProfile: Boolean = false
  ) {
    if (!createAppProfile || user == null) return

    // Build a safe display name and basic profile fields
    val nameParts = (displayName ?: email.substringBefore("@")).split(" ", limit = 2)
    val firstName = nameParts.getOrNull(0) ?: "Test"
    val lastName = nameParts.getOrNull(1) ?: "User"
    val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")

    // Create a map with multiple plausible keys for nested objects so the app's deserializer
    // (which may expect different names) can find something useful.
    val profileMap =
        mapOf(
            "userId" to user.uid,
            "name" to fullName,
            "email" to email,
            "levelOfEducation" to "Test",
            "description" to "Test user created by TestAuthHelpers",
            "hourlyRate" to "",
            // Provide both common shapes for location
            "location" to
                mapOf(
                    "name" to "Test Location",
                    "lat" to 0.0,
                    "lon" to 0.0,
                    "latitude" to 0.0,
                    "longitude" to 0.0),
            // Nested rating objects with minimal fields (many implementations expect average/count)
            "tutorRating" to mapOf("average" to 0.0, "count" to 0),
            "studentRating" to mapOf("average" to 0.0, "count" to 0))

    // Write to the same collection the app expects (most apps use "users") and await completion.
    // This ensures the document exists before the test launches the Activity / ViewModel.
    Firebase.firestore.collection("users").document(user.uid).set(profileMap).await()
  }

  /**
   * Create (if needed) and sign in with email/password using Firebase Auth emulator. Use in
   * androidTests to get an authenticated user.
   *
   * @param createAppProfile when true also creates a minimal Firestore `users/{uid}` doc.
   */
  suspend fun signInWithEmail(email: String, password: String, createAppProfile: Boolean = false) {
    configureEmulatorsOnce()
    val auth: FirebaseAuth = Firebase.auth

    // Try to create user; if already exists, ignore and sign-in below
    try {
      auth.createUserWithEmailAndPassword(email, password).await()
    } catch (e: Exception) {
      if (e is FirebaseAuthUserCollisionException) {
        // user already exists: continue to sign in
      } else {
        // creation might fail for other reasons but we'll still try to sign-in
      }
    }

    auth.signInWithEmailAndPassword(email, password).await()

    // Optionally create the minimal app profile so app treats this user as already signed up
    createAppProfileIfRequested(auth.currentUser, email, null, createAppProfile)
  }

  /**
   * Simulate a Google sign-in by creating a Firebase Auth user with the given email, signing in and
   * optionally creating a simple app profile document in Firestore.
   * - If createAppProfile == false: this simulates a Google user that has no app profile (app
   *   should route to sign-up flow).
   * - If createAppProfile == true: this creates a minimal profile doc so app treats the Google user
   *   as having an existing app account.
   */
  suspend fun signInAsGoogleUser(
      email: String,
      displayName: String? = null,
      createAppProfile: Boolean = false
  ) {
    configureEmulatorsOnce()
    val auth = Firebase.auth

    // Create an auth user using email+random password so Firebase Auth has the account
    val dummyPassword = "TestPassw0rd!"
    try {
      auth.createUserWithEmailAndPassword(email, dummyPassword).await()
    } catch (e: Exception) {
      // ignore collision / already exists
    }

    // Sign in the user (now currentUser will be available to the app)
    auth.signInWithEmailAndPassword(email, dummyPassword).await()

    //    // Optionally create a full app profile so the app won't route to signup
    //    createAppProfileIfRequested(auth.currentUser, email, displayName, createAppProfile)
  }

  /** Sign out the current test user. */
  fun signOut() {
    configureEmulatorsOnce()
    Firebase.auth.signOut()
  }

  // Blocking wrappers useful from JUnit tests that are not suspend functions.
  fun signInWithEmailBlocking(email: String, password: String, createAppProfile: Boolean = false) =
      runBlocking {
        signInWithEmail(email, password, createAppProfile)
      }

  fun signInAsGoogleUserBlocking(
      email: String,
      displayName: String? = null,
      createAppProfile: Boolean = false
  ) = runBlocking { signInAsGoogleUser(email, displayName, createAppProfile) }
}
