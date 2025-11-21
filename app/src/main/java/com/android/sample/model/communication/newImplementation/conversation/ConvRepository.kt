package com.android.sample.model.communication.newImplementation.conversation

import kotlinx.coroutines.flow.Flow

interface ConvRepository {

  fun getNewUid(): String

  suspend fun getConv(convId: String): ConversationNew?

  suspend fun createConv(conversation: ConversationNew)

  suspend fun deleteConv(convId: String)

  fun listenMessages(convId: String): Flow<List<MessageNew>>
}
