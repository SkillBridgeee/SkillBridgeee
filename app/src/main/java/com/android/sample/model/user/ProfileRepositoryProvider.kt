// kotlin
package com.android.sample.model.user

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object ProfileRepositoryProvider {
  @Volatile private var _repository: ProfileRepository? = null

  val repository: ProfileRepository
    get() =
        _repository
            ?: error("Profile not initialized. Call init(...) first or setForTests(...) in tests.")

  fun init(context: Context, useEmulator: Boolean = false) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreProfileRepository(Firebase.firestore)
  }

  fun setForTests(repository: ProfileRepository) {
    _repository = repository
  }
}
