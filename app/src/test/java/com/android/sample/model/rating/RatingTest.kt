package com.android.sample.model.rating

import org.junit.Assert.*
import org.junit.Test

class RatingTest {

  @Test
  fun `test Rating creation with tutor rating type`() {
    val rating =
        Rating(
            ratingId = "rating123",
            fromUserId = "student123",
            toUserId = "tutor456",
            starRating = StarRating.FIVE,
            comment = "Excellent tutor!",
            ratingType = RatingType.Tutor("listing789"))

    assertEquals("rating123", rating.ratingId)
    assertEquals("student123", rating.fromUserId)
    assertEquals("tutor456", rating.toUserId)
    assertEquals(StarRating.FIVE, rating.starRating)
    assertEquals("Excellent tutor!", rating.comment)
    assertTrue(rating.ratingType is RatingType.Tutor)
    assertEquals("listing789", (rating.ratingType as RatingType.Tutor).listingId)
  }

  @Test
  fun `test Rating creation with student rating type`() {
    val rating =
        Rating(
            ratingId = "rating123",
            fromUserId = "tutor456",
            toUserId = "student123",
            starRating = StarRating.FOUR,
            comment = "Great student, very engaged",
            ratingType = RatingType.Student("student123"))

    assertTrue(rating.ratingType is RatingType.Student)
    assertEquals("student123", (rating.ratingType as RatingType.Student).studentId)
    assertEquals("tutor456", rating.fromUserId)
    assertEquals("student123", rating.toUserId)
  }

  @Test
  fun `test Rating creation with listing rating type`() {
    val rating =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "tutor456",
            starRating = StarRating.THREE,
            comment = "Good listing",
            ratingType = RatingType.Listing("listing789"))

    assertTrue(rating.ratingType is RatingType.Listing)
    assertEquals("listing789", (rating.ratingType as RatingType.Listing).listingId)
  }

  @Test
  fun `test Rating with all valid star ratings`() {
    val allRatings =
        listOf(StarRating.ONE, StarRating.TWO, StarRating.THREE, StarRating.FOUR, StarRating.FIVE)

    for (starRating in allRatings) {
      val rating =
          Rating(
              ratingId = "rating123",
              fromUserId = "user123",
              toUserId = "user456",
              starRating = starRating,
              comment = "Test comment",
              ratingType = RatingType.Tutor("listing789"))
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
  fun `test Rating equality with same tutor rating`() {
    val rating1 =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Good",
            ratingType = RatingType.Tutor("listing789"))

    val rating2 =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Good",
            ratingType = RatingType.Tutor("listing789"))

    assertEquals(rating1, rating2)
    assertEquals(rating1.hashCode(), rating2.hashCode())
  }

  @Test
  fun `test Rating equality with different rating types`() {
    val rating1 =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Good",
            ratingType = RatingType.Tutor("listing789"))

    val rating2 =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Good",
            ratingType = RatingType.Student("student123"))

    assertNotEquals(rating1, rating2)
  }

  @Test
  fun `test Rating copy functionality`() {
    val originalRating =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.THREE,
            comment = "Average",
            ratingType = RatingType.Tutor("listing789"))

    val updatedRating = originalRating.copy(starRating = StarRating.FIVE, comment = "Excellent!")

    assertEquals("rating123", updatedRating.ratingId)
    assertEquals(StarRating.FIVE, updatedRating.starRating)
    assertEquals("Excellent!", updatedRating.comment)
    assertTrue(updatedRating.ratingType is RatingType.Tutor)

    assertNotEquals(originalRating, updatedRating)
  }

  @Test
  fun `test Rating with empty comment`() {
    val rating =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "",
            ratingType = RatingType.Student("student123"))

    assertEquals("", rating.comment)
  }

  @Test
  fun `test Rating toString contains key information`() {
    val rating =
        Rating(
            ratingId = "rating123",
            fromUserId = "user123",
            toUserId = "user456",
            starRating = StarRating.FOUR,
            comment = "Great!",
            ratingType = RatingType.Tutor("listing789"))

    val ratingString = rating.toString()
    assertTrue(ratingString.contains("rating123"))
    assertTrue(ratingString.contains("user123"))
    assertTrue(ratingString.contains("user456"))
  }

  @Test
  fun `test RatingType sealed class instances`() {
    val tutorRating = RatingType.Tutor("listing123")
    val studentRating = RatingType.Student("student456")
    val listingRating = RatingType.Listing("listing789")

    assertTrue(tutorRating is RatingType)
    assertTrue(studentRating is RatingType)
    assertTrue(listingRating is RatingType)

    assertEquals("listing123", tutorRating.listingId)
    assertEquals("student456", studentRating.studentId)
    assertEquals("listing789", listingRating.listingId)
  }

  @Test
  fun `test RatingInfo creation with valid values`() {
    val ratingInfo = RatingInfo(averageRating = 4.5, totalRatings = 10)

    assertEquals(4.5, ratingInfo.averageRating, 0.01)
    assertEquals(10, ratingInfo.totalRatings)
  }

  @Test
  fun `test RatingInfo creation with default values`() {
    val ratingInfo = RatingInfo()

    assertEquals(0.0, ratingInfo.averageRating, 0.01)
    assertEquals(0, ratingInfo.totalRatings)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test RatingInfo validation - average rating too low`() {
    RatingInfo(averageRating = 0.5, totalRatings = 5)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test RatingInfo validation - average rating too high`() {
    RatingInfo(averageRating = 5.5, totalRatings = 5)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test RatingInfo validation - negative total ratings`() {
    RatingInfo(averageRating = 4.0, totalRatings = -1)
  }

  @Test
  fun `test RatingInfo with boundary values`() {
    val minRating = RatingInfo(averageRating = 1.0, totalRatings = 1)
    val maxRating = RatingInfo(averageRating = 5.0, totalRatings = 100)

    assertEquals(1.0, minRating.averageRating, 0.01)
    assertEquals(1, minRating.totalRatings)
    assertEquals(5.0, maxRating.averageRating, 0.01)
    assertEquals(100, maxRating.totalRatings)
  }
}
