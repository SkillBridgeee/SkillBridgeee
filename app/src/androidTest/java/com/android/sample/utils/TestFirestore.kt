package com.android.sample.utils

import com.google.firebase.firestore.FirebaseFirestore

object TestFirestore {
  val db: FirebaseFirestore by lazy {
    FirebaseFirestore.getInstance() // simple singleton, déjà configuré par TestRunner
  }
}
