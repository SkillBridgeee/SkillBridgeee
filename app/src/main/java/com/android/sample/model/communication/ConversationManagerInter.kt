package com.android.sample.model.communication

import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.Flow

interface ConversationManagerInter {

  suspend fun createConvAndOverviews(
      creatorId: String,
      otherUserId: String,
      convName: String
  ): String

  suspend fun deleteConvAndOverviews(convId: String, deleterId: String, otherId: String)

  suspend fun sendMessage(convId: String, message: Message)

  suspend fun resetUnreadCount(convId: String, userId: String)

  fun listenMessages(convId: String): Flow<List<Message>>

  fun listenConversationOverviews(userId: String): Flow<List<OverViewConversation>>

  suspend fun getConv(convId: String): Conversation?

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>

  fun getMessageNewUid(): String
}
