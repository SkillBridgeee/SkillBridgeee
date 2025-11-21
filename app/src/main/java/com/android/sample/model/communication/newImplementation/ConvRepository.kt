package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.Conversation

interface ConvRepository {

  suspend fun getConv(convId: String): Conversation?

  suspend fun createConv(convRepository: ConvRepository)

  suspend fun deleteConv(convId: String)
}
