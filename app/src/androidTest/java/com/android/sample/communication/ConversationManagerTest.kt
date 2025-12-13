package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.booking.FirestoreBookingRepository
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
    manager = ConversationManager(convRepo, ovRepo, null)
  }

  @After fun tearDown() = runTest {}

  // ----------------------------------------------------------
  // TEST 1 : CREATE CONV + OVERVIEWS (NEW CONVERSATION)
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
    assertTrue(conv.messages.isEmpty())

    val ovA = ovRepo.getOverViewConvUser(creator).filter { it.linkedConvId == convId }
    val ovB = ovRepo.getOverViewConvUser(other).filter { it.linkedConvId == convId }

    assertEquals(1, ovA.size)
    assertEquals(1, ovB.size)

    val ovCreator = ovA.first()
    val ovOther = ovB.first()

    assertEquals(convId, ovCreator.linkedConvId)
    assertEquals(convId, ovOther.linkedConvId)
    // lastMsg is default Message() on creation
    assertNotNull(ovCreator.lastMsg)
    assertNotNull(ovOther.lastMsg)
    assertEquals(0, ovCreator.nonReadMsgNumber)
    assertEquals(0, ovOther.nonReadMsgNumber)
  }

  // ----------------------------------------------------------
  // TEST 1B : CREATE CONV REUSES EXISTING CONV BETWEEN SAME USERS
  // ----------------------------------------------------------
  @Test
  fun testCreateConvAndOverviews_existingConversationReturnsSameId() = runTest {
    val creator = "UserA-test1b"
    val other = "UserB-test1b"
    val convName = "Chat1"

    val firstId = manager.createConvAndOverviews(creator, other, convName)
    val secondId = manager.createConvAndOverviews(creator, other, "AnotherNameShouldBeIgnored")

    assertEquals(firstId, secondId)

    // Still only one overview per user for that conversation
    val ovA = ovRepo.getOverViewConvUser(creator)
    val ovB = ovRepo.getOverViewConvUser(other)

    assertEquals(1, ovA.size)
    assertEquals(1, ovB.size)
  }

  // ----------------------------------------------------------
  // TEST 2 : DELETE CONV + MANAGE OVERVIEWS ACCORDING TO NEW LOGIC
  // ----------------------------------------------------------
  @Test
  fun testDeleteConvAndOverviews() = runTest {
    val unique = System.currentTimeMillis().toString()

    val creator = "A-test9-$unique"
    val other = "B-test9-$unique"

    convId = manager.createConvAndOverviews(creator, other, "Chat")

    assertNotNull(convRepo.getConv(convId))
    assertEquals(1, ovRepo.getOverViewConvUser(creator).size)
    assertEquals(1, ovRepo.getOverViewConvUser(other).size)

    manager.deleteConvAndOverviews(convId, creator, other)

    // Conversation must be deleted
    assertNull(convRepo.getConv(convId))

    // Deleter's overview(s) for that conv must be deleted
    val creatorOverviews = ovRepo.getOverViewConvUser(creator)
    assertTrue(creatorOverviews.none { it.linkedConvId == convId })

    // Other user's overview should be updated with system "deleted" message
    val otherOverviews = ovRepo.getOverViewConvUser(other)
    // There may be one overview for this conversation
    val otherOverview = otherOverviews.firstOrNull { it.linkedConvId == convId }
    assertNotNull(otherOverview)
    assertEquals("deleted-system-msg", otherOverview!!.lastMsg?.msgId)
    assertEquals("system", otherOverview.lastMsg?.senderId)
    assertEquals(other, otherOverview.lastMsg?.receiverId)
    assertEquals("Conversation deleted", otherOverview.lastMsg?.content)
    assertEquals(0, otherOverview.nonReadMsgNumber)
  }

  // ----------------------------------------------------------
  // TEST 3 : SEND MESSAGE + UPDATE OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testSendMessage() = runTest {
    val unique = System.currentTimeMillis().toString()

    val creator = "A-test9-$unique"
    val other = "B-test9-$unique"

    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val msg = Message(msgId = "1-test3", content = "Hello", senderId = creator, receiverId = other)

    manager.sendMessage(convId, msg)

    val ovA = ovRepo.getOverViewConvUser(creator).first { it.linkedConvId == convId }
    val ovB = ovRepo.getOverViewConvUser(other).first { it.linkedConvId == convId }

    // sender overview: lastMsg updated, unread count NOT incremented
    assertEquals(msg.msgId, ovA.lastMsg!!.msgId)
    assertEquals(0, ovA.nonReadMsgNumber)

    // receiver overview: lastMsg updated, unread count incremented
    assertEquals(msg.msgId, ovB.lastMsg!!.msgId)
    assertEquals(msg.content, ovB.lastMsg!!.content)
    assertEquals(1, ovB.nonReadMsgNumber)
  }

  // ----------------------------------------------------------
  // TEST 4 : RESET UNREAD COUNT
  // ----------------------------------------------------------
  @Test
  fun testResetUnreadCount() = runTest {
    val unique = System.currentTimeMillis().toString()

    val creator = "A-test9-$unique"
    val other = "B-test9-$unique"

    convId = manager.createConvAndOverviews(creator, other, "Chat")

    val msg = Message("1-test4", "Hello", creator, other)
    manager.sendMessage(convId, msg)

    // unread for receiver should be 1
    val overviewBeforeReset = ovRepo.getOverViewConvUser(other).first()
    assertEquals(1, overviewBeforeReset.nonReadMsgNumber)
    assertEquals(msg, overviewBeforeReset.lastMsg)

    // Reset unread count for receiver
    manager.resetUnreadCount(convId, other)

    val overviewAfterReset = ovRepo.getOverViewConvUser(other).first()
    assertEquals(0, overviewAfterReset.nonReadMsgNumber)
    // lastMsg should stay the same
    assertEquals(msg, overviewAfterReset.lastMsg)
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

  // ----------------------------------------------------------
  // TEST 8 : DISCUSSION (STEP-BY-STEP, USING first())
  // ----------------------------------------------------------
  @Test
  fun testAllDiscussion() = runTest {
    val unique = System.currentTimeMillis().toString()

    val creator = "A-test9-$unique"
    val other = "B-test9-$unique"

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

    // Receiver reads the message
    manager.resetUnreadCount(convId, other)
    emittedOtherOverView = flowOtherOverView.first { it.isNotEmpty() }

    assertEquals(1, emittedOtherOverView.size)
    assertEquals(0, emittedOtherOverView[0].nonReadMsgNumber)

    // Receiver sends a message back
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

  // ----------------------------------------------------------
  // TEST 9 : DISCUSSION REAL-TIME WITH COLLECT + advanceUntilIdle
  // ----------------------------------------------------------
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testAllDiscussionRealTime() = runTest {
    val unique = System.currentTimeMillis().toString()

    val creator = "A-test9-$unique"
    val other = "B-test9-$unique"

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

  // ----------------------------------------------------------
  // TEST 10 : CREATE AND GET CONVERSATION
  // ----------------------------------------------------------
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

  // ----------------------------------------------------------
  // TEST 11 : ADD AND GET OVERVIEW
  // ----------------------------------------------------------
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

  // ----------------------------------------------------------
  // TEST 12 : GET MESSAGE NEW UID
  // ----------------------------------------------------------
  @Test
  fun testGetMessageNewUid() {
    val uid1 = manager.getMessageNewUid()
    val uid2 = manager.getMessageNewUid()
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertTrue(uid1.isNotEmpty())
    assertTrue(uid2.isNotEmpty())
    // Most implementations will generate different IDs
    assertNotSame(uid1, uid2)
  }
}
