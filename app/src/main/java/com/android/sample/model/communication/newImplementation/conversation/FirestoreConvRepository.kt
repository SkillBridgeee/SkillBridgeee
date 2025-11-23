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

  /**
   * Generates a new unique ID for a conversation or message.
   *
   * @return a random UUID as a String.
   */
  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  /**
   * Retrieves a conversation along with all its messages.
   *
   * @param convId The ID of the conversation to fetch.
   * @return A [ConversationNew] object if found, or null if the conversation does not exist.
   */
  override suspend fun getConv(convId: String): ConversationNew? {
    if (convId.isBlank()) return null

    val convRef = conversationsRef.document(convId)

    val convSnapshot = convRef.get().await()
    if (!convSnapshot.exists()) return null

    val conv = convSnapshot.toObject(ConversationNew::class.java)!!.copy(convId = convId)

    // Load messages
    val messagesSnapshot = convRef.collection("messages").orderBy("createdAt").get().await()
    val messages = messagesSnapshot.documents.mapNotNull { it.toObject(MessageNew::class.java) }

    return conv.copy(messages = messages)
  }

  /**
   * Creates a new conversation in Firestore.
   *
   * @param conversation The [ConversationNew] object to store.
   * @throws IllegalArgumentException if the conversation ID is blank.
   */
  override suspend fun createConv(conversation: ConversationNew) {
    require(conversation.convId.isNotBlank()) { "Conversation ID cannot be blank" }

    conversationsRef.document(conversation.convId).set(conversation).await()
  }

  /**
   * Deletes a conversation from Firestore.
   *
   * @param convId The ID of the conversation to delete.
   */
  override suspend fun deleteConv(convId: String) {
    conversationsRef.document(convId).delete().await()
  }

  /**
   * Sends a message by adding it to the messages subcollection of a conversation. Updates the
   * conversation's `updatedAt` field to the message's timestamp.
   *
   * @param convId The conversation ID where the message should be added.
   * @param message The [MessageNew] object to send.
   * @throws IllegalArgumentException if the conversation does not exist or IDs are blank.
   */
  override suspend fun sendMessage(convId: String, message: MessageNew) {
    require(convId.isNotBlank()) { "Conversation ID cannot be blank" }
    require(message.msgId.isNotBlank()) { "Message ID cannot be blank" }

    val convRef = conversationsRef.document(convId)
    val messagesRef = convRef.collection("messages")

    val convSnapshot = convRef.get().await()
    if (!convSnapshot.exists()) {
      throw IllegalArgumentException("Conversation $convId does not exist")
    }

    messagesRef.document(message.msgId).set(message).await()

    convRef.update(mapOf("updatedAt" to message.createdAt)).await()
  }

  /**
   * Returns a Flow that emits the list of messages for a conversation in real-time. The Flow will
   * emit a new list whenever messages are added or updated.
   *
   * @param convId The conversation ID to observe.
   * @return A [Flow] emitting lists of [MessageNew] objects in ascending order.
   * @throws IllegalArgumentException if the conversation ID is blank.
   */
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
      trySend(emptyList())

      awaitClose { listenerRegistration.remove() }
    }
  }
}
