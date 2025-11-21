package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreConvRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ConvRepository {

  override suspend fun getConv(convId: String): Conversation? {
    TODO("Not yet implemented")
  }

  override suspend fun createConv(convRepository: ConvRepository) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteConv(convId: String) {
    TODO("Not yet implemented")
  }
}
