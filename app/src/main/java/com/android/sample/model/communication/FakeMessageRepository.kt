package com.android.sample.model.communication

import com.google.firebase.Timestamp

/** Simple in-memory fake repository for tests and previews. */
class FakeMessageRepository(private val currentUserId: String = "test-user-1") : MessageRepository {
  private val messages = mutableMapOf<String, Message>()
  private val conversations = mutableMapOf<String, Conversation>()
  private var messageCounter = 0

  override fun getNewUid(): String =
      synchronized(this) {
        messageCounter += 1
        "msg$messageCounter"
      }

  // ========== Message Operations ==========

  override suspend fun getMessagesInConversation(conversationId: String): List<Message> {
    require(conversationId.isNotBlank()) { "Conversation ID cannot be blank" }

    return synchronized(this) {
      messages.values
          .filter { it.conversationId == conversationId }
          .sortedBy { it.sentTime?.seconds ?: 0 }
    }
  }

  override suspend fun getMessage(messageId: String): Message? {
    require(messageId.isNotBlank()) { "Message ID cannot be blank" }
    return synchronized(this) { messages[messageId] }
  }

  override suspend fun sendMessage(message: Message): String {
    require(message.sentFrom == currentUserId) {
      "Access denied: You can only send messages from your own account."
    }

    message.validate()

    val messageId = message.messageId.ifBlank { getNewUid() }
    val messageToSend =
        message.copy(messageId = messageId, sentTime = message.sentTime ?: Timestamp.now())

    synchronized(this) { messages[messageId] = messageToSend }

    // Update conversation
    updateConversationAfterMessage(messageToSend)

    return messageId
  }

  override suspend fun markMessageAsRead(messageId: String, readTime: Timestamp) {
    require(messageId.isNotBlank()) { "Message ID cannot be blank" }

    val message = getMessage(messageId) ?: throw Exception("Message not found")

    require(message.sentTo == currentUserId) {
      "Access denied: Only the receiver can mark a message as read."
    }

    val updatedMessage = message.copy(isRead = true, readTime = readTime, receiveTime = readTime)

    synchronized(this) { messages[messageId] = updatedMessage }
  }

  override suspend fun deleteMessage(messageId: String) {
    require(messageId.isNotBlank()) { "Message ID cannot be blank" }

    val message = getMessage(messageId) ?: throw Exception("Message not found")

    require(message.sentFrom == currentUserId) {
      "Access denied: Only the sender can delete a message."
    }

    synchronized(this) { messages.remove(messageId) }
  }

  override suspend fun getUnreadMessagesInConversation(
      conversationId: String,
      userId: String
  ): List<Message> {
    require(conversationId.isNotBlank()) { "Conversation ID cannot be blank" }
    require(userId == currentUserId) { "Access denied: You can only get your own unread messages." }

    return synchronized(this) {
      messages.values
          .filter { it.conversationId == conversationId && it.sentTo == userId && !it.isRead }
          .sortedBy { it.sentTime?.seconds ?: 0 }
    }
  }

  // ========== Conversation Operations ==========

  override suspend fun getConversationsForUser(userId: String): List<Conversation> {
    require(userId == currentUserId) { "Access denied: You can only get your own conversations." }

    return synchronized(this) {
      conversations.values
          .filter { it.isParticipant(userId) }
          .sortedByDescending { it.lastMessageTime?.seconds ?: 0 }
    }
  }

  override suspend fun getConversation(conversationId: String): Conversation? {
    require(conversationId.isNotBlank()) { "Conversation ID cannot be blank" }

    val conversation = synchronized(this) { conversations[conversationId] }

    conversation?.let {
      require(it.isParticipant(currentUserId)) {
        "Access denied: You are not a participant in this conversation."
      }
    }

    return conversation
  }

  override suspend fun getOrCreateConversation(userId1: String, userId2: String): Conversation {
    require(userId1.isNotBlank()) { "User 1 ID cannot be blank" }
    require(userId2.isNotBlank()) { "User 2 ID cannot be blank" }
    require(userId1 != userId2) { "Cannot create conversation with yourself" }
    require(userId1 == currentUserId || userId2 == currentUserId) {
      "Access denied: You must be one of the participants."
    }

    val conversationId = Conversation.generateConversationId(userId1, userId2)

    val existingConversation = synchronized(this) { conversations[conversationId] }
    if (existingConversation != null) {
      return existingConversation
    }

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

    synchronized(this) { conversations[conversationId] = newConversation }

    return newConversation
  }

  override suspend fun updateConversation(conversation: Conversation) {
    require(conversation.isParticipant(currentUserId)) {
      "Access denied: You are not a participant in this conversation."
    }

    conversation.validate()

    synchronized(this) { conversations[conversation.conversationId] = conversation }
  }

  override suspend fun markConversationAsRead(conversationId: String, userId: String) {
    require(conversationId.isNotBlank()) { "Conversation ID cannot be blank" }
    require(userId == currentUserId) {
      "Access denied: You can only mark your own messages as read."
    }

    val conversation = getConversation(conversationId) ?: throw Exception("Conversation not found")

    require(conversation.isParticipant(userId)) {
      "Access denied: You are not a participant in this conversation."
    }

    // Update conversation unread count
    val updatedConversation =
        when (userId) {
          conversation.participant1Id -> conversation.copy(unreadCountUser1 = 0)
          conversation.participant2Id -> conversation.copy(unreadCountUser2 = 0)
          else -> throw IllegalStateException("User is not a participant")
        }

    synchronized(this) { conversations[conversationId] = updatedConversation }

    // Mark all unread messages as read
    val unreadMessages = getUnreadMessagesInConversation(conversationId, userId)
    val now = Timestamp.now()
    unreadMessages.forEach { message -> markMessageAsRead(message.messageId, now) }
  }

  override suspend fun deleteConversation(conversationId: String) {
    require(conversationId.isNotBlank()) { "Conversation ID cannot be blank" }

    val conversation = getConversation(conversationId) ?: throw Exception("Conversation not found")

    require(conversation.isParticipant(currentUserId)) {
      "Access denied: You are not a participant in this conversation."
    }

    // Delete all messages in the conversation
    val messagesToDelete = getMessagesInConversation(conversationId)
    synchronized(this) { messagesToDelete.forEach { messages.remove(it.messageId) } }

    // Delete the conversation
    synchronized(this) { conversations.remove(conversationId) }
  }

  // ========== Helper Methods ==========

  private suspend fun updateConversationAfterMessage(message: Message) {
    val conversation = synchronized(this) { conversations[message.conversationId] }

    if (conversation == null) {
      // Create conversation if it doesn't exist
      getOrCreateConversation(message.sentFrom, message.sentTo)
    }

    conversation?.let {
      val updatedConversation =
          when (message.sentTo) {
            it.participant1Id -> {
              it.copy(
                  lastMessageContent = message.content,
                  lastMessageTime = message.sentTime,
                  lastMessageSenderId = message.sentFrom,
                  unreadCountUser1 = it.unreadCountUser1 + 1,
                  updatedAt = Timestamp.now())
            }
            it.participant2Id -> {
              it.copy(
                  lastMessageContent = message.content,
                  lastMessageTime = message.sentTime,
                  lastMessageSenderId = message.sentFrom,
                  unreadCountUser2 = it.unreadCountUser2 + 1,
                  updatedAt = Timestamp.now())
            }
            else -> it
          }

      synchronized(this) { conversations[message.conversationId] = updatedConversation }
    }
  }

  // ========== Test Helper Methods ==========

  /** Clears all data (useful for tests) */
  fun clear() {
    synchronized(this) {
      messages.clear()
      conversations.clear()
    }
  }

  /** Gets all messages (useful for tests) */
  fun getAllMessages(): List<Message> = synchronized(this) { messages.values.toList() }

  /** Gets all conversations (useful for tests) */
  fun getAllConversations(): List<Conversation> =
      synchronized(this) { conversations.values.toList() }
}
