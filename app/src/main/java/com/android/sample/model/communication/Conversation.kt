package com.android.sample.model.communication

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a one-on-one conversation between two users (tutor and student)
 *
 * This model helps organize messages and provides quick access to conversation metadata
 */
data class Conversation(
    @DocumentId val conversationId: String = "", // Unique conversation ID
    val participant1Id: String = "", // First participant (tutor or student)
    val participant2Id: String = "", // Second participant (tutor or student)
    val lastMessageContent: String = "", // Preview of the last message
    @ServerTimestamp val lastMessageTime: Timestamp? = null, // Time of the last message
    val lastMessageSenderId: String = "", // Who sent the last message
    val unreadCountUser1: Int = 0, // Number of unread messages for participant1
    val unreadCountUser2: Int = 0, // Number of unread messages for participant2
    @ServerTimestamp val createdAt: Timestamp? = null, // When the conversation was created
    @ServerTimestamp val updatedAt: Timestamp? = null // Last time conversation was updated
) {
  // No-argument constructor for Firestore deserialization
  constructor() : this("", "", "", "", null, "", 0, 0, null, null)

  /** Validates the conversation data. Throws an [IllegalArgumentException] if invalid. */
  fun validate() {
    require(participant1Id.isNotBlank()) { "Participant 1 ID cannot be blank" }
    require(participant2Id.isNotBlank()) { "Participant 2 ID cannot be blank" }
    require(participant1Id != participant2Id) { "Participants must be different users" }
    require(unreadCountUser1 >= 0) { "Unread count for user 1 cannot be negative" }
    require(unreadCountUser2 >= 0) { "Unread count for user 2 cannot be negative" }
  }

  /**
   * Gets the other participant's ID given one participant's ID
   *
   * @param userId The ID of one participant
   * @return The ID of the other participant
   * @throws IllegalArgumentException if userId is not a participant
   */
  fun getOtherParticipantId(userId: String): String {
    return when (userId) {
      participant1Id -> participant2Id
      participant2Id -> participant1Id
      else ->
          throw IllegalArgumentException("User $userId is not a participant in this conversation")
    }
  }

  /**
   * Gets the unread count for a specific user
   *
   * @param userId The ID of the user
   * @return Number of unread messages for that user
   * @throws IllegalArgumentException if userId is not a participant
   */
  fun getUnreadCountForUser(userId: String): Int {
    return when (userId) {
      participant1Id -> unreadCountUser1
      participant2Id -> unreadCountUser2
      else ->
          throw IllegalArgumentException("User $userId is not a participant in this conversation")
    }
  }

  /**
   * Checks if a user is a participant in this conversation
   *
   * @param userId The ID of the user to check
   * @return true if the user is a participant, false otherwise
   */
  fun isParticipant(userId: String): Boolean {
    return userId == participant1Id || userId == participant2Id
  }

  companion object {
    /**
     * Generates a consistent conversation ID for two users regardless of the order
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return A consistent conversation ID
     */
    fun generateConversationId(userId1: String, userId2: String): String {
      val sortedIds = listOf(userId1, userId2).sorted()
      return "${sortedIds[0]}_${sortedIds[1]}"
    }
  }
}
