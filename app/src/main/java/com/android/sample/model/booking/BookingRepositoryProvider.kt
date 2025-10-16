// kotlin
package com.android.sample.model.booking

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object BookingRepositoryProvider {
  @Volatile private var _repository: BookingRepository? = null

  val repository: BookingRepository
    get() =
        _repository
            ?: error(
                "BookingRepositoryProvider not initialized. Call init(...) first or setForTests(...) in tests.")

  fun init(context: Context, useEmulator: Boolean = false) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreBookingRepository(Firebase.firestore)
  }

  fun setForTests(repository: BookingRepository) {
    _repository = repository
  }
}
