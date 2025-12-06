package com.android.sample.model.communication.conversation

import kotlinx.coroutines.flow.Flow

interface ConvRepository {

  fun getNewUid(): String

  suspend fun getConv(convId: String): Conversation?

  suspend fun createConv(conversation: Conversation)

  suspend fun deleteConv(convId: String)

  suspend fun sendMessage(convId: String, message: Message)

  fun listenMessages(convId: String): Flow<List<Message>>
}
