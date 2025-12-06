package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.ConversationManager
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.FirestoreConvRepository
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConversation
import com.android.sample.utils.TestFirestore
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationManagerTest {

  private lateinit var convRepo: FirestoreConvRepository
  private lateinit var ovRepo: FirestoreOverViewConvRepository
  private lateinit var manager: ConversationManager
  private lateinit var convId: String

  @Before
  fun setup() {
    convRepo = FirestoreConvRepository(TestFirestore.db)
    ovRepo = FirestoreOverViewConvRepository(TestFirestore.db)
    manager = ConversationManager(convRepo, ovRepo)
  }

  @After
  fun tearDown() = runTest {
    if (::convId.isInitialized) {
      manager.deleteConvAndOverviews(convId)
    }
  }

  // ----------------------------------------------------------
  // TEST 1 : CREATE CONV + OVERVIEWS
  // ----------------------------------------------------------
  @Test
  fun testCreateConvAndOverviews() = runTest {
    val creator = "UserA-test1"
    val other = "UserB-test1"
    val convName = "TestChat"

    convId = manager.createConvAndOverviews(creator, other, convName)

    val conv = convRepo.getConv(convId)
    assertNotNull(conv)
    assertEquals(convId, conv!!.convId)
    assertEquals(creator, conv.convCreatorId)
    assertEquals(other, conv.otherPersonId)
    assertEquals(convName, conv.convName)

    val ovA = ovRepo.getOverViewConvUser(creator)
    val ovB = ovRepo.getOverViewConvUser(other)

    assertEquals(1, ovA.size)
    assertEquals(1, ovB.size)

    assertEquals(convId, ovA.first().linkedConvId)
    assertEquals(convId, ovB.first().linkedConvId)
  }

  // ----------------------------------------------------------
  // TEST 2 : DELETE CONV + OVERVIEWS
  // ----------------------------------------------------------
  @Test
  fun testDeleteConvAndOverviews() = runTest {
    val creator = "A-test2"
    val other = "B-test"

    convId = manager.createConvAndOverviews(creator, other, "Chat")

    assertNotNull(convRepo.getConv(convId))
    assertEquals(1, ovRepo.getOverViewConvUser(creator).size)

    manager.deleteConvAndOverviews(convId)

    assertNull(convRepo.getConv(convId))
    assertTrue(ovRepo.getOverViewConvUser(creator).isEmpty())
    assertTrue(ovRepo.getOverViewConvUser(other).isEmpty())
  }

  // ----------------------------------------------------------
  // TEST 3 : SEND MESSAGE + UPDATE OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testSendMessage() = runTest {
    val creator = "A-test3"
    val other = "B-test3"

    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val msg = Message(msgId = "1-test3", content = "Hello", senderId = creator, receiverId = other)

    manager.sendMessage(convId, msg)

    val conv = convRepo.getConv(convId)
    assertEquals(1, conv!!.messages.size)
    assertEquals(msg.msgId, conv.messages.first().msgId)

    val ovA = ovRepo.getOverViewConvUser(creator).first()
    val ovB = ovRepo.getOverViewConvUser(other).first()

    assertEquals(msg.msgId, ovA.lastMsg!!.msgId)
    assertEquals(msg.content, ovB.lastMsg!!.content)
  }

  // ----------------------------------------------------------
  // TEST 4 : RESET UNREAD COUNT
  // ----------------------------------------------------------
  @Test
  fun testResetUnreadCount() = runTest {
    val creator = "A-test4"
    val other = "B-test4"
    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val msg = Message("1-test4", "Hello", creator, other)
    manager.sendMessage(convId, msg)

    manager.resetUnreadCount(convId, creator)

    val overview = ovRepo.getOverViewConvUser(creator).first()

    assertEquals(msg, overview.lastMsg)
  }

  // ----------------------------------------------------------
  // TEST 6 : LISTEN MESSAGES
  // ----------------------------------------------------------
  @Test
  fun testListenMessages() = runTest {
    val creator = "A-test6"
    val other = "B-test6"
    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val flow = manager.listenMessages(convId)

    val msg = Message("1-test6", "Listening OK", creator, other)
    manager.sendMessage(convId, msg)

    val emitted = flow.first { it.isNotEmpty() }

    assertEquals(1, emitted.size)
    assertEquals("1-test6", emitted.first().msgId)
  }

  // ----------------------------------------------------------
  // TEST 7 : LISTEN OVERVIEW FLOW
  // ----------------------------------------------------------
  @Test
  fun testListenConversationOverviews() = runTest {
    val creator = "A-test7"
    val other = "B-test7"
    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val flow = manager.listenConversationOverviews(creator)

    val msg = Message("1-test7", "Yo", creator, other)
    manager.sendMessage(convId, msg)

    val emitted = flow.first { it.isNotEmpty() }

    assertEquals(1, emitted.size)
    assertEquals("1-test7", emitted.first().lastMsg!!.msgId)
  }

  // test 8
  @Test
  fun testallDiscussion() = runTest {
    val creator = "A-test8"
    val other = "B-test8"
    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val flowCreatorMsg = manager.listenMessages(convId)
    val flowOtherMsg = manager.listenMessages(convId)

    val flowCreatorOverView = manager.listenConversationOverviews(creator)
    val flowOtherOverView = manager.listenConversationOverviews(other)

    // Creator send a message

    val msgCreator1 = Message("msg1", "hello", creator, other)
    manager.sendMessage(convId, msgCreator1)

    var emittedCreatorMsg = flowCreatorMsg.first { it.isNotEmpty() }
    var emittedOtherMsg = flowOtherMsg.first { it.isNotEmpty() }

    var emittedCreatorOverView = flowCreatorOverView.first { it.isNotEmpty() }
    var emittedOtherOverView = flowOtherOverView.first { it.isNotEmpty() }

    assertEquals(1, emittedCreatorMsg.size)
    assertEquals(1, emittedOtherMsg.size)
    assertEquals(msgCreator1, emittedCreatorMsg[0])
    assertEquals(msgCreator1, emittedOtherMsg[0])

    assertEquals(1, emittedCreatorOverView.size)
    assertEquals(1, emittedOtherOverView.size)
    assertEquals(0, emittedCreatorOverView[0].nonReadMsgNumber)
    assertEquals(1, emittedOtherOverView[0].nonReadMsgNumber)

    // Creator read the message and also send a message
    manager.resetUnreadCount(convId, other)

    emittedOtherOverView = flowOtherOverView.first { it.isNotEmpty() }

    assertEquals(1, emittedOtherOverView.size)
    assertEquals(0, emittedOtherOverView[0].nonReadMsgNumber)

    val msgOther1 = Message("msg2", "hi", other, creator)
    manager.sendMessage(convId, msgOther1)

    emittedCreatorOverView = flowCreatorOverView.first { it.isNotEmpty() }
    emittedOtherOverView = flowOtherOverView.first { it.isNotEmpty() }

    assertEquals(1, emittedCreatorOverView.size)
    assertEquals(1, emittedOtherOverView.size)

    assertEquals(1, emittedCreatorOverView[0].nonReadMsgNumber)
    assertEquals(0, emittedOtherOverView[0].nonReadMsgNumber)

    emittedCreatorMsg = flowCreatorMsg.first { it.isNotEmpty() }
    emittedOtherMsg = flowOtherMsg.first { it.isNotEmpty() }

    assertEquals(2, emittedCreatorMsg.size)
    assertEquals(2, emittedOtherMsg.size)

    assertEquals(msgCreator1, emittedCreatorMsg[0])
    assertEquals(msgCreator1, emittedOtherMsg[0])

    assertEquals(msgOther1, emittedCreatorMsg[1])
    assertEquals(msgOther1, emittedOtherMsg[1])
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testAllDiscussionRealTime() = runTest {
    val creator = "A-test8"
    val other = "B-test8"
    convId = manager.createConvAndOverviews(creator, other, "Chat")

    // Lists pour collecter toutes les émissions des flows
    val creatorMsgs = mutableListOf<List<Message>>()
    val otherMsgs = mutableListOf<List<Message>>()
    val creatorOv = mutableListOf<List<OverViewConversation>>()
    val otherOv = mutableListOf<List<OverViewConversation>>()

    // Lancer la collecte en parallèle
    val job = launch { manager.listenMessages(convId).collect { creatorMsgs.add(it) } }
    val job2 = launch { manager.listenMessages(convId).collect { otherMsgs.add(it) } }
    val job3 = launch { manager.listenConversationOverviews(creator).collect { creatorOv.add(it) } }

    val job4 = launch { manager.listenConversationOverviews(other).collect { otherOv.add(it) } }

    // --- Creator envoie un message ---
    val msgCreator1 = Message("msg1", "hello", creator, other)
    manager.sendMessage(convId, msgCreator1)

    // attendre un petit peu que les flows émettent
    advanceUntilIdle()

    // Vérifications après le premier message
    assertEquals(1, creatorMsgs.last().size)
    assertEquals(1, otherMsgs.last().size)
    assertEquals(msgCreator1, creatorMsgs.last()[0])
    assertEquals(msgCreator1, otherMsgs.last()[0])

    assertEquals(1, creatorOv.last().size)
    assertEquals(1, otherOv.last().size)
    assertEquals(0, creatorOv.last()[0].nonReadMsgNumber)
    assertEquals(1, otherOv.last()[0].nonReadMsgNumber)

    // --- Receiver lit les messages ---
    manager.resetUnreadCount(convId, other)
    advanceUntilIdle()

    assertEquals(0, otherOv.last()[0].nonReadMsgNumber)

    // --- Receiver envoie un message ---
    val msgOther1 = Message("msg2", "hi", other, creator)
    manager.sendMessage(convId, msgOther1)
    advanceUntilIdle()

    // Vérifications après le deuxième message
    assertEquals(2, creatorMsgs.last().size)
    assertEquals(2, otherMsgs.last().size)
    assertEquals(msgCreator1, creatorMsgs.last()[0])
    assertEquals(msgOther1, creatorMsgs.last()[1])

    assertEquals(msgCreator1, otherMsgs.last()[0])
    assertEquals(msgOther1, otherMsgs.last()[1])

    assertEquals(1, creatorOv.last()[0].nonReadMsgNumber)
    assertEquals(0, otherOv.last()[0].nonReadMsgNumber)

    assertEquals(1, creatorOv.last().size)
    assertEquals(1, otherOv.last().size)
    assertEquals(1, creatorOv.last()[0].nonReadMsgNumber)
    assertEquals(0, otherOv.last()[0].nonReadMsgNumber)

    job.cancel()
    job2.cancel()
    job3.cancel()
    job4.cancel()
  }

  @Test
  fun testCreateAndGetConversation() = runTest {
    val convId = "convCreateTest"
    val conv = Conversation(convId)

    convRepo.createConv(conv)

    val result = manager.getConv(convId)
    assertNotNull(result)
    assertEquals(convId, result!!.convId)
    assertTrue(result.messages.isEmpty())
  }

  @Test
  fun testAddAndGetOverview() = runTest {
    convId = "conv1"
    val overview =
        OverViewConversation(
            overViewId = "id1",
            linkedConvId = "conv1",
            convName = "Conversation 1",
            overViewOwnerId = "userA",
            otherPersonId = "userB")

    ovRepo.addOverViewConvUser(overview)

    val result = manager.getOverViewConvUser("userA")
    assertTrue(result.any { it.linkedConvId == "conv1" })
  }
}
