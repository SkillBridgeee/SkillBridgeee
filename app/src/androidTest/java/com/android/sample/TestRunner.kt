package com.android.sample

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.firestore.FirebaseFirestore

class TestRunner : AndroidJUnitRunner() {
  override fun onCreate(arguments: Bundle?) {
    // Must be called before any Firestore usage
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator("10.0.2.2", 8080)

    super.onCreate(arguments)
  }
}
