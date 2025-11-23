package com.android.sample.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object TestFirestore {

  // Singleton Firestore instance, lazy-initialized
  val db: FirebaseFirestore by lazy {
    Log.i("TestFirestore", "Getting Firestore instance for tests...")

    val instance = FirebaseFirestore.getInstance()

    // Disable persistence for deterministic tests
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    instance.firestoreSettings = settings

    Log.i("TestFirestore", "Firestore instance ready for tests.")

    instance
  }
}
