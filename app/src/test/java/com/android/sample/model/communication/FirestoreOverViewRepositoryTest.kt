package com.android.sample.model.communication

import app.cash.turbine.test
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.utils.RepositoryTest
import java.util.Date
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest

class FirestoreOverViewRepositoryTest : RepositoryTest() {

  @Test
  fun testAddGetDeleteListen() = runTest {
    val repo = FakeOverViewConvRepository()
    val userId = "user1"

    val conv1 =
        OverViewConversation(
            overViewId = "1",
            linkedConvId = "conv1",
            convName = "Chat 1",
            lastMsg = MessageNew("m1", "Hello", "user1", "user2", Date()),
            convCreatorId = "user1",
            otherPersonId = "user2")

    val conv2 =
        OverViewConversation(
            overViewId = "2",
            linkedConvId = "conv2",
            convName = "Chat 2",
            lastMsg = MessageNew("m2", "World", "user2", "user1", Date()),
            convCreatorId = "user2",
            otherPersonId = "user1")

    // add conversations
    repo.addOverViewConvUser(conv1)
    repo.addOverViewConvUser(conv2)

    // get all conversations for user1
    val allConvs = repo.getOverViewConvUser(userId)
    assertEquals(2, allConvs.size)

    // test listenOverView
    repo.listenOverView(userId).test {
      val firstEmission = awaitItem()
      assertEquals(2, firstEmission.size)
      assertEquals("Chat 1", firstEmission[0].convName)
      assertEquals("Chat 2", firstEmission[1].convName)

      // delete one conversation
      repo.deleteOverViewConvUser("conv1")
      val secondEmission = awaitItem()
      assertEquals(1, secondEmission.size)
      assertEquals("Chat 2", secondEmission[0].convName)

      cancelAndIgnoreRemainingEvents()
    }
  }
}

class FakeOverViewConvRepository : OverViewConvRepository {

  private val _overviewsFlow = MutableStateFlow<List<OverViewConversation>>(emptyList())
  val overviewsFlow: MutableStateFlow<List<OverViewConversation>>
    get() = _overviewsFlow

  override suspend fun addOverViewConvUser(overView: OverViewConversation) {
    val current = _overviewsFlow.value.toMutableList()
    val newConv = overView.copy(overViewId = overView.overViewId.ifBlank { getNewUid() })
    current.add(newConv)
    _overviewsFlow.value = current
  }

  override suspend fun deleteOverViewConvUser(convId: String) {
    _overviewsFlow.value = _overviewsFlow.value.filter { it.linkedConvId != convId }
  }

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    return _overviewsFlow.value
        .filter { it.convCreatorId == userId || it.otherPersonId == userId }
        .sortedByDescending { it.lastMsg.createdAt.time }
  }

  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> {
    return _overviewsFlow.map { list ->
      list
          .filter { it.convCreatorId == userId || it.otherPersonId == userId }
          .sortedByDescending { it.lastMsg.createdAt.time }
    }
  }
}
