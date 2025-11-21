package com.android.sample.model.communication.newImplementation.conversation

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

const val CONVERSATIONS_COLLECTION_PATH = "conversations"

class FirestoreConvRepository(
    db: FirebaseFirestore,
) : ConvRepository {

  private val conversationsRef = db.collection(CONVERSATIONS_COLLECTION_PATH)

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  // -------- GET A SINGLE CONVERSATION ------------
  override suspend fun getConv(convId: String): ConversationNew? {
    if (convId.isBlank()) return null

    val snapshot = conversationsRef.document(convId).get().await()
    return snapshot.toObject(ConversationNew::class.java)
  }

  // -------- CREATE A CONVERSATION ------------
  override suspend fun createConv(conversation: ConversationNew) {
    require(conversation.convId.isNotBlank()) { "Conversation ID cannot be blank" }

    conversationsRef.document(conversation.convId).set(conversation).await()
  }

  // -------- DELETE A CONVERSATION ------------
  override suspend fun deleteConv(convId: String) {
    conversationsRef.document(convId).delete().await()
  }

  // -------- LISTEN TO MESSAGES IN REAL TIME ------------
  override fun listenMessages(convId: String): Flow<List<MessageNew>> {
    return callbackFlow {
      if (convId.isBlank()) {
        close(IllegalArgumentException("Conversation ID cannot be blank"))
        return@callbackFlow
      }

      val messagesRef =
          conversationsRef
              .document(convId)
              .collection("messages")
              .orderBy("createdAt", Query.Direction.ASCENDING)

      val listenerRegistration =
          messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
              close(error)
              return@addSnapshotListener
            }

            val messages = snapshot?.toObjects(MessageNew::class.java).orEmpty()
            trySend(messages)
          }

      awaitClose { listenerRegistration.remove() }
    }
  }
}
