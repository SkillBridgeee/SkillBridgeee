package com.android.sample.model.communication.newImplementation.OverViewConv

interface OverViewConvRepository {

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>
}
