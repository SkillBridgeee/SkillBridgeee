package com.android.sample.model.user

import com.android.sample.model.map.Location
import org.junit.Assert.*
import org.junit.Test

class ProfileTest {

  @Test
  fun `test Profile creation with default values`() {
    val profile = Profile()

    assertEquals("", profile.userId)
    assertEquals("", profile.name)
    assertEquals("", profile.email)
    assertEquals(Location(), profile.location)
    assertEquals("", profile.description)
    assertEquals(RatingInfo(), profile.tutorRating)
    assertEquals(RatingInfo(), profile.studentRating)
  }

  @Test
  fun `test Profile creation with custom values`() {
    val customLocation = Location(46.5197, 6.6323, "EPFL, Lausanne")
    val tutorRating = RatingInfo(averageRating = 4.5, totalRatings = 20)
    val studentRating = RatingInfo(averageRating = 4.2, totalRatings = 15)

    val profile =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = customLocation,
            description = "Experienced mathematics tutor",
            tutorRating = tutorRating,
            studentRating = studentRating)

    assertEquals("user123", profile.userId)
    assertEquals("John Doe", profile.name)
    assertEquals("john.doe@example.com", profile.email)
    assertEquals(customLocation, profile.location)
    assertEquals("Experienced mathematics tutor", profile.description)
    assertEquals(4.5, profile.tutorRating.averageRating, 0.01)
    assertEquals(20, profile.tutorRating.totalRatings)
    assertEquals(4.2, profile.studentRating.averageRating, 0.01)
    assertEquals(15, profile.studentRating.totalRatings)
  }

  @Test
  fun `test RatingInfo creation with valid values`() {
    val ratingInfo = RatingInfo(averageRating = 3.5, totalRatings = 10)

    assertEquals(3.5, ratingInfo.averageRating, 0.01)
    assertEquals(10, ratingInfo.totalRatings)
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
  fun `test RatingInfo with zero average and zero ratings`() {
    val ratingInfo = RatingInfo(averageRating = 0.0, totalRatings = 0)

    assertEquals(0.0, ratingInfo.averageRating, 0.01)
    assertEquals(0, ratingInfo.totalRatings)
  }

  @Test
  fun `test RatingInfo boundary values`() {
    val minRating = RatingInfo(averageRating = 1.0, totalRatings = 1)
    val maxRating = RatingInfo(averageRating = 5.0, totalRatings = 100)

    assertEquals(1.0, minRating.averageRating, 0.01)
    assertEquals(5.0, maxRating.averageRating, 0.01)
  }

  @Test
  fun `test Profile data class equality`() {
    val location = Location(46.5197, 6.6323, "EPFL, Lausanne")
    val tutorRating = RatingInfo(averageRating = 4.5, totalRatings = 20)
    val studentRating = RatingInfo(averageRating = 4.2, totalRatings = 15)

    val profile1 =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = location,
            description = "Tutor",
            tutorRating = tutorRating,
            studentRating = studentRating)

    val profile2 =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = location,
            description = "Tutor",
            tutorRating = tutorRating,
            studentRating = studentRating)

    assertEquals(profile1, profile2)
    assertEquals(profile1.hashCode(), profile2.hashCode())
  }

  @Test
  fun `test Profile copy functionality`() {
    val originalProfile =
        Profile(
            userId = "user123",
            name = "John Doe",
            tutorRating = RatingInfo(averageRating = 4.0, totalRatings = 10))

    val updatedRating = RatingInfo(averageRating = 4.5, totalRatings = 15)
    val copiedProfile = originalProfile.copy(name = "Jane Doe", tutorRating = updatedRating)

    assertEquals("user123", copiedProfile.userId)
    assertEquals("Jane Doe", copiedProfile.name)
    assertEquals(4.5, copiedProfile.tutorRating.averageRating, 0.01)
    assertEquals(15, copiedProfile.tutorRating.totalRatings)

    assertNotEquals(originalProfile, copiedProfile)
  }

  @Test
  fun `test Profile with different tutor and student ratings`() {
    val profile =
        Profile(
            userId = "user123",
            tutorRating = RatingInfo(averageRating = 5.0, totalRatings = 50),
            studentRating = RatingInfo(averageRating = 3.5, totalRatings = 20))

    assertTrue(profile.tutorRating.averageRating > profile.studentRating.averageRating)
    assertTrue(profile.tutorRating.totalRatings > profile.studentRating.totalRatings)
  }

  @Test
  fun `test Profile toString contains key information`() {
    val profile = Profile(userId = "user123", name = "John Doe", email = "john.doe@example.com")

    val profileString = profile.toString()
    assertTrue(profileString.contains("user123"))
    assertTrue(profileString.contains("John Doe"))
  }
}
