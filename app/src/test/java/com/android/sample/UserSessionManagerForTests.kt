package com.android.sample

import com.google.firebase.auth.FirebaseAuth

/**
 * Test-only stub of UserSessionManager.
 *
 * This completely replaces the production version on the test classpath,
 * so unit tests never touch FirebaseAuth / FirebaseApp.
 */
object UserSessionManagerForTests {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: IllegalStateException) {
            null  // no Firebase in unit tests
        }
    }

    @Volatile
    private var currentUserId: String? = auth?.currentUser?.uid

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            currentUserId = firebaseAuth.currentUser?.uid
        }
    }

    fun getCurrentUserId(): String? = currentUserId
}
