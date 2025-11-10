package com.android.sample.testutils

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestAuthHelpersTest {

  companion object {
    private const val TAG = "TestAuthHelpersTest"
  }

  @Before
  fun setUpEmulators() {
    // Ensure emulators configured for tests (safe to call repeatedly).
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (e: IllegalStateException) {
      // already configured
    } catch (e: Exception) {
      Log.w(TAG, "Failed to configure emulators in setUp", e)
    }

    // Ensure a clean auth state
    try {
      Firebase.auth.signOut()
    } catch (_: Exception) {}
  }

  @After
  fun tearDown() = runBlocking {
    // Try to clean any remaining authenticated user after each test.
    try {
      Firebase.auth.currentUser?.delete()?.await()
    } catch (e: Exception) {
      // deletion may fail if user is already removed or requires reauth on prod; ignore for
      // emulator.
      Log.w(TAG, "Could not delete test user in tearDown", e)
    }
    try {
      Firebase.auth.signOut()
    } catch (_: Exception) {}
  }

  @Test
  fun signInWithEmail_creates_user_and_profile_when_requested() = runBlocking {
    val email = "test.email1+${System.currentTimeMillis()}@example.com"
    val password = "P@ssw0rd!"
    // createAppProfile = true should create a Firestore users/{uid} document
    TestAuthHelpers.signInWithEmail(email, password, createAppProfile = true)

    val user = Firebase.auth.currentUser
    assertNotNull("User should be signed in after signInWithEmail", user)

    // Verify Firestore doc exists and contains the email
    val uid = user!!.uid
    val doc = Firebase.firestore.collection("users").document(uid).get().await()
    assertTrue("User profile document should exist when createAppProfile=true", doc.exists())
    assertEquals("Email in profile should match", email, doc.getString("email"))

    // Cleanup: remove doc then delete user
    try {
      Firebase.firestore.collection("users").document(uid).delete().await()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to delete profile doc in test cleanup", e)
    }
    try {
      Firebase.auth.currentUser?.delete()?.await()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to delete auth user in test cleanup", e)
    }
    Firebase.auth.signOut()
  }

  @Test
  fun signInAsGoogleUser_does_not_create_profile_by_default() = runBlocking {
    val email = "test.google1+${System.currentTimeMillis()}@example.com"
    // createAppProfile = false => no Firestore profile should be created
    TestAuthHelpers.signInAsGoogleUser(email, displayName = "Google Test", createAppProfile = false)

    val user = Firebase.auth.currentUser
    assertNotNull("User should be signed in after signInAsGoogleUser", user)

    val uid = user!!.uid
    val doc = Firebase.firestore.collection("users").document(uid).get().await()
    assertFalse("User profile document should NOT exist when createAppProfile=false", doc.exists())

    // Cleanup: ensure any doc removed and delete user
    try {
      if (doc.exists()) {
        Firebase.firestore.collection("users").document(uid).delete().await()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to delete profile doc in test cleanup", e)
    }
    try {
      Firebase.auth.currentUser?.delete()?.await()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to delete auth user in test cleanup", e)
    }
    Firebase.auth.signOut()
  }

  @Test
  fun signOut_clears_current_user() = runBlocking {
    val email = "test.signout+${System.currentTimeMillis()}@example.com"
    val password = "SignOut1!"
    TestAuthHelpers.signInWithEmail(email, password, createAppProfile = false)

    assertNotNull("Precondition: user should be signed in", Firebase.auth.currentUser)
    TestAuthHelpers.signOut()

    // After signOut, currentUser should be null
    val current = Firebase.auth.currentUser
    assertNull("Current user should be null after signOut", current)

    // Cleanup: ensure no leftover user (signOut should have cleared)
    try {
      Firebase.auth.signOut()
    } catch (_: Exception) {}
  }
}
