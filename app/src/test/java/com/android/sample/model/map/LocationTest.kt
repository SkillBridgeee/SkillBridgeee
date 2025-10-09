package com.android.sample.model.map

import org.junit.Assert.*
import org.junit.Test

class LocationTest {

  @Test
  fun `test Location creation with default values`() {
    val location = Location()
    assertEquals(0.0, location.latitude, 0.0)
    assertEquals(0.0, location.longitude, 0.0)
    assertEquals("", location.name)
  }

  @Test
  fun `test Location creation with custom values`() {
    val location = Location(latitude = 46.5197, longitude = 6.6323, name = "EPFL, Lausanne")
    assertEquals(46.5197, location.latitude, 0.0001)
    assertEquals(6.6323, location.longitude, 0.0001)
    assertEquals("EPFL, Lausanne", location.name)
  }

  @Test
  fun `test Location with negative coordinates`() {
    val location =
        Location(latitude = -34.6037, longitude = -58.3816, name = "Buenos Aires, Argentina")
    assertEquals(-34.6037, location.latitude, 0.0001)
    assertEquals(-58.3816, location.longitude, 0.0001)
    assertEquals("Buenos Aires, Argentina", location.name)
  }

  @Test
  fun `test Location equality`() {
    val location1 = Location(46.5197, 6.6323, "EPFL")
    val location2 = Location(46.5197, 6.6323, "EPFL")
    val location3 = Location(46.5197, 6.6323, "UNIL")

    assertEquals(location1, location2)
    assertNotEquals(location1, location3)
  }

  @Test
  fun `test Location toString`() {
    val location = Location(46.5197, 6.6323, "EPFL")
    val expectedString = "Location(latitude=46.5197, longitude=6.6323, name=EPFL)"
    assertEquals(expectedString, location.toString())
  }
}
