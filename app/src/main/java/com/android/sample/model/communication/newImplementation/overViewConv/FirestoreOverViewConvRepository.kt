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

/**
 * Repository implementation for managing conversation overviews in Firestore. Each overview
 * represents a lightweight snapshot of a conversation for a specific user.
 */
class FirestoreOverViewConvRepository(
    db: FirebaseFirestore,
) : OverViewConvRepository {

  private val overViewRef = db.collection(OVERVIEW_CONV_COLLECTION_PATH)

  /**
   * Generates a new unique ID for an overview document.
   *
   * @return a random UUID string.
   */
  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  /**
   * Creates or updates a conversation overview for a user.
   *
   * @param overView the overview model to store.
   */
  override suspend fun addOverViewConvUser(overView: OverViewConversation) {
    require(overView.overViewId.isNotBlank()) { "OverView ID cannot be blank" }
    overViewRef.document(overView.overViewId).set(overView).await()
  }

  /**
   * Deletes all overview documents associated with a given conversation ID. Used when a
   * conversation is removed entirely.
   *
   * @param convId the linked conversation ID.
   */
  override suspend fun deleteOverViewConvUser(convId: String) {
    require(convId.isNotBlank()) { "Conv ID cannot be blank" }

    val querySnapshot = overViewRef.whereEqualTo("linkedConvId", convId).get().await()

    if (querySnapshot.isEmpty) return

    val batch = overViewRef.firestore.batch()

    for (doc in querySnapshot.documents) {
      batch.delete(doc.reference)
    }

    batch.commit().await()
  }

  /**
   * Retrieves all overview conversations owned by a specific user.
   *
   * @param userId The ID of the overview owner.
   * @return A sorted list of OverViewConversation, ordered by last message timestamp.
   */
  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    require(userId.isNotBlank()) { "User ID cannot be blank" }

    // Only fetch overview documents where the user is the OWNER
    val snapshot = overViewRef.whereEqualTo("overViewOwnerId", userId).get().await()

    val overviews = snapshot.toObjects(OverViewConversation::class.java)

    return overviews.sortedByDescending { it.lastMsg.createdAt.time }
  }

  /**
   * Observes all overview conversations for a specific user in real-time. Emits a list of overviews
   * whenever a change occurs in Firestore.
   *
   * Two listeners are registered since the user may be the conversation creator or the other
   * participant.
   *
   * @param userId the user's identifier.
   * @return a Flow emitting updated lists of OverViewConversation.
   */
  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> = callbackFlow {
    require(userId.isNotBlank()) { close(IllegalArgumentException("User ID cannot be blank")) }

    val listenerCreator: ListenerRegistration =
        overViewRef
            .whereEqualTo("overViewOwnerId", userId)
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

    // Remove listeners when the Flow is cancelled.
    awaitClose {
      listenerCreator.remove()
      listenerOther.remove()
    }
  }
}
