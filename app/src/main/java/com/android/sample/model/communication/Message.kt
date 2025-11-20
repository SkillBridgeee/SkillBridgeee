package com.android.sample.model.communication

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/** Data class representing a message between users */
data class Message(
    @DocumentId var messageId: String = "", // Unique message ID (Firestore document ID)
    val conversationId: String = "", // ID of the conversation this message belongs to
    val sentFrom: String = "", // UID of the sender
    val sentTo: String = "", // UID of the receiver
    @ServerTimestamp var sentTime: Timestamp? = null, // Timestamp when message was sent
    val receiveTime: Timestamp? = null, // Timestamp when message was received
    val readTime: Timestamp? = null, // Timestamp when message was read for the first time
    val content: String = "", // The actual message content
    val isRead: Boolean = false // Flag to quickly check if message has been read
) {

  /** Validates the message data. Throws an [IllegalArgumentException] if the data is invalid. */
  fun validate() {
    require(sentFrom.isNotBlank()) { "Sender ID cannot be blank" }
    require(sentTo.isNotBlank()) { "Receiver ID cannot be blank" }
    require(sentFrom != sentTo) { "Sender and receiver cannot be the same user" }
    require(conversationId.isNotBlank()) { "Conversation ID cannot be blank" }
    require(content.isNotBlank()) { "Message content cannot be blank" }
  }
}
