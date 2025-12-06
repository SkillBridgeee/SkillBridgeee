package com.android.sample.model.authentication

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

/**
 * A JUnit rule that initializes Firebase for testing. This rule ensures that Firebase is properly
 * initialized before each test and cleaned up afterwards.
 */
class FirebaseTestRule : TestRule {

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        initializeFirebase()
        try {
          base.evaluate()
        } finally {
          cleanupFirebase()
        }
      }
    }
  }

  private fun initializeFirebase() {
    try {
      // Check if Firebase is already initialized
      FirebaseApp.getInstance()
    } catch (_: IllegalStateException) {
      // Firebase is not initialized, so initialize it
      val options =
          FirebaseOptions.Builder()
              .setApplicationId("test-app-id")
              .setApiKey("test-api-key")
              .setProjectId("test-project-id")
              .build()

      FirebaseApp.initializeApp(RuntimeEnvironment.getApplication(), options)
    }
  }

  private fun cleanupFirebase() {
    try {
      // Use clearInstancesForTest() instead of delete() to avoid hanging in CI
      // This properly cleans up Firebase for testing without blocking
      FirebaseApp.clearInstancesForTest()
    } catch (_: Exception) {
      // Ignore cleanup errors in tests
      // Some test environments may not support clearInstancesForTest()
    }
  }
}
