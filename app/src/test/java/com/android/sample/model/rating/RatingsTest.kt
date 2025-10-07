package com.android.sample.model.rating

import org.junit.Assert.*
import org.junit.Test

class RatingsTest {

  @Test
  fun `test Ratings creation with default values`() {
    // This will fail validation because default rating is 0 (invalid)
    try {
      val rating = Ratings()
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Rating must be between 1 and 5"))
    }
  }

  @Test
  fun `test Ratings creation with valid values`() {
    val rating =
        Ratings(
            rating = 5, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")

    assertEquals(5, rating.rating)
    assertEquals("user123", rating.fromUserId)
    assertEquals("John Doe", rating.fromUserName)
    assertEquals("tutor456", rating.ratingId)
  }

  @Test
  fun `test Ratings with all valid rating values`() {
    for (ratingValue in 1..5) {
      val rating =
          Ratings(
              rating = ratingValue,
              fromUserId = "user123",
              fromUserName = "John Doe",
              ratingId = "tutor456")
      assertEquals(ratingValue, rating.rating)
    }
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Ratings validation - rating too low`() {
    Ratings(rating = 0, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Ratings validation - rating too high`() {
    Ratings(rating = 6, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Ratings validation - negative rating`() {
    Ratings(rating = -1, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")
  }

  @Test
  fun `test Ratings boundary values`() {
    // Test minimum valid rating
    val minRating =
        Ratings(
            rating = 1, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")
    assertEquals(1, minRating.rating)

    // Test maximum valid rating
    val maxRating =
        Ratings(
            rating = 5, fromUserId = "user456", fromUserName = "Jane Doe", ratingId = "tutor789")
    assertEquals(5, maxRating.rating)
  }

  @Test
  fun `test Ratings equality and hashCode`() {
    val rating1 =
        Ratings(
            rating = 4, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")

    val rating2 =
        Ratings(
            rating = 4, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")

    assertEquals(rating1, rating2)
    assertEquals(rating1.hashCode(), rating2.hashCode())
  }

  @Test
  fun `test Ratings copy functionality`() {
    val originalRating =
        Ratings(
            rating = 3, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")

    val updatedRating = originalRating.copy(rating = 5, fromUserName = "John Smith")

    assertEquals(5, updatedRating.rating)
    assertEquals("user123", updatedRating.fromUserId)
    assertEquals("John Smith", updatedRating.fromUserName)
    assertEquals("tutor456", updatedRating.ratingId)

    assertNotEquals(originalRating, updatedRating)
  }

  @Test
  fun `test Ratings with empty string fields`() {
    val rating = Ratings(rating = 3, fromUserId = "", fromUserName = "", ratingId = "")

    assertEquals(3, rating.rating)
    assertEquals("", rating.fromUserId)
    assertEquals("", rating.fromUserName)
    assertEquals("", rating.ratingId)
  }

  @Test
  fun `test Ratings toString contains relevant information`() {
    val rating =
        Ratings(
            rating = 4, fromUserId = "user123", fromUserName = "John Doe", ratingId = "tutor456")

    val ratingString = rating.toString()
    assertTrue(ratingString.contains("4"))
    assertTrue(ratingString.contains("user123"))
    assertTrue(ratingString.contains("John Doe"))
    assertTrue(ratingString.contains("tutor456"))
  }

  @Test
  fun `test Ratings with different user combinations`() {
    val rating1 =
        Ratings(rating = 5, fromUserId = "user123", fromUserName = "Alice", ratingId = "tutor456")

    val rating2 =
        Ratings(rating = 3, fromUserId = "user789", fromUserName = "Bob", ratingId = "tutor456")

    // Same tutor, different raters
    assertEquals("tutor456", rating1.ratingId)
    assertEquals("tutor456", rating2.ratingId)
    assertNotEquals(rating1.fromUserId, rating2.fromUserId)
    assertNotEquals(rating1.fromUserName, rating2.fromUserName)
    assertNotEquals(rating1.rating, rating2.rating)
  }
}
