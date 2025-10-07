package com.android.sample.model.user

import org.junit.Assert.*
import org.junit.Test

class ProfileTest {

  @Test
  fun `test Profile creation with default values`() {
    val profile = Profile()

    assertEquals("", profile.userId)
    assertEquals("", profile.name)
    assertEquals("", profile.email)
    assertEquals("", profile.location)
    assertEquals("", profile.description)
  }

  @Test
  fun `test Profile creation with custom values`() {
    val profile =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = "New York",
            description = "Software Engineer")

    assertEquals("user123", profile.userId)
    assertEquals("John Doe", profile.name)
    assertEquals("john.doe@example.com", profile.email)
    assertEquals("New York", profile.location)
    assertEquals("Software Engineer", profile.description)
  }

  @Test
  fun `test Profile data class properties`() {
    val profile1 =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = "New York",
            description = "Software Engineer")

    val profile2 =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = "New York",
            description = "Software Engineer")

    // Test equality
    assertEquals(profile1, profile2)
    assertEquals(profile1.hashCode(), profile2.hashCode())

    // Test toString contains key information
    val profileString = profile1.toString()
    assertTrue(profileString.contains("user123"))
    assertTrue(profileString.contains("John Doe"))
  }

  @Test
  fun `test Profile with empty strings`() {
    val profile = Profile(userId = "", name = "", email = "", location = "", description = "")

    assertNotNull(profile)
    assertEquals("", profile.userId)
    assertEquals("", profile.name)
    assertEquals("", profile.email)
    assertEquals("", profile.location)
    assertEquals("", profile.description)
  }

  @Test
  fun `test Profile copy functionality`() {
    val originalProfile =
        Profile(
            userId = "user123",
            name = "John Doe",
            email = "john.doe@example.com",
            location = "New York",
            description = "Software Engineer")

    val copiedProfile = originalProfile.copy(name = "Jane Doe")

    assertEquals("user123", copiedProfile.userId)
    assertEquals("Jane Doe", copiedProfile.name)
    assertEquals("john.doe@example.com", copiedProfile.email)
    assertEquals("New York", copiedProfile.location)
    assertEquals("Software Engineer", copiedProfile.description)

    assertNotEquals(originalProfile, copiedProfile)
  }
}
