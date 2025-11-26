package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.newImplementation.conversation.MessageNew

interface ConversationManagerInter {

  //  CONVERSATION LIFECYCLE
  suspend fun createConvAndOverviews(
      creatorId: String,
      otherUserId: String,
      convName: String
  ): String

  suspend fun deleteConvAndOverviews(convId: String)

  // MESSAGES
  suspend fun sendMessage(convId: String, message: MessageNew)
}
