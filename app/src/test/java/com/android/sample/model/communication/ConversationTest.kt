package com.android.sample.model.communication

import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConversationTest {

  @Test
  fun `default ConversationNew values`() {
    val conv = ConversationNew()

      assertEquals("", conv.convId)
      assertEquals("", conv.convCreatorId)
      assertEquals("", conv.otherPersonId)
      assertEquals("", conv.convName)
      assertTrue(conv.messages.isEmpty())
  }

  @Test
  fun `ConversationNew initialization with parameters`() {
    val msg1 = MessageNew(msgId = "1", content = "Hello", senderId = "user1", receiverId = "user2")
    val msg2 = MessageNew(msgId = "2", content = "World", senderId = "user2", receiverId = "user1")

    val conv =
        ConversationNew(
            convId = "conv123",
            convCreatorId = "user1",
            otherPersonId = "user2",
            convName = "Test Chat",
            messages = listOf(msg1, msg2)
        )

      assertEquals("conv123", conv.convId)
      assertEquals("user1", conv.convCreatorId)
      assertEquals("user2", conv.otherPersonId)
      assertEquals("Test Chat", conv.convName)
      assertEquals(2, conv.messages.size)
      assertEquals("Hello", conv.messages[0].content)
      assertEquals("World", conv.messages[1].content)
  }
}