package com.android.sample.ui.communication

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeFormatUtilsTest {

  @Test
  fun formatMessageTimestamp_today_showsTimeOnly() {
    // Arrange
    val now = Calendar.getInstance()
    now.set(Calendar.HOUR_OF_DAY, 14)
    now.set(Calendar.MINUTE, 30)
    val timestamp = now.time

    // Act
    val result = TimeFormatUtils.formatMessageTimestamp(timestamp)

    // Assert
    assertTrue(result.contains("2:30") || result.contains("14:30"))
    assertTrue(result.contains("PM") || result.contains("pm"))
  }

  @Test
  fun formatMessageTimestamp_yesterday_showsYesterdayAndTime() {
    // Arrange
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    yesterday.set(Calendar.HOUR_OF_DAY, 10)
    yesterday.set(Calendar.MINUTE, 15)
    val timestamp = yesterday.time

    // Act
    val result = TimeFormatUtils.formatMessageTimestamp(timestamp)

    // Assert
    assertTrue(result.startsWith("Yesterday"))
    assertTrue(result.contains("10:15") || result.contains("AM") || result.contains("am"))
  }

  @Test
  fun formatMessageTimestamp_lastWeek_showsDayAndTime() {
    // Arrange
    val lastWeek = Calendar.getInstance()
    lastWeek.add(Calendar.DAY_OF_YEAR, -5)
    lastWeek.set(Calendar.HOUR_OF_DAY, 9)
    lastWeek.set(Calendar.MINUTE, 45)
    val timestamp = lastWeek.time

    // Act
    val result = TimeFormatUtils.formatMessageTimestamp(timestamp)

    // Assert
    // Should contain day of week (Mon, Tue, etc.) and time
    assertTrue(result.contains(":"))
    // Should not be "Yesterday"
    assertTrue(!result.startsWith("Yesterday"))
  }

  @Test
  fun formatMessageTimestamp_thisYear_showsMonthDayAndTime() {
    // Arrange
    val thisYear = Calendar.getInstance()
    thisYear.add(Calendar.MONTH, -2)
    thisYear.set(Calendar.HOUR_OF_DAY, 16)
    thisYear.set(Calendar.MINUTE, 20)
    val timestamp = thisYear.time

    // Act
    val result = TimeFormatUtils.formatMessageTimestamp(timestamp)

    // Assert
    assertTrue(result.contains(":"))
    // Should not contain year
    assertTrue(!result.contains("202") && !result.contains("201"))
  }

  @Test
  fun formatMessageTimestamp_lastYear_showsFullDate() {
    // Arrange
    val lastYear = Calendar.getInstance()
    lastYear.add(Calendar.YEAR, -1)
    lastYear.set(Calendar.HOUR_OF_DAY, 12)
    lastYear.set(Calendar.MINUTE, 0)
    val timestamp = lastYear.time

    // Act
    val result = TimeFormatUtils.formatMessageTimestamp(timestamp)

    // Assert
    assertTrue(result.contains(":"))
    // Should contain year
    val expectedYear = lastYear[Calendar.YEAR].toString()
    assertTrue(result.contains(expectedYear))
  }

  @Test
  fun formatDiscussionTimestamp_justNow_returnsJustNow() {
    // Arrange
    val now = Date()

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(now)

    // Assert
    assertEquals("Just now", result)
  }

  @Test
  fun formatDiscussionTimestamp_fewMinutesAgo_returnsMinutes() {
    // Arrange
    val fiveMinutesAgo = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5))

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(fiveMinutesAgo)

    // Assert
    assertTrue(result.endsWith("m ago"))
    assertTrue(result.startsWith("5") || result.startsWith("4") || result.startsWith("6"))
  }

  @Test
  fun formatDiscussionTimestamp_fewHoursAgo_returnsHours() {
    // Arrange
    val threeHoursAgo = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3))

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(threeHoursAgo)

    // Assert
    assertTrue(result.endsWith("h ago"))
    assertTrue(result.startsWith("3") || result.startsWith("2") || result.startsWith("4"))
  }

  @Test
  fun formatDiscussionTimestamp_yesterday_returnsYesterday() {
    // Arrange
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    val timestamp = yesterday.time

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(timestamp)

    // Assert
    assertEquals("Yesterday", result)
  }

  @Test
  fun formatDiscussionTimestamp_lastWeek_returnsDayName() {
    // Arrange
    val lastWeek = Calendar.getInstance()
    lastWeek.add(Calendar.DAY_OF_YEAR, -5)
    val timestamp = lastWeek.time

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(timestamp)

    // Assert
    // Should be a day name like "Monday", "Tuesday", etc.
    assertTrue(
        result.equals("Monday") ||
            result.equals("Tuesday") ||
            result.equals("Wednesday") ||
            result.equals("Thursday") ||
            result.equals("Friday") ||
            result.equals("Saturday") ||
            result.equals("Sunday"))
  }

  @Test
  fun formatDiscussionTimestamp_thisYear_returnsMonthAndDay() {
    // Arrange
    val twoMonthsAgo = Calendar.getInstance()
    twoMonthsAgo.add(Calendar.MONTH, -2)
    val timestamp = twoMonthsAgo.time

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(timestamp)

    // Assert
    // Should not contain year
    assertTrue(!result.contains("202") && !result.contains("201"))
    // Should be in format like "Oct 15"
    assertTrue(result.contains(" "))
  }

  @Test
  fun formatDiscussionTimestamp_lastYear_returnsFullDate() {
    // Arrange
    val lastYear = Calendar.getInstance()
    lastYear.add(Calendar.YEAR, -1)
    val timestamp = lastYear.time

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(timestamp)

    // Assert
    // Should contain year
    val expectedYear = lastYear[Calendar.YEAR].toString()
    assertTrue(result.contains(expectedYear))
  }

  @Test
  fun formatDiscussionTimestamp_30SecondsAgo_returnsJustNow() {
    // Arrange
    val thirtySecondsAgo = Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(30))

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(thirtySecondsAgo)

    // Assert
    assertEquals("Just now", result)
  }

  @Test
  fun formatDiscussionTimestamp_59MinutesAgo_returnsMinutes() {
    // Arrange
    val fiftyNineMinutesAgo = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(59))

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(fiftyNineMinutesAgo)

    // Assert
    assertTrue(result.endsWith("m ago"))
    assertTrue(result.startsWith("59") || result.startsWith("58") || result.startsWith("60"))
  }

  @Test
  fun formatDiscussionTimestamp_23HoursAgo_returnsHours() {
    // Arrange
    val twentyThreeHoursAgo = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(23))

    // Act
    val result = TimeFormatUtils.formatDiscussionTimestamp(twentyThreeHoursAgo)

    // Assert
    assertTrue(result.endsWith("h ago"))
    assertTrue(result.startsWith("23") || result.startsWith("22") || result.startsWith("24"))
  }
}
