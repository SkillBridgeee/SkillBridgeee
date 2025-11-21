package com.android.sample.model.communication.newImplementation.overview

import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

const val OVERVIEW_CONV_COLLECTION_PATH = "overViewConv"

class FirestoreOverViewConvRepository(
    db: FirebaseFirestore,
) : OverViewConvRepository {

  private val overViewRef = db.collection(OVERVIEW_CONV_COLLECTION_PATH)

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    require(userId.isNotBlank()) { "User ID cannot be blank" }

    val snapshotCreator = overViewRef.whereEqualTo("convCreatorId", userId).get().await()

    val snapshotOther = overViewRef.whereEqualTo("otherPersonId", userId).get().await()

    val allOverviews =
        snapshotCreator.toObjects(OverViewConversation::class.java) +
            snapshotOther.toObjects(OverViewConversation::class.java)

    return allOverviews.sortedByDescending { it.lastMsg.createdAt.time }
  }
}
