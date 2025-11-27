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

  override suspend fun deleteConvAndOverviews(convId: String) {
    convRepo.deleteConv(convId)
    overViewRepo.deleteOverViewConvUser(convId)
  }

  // ---------------------------
  // Send message & update overview
  // ---------------------------
  override suspend fun sendMessage(convId: String, message: MessageNew) {
    convRepo.sendMessage(convId, message)

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

  override suspend fun resetUnreadCount(convId: String, userId: String) {
    val overview =
        overViewRepo.getOverViewConvUser(userId).firstOrNull { it.linkedConvId == convId } ?: return
    val updated = overview.copy(nonReadMsgNumber = 0)
    overViewRepo.addOverViewConvUser(updated)
  }

  override fun listenMessages(convId: String): Flow<List<MessageNew>> {
    return convRepo.listenMessages(convId)
  }

  override fun listenConversationOverviews(userId: String): Flow<List<OverViewConversation>> {
    return overViewRepo.listenOverView(userId)
  }
}
