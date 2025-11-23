package com.android.sample

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class TestRunner : AndroidJUnitRunner() {
  override fun onCreate(arguments: Bundle?) {
    super.onCreate(arguments)

    // ⚠ Must be before any Firestore instance is used
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator("10.0.2.2", 8080)

    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    firestore.firestoreSettings = settings
  }
}
