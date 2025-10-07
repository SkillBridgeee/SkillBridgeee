package com.android.sample.model.user

import org.junit.Assert.*
import org.junit.Test

class TutorTest {

  @Test
  fun `test Tutor creation with default values`() {
    val tutor = Tutor()

    assertEquals("", tutor.userId)
    assertEquals("", tutor.name)
    assertEquals("", tutor.email)
    assertEquals("", tutor.location)
    assertEquals("", tutor.description)
    assertEquals(emptyList<String>(), tutor.skills)
    assertEquals(0.0, tutor.starRating, 0.01)
    assertEquals(0, tutor.ratingNumber)
  }

  @Test
  fun `test Tutor creation with valid values`() {
    val skills = listOf("MATHEMATICS", "PHYSICS")
    val tutor =
        Tutor(
            userId = "tutor123",
            name = "Dr. Smith",
            email = "dr.smith@example.com",
            location = "Boston",
            description = "Math and Physics tutor",
            skills = skills,
            starRating = 4.5,
            ratingNumber = 20)

    assertEquals("tutor123", tutor.userId)
    assertEquals("Dr. Smith", tutor.name)
    assertEquals("dr.smith@example.com", tutor.email)
    assertEquals("Boston", tutor.location)
    assertEquals("Math and Physics tutor", tutor.description)
    assertEquals(skills, tutor.skills)
    assertEquals(4.5, tutor.starRating, 0.01)
    assertEquals(20, tutor.ratingNumber)
  }

  @Test
  fun `test Tutor validation - valid star rating bounds`() {
    // Test minimum valid rating
    val tutorMin = Tutor(starRating = 0.0, ratingNumber = 0)
    assertEquals(0.0, tutorMin.starRating, 0.01)

    // Test maximum valid rating
    val tutorMax = Tutor(starRating = 5.0, ratingNumber = 100)
    assertEquals(5.0, tutorMax.starRating, 0.01)

    // Test middle rating
    val tutorMid = Tutor(starRating = 3.7, ratingNumber = 15)
    assertEquals(3.7, tutorMid.starRating, 0.01)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Tutor validation - star rating too low`() {
    Tutor(starRating = -0.1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Tutor validation - star rating too high`() {
    Tutor(starRating = 5.1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Tutor validation - negative rating number`() {
    Tutor(ratingNumber = -1)
  }

  @Test
  fun `test Tutor equality and hashCode`() {
    val tutor1 = Tutor(userId = "tutor123", name = "Dr. Smith", starRating = 4.5, ratingNumber = 20)

    val tutor2 = Tutor(userId = "tutor123", name = "Dr. Smith", starRating = 4.5, ratingNumber = 20)

    assertEquals(tutor1, tutor2)
    assertEquals(tutor1.hashCode(), tutor2.hashCode())
  }

  @Test
  fun `test Tutor copy functionality`() {
    val originalTutor =
        Tutor(userId = "tutor123", name = "Dr. Smith", starRating = 4.5, ratingNumber = 20)

    val updatedTutor = originalTutor.copy(starRating = 4.8, ratingNumber = 25)

    assertEquals("tutor123", updatedTutor.userId)
    assertEquals("Dr. Smith", updatedTutor.name)
    assertEquals(4.8, updatedTutor.starRating, 0.01)
    assertEquals(25, updatedTutor.ratingNumber)

    assertNotEquals(originalTutor, updatedTutor)
  }

  @Test
  fun `test Tutor with empty skills list`() {
    val tutor = Tutor(skills = emptyList())
    assertTrue(tutor.skills.isEmpty())
  }

  @Test
  fun `test Tutor with multiple skills`() {
    val skills = listOf("MATHEMATICS", "PHYSICS", "CHEMISTRY")
    val tutor = Tutor(skills = skills)

    assertEquals(3, tutor.skills.size)
    assertTrue(tutor.skills.contains("MATHEMATICS"))
    assertTrue(tutor.skills.contains("PHYSICS"))
    assertTrue(tutor.skills.contains("CHEMISTRY"))
  }
}
