package com.android.sample.model.communication

import org.junit.Assert.*
import org.junit.Test

class NotificationTest {

  @Test
  fun `test Notification creation with default values`() {
    // This will fail validation, so we need to provide valid values
    try {
      val notification = Notification()
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(
          e.message!!.contains("User ID cannot be blank") ||
              e.message!!.contains("Notification message cannot be blank"))
    }
  }

  @Test
  fun `test Notification creation with valid values`() {
    val notification =
        Notification(
            userId = "user123",
            notificationType = NotificationType.BOOKING_REQUEST,
            notificationMessage = "You have a new booking request")

    assertEquals("user123", notification.userId)
    assertEquals(NotificationType.BOOKING_REQUEST, notification.notificationType)
    assertEquals("You have a new booking request", notification.notificationMessage)
  }

  @Test
  fun `test all NotificationType enum values`() {
    val notification1 = Notification("user1", NotificationType.BOOKING_REQUEST, "Message 1")
    val notification2 = Notification("user2", NotificationType.BOOKING_CONFIRMED, "Message 2")
    val notification3 = Notification("user3", NotificationType.BOOKING_CANCELLED, "Message 3")
    val notification4 = Notification("user4", NotificationType.MESSAGE_RECEIVED, "Message 4")
    val notification5 = Notification("user5", NotificationType.RATING_RECEIVED, "Message 5")
    val notification6 = Notification("user6", NotificationType.SYSTEM_UPDATE, "Message 6")
    val notification7 = Notification("user7", NotificationType.REMINDER, "Message 7")

    assertEquals(NotificationType.BOOKING_REQUEST, notification1.notificationType)
    assertEquals(NotificationType.BOOKING_CONFIRMED, notification2.notificationType)
    assertEquals(NotificationType.BOOKING_CANCELLED, notification3.notificationType)
    assertEquals(NotificationType.MESSAGE_RECEIVED, notification4.notificationType)
    assertEquals(NotificationType.RATING_RECEIVED, notification5.notificationType)
    assertEquals(NotificationType.SYSTEM_UPDATE, notification6.notificationType)
    assertEquals(NotificationType.REMINDER, notification7.notificationType)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Notification validation - blank userId`() {
    Notification(
        userId = "",
        notificationType = NotificationType.SYSTEM_UPDATE,
        notificationMessage = "Valid message")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Notification validation - blank message`() {
    Notification(
        userId = "user123",
        notificationType = NotificationType.SYSTEM_UPDATE,
        notificationMessage = "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Notification validation - whitespace only userId`() {
    Notification(
        userId = "   ",
        notificationType = NotificationType.SYSTEM_UPDATE,
        notificationMessage = "Valid message")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Notification validation - whitespace only message`() {
    Notification(
        userId = "user123",
        notificationType = NotificationType.SYSTEM_UPDATE,
        notificationMessage = "   ")
  }

  @Test
  fun `test Notification equality and hashCode`() {
    val notification1 =
        Notification(
            userId = "user123",
            notificationType = NotificationType.BOOKING_REQUEST,
            notificationMessage = "Test message")

    val notification2 =
        Notification(
            userId = "user123",
            notificationType = NotificationType.BOOKING_REQUEST,
            notificationMessage = "Test message")

    assertEquals(notification1, notification2)
    assertEquals(notification1.hashCode(), notification2.hashCode())
  }

  @Test
  fun `test Notification copy functionality`() {
    val originalNotification =
        Notification(
            userId = "user123",
            notificationType = NotificationType.BOOKING_REQUEST,
            notificationMessage = "Original message")

    val copiedNotification =
        originalNotification.copy(
            notificationType = NotificationType.BOOKING_CONFIRMED,
            notificationMessage = "Updated message")

    assertEquals("user123", copiedNotification.userId)
    assertEquals(NotificationType.BOOKING_CONFIRMED, copiedNotification.notificationType)
    assertEquals("Updated message", copiedNotification.notificationMessage)

    assertNotEquals(originalNotification, copiedNotification)
  }

  @Test
  fun `test NotificationType enum properties`() {
    val allTypes = NotificationType.values()
    assertEquals(7, allTypes.size)

    // Test enum names
    assertEquals("BOOKING_REQUEST", NotificationType.BOOKING_REQUEST.name)
    assertEquals("BOOKING_CONFIRMED", NotificationType.BOOKING_CONFIRMED.name)
    assertEquals("BOOKING_CANCELLED", NotificationType.BOOKING_CANCELLED.name)
    assertEquals("MESSAGE_RECEIVED", NotificationType.MESSAGE_RECEIVED.name)
    assertEquals("RATING_RECEIVED", NotificationType.RATING_RECEIVED.name)
    assertEquals("SYSTEM_UPDATE", NotificationType.SYSTEM_UPDATE.name)
    assertEquals("REMINDER", NotificationType.REMINDER.name)
  }
}
