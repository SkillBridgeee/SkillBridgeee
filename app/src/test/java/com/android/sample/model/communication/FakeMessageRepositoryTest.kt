package com.android.sample.model.communication

import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FakeMessageRepositoryTest {

  private lateinit var repository: FakeMessageRepository
  private val currentUserId = "test-user-1"
  private val otherUserId = "test-user-2"

  @Before
  fun setup() {
    repository = FakeMessageRepository(currentUserId = currentUserId)
  }

  @Test
  fun getNewUid_generatesUniqueIds() = runTest {
    val id1 = repository.getNewUid()
    val id2 = repository.getNewUid()

    assertNotNull(id1)
    assertNotNull(id2)
    assertTrue(id1.startsWith("msg"))
    assertTrue(id2.startsWith("msg"))
    assertFalse(id1 == id2)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getMessagesInConversation_throwsForBlankConversationId() = runTest {
    repository.getMessagesInConversation("")
  }

  @Test
  fun getMessage_returnsCorrectMessage() = runTest {
    val message =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = currentUserId,
            sentTo = otherUserId,
            content = "Test message")

    repository.sendMessage(message)

    val retrieved = repository.getMessage("msg1")

    assertNotNull(retrieved)
    assertEquals("Test message", retrieved?.content)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getMessage_throwsForBlankMessageId() = runTest { repository.getMessage("") }

  @Test
  fun sendMessage_addsMessageAndReturnsId() = runTest {
    val message =
        Message(
            conversationId = "conv1",
            sentFrom = currentUserId,
            sentTo = otherUserId,
            content = "Test message")

    val messageId = repository.sendMessage(message)

    assertNotNull(messageId)
    val retrieved = repository.getMessage(messageId)
    assertNotNull(retrieved)
    assertEquals("Test message", retrieved?.content)
    assertNotNull(retrieved?.sentTime)
  }

  @Test(expected = IllegalArgumentException::class)
  fun sendMessage_throwsForWrongSender() = runTest {
    val message =
        Message(
            conversationId = "conv1",
            sentFrom = "wrong-user",
            sentTo = otherUserId,
            content = "Test message")

    repository.sendMessage(message)
  }

  @Test(expected = Exception::class)
  fun markMessageAsRead_throwsForNonReceiver() = runTest {
    val message =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = currentUserId,
            sentTo = otherUserId,
            content = "Test message")

    repository.sendMessage(message)

    repository.markMessageAsRead("msg1", Timestamp.now())
  }

  @Test
  fun deleteMessage_removesMessage() = runTest {
    val message =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = currentUserId,
            sentTo = otherUserId,
            content = "Test message")

    repository.sendMessage(message)

    repository.deleteMessage("msg1")

    val deleted = repository.getMessage("msg1")
    assertNull(deleted)
  }

  @Test(expected = Exception::class)
  fun deleteMessage_throwsForNonSender() = runTest {
    val message =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = otherUserId,
            sentTo = currentUserId,
            content = "Test message")

    repository.sendMessage(message)

    repository.deleteMessage("msg1")
  }

  @Test
  fun getConversationsForUser_returnsUserConversations() = runTest {
    val conversation = repository.getOrCreateConversation(currentUserId, otherUserId)

    val conversations = repository.getConversationsForUser(currentUserId)

    assertEquals(1, conversations.size)
    assertEquals(conversation.conversationId, conversations[0].conversationId)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getConversationsForUser_throwsForWrongUser() = runTest {
    repository.getConversationsForUser("wrong-user")
  }

  @Test
  fun getOrCreateConversation_createsNewConversation() = runTest {
    val conversation = repository.getOrCreateConversation(currentUserId, otherUserId)

    assertNotNull(conversation)
    assertEquals(currentUserId, conversation.participant1Id)
    assertEquals(otherUserId, conversation.participant2Id)
  }

  @Test
  fun getOrCreateConversation_returnsExistingConversation() = runTest {
    val conversation1 = repository.getOrCreateConversation(currentUserId, otherUserId)
    val conversation2 = repository.getOrCreateConversation(currentUserId, otherUserId)

    assertEquals(conversation1.conversationId, conversation2.conversationId)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getOrCreateConversation_throwsForSameUser() = runTest {
    repository.getOrCreateConversation(currentUserId, currentUserId)
  }

  @Test
  fun deleteConversation_removesConversationAndMessages() = runTest {
    val conversation = repository.getOrCreateConversation(currentUserId, otherUserId)
    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = currentUserId,
            sentTo = otherUserId,
            content = "Test message")

    repository.sendMessage(message)

    repository.deleteConversation(conversation.conversationId)

    val deletedConversation = repository.getConversation(conversation.conversationId)
    assertNull(deletedConversation)

    val messages = repository.getMessagesInConversation(conversation.conversationId)
    assertTrue(messages.isEmpty())
  }

  @Test
  fun clear_removesAllData() = runTest {
    val conversation = repository.getOrCreateConversation(currentUserId, otherUserId)
    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = currentUserId,
            sentTo = otherUserId,
            content = "Test message")

    repository.sendMessage(message)

    repository.clear()

    val conversations = repository.getConversationsForUser(currentUserId)
    assertTrue(conversations.isEmpty())

    val messages = repository.getMessagesInConversation(conversation.conversationId)
    assertTrue(messages.isEmpty())
  }
}
