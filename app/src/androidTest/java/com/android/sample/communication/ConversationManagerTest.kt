package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.FirestoreConvRepository
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.utils.TestFirestore
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationManagerTest {

  private lateinit var convRepo: FirestoreConvRepository
  private lateinit var ovRepo: FirestoreOverViewConvRepository
  private lateinit var manager: ConversationManager

  @Before
  fun setup() {
    convRepo = FirestoreConvRepository(TestFirestore.db)
    ovRepo = FirestoreOverViewConvRepository(TestFirestore.db)
    manager = ConversationManager(convRepo, ovRepo)
  }

  // ----------------------------------------------------------
  // TEST 1 : CREATE CONV + OVERVIEWS
  // ----------------------------------------------------------
  @Test
  fun testCreateConvAndOverviews() = runTest {
    val creator = "UserA-test1"
    val other = "UserB-test1"
    val convName = "TestChat"

    val convId = manager.createConvAndOverviews(creator, other, convName)

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

    val convId = manager.createConvAndOverviews(creator, other, "Chat")

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

    val convId = manager.createConvAndOverviews(creator, other, "Chat")

    val msg =
        MessageNew(msgId = "1-test3", content = "Hello", senderId = creator, receiverId = other)

    manager.sendMessage(convId, msg)

    val conv = convRepo.getConv(convId)
    assertEquals(1, conv!!.messages.size)
    assertEquals(msg.msgId, conv.messages.first().msgId)

    val ovA = ovRepo.getOverViewConvUser(creator).first()
    val ovB = ovRepo.getOverViewConvUser(other).first()

    assertEquals(msg.msgId, ovA.lastMsg.msgId)
    assertEquals(msg.content, ovB.lastMsg.content)
  }

  // ----------------------------------------------------------
  // TEST 4 : RESET UNREAD COUNT
  // ----------------------------------------------------------
  @Test
  fun testResetUnreadCount() = runTest {
    val creator = "A-test4"
    val other = "B-test4"
    val convId = manager.createConvAndOverviews(creator, other, "Chat")

    val msg = MessageNew("1-test4", "Hello", creator, other)
    manager.sendMessage(convId, msg)

    manager.resetUnreadCount(convId, creator)

    val overview = ovRepo.getOverViewConvUser(creator).first()

    assertEquals("1-test4", overview.lastReadMessageId)
    assertEquals(0, overview.nonReadMsgNumber)
  }

  // ----------------------------------------------------------
  // TEST 5 : CALCULATE UNREAD COUNT
  // ----------------------------------------------------------
  @Test
  fun testCalculateUnreadCount() = runTest {
    val creator = "A-test5"
    val other = "B-test5"

    val convId = manager.createConvAndOverviews(creator, other, "Chat")

    val m1 = MessageNew("1-test5", "Msg1", creator, other)
    val m2 = MessageNew("2-test5", "Msg2", creator, other)
    val m3 = MessageNew("3-test5", "Msg3", creator, other)

    manager.sendMessage(convId, m1)
    manager.sendMessage(convId, m2)
    manager.sendMessage(convId, m3)

    // User that send shouldn't have unseen message
    val unread = manager.calculateUnreadCount(convId, creator)
    assertEquals(0, unread)

    val unreadForB = manager.calculateUnreadCount(convId, other)
    assertEquals(3, unreadForB)
  }

  // ----------------------------------------------------------
  // TEST 6 : LISTEN MESSAGES
  // ----------------------------------------------------------
  @Test
  fun testListenMessages() = runTest {
    val creator = "A-test6"
    val other = "B-test6"
    val convId = manager.createConvAndOverviews(creator, other, "Chat")

    val flow = manager.listenMessages(convId)

    val msg = MessageNew("1-test6", "Listening OK", creator, other)
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
    val convId = manager.createConvAndOverviews(creator, other, "Chat")

    val flow = manager.listenConversationOverviews(creator)

    val msg = MessageNew("1-test7", "Yo", creator, other)
    manager.sendMessage(convId, msg)

    val emitted = flow.first { it.isNotEmpty() }

    assertEquals(1, emitted.size)
    assertEquals("1-test7", emitted.first().lastMsg.msgId)
  }
}
