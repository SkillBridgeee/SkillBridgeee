package com.android.sample.model.rating

import org.junit.Assert.*
import org.junit.Test

class RatingTest {

  @Test
  fun `test Rating creation with default values`() {
    val rating = Rating()

    assertEquals("", rating.ratingId)
    assertEquals("", rating.bookingId)
    assertEquals("", rating.listingId)
    assertEquals("", rating.fromUserId)
    assertEquals("", rating.toUserId)
    assertEquals(StarRating.ONE, rating.starRating)
    assertEquals("", rating.comment)
    assertEquals(RatingType.TUTOR, rating.ratingType)
  }

  @Test
  fun `test Rating creation with valid tutor rating`() {
    val rating =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "student123",
            toUserId = "tutor456",
            starRating = StarRating.FIVE,
            comment = "Excellent tutor!",
            ratingType = RatingType.TUTOR)

    assertEquals("rating123", rating.ratingId)
    assertEquals("booking456", rating.bookingId)
    assertEquals("listing789", rating.listingId)
    assertEquals("student123", rating.fromUserId)
    assertEquals("tutor456", rating.toUserId)
    assertEquals(StarRating.FIVE, rating.starRating)
    assertEquals("Excellent tutor!", rating.comment)
    assertEquals(RatingType.TUTOR, rating.ratingType)
  }

  @Test
  fun `test Rating creation with valid student rating`() {
    val rating =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "tutor456",
            toUserId = "student123",
            starRating = StarRating.FOUR,
            comment = "Great student, very engaged",
            ratingType = RatingType.STUDENT)

    assertEquals(RatingType.STUDENT, rating.ratingType)
    assertEquals("tutor456", rating.fromUserId)
    assertEquals("student123", rating.toUserId)
  }

  @Test
  fun `test Rating with all valid star ratings`() {
    val allRatings =
        listOf(StarRating.ONE, StarRating.TWO, StarRating.THREE, StarRating.FOUR, StarRating.FIVE)

    for (starRating in allRatings) {
      val rating =
          Rating(
              ratingId = "rating123",
              bookingId = "booking456",
              listingId = "listing789",
              fromUserId = "user123",
              toUserId = "user456",
              starRating = starRating,
              ratingType = RatingType.TUTOR)
      assertEquals(starRating, rating.starRating)
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
  fun `test RatingType enum values`() {
    assertEquals(2, RatingType.values().size)
    assertTrue(RatingType.values().contains(RatingType.TUTOR))
    assertTrue(RatingType.values().contains(RatingType.STUDENT))
  }

  @Test
  fun `test Rating equality and hashCode`() {
    val rating1 =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Good",
            ratingType = RatingType.TUTOR)

    val rating2 =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Good",
            ratingType = RatingType.TUTOR)

    assertEquals(rating1, rating2)
    assertEquals(rating1.hashCode(), rating2.hashCode())
  }

  @Test
  fun `test Rating copy functionality`() {
    val originalRating =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.THREE,
            comment = "Average",
            ratingType = RatingType.TUTOR)

    val updatedRating = originalRating.copy(starRating = StarRating.FIVE, comment = "Excellent!")

    assertEquals("rating123", updatedRating.ratingId)
    assertEquals("booking456", updatedRating.bookingId)
    assertEquals("listing789", updatedRating.listingId)
    assertEquals(StarRating.FIVE, updatedRating.starRating)
    assertEquals("Excellent!", updatedRating.comment)

    assertNotEquals(originalRating, updatedRating)
  }

  @Test
  fun `test Rating with empty comment`() {
    val rating =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "",
            ratingType = RatingType.STUDENT)

    assertEquals("", rating.comment)
  }

  @Test
  fun `test Rating toString contains key information`() {
    val rating =
        Rating(
            ratingId = "rating123",
            bookingId = "booking456",
            listingId = "listing789",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Great!",
            ratingType = RatingType.TUTOR)

    val ratingString = rating.toString()
    assertTrue(ratingString.contains("rating123"))
    assertTrue(ratingString.contains("listing789"))
    assertTrue(ratingString.contains("user123"))
    assertTrue(ratingString.contains("user456"))
  }
}
