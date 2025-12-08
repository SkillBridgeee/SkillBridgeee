package com.android.sample

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.android.sample.e2e.TestConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.functions

class TestRunner : AndroidJUnitRunner() {

  override fun onCreate(arguments: Bundle?) {
    super.onCreate(arguments)

    // Configure Firestore emulator
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator(TestConfig.EMULATOR_HOST, TestConfig.FIRESTORE_PORT)

    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    firestore.firestoreSettings = settings

    // Configure Auth emulator
    Firebase.auth.useEmulator(TestConfig.EMULATOR_HOST, TestConfig.AUTH_PORT)

    // Configure Functions emulator
    val functions = Firebase.functions
    functions.useEmulator(TestConfig.EMULATOR_HOST, TestConfig.FUNCTIONS_PORT)
  }
}
