package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import java.util.Date
import kotlin.test.assertEquals
import org.junit.Test

class MessageTest {

  @Test
  fun `default MessageNew values`() {
    val msg = MessageNew()

    assertEquals("", msg.msgId)
    assertEquals("", msg.content)
    assertEquals("", msg.senderId)
    assertEquals("", msg.receiverId)
  }

  @Test
  fun `MessageNew initialization with parameters`() {
    val now = Date()
    val msg =
        MessageNew(
            msgId = "msg123",
            content = "Hello World",
            senderId = "user1",
            receiverId = "user2",
            createdAt = now)

    assertEquals("msg123", msg.msgId)
    assertEquals("Hello World", msg.content)
    assertEquals("user1", msg.senderId)
    assertEquals("user2", msg.receiverId)
    assertEquals(now, msg.createdAt)
  }
}
