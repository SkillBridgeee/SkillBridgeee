package com.android.sample.model.communication

interface MessageRepository {
  fun getNewUid(): String

  suspend fun getAllMessages(): List<Message>

  suspend fun getMessage(messageId: String): Message

  suspend fun getMessagesBetweenUsers(userId1: String, userId2: String): List<Message>

  suspend fun getMessagesSentByUser(userId: String): List<Message>

  suspend fun getMessagesReceivedByUser(userId: String): List<Message>

  suspend fun addMessage(message: Message)

  suspend fun updateMessage(messageId: String, message: Message)

  suspend fun deleteMessage(messageId: String)

  /** Marks message as received */
  suspend fun markAsReceived(messageId: String, receiveTime: java.util.Date)

  /** Marks message as read */
  suspend fun markAsRead(messageId: String, readTime: java.util.Date)

  /** Gets unread messages for a user */
  suspend fun getUnreadMessages(userId: String): List<Message>
}
