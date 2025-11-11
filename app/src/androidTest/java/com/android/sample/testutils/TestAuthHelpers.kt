package com.android.sample.testutils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

object TestAuthHelpers {
  private val emulatorConfigured = AtomicBoolean(false)

  private fun configureEmulatorsOnce() {
    if (emulatorConfigured.compareAndSet(false, true)) {
      try {
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: Exception) {}
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
      } catch (_: Exception) {}
    }
  }

  suspend fun signInWithEmail(email: String, password: String) {
    configureEmulatorsOnce()
    val auth: FirebaseAuth = Firebase.auth

    try {
      auth.createUserWithEmailAndPassword(email, password).await()
    } catch (e: Exception) {
      if (e is FirebaseAuthUserCollisionException) {
        // user already exists: continue to sign in
      }
    }

    auth.signInWithEmailAndPassword(email, password).await()
  }

  suspend fun signInAsGoogleUser(email: String, displayName: String? = null) {
    configureEmulatorsOnce()
    val auth = Firebase.auth

    val dummyPassword = "TestPassw0rd!"
    try {
      auth.createUserWithEmailAndPassword(email, dummyPassword).await()
    } catch (_: Exception) {}

    auth.signInWithEmailAndPassword(email, dummyPassword).await()
  }

  fun signOut() {
    configureEmulatorsOnce()
    Firebase.auth.signOut()
  }

  fun signInWithEmailBlocking(email: String, password: String) = runBlocking {
    signInWithEmail(email, password)
  }

  fun signInAsGoogleUserBlocking(email: String, displayName: String? = null) = runBlocking {
    signInAsGoogleUser(email, displayName)
  }
}
