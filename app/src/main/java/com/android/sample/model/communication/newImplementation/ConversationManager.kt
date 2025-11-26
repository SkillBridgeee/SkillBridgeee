package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.newImplementation.conversation.ConvRepository
import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation

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
            otherPersonId = otherUserId)
    val overview2 =
        OverViewConversation(
            overViewId = overViewRepo.getNewUid(),
            linkedConvId = convId,
            convName = convName,
            lastMsg = MessageNew(),
            nonReadMsgNumber = 0,
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

  override suspend fun sendMessage(convId: String, message: MessageNew) {
    TODO("Not yet implemented")
  }
}
