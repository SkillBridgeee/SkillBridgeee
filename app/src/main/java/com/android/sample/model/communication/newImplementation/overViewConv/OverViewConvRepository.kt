package com.android.sample.model.communication.newImplementation.overViewConv

import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import kotlinx.coroutines.flow.Flow

interface OverViewConvRepository {

  fun getNewUid(): String

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>

  suspend fun addOverViewConvUser(overView: OverViewConversation)

  suspend fun deleteOverViewConvUser(convId: String)

  fun listenOverView(userId: String): Flow<List<OverViewConversation>>

  suspend fun updateOverViewConvUser(overView: OverViewConversation)

  suspend fun updateLastMessage(convId: String, lastMsg: MessageNew)

  suspend fun updateUnreadCount(convId: String, count: Int)

  suspend fun updateConvName(convId: String, newName: String)
}
