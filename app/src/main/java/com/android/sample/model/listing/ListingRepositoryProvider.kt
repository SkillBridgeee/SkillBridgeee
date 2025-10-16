package com.android.sample.model.listing

import android.content.Context
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.FirestoreBookingRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object ListingRepositoryProvider {

  @Volatile private var _repository: ListingRepository? = null

  val repository: ListingRepository
    get() =
      _repository
        ?: error(
          "ListingRepository not initialized. Call init(...) first or setForTests(...) in tests.")

  fun init(context: Context, useEmulator: Boolean = false) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreListingRepository(Firebase.firestore)
  }

  fun setForTests(repository: FirestoreListingRepository) {
    _repository = repository
  }
}
