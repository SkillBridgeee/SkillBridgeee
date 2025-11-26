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

    val overview1 =
        OverViewConversation(
            overViewId = overViewRepo.getNewUid(),
            linkedConvId = convId,
            convName = convName,
            lastMsg = MessageNew(),
            nonReadMsgNumber = 0,
            overViewOwnerId = creatorId,
            otherPersonId = otherUserId,
            lastReadMessageId = null)
    val overview2 =
        overview1.copy(
            overViewId = overViewRepo.getNewUid(),
            overViewOwnerId = otherUserId,
            otherPersonId = creatorId)

    overViewRepo.addOverViewConvUser(overview1)
    overViewRepo.addOverViewConvUser(overview2)

    return convId
  }

  override suspend fun deleteConvAndOverviews(convId: String) {
    convRepo.deleteConv(convId)
    overViewRepo.deleteOverViewConvUser(convId)
  }

  // ---------------------------
  // Send message & update overview
  // ---------------------------
  override suspend fun sendMessage(convId: String, message: MessageNew) {
    convRepo.sendMessage(convId, message)

    // Update lastMsg in both overviews
    val participants = listOf(message.senderId, message.receiverId)
    participants.forEach { userId ->
      val overviews = overViewRepo.getOverViewConvUser(userId)
      val overview = overviews.firstOrNull { it.linkedConvId == convId } ?: return@forEach
      val updatedOverview = overview.copy(lastMsg = message)
      overViewRepo.addOverViewConvUser(updatedOverview)
    }
  }

  override suspend fun resetUnreadCount(convId: String, userId: String) {
    val overviews = overViewRepo.getOverViewConvUser(userId)
    val overview = overviews.firstOrNull { it.linkedConvId == convId } ?: return

    val updatedOverview =
        overview.copy(lastReadMessageId = overview.lastMsg.msgId, nonReadMsgNumber = 0)
    overViewRepo.addOverViewConvUser(updatedOverview)
  }

  suspend fun calculateUnreadCount(convId: String, userId: String): Int {
    val messages = convRepo.getConv(convId)?.messages ?: return 0
    val overview =
        overViewRepo.getOverViewConvUser(userId).firstOrNull { it.linkedConvId == convId }

    val lastReadId = overview?.lastReadMessageId
    if (lastReadId == null) return messages.size

    val index = messages.indexOfFirst { it.msgId == lastReadId }
    return if (index == -1) messages.size else messages.size - index - 1
  }

  override fun listenMessages(convId: String): Flow<List<MessageNew>> {
    return convRepo.listenMessages(convId)
  }

  override fun listenConversationOverviews(userId: String): Flow<List<OverViewConversation>> {
    return overViewRepo.listenOverView(userId)
  }
}
