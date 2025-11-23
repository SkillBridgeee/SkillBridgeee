package com.android.sample.model.communication.newImplementation.overViewConv

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

const val OVERVIEW_CONV_COLLECTION_PATH = "overViewConv"

class FirestoreOverViewConvRepository(
    db: FirebaseFirestore,
) : OverViewConvRepository {

  private val overViewRef = db.collection(OVERVIEW_CONV_COLLECTION_PATH)

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun addOverViewConvUser(overView: OverViewConversation) {
    val docId = overView.overViewId.ifBlank { getNewUid() }
    overViewRef.document(docId).set(overView).await()
  }

  override suspend fun deleteOverViewConvUser(convId: String) {
    val querySnapshot = overViewRef.whereEqualTo("linkedConvId", convId).get().await()

    for (doc in querySnapshot.documents) {
      overViewRef.document(doc.id).delete().await()
    }
  }

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    require(userId.isNotBlank()) { "User ID cannot be blank" }

    val snapshotCreator = overViewRef.whereEqualTo("convCreatorId", userId).get().await()

    val snapshotOther = overViewRef.whereEqualTo("otherPersonId", userId).get().await()

    val allOverviews =
        snapshotCreator.toObjects(OverViewConversation::class.java) +
            snapshotOther.toObjects(OverViewConversation::class.java)

    return allOverviews.sortedByDescending { it.lastMsg.createdAt.time }
  }

  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> = callbackFlow {
    require(userId.isNotBlank()) { close(IllegalArgumentException("User ID cannot be blank")) }

    val listenerCreator: ListenerRegistration =
        overViewRef
            .whereEqualTo("convCreatorId", userId)
            .orderBy("lastMsg.createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }

              val messages = snapshot?.toObjects(OverViewConversation::class.java).orEmpty()
              trySend(messages)
            }

    val listenerOther: ListenerRegistration =
        overViewRef
            .whereEqualTo("otherPersonId", userId)
            .orderBy("lastMsg.createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }

              val messages = snapshot?.toObjects(OverViewConversation::class.java).orEmpty()
              trySend(messages)
            }

    // Clean up
    awaitClose {
      listenerCreator.remove()
      listenerOther.remove()
    }
  }
}
