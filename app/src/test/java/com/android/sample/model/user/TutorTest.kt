package com.android.sample.model.user

import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import org.junit.Assert.*
import org.junit.Test

class TutorTest {

  @Test
  fun `test Tutor creation with default values`() {
    val tutor = Tutor()

    assertEquals("", tutor.userId)
    assertEquals("", tutor.name)
    assertEquals("", tutor.email)
    assertEquals(Location(), tutor.location)
    assertEquals("", tutor.description)
    assertEquals(emptyList<Skill>(), tutor.skills)
    assertEquals(0.0, tutor.starRating, 0.01)
    assertEquals(0, tutor.ratingNumber)
  }

  @Test
  fun `test Tutor creation with valid values`() {
    val customLocation = Location(42.3601, -71.0589, "Boston, MA")
    val skills =
        listOf(
            Skill(
                userId = "tutor123",
                mainSubject = MainSubject.ACADEMICS,
                skill = "MATHEMATICS",
                skillTime = 5.0,
                expertise = ExpertiseLevel.EXPERT),
            Skill(
                userId = "tutor123",
                mainSubject = MainSubject.ACADEMICS,
                skill = "PHYSICS",
                skillTime = 3.0,
                expertise = ExpertiseLevel.ADVANCED))
    val tutor =
        Tutor(
            userId = "tutor123",
            name = "Dr. Smith",
            email = "dr.smith@example.com",
            location = customLocation,
            description = "Math and Physics tutor",
            skills = skills,
            starRating = 4.5,
            ratingNumber = 20)

    assertEquals("tutor123", tutor.userId)
    assertEquals("Dr. Smith", tutor.name)
    assertEquals("dr.smith@example.com", tutor.email)
    assertEquals(customLocation, tutor.location)
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
    Tutor(starRating = -0.1, ratingNumber = 1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Tutor validation - star rating too high`() {
    Tutor(starRating = 5.1, ratingNumber = 1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Tutor validation - negative rating number`() {
    Tutor(ratingNumber = -1)
  }

  @Test
  fun `test Tutor equality and hashCode`() {
    val location = Location(42.3601, -71.0589, "Boston, MA")
    val tutor1 =
        Tutor(
            userId = "tutor123",
            name = "Dr. Smith",
            location = location,
            starRating = 4.5,
            ratingNumber = 20)
    val tutor2 =
        Tutor(
            userId = "tutor123",
            name = "Dr. Smith",
            location = location,
            starRating = 4.5,
            ratingNumber = 20)

    assertEquals(tutor1, tutor2)
    assertEquals(tutor1.hashCode(), tutor2.hashCode())
  }

  @Test
  fun `test Tutor copy functionality`() {
    val location = Location(42.3601, -71.0589, "Boston, MA")
    val originalTutor =
        Tutor(
            userId = "tutor123",
            name = "Dr. Smith",
            location = location,
            starRating = 4.5,
            ratingNumber = 20)

    val updatedTutor = originalTutor.copy(starRating = 4.8, ratingNumber = 25)

    assertEquals("tutor123", updatedTutor.userId)
    assertEquals("Dr. Smith", updatedTutor.name)
    assertEquals(location, updatedTutor.location)
    assertEquals(4.8, updatedTutor.starRating, 0.01)
    assertEquals(25, updatedTutor.ratingNumber)

    assertNotEquals(originalTutor, updatedTutor)
  }

  @Test
  fun `test Tutor with skills`() {
    val skills =
        listOf(
            Skill(
                userId = "tutor456",
                mainSubject = MainSubject.ACADEMICS,
                skill = "MATHEMATICS",
                skillTime = 2.5,
                expertise = ExpertiseLevel.INTERMEDIATE),
            Skill(
                userId = "tutor456",
                mainSubject = MainSubject.ACADEMICS,
                skill = "CHEMISTRY",
                skillTime = 4.0,
                expertise = ExpertiseLevel.ADVANCED))
    val tutor = Tutor(userId = "tutor456", skills = skills)

    assertEquals(skills, tutor.skills)
    assertEquals(2, tutor.skills.size)
    assertEquals("MATHEMATICS", tutor.skills[0].skill)
    assertEquals("CHEMISTRY", tutor.skills[1].skill)
    assertEquals(MainSubject.ACADEMICS, tutor.skills[0].mainSubject)
    assertEquals(ExpertiseLevel.INTERMEDIATE, tutor.skills[0].expertise)
  }

  @Test
  fun `test Tutor toString contains key information`() {
    val tutor = Tutor(userId = "tutor123", name = "Dr. Smith")
    val tutorString = tutor.toString()

    assertTrue(tutorString.contains("tutor123"))
    assertTrue(tutorString.contains("Dr. Smith"))
  }
}
