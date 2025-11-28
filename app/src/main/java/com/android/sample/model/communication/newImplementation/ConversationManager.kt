package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.newImplementation.conversation.ConvRepository
import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.Flow

class ConversationManager(
    private val convRepo: ConvRepository,
    private val overViewRepo: OverViewConvRepository,
) : ConversationManagerInter {

  /**
   * Creates a new conversation and the corresponding overview entries for both participants.
   *
   * @param creatorId The user ID of the conversation creator.
   * @param otherUserId The user ID of the other participant.
   * @param convName The display name of the conversation.
   * @return The newly generated conversation ID.
   *
   * This method:
   * 1. Generates a new conversation ID.
   * 2. Creates the Conversation entity.
   * 3. Creates two OverViewConversation entries (one per participant).
   * 4. Saves both overview entries to the repository.
   */
  override suspend fun createConvAndOverviews(
      creatorId: String,
      otherUserId: String,
      convName: String
  ): String {

    val convId = convRepo.getNewUid()
    val conversation =
        ConversationNew(
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
            lastMsg = MessageNew(),
            overViewOwnerId = creatorId,
            otherPersonId = otherUserId)
    val overviewOtherPerson =
        overviewConvCreator.copy(
            overViewId = overViewRepo.getNewUid(),
            overViewOwnerId = otherUserId,
            otherPersonId = creatorId)

    overViewRepo.addOverViewConvUser(overviewConvCreator)
    overViewRepo.addOverViewConvUser(overviewOtherPerson)

    return convId
  }

  /**
   * Deletes a conversation and all associated overview entries for every user.
   *
   * @param convId The ID of the conversation to delete.
   *
   * This removes:
   * - The conversation itself.
   * - All overview entries linked to this conversation.
   */
  override suspend fun deleteConvAndOverviews(convId: String) {
    convRepo.deleteConv(convId)
    overViewRepo.deleteOverViewConvUser(convId)
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
  override suspend fun sendMessage(convId: String, message: MessageNew) {
    convRepo.sendMessage(convId, message)
    updateOverviewsAfterSend(convId, message)
  }

  private suspend fun updateOverviewsAfterSend(convId: String, message: MessageNew) {
    val participants = listOf(message.senderId, message.receiverId)
    participants.forEach { userId ->
      val overview =
          overViewRepo.getOverViewConvUser(userId).firstOrNull { it.linkedConvId == convId }
              ?: return@forEach
      val updated =
          if (userId == message.senderId) overview.copy(lastMsg = message)
          else overview.copy(lastMsg = message, nonReadMsgNumber = overview.nonReadMsgNumber + 1)
      overViewRepo.addOverViewConvUser(updated)
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
  override fun listenMessages(convId: String): Flow<List<MessageNew>> {
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
}
