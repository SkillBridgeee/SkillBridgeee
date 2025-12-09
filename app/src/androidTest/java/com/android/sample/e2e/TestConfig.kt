package com.android.sample.e2e

/** Centralized configuration for E2E tests, particularly for Firebase emulator settings. */
object TestConfig {
  const val EMULATOR_HOST = "10.0.2.2"
  const val FIRESTORE_PORT = 8080
  const val AUTH_PORT = 9099
  const val FUNCTIONS_PORT = 5001
  const val FUNCTIONS_REGION = "us-central1"
}
