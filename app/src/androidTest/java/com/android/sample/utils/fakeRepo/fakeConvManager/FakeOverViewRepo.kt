package com.android.sample.utils.fakeRepo.fakeConvManager

import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.Flow

class FakeOverViewRepo : OverViewConvRepository {
  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    TODO("Not yet implemented")
  }

  override suspend fun addOverViewConvUser(overView: OverViewConversation) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteOverViewConvUser(convId: String) {
    TODO("Not yet implemented")
  }

  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> {
    TODO("Not yet implemented")
  }
}
