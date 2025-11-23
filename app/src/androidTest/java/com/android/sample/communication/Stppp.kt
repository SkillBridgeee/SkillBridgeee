package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.FirestoreConvRepository
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.utils.TestFirestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Stppp {

  private lateinit var repo: FirestoreConvRepository

  @Before
  fun setup() {
    // Initialise et connecte Firestore à l’émulateur
    val db = TestFirestore.db

    repo = FirestoreConvRepository(db)
  }

  @Test
  fun testSendMessage() = runTest {
    val convId = "testConv"
    val conv = ConversationNew(convId)
    repo.createConv(conv)

    val pre = repo.getConv(convId)
    assertNotNull(pre)

    val msg =
        MessageNew(
            msgId = "1",
            content = "Hello World",
            senderId = "A",
            receiverId = "B",
        )

    repo.sendMessage(convId, msg)

    val result = repo.getConv(convId)
    assertNotNull(result)

    val returnedMsg = result!!.messages.first()

    assertEquals(msg.msgId, returnedMsg.msgId)
    assertEquals(msg.content, returnedMsg.content)
    assertEquals(msg.senderId, returnedMsg.senderId)
    assertEquals(msg.receiverId, returnedMsg.receiverId)
    assertEquals(msg.createdAt, returnedMsg.createdAt)
  }
}
