package com.android.sample.model.communication.newImplementation.overViewConv

import kotlinx.coroutines.flow.Flow

interface OverViewConvRepository {

  fun getNewUid(): String

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>

  suspend fun addOverViewConvUser(overView: OverViewConversation)

  suspend fun deleteOverViewConvUser(convId: String)

  fun listenOverView(userId: String): Flow<List<OverViewConversation>>
}
