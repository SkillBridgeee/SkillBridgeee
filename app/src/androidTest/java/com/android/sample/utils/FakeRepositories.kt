package com.android.sample.utils

import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeConversationRepo : ConvRepository {
  override fun getNewUid(): String = "fake-conv-id"

  override suspend fun getConv(convId: String): Conversation? = null

  override suspend fun createConv(conversation: Conversation) {}

  override suspend fun deleteConv(convId: String) {}

  override suspend fun sendMessage(convId: String, message: Message) {}

  override fun listenMessages(convId: String): Flow<List<Message>> = emptyFlow()
}

class FakeOverViewConvRepo : OverViewConvRepository {
  override fun getNewUid(): String = "fake-overview-id"

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> = emptyList()

  override suspend fun addOverViewConvUser(overView: OverViewConversation) {}

  override suspend fun deleteOverViewConvUser(convId: String) {}

  override suspend fun deleteOverViewById(overViewId: String) {}

  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> = emptyFlow()
}
