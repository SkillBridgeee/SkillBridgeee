package com.android.sample.model.communication

import com.google.firebase.Timestamp

interface MessageRepository {
  fun getNewUid(): String

  // ========== Message Operations ==========

  /** Gets all messages in a specific conversation */
  suspend fun getMessagesInConversation(conversationId: String): List<Message>

  /** Gets a single message by ID */
  suspend fun getMessage(messageId: String): Message?

  /** Sends a new message */
  suspend fun sendMessage(message: Message): String

  /** Marks a message as read */
  suspend fun markMessageAsRead(messageId: String, readTime: Timestamp)

  /** Deletes a message */
  suspend fun deleteMessage(messageId: String)

  /** Gets unread messages for a user in a specific conversation */
  suspend fun getUnreadMessagesInConversation(conversationId: String, userId: String): List<Message>

  // ========== Conversation Operations ==========

  /** Gets all conversations for a user */
  suspend fun getConversationsForUser(userId: String): List<Conversation>

  /** Gets a specific conversation by ID */
  suspend fun getConversation(conversationId: String): Conversation?

  /** Gets or creates a conversation between two users */
  suspend fun getOrCreateConversation(userId1: String, userId2: String): Conversation

  /** Updates a conversation (e.g., when a new message is sent) */
  suspend fun updateConversation(conversation: Conversation)

  /** Marks all messages in a conversation as read for a specific user */
  suspend fun markConversationAsRead(conversationId: String, userId: String)

  /** Deletes a conversation and all its messages */
  suspend fun deleteConversation(conversationId: String)
}
