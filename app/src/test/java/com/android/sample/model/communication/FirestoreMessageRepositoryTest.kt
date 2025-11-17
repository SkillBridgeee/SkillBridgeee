package com.android.sample.model.communication

import com.android.sample.utils.FirebaseEmulator
import com.android.sample.utils.RepositoryTest
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class FirestoreMessageRepositoryTest : RepositoryTest() {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var messageRepository: MessageRepository
  private val testUser1Id = "test-user-1"
  private val testUser2Id = "test-user-2"

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    auth = mockk()
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUser1Id

    messageRepository = FirestoreMessageRepository(firestore, auth)
  }

  @After
  override fun tearDown() = runBlocking {
    // Clean up messages
    val messagesSnapshot = firestore.collection(MESSAGES_COLLECTION_PATH).get().await()
    for (document in messagesSnapshot.documents) {
      document.reference.delete().await()
    }

    // Clean up conversations
    val conversationsSnapshot = firestore.collection(CONVERSATIONS_COLLECTION_PATH).get().await()
    for (document in conversationsSnapshot.documents) {
      document.reference.delete().await()
    }

    super.tearDown()
  }

  @Test
  fun getNewUidReturnsUniqueIDs() {
    val uid1 = messageRepository.getNewUid()
    val uid2 = messageRepository.getNewUid()
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertNotEquals(uid1, uid2)
  }

  // ========== Conversation Tests ==========

  @Test
  fun getOrCreateConversationCreatesNewConversation() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    assertNotNull(conversation)
    assertEquals(
        Conversation.generateConversationId(testUser1Id, testUser2Id), conversation.conversationId)
    assertTrue(conversation.isParticipant(testUser1Id))
    assertTrue(conversation.isParticipant(testUser2Id))
    assertEquals(0, conversation.unreadCountUser1)
    assertEquals(0, conversation.unreadCountUser2)
  }

  @Test
  fun getOrCreateConversationReturnsExistingConversation() = runTest {
    val conversation1 = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val conversation2 = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    assertEquals(conversation1.conversationId, conversation2.conversationId)
  }

  @Test
  fun getOrCreateConversationWorksWithReversedOrder() = runTest {
    val conversation1 = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val conversation2 = messageRepository.getOrCreateConversation(testUser2Id, testUser1Id)

    assertEquals(conversation1.conversationId, conversation2.conversationId)
  }

  @Test
  fun getOrCreateConversationFailsWhenUserNotAuthenticated() = runTest {
    every { auth.currentUser } returns null

    assertThrows(Exception::class.java) {
      runTest { messageRepository.getOrCreateConversation(testUser1Id, testUser2Id) }
    }
  }

  @Test
  fun getOrCreateConversationFailsWhenCurrentUserNotParticipant() = runTest {
    assertThrows(Exception::class.java) {
      runTest { messageRepository.getOrCreateConversation("otherUser1", "otherUser2") }
    }
  }

  @Test
  fun getConversationReturnsCorrectConversation() = runTest {
    val created = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val retrieved = messageRepository.getConversation(created.conversationId)

    assertNotNull(retrieved)
    assertEquals(created.conversationId, retrieved!!.conversationId)
  }

  @Test
  fun getConversationReturnsNullWhenNotFound() = runTest {
    val result = messageRepository.getConversation("nonexistent-conversation")
    assertNull(result)
  }

  @Test
  fun getConversationsForUserReturnsUserConversations() = runTest {
    // Create conversations
    messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    messageRepository.getOrCreateConversation(testUser1Id, "user3")

    val conversations = messageRepository.getConversationsForUser(testUser1Id)

    assertEquals(2, conversations.size)
    assertTrue(conversations.all { it.isParticipant(testUser1Id) })
  }

  @Test
  fun getConversationsForUserReturnsEmptyListWhenNoConversations() = runTest {
    val conversations = messageRepository.getConversationsForUser(testUser1Id)
    assertEquals(0, conversations.size)
  }

  @Test
  fun getConversationsForUserFailsWhenNotCurrentUser() = runTest {
    assertThrows(Exception::class.java) {
      runTest { messageRepository.getConversationsForUser("other-user") }
    }
  }

  @Test
  fun updateConversationWorksCorrectly() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val updated = conversation.copy(lastMessageContent = "Updated message", unreadCountUser2 = 5)

    messageRepository.updateConversation(updated)

    val retrieved = messageRepository.getConversation(conversation.conversationId)
    assertNotNull(retrieved)
    assertEquals("Updated message", retrieved!!.lastMessageContent)
    assertEquals(5, retrieved.unreadCountUser2)
  }

  @Test
  fun deleteConversationRemovesConversation() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    messageRepository.deleteConversation(conversation.conversationId)

    val retrieved = messageRepository.getConversation(conversation.conversationId)
    assertNull(retrieved)
  }

  // ========== Message Tests ==========

  @Test
  fun sendMessageCreatesMessageSuccessfully() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Hello, this is a test message!")

    val messageId = messageRepository.sendMessage(message)

    assertNotNull(messageId)
    assertTrue(messageId.isNotBlank())
  }

  @Test
  fun sendMessageFailsWhenSenderNotCurrentUser() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = "wrong-user",
            sentTo = testUser2Id,
            content = "Test")

    assertThrows(Exception::class.java) { runTest { messageRepository.sendMessage(message) } }
  }

  @Test
  fun sendMessageFailsWithInvalidMessage() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "") // Empty content

    assertThrows(Exception::class.java) { runTest { messageRepository.sendMessage(message) } }
  }

  @Test
  fun getMessageReturnsCorrectMessage() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Test message")

    val messageId = messageRepository.sendMessage(message)
    val retrieved = messageRepository.getMessage(messageId)

    assertNotNull(retrieved)
    assertEquals(messageId, retrieved!!.messageId)
    assertEquals("Test message", retrieved.content)
  }

  @Test
  fun getMessageReturnsNullWhenNotFound() = runTest {
    val result = messageRepository.getMessage("nonexistent-message")
    assertNull(result)
  }

  @Test
  fun getMessagesInConversationReturnsMessages() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    // Send multiple messages
    val message1 =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "First message")
    val message2 =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Second message")

    messageRepository.sendMessage(message1)
    Thread.sleep(100) // Ensure different timestamps
    messageRepository.sendMessage(message2)

    val messages = messageRepository.getMessagesInConversation(conversation.conversationId)

    assertEquals(2, messages.size)
    assertEquals("First message", messages[0].content)
    assertEquals("Second message", messages[1].content)
  }

  @Test
  fun getMessagesInConversationReturnsEmptyListWhenNoMessages() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val messages = messageRepository.getMessagesInConversation(conversation.conversationId)

    assertEquals(0, messages.size)
  }

  @Test
  fun markMessageAsReadFailsWhenNotReceiver() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Test")

    val messageId = messageRepository.sendMessage(message)

    // Try to mark as read when current user is sender (should fail)
    assertThrows(Exception::class.java) {
      runTest { messageRepository.markMessageAsRead(messageId, Timestamp.now()) }
    }
  }

  @Test
  fun deleteMessageWorksCorrectly() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)
    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "To be deleted")

    val messageId = messageRepository.sendMessage(message)
    messageRepository.deleteMessage(messageId)

    val retrieved = messageRepository.getMessage(messageId)
    assertNull(retrieved)
  }

  @Test
  fun deleteMessageFailsWhenNotSender() = runTest {
    // Create repository for user2
    val auth2 = mockk<FirebaseAuth>()
    val mockUser2 = mockk<FirebaseUser>()
    every { auth2.currentUser } returns mockUser2
    every { mockUser2.uid } returns testUser2Id
    val messageRepo2 = FirestoreMessageRepository(firestore, auth2)

    val conversation = messageRepo2.getOrCreateConversation(testUser1Id, testUser2Id)
    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser2Id,
            sentTo = testUser1Id,
            content = "Test")

    val messageId = messageRepo2.sendMessage(message)

    // User1 tries to delete (should fail)
    assertThrows(Exception::class.java) { runTest { messageRepository.deleteMessage(messageId) } }
  }

  @Test
  fun markConversationAsReadMarksAllMessagesRead() = runTest {
    // Create repository for user2
    val auth2 = mockk<FirebaseAuth>()
    val mockUser2 = mockk<FirebaseUser>()
    every { auth2.currentUser } returns mockUser2
    every { mockUser2.uid } returns testUser2Id
    val messageRepo2 = FirestoreMessageRepository(firestore, auth2)

    val conversation = messageRepo2.getOrCreateConversation(testUser1Id, testUser2Id)

    messageRepo2.sendMessage(
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser2Id,
            sentTo = testUser1Id,
            content = "Message 1"))
    messageRepo2.sendMessage(
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser2Id,
            sentTo = testUser1Id,
            content = "Message 2"))

    // Switch to user1 to mark all as read
    messageRepository.markConversationAsRead(conversation.conversationId, testUser1Id)

    val unread =
        messageRepository.getUnreadMessagesInConversation(conversation.conversationId, testUser1Id)
    assertEquals(0, unread.size)
  }

  @Test
  fun sendMessageUpdatesConversationMetadata() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    val message =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "This updates metadata")

    messageRepository.sendMessage(message)

    // Give it a moment to update
    Thread.sleep(100)

    val updatedConv = messageRepository.getConversation(conversation.conversationId)
    assertNotNull(updatedConv)
    assertEquals("This updates metadata", updatedConv!!.lastMessageContent)
    assertEquals(testUser1Id, updatedConv.lastMessageSenderId)
  }

  @Test
  fun deleteConversationDeletesAllMessages() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    val message1 =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Message 1")
    val message2 =
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Message 2")

    messageRepository.sendMessage(message1)
    messageRepository.sendMessage(message2)

    messageRepository.deleteConversation(conversation.conversationId)

    val messages = messageRepository.getMessagesInConversation(conversation.conversationId)
    assertEquals(0, messages.size)
  }

  @Test
  fun conversationUnreadCountIncrementsWhenMessageSent() = runTest {
    val conversation = messageRepository.getOrCreateConversation(testUser1Id, testUser2Id)

    // Send message from user1 to user2
    messageRepository.sendMessage(
        Message(
            conversationId = conversation.conversationId,
            sentFrom = testUser1Id,
            sentTo = testUser2Id,
            content = "Test"))

    Thread.sleep(100) // Wait for update

    val updated = messageRepository.getConversation(conversation.conversationId)
    assertNotNull(updated)
    // The unread count for user2 should be incremented
    assertTrue(updated!!.getUnreadCountForUser(testUser2Id) > 0)
  }
}
