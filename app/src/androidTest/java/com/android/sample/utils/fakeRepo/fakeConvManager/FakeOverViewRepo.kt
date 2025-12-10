package com.android.sample.utils.fakeRepo.fakeConvManager

import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConversation
import java.util.UUID
import kotlinx.coroutines.flow.*

class FakeOverViewRepo : OverViewConvRepository {

  // Toutes les overviews stockées en mémoire
  private val overviews = mutableMapOf<String, OverViewConversation>()

  // Flows par utilisateur
  private val userFlows = mutableMapOf<String, MutableStateFlow<List<OverViewConversation>>>()

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    return overviews.values.filter { it.overViewOwnerId == userId }
  }

  override suspend fun addOverViewConvUser(overView: OverViewConversation) {
    val id = overView.overViewId.ifEmpty { getNewUid() }

    val newOverView = overView.copy(overViewId = id)
    overviews[id] = newOverView

    refreshUserFlow(newOverView.overViewOwnerId)
  }

  override suspend fun deleteOverViewConvUser(convId: String) {
    val target = overviews.values.find { it.linkedConvId == convId }
    target?.let {
      overviews.remove(it.overViewId)
      refreshUserFlow(it.overViewOwnerId)
    }
  }

  override suspend fun deleteOverViewById(overViewId: String) {
    val overview = overviews.remove(overViewId)
    overview?.let { refreshUserFlow(it.overViewOwnerId) }
  }

  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> {
    return userFlows.getOrPut(userId) { MutableStateFlow(emptyList()) }
  }

  // ---------------------
  // Helpers
  // ---------------------

  private suspend fun refreshUserFlow(userId: String) {
    val list = getOverViewConvUser(userId)
    userFlows[userId]?.value = list
  }
}
