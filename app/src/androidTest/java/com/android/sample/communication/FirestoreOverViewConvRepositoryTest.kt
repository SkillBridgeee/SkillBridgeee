package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.utils.TestFirestore
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreOverViewConvRepositoryTest {

  private lateinit var repo: FirestoreOverViewConvRepository

  private val userA = "userA"
  private val userB = "userB"

  @Before
  fun setup() {
    repo = FirestoreOverViewConvRepository(TestFirestore.db)
  }

  // ----------------------------------------------------------
  // TEST 1 : CREATE + GET OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testAddAndGetOverview() = runTest {
    val overview =
        OverViewConversation(
            linkedConvId = "conv1",
            convName = "Conversation 1",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val result = repo.getOverViewConvUser(userA)
    assertTrue(result.any { it.linkedConvId == "conv1" })

    val resultOther = repo.getOverViewConvUser(userB)
    assertTrue(resultOther.any { it.linkedConvId == "conv1" })
  }

  // ----------------------------------------------------------
  // TEST 2 : UPDATE LAST MESSAGE
  // ----------------------------------------------------------
  @Test
  fun testUpdateLastMessage() = runTest {
    val convId = "conv2"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Conversation 2",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val msg = MessageNew(msgId = "msg1", content = "Hello!", senderId = userA, receiverId = userB)
    repo.updateLastMessage(convId, msg)

    val updated = repo.getOverViewConvUser(userA).first { it.linkedConvId == convId }
    assertEquals("Hello!", updated.lastMsg.content)
    assertEquals("msg1", updated.lastMsg.msgId)
  }

  // ----------------------------------------------------------
  // TEST 3 : UPDATE UNREAD COUNT
  // ----------------------------------------------------------
  @Test
  fun testUpdateUnreadCount() = runTest {
    val convId = "conv3"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Conversation 3",
            convCreatorId = userA,
            otherPersonId = userB,
            nonReadMsgNumber = 0)

    repo.addOverViewConvUser(overview)

    repo.updateUnreadCount(convId, 5)

    val updated = repo.getOverViewConvUser(userA).first { it.linkedConvId == convId }
    assertEquals(5, updated.nonReadMsgNumber)
  }

  // ----------------------------------------------------------
  // TEST 4 : UPDATE CONVERSATION NAME
  // ----------------------------------------------------------
  @Test
  fun testUpdateConvName() = runTest {
    val convId = "conv4"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Old Name",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    repo.updateConvName(convId, "New Name")

    val updated = repo.getOverViewConvUser(userA).first { it.linkedConvId == convId }
    assertEquals("New Name", updated.convName)
  }

  // ----------------------------------------------------------
  // TEST 5 : DELETE OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testDeleteOverview() = runTest {
    val convId = "conv5"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Conversation 5",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    repo.deleteOverViewConvUser(convId)

    val result = repo.getOverViewConvUser(userA)
    assertTrue(result.none { it.linkedConvId == convId })
  }

  // ----------------------------------------------------------
  // TEST 6 : LISTEN OVERVIEW FLOW
  // ----------------------------------------------------------
  @Test
  fun testListenOverviewFlow() = runTest {
    val convId = "conv6"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Conversation 6",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val flow = repo.listenOverView(userA)

    val emitted = flow.first { it.isNotEmpty() && it.any { it.linkedConvId == convId } }
    assertTrue(emitted.any { it.linkedConvId == convId })
  }
}
