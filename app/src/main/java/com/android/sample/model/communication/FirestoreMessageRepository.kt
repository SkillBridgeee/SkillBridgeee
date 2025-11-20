package com.android.sample.model.communication

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.tasks.await

const val CONVERSATIONS_COLLECTION_PATH = "conversations"
const val MESSAGES_COLLECTION_PATH = "messages"

class FirestoreMessageRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : MessageRepository {

  private companion object {
    private const val MESSAGE_MAX_LENGTH = 5000 // Max message content length
    private const val ERROR_CONVERSATION_ID_BLANK = "Conversation ID cannot be blank"
    private const val ERROR_MESSAGE_ID_BLANK = "Message ID cannot be blank"
    private const val ERROR_NOT_PARTICIPANT =
        "Access denied: You are not a participant in this conversation."
  }

  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  // ========== Message Operations ==========

  override suspend fun getMessagesInConversation(conversationId: String): List<Message> {
    return try {
      require(conversationId.isNotBlank()) { ERROR_CONVERSATION_ID_BLANK }

      val snapshot =
          db.collection(MESSAGES_COLLECTION_PATH)
              .whereEqualTo("conversationId", conversationId)
              .orderBy("sentTime", Query.Direction.ASCENDING)
              .get()
              .await()

      snapshot.toObjects(Message::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to get messages for conversation $conversationId: ${e.message}")
    }
  }

  override suspend fun getMessage(messageId: String): Message? {
    return try {
      require(messageId.isNotBlank()) { ERROR_MESSAGE_ID_BLANK }

      val document = db.collection(MESSAGES_COLLECTION_PATH).document(messageId).get().await()

      if (!document.exists()) {
        return null
      }

      document.toObject(Message::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to get message $messageId: ${e.message}")
    }
  }

  override suspend fun sendMessage(message: Message): String {
    return try {
      // Validate that the current user is the sender
      require(message.sentFrom == currentUserId) {
        "Access denied: You can only send messages from your own account."
      }

      // Validate message
      validateMessage(message)

      // Generate message ID if not provided
      val messageId = message.messageId.ifBlank { getNewUid() }
      val messageToSend =
          message.copy(messageId = messageId, sentTime = message.sentTime ?: Timestamp.now())

      // Save message to Firestore
      db.collection(MESSAGES_COLLECTION_PATH).document(messageId).set(messageToSend).await()

      // Update conversation
      updateConversationAfterMessage(messageToSend)

      messageId
    } catch (e: Exception) {
      throw Exception("Failed to send message: ${e.message}")
    }
  }

  override suspend fun markMessageAsRead(messageId: String, readTime: Timestamp) {
    try {
      require(messageId.isNotBlank()) { ERROR_MESSAGE_ID_BLANK }

      val message = getMessage(messageId) ?: throw Exception("Message not found")

      // Only the receiver can mark a message as read
      require(message.sentTo == currentUserId) {
        "Access denied: Only the receiver can mark a message as read."
      }

      val updates = mapOf("readTime" to readTime, "isRead" to true, "receiveTime" to (readTime))

      db.collection(MESSAGES_COLLECTION_PATH).document(messageId).update(updates).await()
    } catch (e: Exception) {
      throw Exception("Failed to mark message as read: ${e.message}")
    }
  }

  override suspend fun deleteMessage(messageId: String) {
    try {
      require(messageId.isNotBlank()) { ERROR_MESSAGE_ID_BLANK }

      val message = getMessage(messageId) ?: throw Exception("Message not found")

      // Only the sender can delete a message
      require(message.sentFrom == currentUserId) {
        "Access denied: Only the sender can delete a message."
      }

      db.collection(MESSAGES_COLLECTION_PATH).document(messageId).delete().await()
    } catch (e: Exception) {
      throw Exception("Failed to delete message $messageId: ${e.message}")
    }
  }

  override suspend fun getUnreadMessagesInConversation(
      conversationId: String,
      userId: String
  ): List<Message> {
    return try {
      require(conversationId.isNotBlank()) { ERROR_CONVERSATION_ID_BLANK }
      require(userId == currentUserId) {
        "Access denied: You can only get your own unread messages."
      }

      val snapshot =
          db.collection(MESSAGES_COLLECTION_PATH)
              .whereEqualTo("conversationId", conversationId)
              .whereEqualTo("sentTo", userId)
              .whereEqualTo("isRead", false)
              .orderBy("sentTime", Query.Direction.ASCENDING)
              .get()
              .await()

      snapshot.toObjects(Message::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to get unread messages: ${e.message}")
    }
  }

  // ========== Conversation Operations ==========

  override suspend fun getConversationsForUser(userId: String): List<Conversation> {
    return try {
      require(userId == currentUserId) { "Access denied: You can only get your own conversations." }

      // Get conversations where user is participant1
      val snapshot1 =
          db.collection(CONVERSATIONS_COLLECTION_PATH)
              .whereEqualTo("participant1Id", userId)
              .get()
              .await()

      // Get conversations where user is participant2
      val snapshot2 =
          db.collection(CONVERSATIONS_COLLECTION_PATH)
              .whereEqualTo("participant2Id", userId)
              .get()
              .await()

      val conversations1 = snapshot1.toObjects(Conversation::class.java)
      val conversations2 = snapshot2.toObjects(Conversation::class.java)

      // Combine and sort by last message time
      (conversations1 + conversations2).sortedByDescending { it.lastMessageTime?.seconds ?: 0 }
    } catch (e: Exception) {
      throw Exception("Failed to get conversations for user $userId: ${e.message}")
    }
  }

  override suspend fun getConversation(conversationId: String): Conversation? {
    return try {
      require(conversationId.isNotBlank()) { ERROR_CONVERSATION_ID_BLANK }

      val document =
          db.collection(CONVERSATIONS_COLLECTION_PATH).document(conversationId).get().await()

      if (!document.exists()) {
        return null
      }

      val conversation = document.toObject(Conversation::class.java)

      // Verify current user is a participant
      conversation?.let { require(it.isParticipant(currentUserId)) { ERROR_NOT_PARTICIPANT } }

      conversation
    } catch (e: Exception) {
      throw Exception("Failed to get conversation $conversationId: ${e.message}")
    }
  }

  override suspend fun getOrCreateConversation(userId1: String, userId2: String): Conversation {
    return try {
      require(userId1.isNotBlank()) { "User 1 ID cannot be blank" }
      require(userId2.isNotBlank()) { "User 2 ID cannot be blank" }
      require(userId1 != userId2) { "Cannot create conversation with yourself" }
      require(userId1 == currentUserId || userId2 == currentUserId) {
        "Access denied: You must be one of the participants."
      }

      // Generate consistent conversation ID
      val conversationId = Conversation.generateConversationId(userId1, userId2)

      // Check if conversation already exists
      val existingConversation = getConversation(conversationId)
      if (existingConversation != null) {
        return existingConversation
      }

      // Create new conversation
      val sortedIds = listOf(userId1, userId2).sorted()
      val newConversation =
          Conversation(
              conversationId = conversationId,
              participant1Id = sortedIds[0],
              participant2Id = sortedIds[1],
              lastMessageContent = "",
              lastMessageTime = Timestamp.now(),
              lastMessageSenderId = "",
              unreadCountUser1 = 0,
              unreadCountUser2 = 0,
              createdAt = Timestamp.now(),
              updatedAt = Timestamp.now())

      db.collection(CONVERSATIONS_COLLECTION_PATH)
          .document(conversationId)
          .set(newConversation)
          .await()

      newConversation
    } catch (e: Exception) {
      throw Exception("Failed to get or create conversation: ${e.message}")
    }
  }

  override suspend fun updateConversation(conversation: Conversation) {
    try {
      require(conversation.isParticipant(currentUserId)) { ERROR_NOT_PARTICIPANT }

      conversation.validate()

      db.collection(CONVERSATIONS_COLLECTION_PATH)
          .document(conversation.conversationId)
          .set(conversation)
          .await()
    } catch (e: Exception) {
      throw Exception("Failed to update conversation: ${e.message}")
    }
  }

  override suspend fun markConversationAsRead(conversationId: String, userId: String) {
    try {
      require(conversationId.isNotBlank()) { ERROR_CONVERSATION_ID_BLANK }
      require(userId == currentUserId) {
        "Access denied: You can only mark your own messages as read."
      }

      val conversation =
          getConversation(conversationId) ?: throw Exception("Conversation not found")

      require(conversation.isParticipant(userId)) { ERROR_NOT_PARTICIPANT }

      // Update unread count for the user
      val updates =
          when (userId) {
            conversation.participant1Id -> mapOf("unreadCountUser1" to 0)
            conversation.participant2Id -> mapOf("unreadCountUser2" to 0)
            else -> error("User is not a participant")
          }

      db.collection(CONVERSATIONS_COLLECTION_PATH).document(conversationId).update(updates).await()

      // Mark all unread messages as read
      val unreadMessages = getUnreadMessagesInConversation(conversationId, userId)
      val now = Timestamp.now()
      unreadMessages.forEach { message -> markMessageAsRead(message.messageId, now) }
    } catch (e: Exception) {
      throw Exception("Failed to mark conversation as read: ${e.message}")
    }
  }

  override suspend fun deleteConversation(conversationId: String) {
    try {
      require(conversationId.isNotBlank()) { ERROR_CONVERSATION_ID_BLANK }

      val conversation =
          getConversation(conversationId) ?: throw Exception("Conversation not found")

      require(conversation.isParticipant(currentUserId)) { ERROR_NOT_PARTICIPANT }

      // Delete all messages in the conversation
      val messages = getMessagesInConversation(conversationId)
      messages.forEach { message ->
        db.collection(MESSAGES_COLLECTION_PATH).document(message.messageId).delete().await()
      }

      // Delete the conversation
      db.collection(CONVERSATIONS_COLLECTION_PATH).document(conversationId).delete().await()
    } catch (e: Exception) {
      throw Exception("Failed to delete conversation $conversationId: ${e.message}")
    }
  }

  // ========== Private Helper Methods ==========

  private fun validateMessage(message: Message) {
    message.validate()
    require(message.content.length <= MESSAGE_MAX_LENGTH) {
      "Message content exceeds maximum length of $MESSAGE_MAX_LENGTH characters"
    }
  }

  /**
   * Updates the conversation metadata after a message is sent This includes updating the last
   * message content, time, and incrementing unread count for the receiver
   */
  private suspend fun updateConversationAfterMessage(message: Message) {
    try {
      var conversation = getConversation(message.conversationId)

      if (conversation == null) {
        // Create conversation if it doesn't exist
        conversation = getOrCreateConversation(message.sentFrom, message.sentTo)
      }

      val updates =
          mutableMapOf<String, Any>(
              "lastMessageContent" to message.content,
              "lastMessageTime" to (message.sentTime ?: Timestamp.now()),
              "lastMessageSenderId" to message.sentFrom,
              "updatedAt" to Timestamp.now())

      when (message.sentTo) {
        conversation.participant1Id ->
            updates["unreadCountUser1"] = conversation.unreadCountUser1 + 1
        conversation.participant2Id ->
            updates["unreadCountUser2"] = conversation.unreadCountUser2 + 1
      }

      db.collection(CONVERSATIONS_COLLECTION_PATH)
          .document(message.conversationId)
          .update(updates)
          .await()
    } catch (e: Exception) {
      // Log error but don't fail the message send
      println("Warning: Failed to update conversation after message: ${e.message}")
    }
  }
}
