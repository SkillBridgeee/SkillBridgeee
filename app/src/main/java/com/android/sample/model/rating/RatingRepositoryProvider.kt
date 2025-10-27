package com.android.sample.model.rating

import android.content.Context
import com.android.sample.model.RepositoryProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object RatingRepositoryProvider : RepositoryProvider<RatingRepository>() {
  override fun init(context: Context, useEmulator: Boolean) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreRatingRepository(Firebase.firestore)
  }
}
