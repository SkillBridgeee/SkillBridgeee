package com.android.sample.model.communication.newImplementation.overViewConv

import android.content.Context
import com.android.sample.model.RepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore

object OverViewConvRepositoryProvider : RepositoryProvider<OverViewConvRepository>() {
  override fun init(context: Context, useEmulator: Boolean) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreOverViewConvRepository(db = Firebase.firestore)
  }
}
