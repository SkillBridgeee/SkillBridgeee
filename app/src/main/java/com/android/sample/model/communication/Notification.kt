package com.android.sample.model.communication

/** Enum representing different types of notifications */
enum class NotificationType {
  BOOKING_REQUEST,
  BOOKING_CONFIRMED,
  BOOKING_CANCELLED,
  MESSAGE_RECEIVED,
  RATING_RECEIVED,
  SYSTEM_UPDATE,
  REMINDER
}

/** Data class representing a notification */
data class Notification(
    val userId: String = "", // UID of the user receiving the notification
    val notificationType: NotificationType = NotificationType.SYSTEM_UPDATE,
    val notificationMessage: String = ""
) {
  init {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    require(notificationMessage.isNotBlank()) { "Notification message cannot be blank" }
  }
}
