package com.android.sample.model.communication

import java.util.Date
import org.junit.Assert.*
import org.junit.Test

class MessageTest {

  @Test
  fun `test Message creation with default values`() {
    // Default values will fail validation since sentFrom and sentTo are both empty strings
    // So we need to provide different values
    val message = Message(sentFrom = "user1", sentTo = "user2")

    assertEquals("user1", message.sentFrom)
    assertEquals("user2", message.sentTo)
    assertNotNull(message.sentTime)
    assertNull(message.receiveTime)
    assertNull(message.readTime)
    assertEquals("", message.message)
  }

  @Test
  fun `test Message creation with valid values`() {
    val sentTime = Date()
    val receiveTime = Date(sentTime.time + 1000)
    val readTime = Date(receiveTime.time + 1000)

    val message =
        Message(
            sentFrom = "user123",
            sentTo = "user456",
            sentTime = sentTime,
            receiveTime = receiveTime,
            readTime = readTime,
            message = "Hello, how are you?")

    assertEquals("user123", message.sentFrom)
    assertEquals("user456", message.sentTo)
    assertEquals(sentTime, message.sentTime)
    assertEquals(receiveTime, message.receiveTime)
    assertEquals(readTime, message.readTime)
    assertEquals("Hello, how are you?", message.message)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validation - same sender and receiver`() {
    Message(sentFrom = "user123", sentTo = "user123", message = "Test message")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validation - receive time before sent time`() {
    val sentTime = Date()
    val receiveTime = Date(sentTime.time - 1000) // 1 second before sent time

    Message(
        sentFrom = "user123",
        sentTo = "user456",
        sentTime = sentTime,
        receiveTime = receiveTime,
        message = "Test message")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validation - read time before sent time`() {
    val sentTime = Date()
    val readTime = Date(sentTime.time - 1000) // 1 second before sent time

    Message(
        sentFrom = "user123",
        sentTo = "user456",
        sentTime = sentTime,
        readTime = readTime,
        message = "Test message")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Message validation - read time before receive time`() {
    val sentTime = Date()
    val receiveTime = Date(sentTime.time + 1000)
    val readTime = Date(receiveTime.time - 500) // Before receive time

    Message(
        sentFrom = "user123",
        sentTo = "user456",
        sentTime = sentTime,
        receiveTime = receiveTime,
        readTime = readTime,
        message = "Test message")
  }

  @Test
  fun `test Message with valid time sequence`() {
    val sentTime = Date()
    val receiveTime = Date(sentTime.time + 1000)
    val readTime = Date(receiveTime.time + 500)

    val message =
        Message(
            sentFrom = "user123",
            sentTo = "user456",
            sentTime = sentTime,
            receiveTime = receiveTime,
            readTime = readTime,
            message = "Test message")

    assertTrue(message.sentTime.before(message.receiveTime))
    assertTrue(message.receiveTime!!.before(message.readTime))
  }

  @Test
  fun `test Message with only sent time`() {
    val message = Message(sentFrom = "user123", sentTo = "user456", message = "Test message")

    assertNotNull(message.sentTime)
    assertNull(message.receiveTime)
    assertNull(message.readTime)
  }

  @Test
  fun `test Message with sent and receive time only`() {
    val sentTime = Date()
    val receiveTime = Date(sentTime.time + 1000)

    val message =
        Message(
            sentFrom = "user123",
            sentTo = "user456",
            sentTime = sentTime,
            receiveTime = receiveTime,
            message = "Test message")

    assertEquals(sentTime, message.sentTime)
    assertEquals(receiveTime, message.receiveTime)
    assertNull(message.readTime)
  }

  @Test
  fun `test Message equality and hashCode`() {
    val sentTime = Date()
    val message1 =
        Message(
            sentFrom = "user123", sentTo = "user456", sentTime = sentTime, message = "Test message")

    val message2 =
        Message(
            sentFrom = "user123", sentTo = "user456", sentTime = sentTime, message = "Test message")

    assertEquals(message1, message2)
    assertEquals(message1.hashCode(), message2.hashCode())
  }

  @Test
  fun `test Message copy functionality`() {
    val originalMessage =
        Message(sentFrom = "user123", sentTo = "user456", message = "Original message")

    val readTime = Date()
    val updatedMessage = originalMessage.copy(readTime = readTime, message = "Updated message")

    assertEquals("user123", updatedMessage.sentFrom)
    assertEquals("user456", updatedMessage.sentTo)
    assertEquals(readTime, updatedMessage.readTime)
    assertEquals("Updated message", updatedMessage.message)

    assertNotEquals(originalMessage, updatedMessage)
  }
}
