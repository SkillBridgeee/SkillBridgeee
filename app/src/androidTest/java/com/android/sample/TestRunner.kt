package com.android.sample

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.functions

class TestRunner : AndroidJUnitRunner() {
  override fun onCreate(arguments: Bundle?) {
    super.onCreate(arguments)

    // ⚠ Must be before any Firebase instance is used
    // Use 10.0.2.2 to reach host machine from Android emulator
    // Configure Firestore emulator
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator("10.0.2.2", 8080)

    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    firestore.firestoreSettings = settings

    // Configure Auth emulator
    Firebase.auth.useEmulator("10.0.2.2", 9099)

    // Configure Functions emulator
    Firebase.functions.useEmulator("10.0.2.2", 5001)
  }
}
