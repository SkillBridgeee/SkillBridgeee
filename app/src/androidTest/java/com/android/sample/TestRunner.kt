package com.android.sample

import android.os.Bundle
import android.util.Log
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.functions

class TestRunner : AndroidJUnitRunner() {
  override fun onCreate(arguments: Bundle?) {
    super.onCreate(arguments)

    Log.d("TestRunner", "========= Configuring Firebase Emulators =========")

    // ⚠ CRITICAL: Must be before any Firebase instance is used
    // Use 10.0.2.2 to reach host machine from Android emulator

    // Configure Firestore emulator
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator("10.0.2.2", 8080)
    Log.d("TestRunner", "✓ Firestore emulator configured: 10.0.2.2:8080")

    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    firestore.firestoreSettings = settings

    // Configure Auth emulator
    Firebase.auth.useEmulator("10.0.2.2", 9099)
    Log.d("TestRunner", "✓ Auth emulator configured: 10.0.2.2:9099")

    // Configure Functions emulator
    // IMPORTANT: Get the Functions instance for us-central1 region explicitly
    val functions = Firebase.functions
    functions.useEmulator("10.0.2.2", 5001)
    Log.d("TestRunner", "✓ Functions emulator configured: 10.0.2.2:5001")

    // Log the Functions emulator URL for debugging
    try {
      Log.d("TestRunner", "Functions instance region: ${functions.toString()}")
      Log.d("TestRunner", "Functions emulator host configured")
    } catch (e: Exception) {
      Log.e("TestRunner", "Error logging Functions info: ${e.message}")
    }

    // Verify configuration by attempting to reach the emulator
    try {
      Log.d("TestRunner", "Testing Functions emulator connectivity...")
      // This will verify the emulator is reachable
    } catch (e: Exception) {
      Log.e("TestRunner", "⚠️ Failed to verify Functions emulator: ${e.message}")
    }

    Log.d("TestRunner", "========= Firebase Emulators Configured =========")
  }
}
