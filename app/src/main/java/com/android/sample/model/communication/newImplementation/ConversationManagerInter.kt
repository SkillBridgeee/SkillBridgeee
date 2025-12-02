package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.Flow

interface ConversationManagerInter {

  suspend fun createConvAndOverviews(
      creatorId: String,
      otherUserId: String,
      convName: String
  ): String

  suspend fun deleteConvAndOverviews(convId: String)

  suspend fun sendMessage(convId: String, message: MessageNew)

  suspend fun resetUnreadCount(convId: String, userId: String)

  fun listenMessages(convId: String): Flow<List<MessageNew>>

  fun listenConversationOverviews(userId: String): Flow<List<OverViewConversation>>

  suspend fun getConv(convId: String): ConversationNew?

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>

  fun getMessageNewUid(): String
}
