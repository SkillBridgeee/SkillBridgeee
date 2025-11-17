package com.android.sample.model.communication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object MessageRepositoryProvider {
  private var repository: MessageRepository? = null

  fun getRepository(): MessageRepository {
    return repository
        ?: FirestoreMessageRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
            .also { repository = it }
  }

  fun setRepository(repo: MessageRepository) {
    repository = repo
  }

  fun reset() {
    repository = null
  }
}
