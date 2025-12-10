package com.android.sample.model.communication.overViewConv

import kotlinx.coroutines.flow.Flow

interface OverViewConvRepository {

  fun getNewUid(): String

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>

  suspend fun addOverViewConvUser(overView: OverViewConversation)

  suspend fun deleteOverViewConvUser(convId: String)

  suspend fun deleteOverViewById(overViewId: String)

  fun listenOverView(userId: String): Flow<List<OverViewConversation>>
}
