package com.android.sample.model.communication

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.tasks.await

const val MESSAGES_COLLECTION_PATH = "messages"

class MessageRepositoryFirestore(private val db: FirebaseFirestore) : MessageRepository {

  override fun getNewUid(): String {
    return db.collection(MESSAGES_COLLECTION_PATH).document().id
  }

  override suspend fun getAllMessages(): List<Message> {
    val snapshot = db.collection(MESSAGES_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToMessage(it) }
  }

  override suspend fun getMessage(messageId: String): Message {
    val document = db.collection(MESSAGES_COLLECTION_PATH).document(messageId).get().await()
    return documentToMessage(document)
        ?: throw Exception("MessageRepositoryFirestore: Message not found")
  }

  override suspend fun getMessagesBetweenUsers(userId1: String, userId2: String): List<Message> {
    val sentMessages =
        db.collection(MESSAGES_COLLECTION_PATH)
            .whereEqualTo("sentFrom", userId1)
            .whereEqualTo("sentTo", userId2)
            .get()
            .await()

    val receivedMessages =
        db.collection(MESSAGES_COLLECTION_PATH)
            .whereEqualTo("sentFrom", userId2)
            .whereEqualTo("sentTo", userId1)
            .get()
            .await()

    return (sentMessages.mapNotNull { documentToMessage(it) } +
            receivedMessages.mapNotNull { documentToMessage(it) })
        .sortedBy { it.sentTime }
  }

  override suspend fun getMessagesSentByUser(userId: String): List<Message> {
    val snapshot =
        db.collection(MESSAGES_COLLECTION_PATH).whereEqualTo("sentFrom", userId).get().await()
    return snapshot.mapNotNull { documentToMessage(it) }
  }

  override suspend fun getMessagesReceivedByUser(userId: String): List<Message> {
    val snapshot =
        db.collection(MESSAGES_COLLECTION_PATH).whereEqualTo("sentTo", userId).get().await()
    return snapshot.mapNotNull { documentToMessage(it) }
  }

  override suspend fun addMessage(message: Message) {
    val messageId = getNewUid()
    db.collection(MESSAGES_COLLECTION_PATH).document(messageId).set(message).await()
  }

  override suspend fun updateMessage(messageId: String, message: Message) {
    db.collection(MESSAGES_COLLECTION_PATH).document(messageId).set(message).await()
  }

  override suspend fun deleteMessage(messageId: String) {
    db.collection(MESSAGES_COLLECTION_PATH).document(messageId).delete().await()
  }

  override suspend fun markAsReceived(messageId: String, receiveTime: Date) {
    db.collection(MESSAGES_COLLECTION_PATH)
        .document(messageId)
        .update("receiveTime", receiveTime)
        .await()
  }

  override suspend fun markAsRead(messageId: String, readTime: Date) {
    db.collection(MESSAGES_COLLECTION_PATH).document(messageId).update("readTime", readTime).await()
  }

  override suspend fun getUnreadMessages(userId: String): List<Message> {
    val snapshot =
        db.collection(MESSAGES_COLLECTION_PATH)
            .whereEqualTo("sentTo", userId)
            .whereEqualTo("readTime", null)
            .get()
            .await()
    return snapshot.mapNotNull { documentToMessage(it) }
  }

  private fun documentToMessage(document: DocumentSnapshot): Message? {
    return try {
      val sentFrom = document.getString("sentFrom") ?: return null
      val sentTo = document.getString("sentTo") ?: return null
      val sentTime = document.getTimestamp("sentTime")?.toDate() ?: return null
      val receiveTime = document.getTimestamp("receiveTime")?.toDate()
      val readTime = document.getTimestamp("readTime")?.toDate()
      val message = document.getString("message") ?: return null

      Message(
          sentFrom = sentFrom,
          sentTo = sentTo,
          sentTime = sentTime,
          receiveTime = receiveTime,
          readTime = readTime,
          message = message)
    } catch (e: Exception) {
      Log.e("MessageRepositoryFirestore", "Error converting document to Message", e)
      null
    }
  }
}
