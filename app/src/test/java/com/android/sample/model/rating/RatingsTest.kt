package com.android.sample.model.rating

import org.junit.Assert.*
import org.junit.Test

class RatingsTest {

  @Test
  fun `test Ratings creation with default values`() {
    val rating = Ratings()

    assertEquals(StarRating.ONE, rating.rating)
    assertEquals("", rating.fromUserId)
    assertEquals("", rating.fromUserName)
    assertEquals("", rating.ratingUID)
  }

  @Test
  fun `test Ratings creation with valid values`() {
    val rating =
        Ratings(
            rating = StarRating.FIVE,
            fromUserId = "user123",
            fromUserName = "John Doe",
            ratingUID = "tutor456")

    assertEquals(StarRating.FIVE, rating.rating)
    assertEquals("user123", rating.fromUserId)
    assertEquals("John Doe", rating.fromUserName)
    assertEquals("tutor456", rating.ratingUID)
  }

  @Test
  fun `test Ratings with all valid rating values`() {
    val allRatings =
        listOf(StarRating.ONE, StarRating.TWO, StarRating.THREE, StarRating.FOUR, StarRating.FIVE)

    for (starRating in allRatings) {
      val rating =
          Ratings(
              rating = starRating,
              fromUserId = "user123",
              fromUserName = "John Doe",
              ratingUID = "tutor456")
      assertEquals(starRating, rating.rating)
    }
  }

  @Test
  fun `test StarRating enum values`() {
    assertEquals(1, StarRating.ONE.value)
    assertEquals(2, StarRating.TWO.value)
    assertEquals(3, StarRating.THREE.value)
    assertEquals(4, StarRating.FOUR.value)
    assertEquals(5, StarRating.FIVE.value)
  }

  @Test
  fun `test StarRating fromInt conversion`() {
    assertEquals(StarRating.ONE, StarRating.fromInt(1))
    assertEquals(StarRating.TWO, StarRating.fromInt(2))
    assertEquals(StarRating.THREE, StarRating.fromInt(3))
    assertEquals(StarRating.FOUR, StarRating.fromInt(4))
    assertEquals(StarRating.FIVE, StarRating.fromInt(5))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test StarRating fromInt with invalid value - too low`() {
    StarRating.fromInt(0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test StarRating fromInt with invalid value - too high`() {
    StarRating.fromInt(6)
  }

  @Test
  fun `test Ratings equality and hashCode`() {
    val rating1 =
        Ratings(
            rating = StarRating.FOUR,
            fromUserId = "user123",
            fromUserName = "John Doe",
            ratingUID = "tutor456")

    val rating2 =
        Ratings(
            rating = StarRating.FOUR,
            fromUserId = "user123",
            fromUserName = "John Doe",
            ratingUID = "tutor456")

    assertEquals(rating1, rating2)
    assertEquals(rating1.hashCode(), rating2.hashCode())
  }

  @Test
  fun `test Ratings copy functionality`() {
    val originalRating =
        Ratings(
            rating = StarRating.THREE,
            fromUserId = "user123",
            fromUserName = "John Doe",
            ratingUID = "tutor456")

    val updatedRating = originalRating.copy(rating = StarRating.FIVE, fromUserName = "Jane Doe")

    assertEquals(StarRating.FIVE, updatedRating.rating)
    assertEquals("user123", updatedRating.fromUserId)
    assertEquals("Jane Doe", updatedRating.fromUserName)
    assertEquals("tutor456", updatedRating.ratingUID)

    assertNotEquals(originalRating, updatedRating)
  }

  @Test
  fun `test Ratings toString contains key information`() {
    val rating =
        Ratings(
            rating = StarRating.FOUR,
            fromUserId = "user123",
            fromUserName = "John Doe",
            ratingUID = "tutor456")

    val ratingString = rating.toString()
    assertTrue(ratingString.contains("user123"))
    assertTrue(ratingString.contains("John Doe"))
    assertTrue(ratingString.contains("tutor456"))
  }
}
