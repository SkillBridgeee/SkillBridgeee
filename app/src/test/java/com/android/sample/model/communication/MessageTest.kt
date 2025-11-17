package com.android.sample.model.communication

import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test

class MessageTest {

  @Test
  fun `test Message no-arg constructor`() {
    val message = Message()

    assertEquals("", message.messageId)
    assertEquals("", message.conversationId)
    assertEquals("", message.sentFrom)
    assertEquals("", message.sentTo)
    assertNull(message.sentTime)
    assertNull(message.receiveTime)
    assertNull(message.readTime)
    assertEquals("", message.content)
    assertFalse(message.isRead)
  }

  @Test
  fun `test Message creation with valid values`() {
    val sentTime = Timestamp.now()
    val receiveTime = Timestamp(sentTime.seconds + 1, sentTime.nanoseconds)
    val readTime = Timestamp(receiveTime.seconds + 1, receiveTime.nanoseconds)

    val message =
        Message(
            messageId = "msg123",
            conversationId = "conv456",
            sentFrom = "user123",
            sentTo = "user456",
            sentTime = sentTime,
            receiveTime = receiveTime,
            readTime = readTime,
            content = "Hello, how are you?",
            isRead = true)

    assertEquals("msg123", message.messageId)
    assertEquals("conv456", message.conversationId)
    assertEquals("user123", message.sentFrom)
    assertEquals("user456", message.sentTo)
    assertEquals(sentTime, message.sentTime)
    assertEquals(receiveTime, message.receiveTime)
    assertEquals(readTime, message.readTime)
    assertEquals("Hello, how are you?", message.content)
    assertTrue(message.isRead)
  }

  @Test
  fun `test Message creation with minimal values`() {
    val message =
        Message(
            conversationId = "conv123",
            sentFrom = "user1",
            sentTo = "user2",
            content = "Test message")

    assertEquals("conv123", message.conversationId)
    assertEquals("user1", message.sentFrom)
    assertEquals("user2", message.sentTo)
    assertEquals("Test message", message.content)
    assertFalse(message.isRead)
  }

  @Test
  fun `test Message validate passes with valid data`() {
    val message =
        Message(
            conversationId = "conv123",
            sentFrom = "user1",
            sentTo = "user2",
            content = "Valid message")

    // Should not throw
    message.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validate fails when sentFrom is blank`() {
    val message =
        Message(conversationId = "conv123", sentFrom = "", sentTo = "user2", content = "Test")

    message.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validate fails when sentTo is blank`() {
    val message =
        Message(conversationId = "conv123", sentFrom = "user1", sentTo = "", content = "Test")

    message.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validate fails when sender and receiver are same`() {
    val message =
        Message(
            conversationId = "conv123",
            sentFrom = "user123",
            sentTo = "user123",
            content = "Test message")

    message.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validate fails when conversationId is blank`() {
    val message =
        Message(conversationId = "", sentFrom = "user1", sentTo = "user2", content = "Test")

    message.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validate fails when content is blank`() {
    val message =
        Message(conversationId = "conv123", sentFrom = "user1", sentTo = "user2", content = "")

    message.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validate fails when content is whitespace only`() {
    val message =
        Message(conversationId = "conv123", sentFrom = "user1", sentTo = "user2", content = "   ")

    message.validate()
  }

  @Test
  fun `test Message with null timestamps`() {
    val message =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = "user1",
            sentTo = "user2",
            sentTime = null,
            receiveTime = null,
            readTime = null,
            content = "Test",
            isRead = false)

    assertNull(message.sentTime)
    assertNull(message.receiveTime)
    assertNull(message.readTime)
  }

  @Test
  fun `test Message isRead flag`() {
    val message1 =
        Message(conversationId = "conv1", sentFrom = "u1", sentTo = "u2", content = "Test")
    assertFalse(message1.isRead)

    val message2 =
        Message(
            conversationId = "conv1",
            sentFrom = "u1",
            sentTo = "u2",
            content = "Test",
            isRead = true)
    assertTrue(message2.isRead)
  }

  @Test
  fun `test Message copy with different values`() {
    val original =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = "user1",
            sentTo = "user2",
            content = "Original",
            isRead = false)

    val copy = original.copy(content = "Modified", isRead = true)

    assertEquals("msg1", copy.messageId)
    assertEquals("Modified", copy.content)
    assertTrue(copy.isRead)
  }

  @Test
  fun `test Message equality`() {
    val message1 =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = "user1",
            sentTo = "user2",
            content = "Test")

    val message2 =
        Message(
            messageId = "msg1",
            conversationId = "conv1",
            sentFrom = "user1",
            sentTo = "user2",
            content = "Test")

    assertEquals(message1, message2)
  }
}
