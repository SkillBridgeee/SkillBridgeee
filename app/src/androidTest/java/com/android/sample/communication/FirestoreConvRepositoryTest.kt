package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.conversation.BLANK_CONVID_ERR_MSG
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.FirestoreConvRepository
import com.android.sample.model.communication.conversation.Message
import com.android.sample.utils.TestFirestore
import java.util.UUID
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreConvRepositoryTest {

  private lateinit var repo: FirestoreConvRepository

  @Before
  fun setup() {
    repo = FirestoreConvRepository(TestFirestore.db)
  }

  // ----------------------------------------------------------
  // TEST 1 : CREATE + GET CONVERSATION
  // ----------------------------------------------------------
  @Test
  fun testCreateAndGetConversation() = runTest {
    val convId = "convCreateTest"
    val conv = Conversation(convId)

    repo.createConv(conv)

    val result = repo.getConv(convId)
    assertNotNull(result)
    assertEquals(convId, result!!.convId)
    assertTrue(result.messages.isEmpty())
  }

  // ----------------------------------------------------------
  // TEST 2 : SEND MESSAGE + GET CONVERSATION
  // ----------------------------------------------------------
  @Test
  fun testSendMessage() = runTest {
    val convId = "convSendTest"
    val conv = Conversation(convId)
    repo.createConv(conv)

    val msg = Message(msgId = "1", content = "Hello World", senderId = "A", receiverId = "B")

    repo.sendMessage(convId, msg)

    val result = repo.getConv(convId)
    assertNotNull(result)
    assertEquals(1, result!!.messages.size)

    val returnedMsg = result.messages.first()

    // Vérification complète
    assertEquals(msg.msgId, returnedMsg.msgId)
    assertEquals(msg.content, returnedMsg.content)
    assertEquals(msg.senderId, returnedMsg.senderId)
    assertEquals(msg.receiverId, returnedMsg.receiverId)
    assertEquals(msg.createdAt, returnedMsg.createdAt)
  }

  // ----------------------------------------------------------
  // TEST 3 : DELETE CONVERSATION
  // ----------------------------------------------------------
  @Test
  fun testDeleteConversation() = runTest {
    val convId = "convDeleteTest"
    val conv = Conversation(convId)

    repo.createConv(conv)
    assertNotNull(repo.getConv(convId))

    repo.deleteConv(convId)

    val result = repo.getConv(convId)
    assertNull(result)
  }

  // ----------------------------------------------------------
  // TEST 4 : LISTEN MESSAGES (Flow)
  // ----------------------------------------------------------
  @Test
  fun testListenMessages() = runTest {
    val convId = "convListenTest"
    val conv = Conversation(convId)
    repo.createConv(conv)

    val collectJob = repo.listenMessages(convId)

    // send message
    val msg = Message(msgId = "42", content = "Listening works!", senderId = "A", receiverId = "B")
    repo.sendMessage(convId, msg)

    val emittedMessages = collectJob.first { it.isNotEmpty() }

    val returnedMsg = emittedMessages.first()

    assertEquals(msg.msgId, returnedMsg.msgId)
    assertEquals(msg.content, returnedMsg.content)
    assertEquals(msg.senderId, returnedMsg.senderId)
    assertEquals(msg.receiverId, returnedMsg.receiverId)
  }

  @Test
  fun testListenMessagesTwoUsers() = runTest {
    val convId = "convListenTwoUsers"
    val conv = Conversation(convId)
    repo.createConv(conv)

    val flow = repo.listenMessages(convId)

    // 1er message (user A)
    val msg1 = Message(msgId = "1", content = "Hello, it's A", senderId = "A", receiverId = "B")
    repo.sendMessage(convId, msg1)

    // 2e message (user B)
    val msg2 = Message(msgId = "2", content = "Hi A, it's B", senderId = "B", receiverId = "A")
    repo.sendMessage(convId, msg2)

    // On attend que le flow émette une liste contenant les 2 messages
    val emittedMessages = flow.first { it.size == 2 }

    assertEquals(2, emittedMessages.size)

    val returned1 = emittedMessages[0]
    val returned2 = emittedMessages[1]

    // Vérifier msg1
    assertEquals(msg1.msgId, returned1.msgId)
    assertEquals(msg1.content, returned1.content)
    assertEquals(msg1.senderId, returned1.senderId)
    assertEquals(msg1.receiverId, returned1.receiverId)

    // Vérifier msg2
    assertEquals(msg2.msgId, returned2.msgId)
    assertEquals(msg2.content, returned2.content)
    assertEquals(msg2.senderId, returned2.senderId)
    assertEquals(msg2.receiverId, returned2.receiverId)
  }

  @Test
  fun testGetNewUid() = runTest {
    val id1 = repo.getNewUid()
    val id2 = repo.getNewUid()

    assertTrue("ID1 should not be blank", id1.isNotBlank())
    assertTrue("ID2 should not be blank", id2.isNotBlank())

    assertNotSame("Each generated UID must be unique", id1, id2)

    try {
      UUID.fromString(id1)
      UUID.fromString(id2)
    } catch (e: Exception) {
      fail("Generated IDs are not valid UUIDs: $e")
    }
  }

  @Test
  fun testListenMessagesInvalidConvId() = runTest {
    val invalidConvId = ""

    try {
      repo.listenMessages(invalidConvId).first()
      fail("Expected IllegalArgumentException but no exception was thrown.")
    } catch (e: IllegalArgumentException) {
      assertEquals(BLANK_CONVID_ERR_MSG, e.message)
    }
  }
}
