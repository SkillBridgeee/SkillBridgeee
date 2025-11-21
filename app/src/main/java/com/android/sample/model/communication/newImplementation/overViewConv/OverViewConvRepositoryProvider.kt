package com.android.sample.model.communication.newImplementation.overViewConv

import android.content.Context
import com.android.sample.model.RepositoryProvider
import com.google.firebase.FirebaseApp

object OverViewConvRepositoryProvider : RepositoryProvider<OverViewConvRepository>() {
  override fun init(context: Context, useEmulator: Boolean) {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    _repository = FirestoreOverViewConvRepository()
  }
}
