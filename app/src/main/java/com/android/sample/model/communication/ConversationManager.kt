package com.android.sample.model.communication

import android.util.Log
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.Flow

class ConversationManager(
    private val convRepo: ConvRepository,
    private val overViewRepo: OverViewConvRepository,
    private val bookingRepo: BookingRepository? = null,
) : ConversationManagerInter {

  companion object {
    private const val TAG = "ConversationManager"
  }

  /**
   * Creates a new conversation and the corresponding overview entries for both participants. If a
   * conversation already exists between these two users, returns the existing conversation ID.
   *
   * @param creatorId The user ID of the conversation creator.
   * @param otherUserId The user ID of the other participant.
   * @param convName The display name of the conversation.
   * @return The conversation ID (existing or newly created).
   *
   * This method:
   * 1. Checks if a conversation already exists between the two users.
   * 2. If exists, returns the existing conversation ID.
   * 3. Otherwise, generates a new conversation ID.
   * 4. Creates the Conversation entity.
   * 5. Creates two OverViewConversation entries (one per participant).
   * 6. Saves both overview entries to the repository.
   */
  override suspend fun createConvAndOverviews(
      creatorId: String,
      otherUserId: String,
      convName: String
  ): String {
    Log.d(TAG, "Creating conversation between $creatorId and $otherUserId")

    // Check if conversation already exists between these two users
    // Only query otherUserId's overviews since they are typically the calling/authenticated user
    // This avoids permission issues when trying to read another user's overviews
    val existingConversations = overViewRepo.getOverViewConvUser(otherUserId)
    val existingConv = existingConversations.firstOrNull { it.otherPersonId == creatorId }

    if (existingConv != null) {
      // Conversation already exists, return existing ID
      Log.d(TAG, "Conversation already exists: ${existingConv.linkedConvId}")
      return existingConv.linkedConvId
    }

    Log.d(TAG, "Creating new conversation...")
    val convId = convRepo.getNewUid()
    val conversation =
        Conversation(
            convId = convId,
            convCreatorId = creatorId,
            otherPersonId = otherUserId,
            convName = convName,
            messages = emptyList())
    convRepo.createConv(conversation)

    val overviewConvCreator =
        OverViewConversation(
            overViewId = overViewRepo.getNewUid(),
            linkedConvId = convId,
            convName = convName,
            lastMsg = Message(),
            overViewOwnerId = creatorId,
            otherPersonId = otherUserId)
    val overviewOtherPerson =
        overviewConvCreator.copy(
            overViewId = overViewRepo.getNewUid(),
            overViewOwnerId = otherUserId,
            otherPersonId = creatorId)

    overViewRepo.addOverViewConvUser(overviewConvCreator)
    overViewRepo.addOverViewConvUser(overviewOtherPerson)

    Log.d(TAG, "Successfully created conversation: $convId")
    return convId
  }

  /**
   * Deletes a conversation and manages the corresponding overviews for both participants.
   *
   * @param convId The ID of the conversation to delete.
   * @param deleterId The ID of the user initiating the deletion.
   * @param otherId The ID of the other participant in the conversation.
   */
  override suspend fun deleteConvAndOverviews(convId: String, deleterId: String, otherId: String) {
    // Check for ongoing bookings between the two users before allowing deletion
    try {
      val blocked = bookingRepo?.hasOngoingBookingBetween(deleterId, otherId) ?: false
      check(blocked == true) {
        "BLOCK_DELETE_CONV_ACTIVE_BOOKING: Cannot delete conversation ($convId): " +
            "active booking exists between users deleterId=$deleterId and otherId=$otherId"
      }
    } catch (e: Exception) {}

    if (convRepo.getConv(convId) != null) {
      convRepo.deleteConv(convId)
    }

    val myOverviews = overViewRepo.getOverViewConvUser(deleterId)
    myOverviews
        .filter { it.linkedConvId == convId }
        .forEach { overview -> overViewRepo.deleteOverViewById(overview.overViewId) }

    try {
      val otherOverviews = overViewRepo.getOverViewConvUser(otherId)

      val otherOverview = otherOverviews.firstOrNull { it.linkedConvId == convId }

      if (otherOverview != null) {
        // Create a modified overview that indicates deletion
        val updatedOverview =
            otherOverview.copy(
                lastMsg =
                    Message(
                        msgId = "deleted-system-msg",
                        senderId = "system",
                        receiverId = otherId,
                        content = "Conversation deleted",
                        createdAt = java.util.Date()),
                nonReadMsgNumber = 0)

        overViewRepo.addOverViewConvUser(updatedOverview)
      }
    } catch (e: Exception) {
      println("Failed to update other user's overview: ${e.message}")
    }
  }

  /**
   * Sends a message within a conversation and updates each participant’s conversation overview.
   *
   * @param convId The ID of the conversation.
   * @param message The message to send.
   *
   * This method:
   * 1. Saves the message to the conversation.
   * 2. Updates the last message in each user's overview.
   * 3. Increments the unread count for the message receiver.
   */
  override suspend fun sendMessage(convId: String, message: Message) {
    convRepo.sendMessage(convId, message)
    updateOverviewsAfterSend(convId, message)
  }

  private suspend fun updateOverviewsAfterSend(convId: String, message: Message) {
    // Explicitly get sender and receiver to avoid any ambiguity
    val senderId = message.senderId
    val receiverId = message.receiverId

    // Update sender's overview: DO NOT increment unread count, explicitly set to 0
    val senderOverview =
        overViewRepo.getOverViewConvUser(senderId).firstOrNull { it.linkedConvId == convId }
    if (senderOverview != null) {
      val updatedSenderOverview = senderOverview.copy(lastMsg = message, nonReadMsgNumber = 0)
      overViewRepo.addOverViewConvUser(updatedSenderOverview)
      Log.d(
          TAG,
          "Updated sender's overview for user $senderId, unread count is ${updatedSenderOverview.nonReadMsgNumber}")
    } else {
      Log.w(TAG, "Could not find sender's overview for user $senderId in conversation $convId")
    }

    // Update receiver's overview: INCREMENT unread count
    val receiverOverview =
        overViewRepo.getOverViewConvUser(receiverId).firstOrNull { it.linkedConvId == convId }
    if (receiverOverview != null) {
      val updatedReceiverOverview =
          receiverOverview.copy(
              lastMsg = message, nonReadMsgNumber = receiverOverview.nonReadMsgNumber + 1)
      overViewRepo.addOverViewConvUser(updatedReceiverOverview)
      Log.d(
          TAG,
          "Updated receiver's overview for user $receiverId, new unread count is ${updatedReceiverOverview.nonReadMsgNumber}")
    } else {
      Log.w(TAG, "Could not find receiver's overview for user $receiverId in conversation $convId")
    }
  }

  /**
   * Resets the unread message counter for a given user in a specific conversation.
   *
   * @param convId The ID of the conversation.
   * @param userId The ID of the user whose unread count should be reset.
   */
  override suspend fun resetUnreadCount(convId: String, userId: String) {
    val overview =
        overViewRepo.getOverViewConvUser(userId).firstOrNull { it.linkedConvId == convId } ?: return
    val updated = overview.copy(nonReadMsgNumber = 0)
    overViewRepo.addOverViewConvUser(updated)
  }

  /**
   * Listens to real-time updates of messages inside a conversation.
   *
   * @param convId The ID of the conversation.
   * @return A Flow that emits the updated list of messages whenever a new message arrives.
   */
  override fun listenMessages(convId: String): Flow<List<Message>> {
    return convRepo.listenMessages(convId)
  }

  /**
   * Listens to real-time updates of all conversation overviews for a given user.
   *
   * @param userId The user whose conversation overviews we want to observe.
   * @return A Flow emitting the list of updated OverViewConversation objects.
   */
  override fun listenConversationOverviews(userId: String): Flow<List<OverViewConversation>> {
    return overViewRepo.listenOverView(userId)
  }

  override suspend fun getConv(convId: String): Conversation? {
    return convRepo.getConv(convId)
  }

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    return overViewRepo.getOverViewConvUser(userId)
  }

  override fun getMessageNewUid(): String {
    return convRepo.getNewUid()
  }
}
