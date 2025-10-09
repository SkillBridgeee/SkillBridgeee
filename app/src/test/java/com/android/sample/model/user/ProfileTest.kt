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
    assertEquals(false, profile.isTutor)
  }

  @Test
  fun `test Profile creation with custom values`() {
    val customLocation = Location(46.5197, 6.6323, "EPFL, Lausanne")
    val profile =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = customLocation,
            description = "Software Engineer",
            isTutor = true)

    assertEquals("user123", profile.userId)
    assertEquals("John Doe", profile.name)
    assertEquals("john.doe@example.com", profile.email)
    assertEquals(customLocation, profile.location)
    assertEquals("Software Engineer", profile.description)
    assertEquals(true, profile.isTutor)
  }

  @Test
  fun `test Profile data class properties`() {
    val customLocation = Location(40.7128, -74.0060, "New York")
    val profile1 =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = customLocation,
            description = "Software Engineer",
            isTutor = false)

    val profile2 =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = customLocation,
            description = "Software Engineer",
            isTutor = false)

    // Test equality
    assertEquals(profile1, profile2)
    assertEquals(profile1.hashCode(), profile2.hashCode())

    // Test toString contains key information
    val profileString = profile1.toString()
    assertTrue(profileString.contains("user123"))
    assertTrue(profileString.contains("John Doe"))
  }

  @Test
  fun `test Profile with empty values`() {
    val profile =
        Profile(
            userId = "",
            name = "",
            email = "",
            location = Location(),
            description = "",
            isTutor = false)

    assertNotNull(profile)
    assertEquals("", profile.userId)
    assertEquals("", profile.name)
    assertEquals("", profile.email)
    assertEquals(Location(), profile.location)
    assertEquals("", profile.description)
    assertEquals(false, profile.isTutor)
  }

  @Test
  fun `test Profile copy functionality`() {
    val originalLocation = Location(46.5197, 6.6323, "EPFL, Lausanne")
    val originalProfile =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = originalLocation,
            description = "Software Engineer",
            isTutor = false)

    val copiedProfile = originalProfile.copy(name = "Jane Doe", isTutor = true)

    assertEquals("user123", copiedProfile.userId)
    assertEquals("Jane Doe", copiedProfile.name)
    assertEquals("john.doe@example.com", copiedProfile.email)
    assertEquals(originalLocation, copiedProfile.location)
    assertEquals("Software Engineer", copiedProfile.description)
    assertEquals(true, copiedProfile.isTutor)

    assertNotEquals(originalProfile, copiedProfile)
  }

  @Test
  fun `test Profile tutor status`() {
    val nonTutorProfile = Profile(isTutor = false)
    val tutorProfile = Profile(isTutor = true)

    assertFalse(nonTutorProfile.isTutor)
    assertTrue(tutorProfile.isTutor)
  }
}
