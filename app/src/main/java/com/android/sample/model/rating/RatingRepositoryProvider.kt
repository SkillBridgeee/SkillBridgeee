// kotlin
package com.android.sample.model.rating

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object RatingRepositoryProvider {
  @Volatile private var _repository: RatingRepository? = null

  val repository: RatingRepository
    get() =
        _repository
            ?: error(
                "RatingRepository not initialized. Call init(...) first or setForTests(...) in tests.")

  fun init(context: Context, useEmulator: Boolean = false) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreRatingRepository(Firebase.firestore)
  }

  fun setForTests(repository: RatingRepository) {
    _repository = repository
  }
}
