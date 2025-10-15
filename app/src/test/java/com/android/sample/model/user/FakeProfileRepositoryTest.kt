package com.android.sample.model.user

import com.android.sample.model.map.Location
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FakeProfileRepositoryTest {

  @Test
  fun uid_add_get_update_delete_roundtrip() = runTest {
    val repo = FakeProfileRepository()
    val uid1 = repo.getNewUid()
    val uid2 = repo.getNewUid()
    assertNotEquals(uid1, uid2)

    val p = Profile(userId = "", name = "Alice", email = "a@a.com")
    repo.addProfile(p)
    val saved = repo.getAllProfiles().single()
    assertTrue(saved.userId.isNotBlank())

    val fetched = repo.getProfile(saved.userId)
    assertEquals("Alice", fetched.name)

    repo.updateProfile(saved.userId, fetched.copy(name = "Alice M."))
    assertEquals("Alice M.", repo.getProfile(saved.userId).name)

    repo.deleteProfile(saved.userId)
    assertTrue(repo.getAllProfiles().isEmpty())
  }

  @Test
  fun search_by_location_respects_radius() = runTest {
    val repo = FakeProfileRepository()
    val center = Location(latitude = 41.0, longitude = 29.0)
    val near = Location(latitude = 41.01, longitude = 29.01) // ~1.4 km
    val far = Location(latitude = 41.2, longitude = 29.2) // >> 10 km

    repo.addProfile(Profile("", "Center", "c@c", location = center))
    repo.addProfile(Profile("", "Near", "n@n", location = near))
    repo.addProfile(Profile("", "Far", "f@f", location = far))

    // radius <= 0 => all
    assertEquals(3, repo.searchProfilesByLocation(center, 0.0).size)

    // ~2 km => Center + Near
    val names = repo.searchProfilesByLocation(center, 2.0).map { it.name }.toSet()
    assertEquals(setOf("Center", "Near"), names)
  }
}
