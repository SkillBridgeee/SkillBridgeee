package com.android.sample.model.communication

import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test

class ConversationTest {

  @Test
  fun `test Conversation no-arg constructor`() {
    val conversation = Conversation()

    assertEquals("", conversation.conversationId)
    assertEquals("", conversation.participant1Id)
    assertEquals("", conversation.participant2Id)
    assertEquals("", conversation.lastMessageContent)
    assertNull(conversation.lastMessageTime)
    assertEquals("", conversation.lastMessageSenderId)
    assertEquals(0, conversation.unreadCountUser1)
    assertEquals(0, conversation.unreadCountUser2)
    assertNull(conversation.createdAt)
    assertNull(conversation.updatedAt)
  }

  @Test
  fun `test Conversation creation with valid values`() {
    val now = Timestamp.now()
    val conversation =
        Conversation(
            conversationId = "user1_user2",
            participant1Id = "user1",
            participant2Id = "user2",
            lastMessageContent = "Hello!",
            lastMessageTime = now,
            lastMessageSenderId = "user1",
            unreadCountUser1 = 0,
            unreadCountUser2 = 3,
            createdAt = now,
            updatedAt = now)

    assertEquals("user1_user2", conversation.conversationId)
    assertEquals("user1", conversation.participant1Id)
    assertEquals("user2", conversation.participant2Id)
    assertEquals("Hello!", conversation.lastMessageContent)
    assertEquals(now, conversation.lastMessageTime)
    assertEquals("user1", conversation.lastMessageSenderId)
    assertEquals(0, conversation.unreadCountUser1)
    assertEquals(3, conversation.unreadCountUser2)
    assertEquals(now, conversation.createdAt)
    assertEquals(now, conversation.updatedAt)
  }

  @Test
  fun `test Conversation validate passes with valid data`() {
    val conversation =
        Conversation(
            conversationId = "conv123",
            participant1Id = "user1",
            participant2Id = "user2",
            unreadCountUser1 = 0,
            unreadCountUser2 = 5)

    // Should not throw
    conversation.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Conversation validate fails when participant1Id is blank`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "", participant2Id = "user2")

    conversation.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Conversation validate fails when participant2Id is blank`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "user1", participant2Id = "")

    conversation.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Conversation validate fails when participants are same`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "user1", participant2Id = "user1")

    conversation.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Conversation validate fails when unreadCountUser1 is negative`() {
    val conversation =
        Conversation(
            conversationId = "conv123",
            participant1Id = "user1",
            participant2Id = "user2",
            unreadCountUser1 = -1)

    conversation.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Conversation validate fails when unreadCountUser2 is negative`() {
    val conversation =
        Conversation(
            conversationId = "conv123",
            participant1Id = "user1",
            participant2Id = "user2",
            unreadCountUser2 = -5)

    conversation.validate()
  }

  @Test
  fun `test getOtherParticipantId returns correct participant`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "alice", participant2Id = "bob")

    assertEquals("bob", conversation.getOtherParticipantId("alice"))
    assertEquals("alice", conversation.getOtherParticipantId("bob"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test getOtherParticipantId throws when user is not a participant`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "alice", participant2Id = "bob")

    conversation.getOtherParticipantId("charlie")
  }

  @Test
  fun `test getUnreadCountForUser returns correct count`() {
    val conversation =
        Conversation(
            conversationId = "conv123",
            participant1Id = "user1",
            participant2Id = "user2",
            unreadCountUser1 = 3,
            unreadCountUser2 = 7)

    assertEquals(3, conversation.getUnreadCountForUser("user1"))
    assertEquals(7, conversation.getUnreadCountForUser("user2"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test getUnreadCountForUser throws when user is not a participant`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "user1", participant2Id = "user2")

    conversation.getUnreadCountForUser("user3")
  }

  @Test
  fun `test isParticipant returns true for participants`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "alice", participant2Id = "bob")

    assertTrue(conversation.isParticipant("alice"))
    assertTrue(conversation.isParticipant("bob"))
  }

  @Test
  fun `test isParticipant returns false for non-participants`() {
    val conversation =
        Conversation(conversationId = "conv123", participant1Id = "alice", participant2Id = "bob")

    assertFalse(conversation.isParticipant("charlie"))
    assertFalse(conversation.isParticipant(""))
  }

  @Test
  fun `test generateConversationId creates consistent IDs`() {
    val id1 = Conversation.generateConversationId("user1", "user2")
    val id2 = Conversation.generateConversationId("user2", "user1")

    assertEquals(id1, id2)
    assertEquals("user1_user2", id1)
  }

  @Test
  fun `test generateConversationId sorts participants alphabetically`() {
    val id1 = Conversation.generateConversationId("zebra", "apple")
    assertEquals("apple_zebra", id1)

    val id2 = Conversation.generateConversationId("bob", "alice")
    assertEquals("alice_bob", id2)
  }

  @Test
  fun `test Conversation copy works correctly`() {
    val original =
        Conversation(
            conversationId = "conv1",
            participant1Id = "user1",
            participant2Id = "user2",
            lastMessageContent = "Original",
            unreadCountUser1 = 5,
            unreadCountUser2 = 0)

    val modified = original.copy(lastMessageContent = "Modified", unreadCountUser1 = 0)

    assertEquals("Modified", modified.lastMessageContent)
    assertEquals(0, modified.unreadCountUser1)
    assertEquals("conv1", modified.conversationId)
    assertEquals("user2", modified.participant2Id)
  }

  @Test
  fun `test Conversation equality`() {
    val conv1 =
        Conversation(
            conversationId = "conv123",
            participant1Id = "user1",
            participant2Id = "user2",
            lastMessageContent = "Hello")

    val conv2 =
        Conversation(
            conversationId = "conv123",
            participant1Id = "user1",
            participant2Id = "user2",
            lastMessageContent = "Hello")

    assertEquals(conv1, conv2)
  }

  @Test
  fun `test Conversation with different unread counts`() {
    val conversation =
        Conversation(
            conversationId = "conv1",
            participant1Id = "user1",
            participant2Id = "user2",
            unreadCountUser1 = 10,
            unreadCountUser2 = 0)

    assertEquals(10, conversation.unreadCountUser1)
    assertEquals(0, conversation.unreadCountUser2)
  }

  @Test
  fun `test Conversation with empty lastMessageContent`() {
    val conversation =
        Conversation(
            conversationId = "conv1",
            participant1Id = "user1",
            participant2Id = "user2",
            lastMessageContent = "")

    assertEquals("", conversation.lastMessageContent)
    conversation.validate() // Should not throw
  }

  @Test
  fun `test Conversation with null timestamps`() {
    val conversation =
        Conversation(
            conversationId = "conv1",
            participant1Id = "user1",
            participant2Id = "user2",
            lastMessageTime = null,
            createdAt = null,
            updatedAt = null)

    assertNull(conversation.lastMessageTime)
    assertNull(conversation.createdAt)
    assertNull(conversation.updatedAt)
  }
}
