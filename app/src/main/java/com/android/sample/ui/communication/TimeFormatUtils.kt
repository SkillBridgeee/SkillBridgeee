package com.android.sample.ui.communication

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Utility object for formatting timestamps in the communication UI. */
object TimeFormatUtils {

  /**
   * Formats a timestamp for display in message bubbles. Shows time for today's messages, or date +
   * time for older messages.
   *
   * @param timestamp The timestamp to format
   * @return Formatted string (e.g., "10:30 AM", "Yesterday 3:45 PM", "Dec 10, 2:15 PM")
   */
  fun formatMessageTimestamp(timestamp: Date): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = timestamp }

    val diffInMillis = now.timeInMillis - messageTime.timeInMillis
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    return when {
      // Today - show only time
      isSameDay(now, messageTime) -> {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp)
      }
      // Yesterday
      diffInDays == 1L -> {
        "Yesterday " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp)
      }
      // Within last week - show day and time
      diffInDays < 7 -> {
        SimpleDateFormat("EEE h:mm a", Locale.getDefault()).format(timestamp)
      }
      // Within same year - show month, day, time
      now[Calendar.YEAR] == messageTime[Calendar.YEAR] -> {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(timestamp)
      }
      // Different year - show full date and time
      else -> {
        SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(timestamp)
      }
    }
  }

  /**
   * Formats a timestamp for display in the discussion list (conversation overview). Shows relative
   * time for recent messages, or date for older ones.
   *
   * @param timestamp The timestamp to format
   * @return Formatted string (e.g., "Just now", "5m ago", "2h ago", "Yesterday", "Dec 10")
   */
  fun formatDiscussionTimestamp(timestamp: Date): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = timestamp }

    val diffInMillis = now.timeInMillis - messageTime.timeInMillis
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    return when {
      // Less than 1 minute
      diffInMinutes < 1 -> "Just now"
      // Less than 1 hour
      diffInMinutes < 60 -> "${diffInMinutes}m ago"
      // Less than 24 hours
      diffInHours < 24 -> "${diffInHours}h ago"
      // Yesterday (1-2 days ago, but more than 24 hours)
      diffInDays == 1L -> "Yesterday"
      // Within last week
      diffInDays < 7 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(timestamp)
      // Within same year
      now[Calendar.YEAR] == messageTime[Calendar.YEAR] -> {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(timestamp)
      }
      // Different year
      else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(timestamp)
    }
  }

  /** Checks if two Calendar instances represent the same day. */
  private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
        cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
  }
}
