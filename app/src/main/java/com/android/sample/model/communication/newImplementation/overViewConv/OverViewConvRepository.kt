package com.android.sample.model.communication.newImplementation.overViewConv

interface OverViewConvRepository {

  suspend fun getOverViewConvUser(userId: String): List<OverViewConversation>
}
