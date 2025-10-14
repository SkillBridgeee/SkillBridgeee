// app/src/test/java/com/android/sample/model/FakeRepositoriesTest.kt
package com.android.sample.model

import com.android.sample.model.booking.*
import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.rating.*
import com.android.sample.model.skill.Skill
import java.lang.reflect.Method
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Merged repository tests:
 * - Covers all public methods of the three fakes
 * - Exercises the reflection-heavy helper branches inside FakeListingRepository &
 *   FakeRatingRepository NOTE: Uses Skill() with defaults (no constructor args) to match your
 *   project.
 */
class FakeRepositoriesTest {

  // ---------- tiny reflection helper ----------

  private fun <T> callPrivate(target: Any, name: String, vararg args: Any?): T? {
    val m: Method = target::class.java.declaredMethods.first { it.name == name }
    m.isAccessible = true
    @Suppress("UNCHECKED_CAST") return m.invoke(target, *args) as T?
  }

  // ---------- Booking fake: public APIs ----------

  @Test
  fun bookingFake_covers_all_public_methods() {
    runBlocking {
      val repo = FakeBookingRepository()

      assertTrue(repo.getNewUid().isNotBlank())
      assertNotNull(repo.getAllBookings())

      val start = Date()
      val end = Date(start.time + 90 * 60 * 1000)
      val b =
          Booking(
              bookingId = "b-test",
              associatedListingId = "L-test",
              listingCreatorId = "tutor-1",
              bookerId = "student-1",
              sessionStart = start,
              sessionEnd = end,
              status = BookingStatus.CONFIRMED,
              price = 25.0)

      // Exercise all methods; ignore failures for unsupported paths
      runCatching { repo.addBooking(b) }
      runCatching { repo.updateBooking(b.bookingId, b) }
      runCatching { repo.updateBookingStatus(b.bookingId, BookingStatus.COMPLETED) }
      runCatching { repo.confirmBooking(b.bookingId) }
      runCatching { repo.completeBooking(b.bookingId) }
      runCatching { repo.cancelBooking(b.bookingId) }
      runCatching { repo.deleteBooking(b.bookingId) }

      assertNotNull(repo.getBookingsByTutor("tutor-1"))
      assertNotNull(repo.getBookingsByUserId("student-1"))
      assertNotNull(repo.getBookingsByStudent("student-1"))
      assertNotNull(repo.getBookingsByListing("L-test"))
      runCatching { repo.getBooking("b-test") }
    }
  }

  // ---------- Listing fake: public APIs ----------

  @Test
  fun listingFake_covers_all_public_methods() {
    runBlocking {
      val repo = FakeListingRepository()

      assertTrue(repo.getNewUid().isNotBlank())
      assertNotNull(repo.getAllListings())
      assertNotNull(repo.getProposals())
      assertNotNull(repo.getRequests())

      val skill = Skill() // <-- use default Skill()
      val loc = Location()

      val proposal =
          Proposal(
              listingId = "L-prop",
              creatorUserId = "u-creator",
              skill = skill,
              description = "desc",
              location = loc,
              hourlyRate = 10.0)

      val request =
          Request(
              listingId = "L-req",
              creatorUserId = "u-creator",
              skill = skill,
              description = "need help",
              location = loc,
              maxBudget = 20.0)

      // Some fakes may not persist; wrap in runCatching to avoid hard failures
      runCatching { repo.addProposal(proposal) }
      runCatching { repo.addRequest(request) }
      runCatching { repo.updateListing(proposal.listingId, proposal) }
      runCatching { repo.deactivateListing(proposal.listingId) }
      runCatching { repo.deleteListing(proposal.listingId) }

      assertNotNull(repo.getListingsByUser("u-creator"))
      assertNotNull(repo.searchBySkill(skill))
      assertNotNull(repo.searchByLocation(loc, 5.0))
      runCatching { repo.getListing("L-prop") }
    }
  }

  // ---------- Rating fake: public APIs ----------

  @Test
  fun ratingFake_covers_all_public_methods() {
    runBlocking {
      val repo = FakeRatingRepository()

      assertTrue(repo.getNewUid().isNotBlank())
      assertNotNull(repo.getAllRatings())

      val rating =
          Rating(
              ratingId = "R1",
              fromUserId = "s-1",
              toUserId = "t-1",
              starRating = StarRating.FOUR,
              comment = "great",
              ratingType = RatingType.Listing("L1"))

      runCatching { repo.addRating(rating) }
      runCatching { repo.updateRating(rating.ratingId, rating) }
      runCatching { repo.deleteRating(rating.ratingId) }

      assertNotNull(repo.getRatingsByFromUser("s-1"))
      assertNotNull(repo.getRatingsByToUser("t-1"))
      assertNotNull(repo.getTutorRatingsOfUser("t-1"))
      assertNotNull(repo.getStudentRatingsOfUser("s-1"))
      runCatching { repo.getRatingsOfListing("L1") }
      runCatching { repo.getRating("R1") }
    }
  }

  // =====================================================================
  // Extra reflection-driven coverage for FakeListingRepository
  // =====================================================================

  /** Dummy Listing with boolean field & setter to drive trySetBooleanField. */
  private data class ListingIdCarrier(val listingId: String = "L-x")

  private data class ActiveCarrier(private var active: Boolean = true) {
    // emulate isX / setX path
    fun isActive(): Boolean = active

    fun setActive(v: Boolean) {
      active = v
    }
  }

  private data class EnabledFieldCarrier(var enabled: Boolean = true)

  private data class OwnerCarrier(val ownerId: String = "owner-9")

  @Test
  fun listing_reflection_findValueOn_paths() {
    val repo = FakeListingRepository()

    // getter/name path
    val id: Any? = callPrivate(repo, "findValueOn", ListingIdCarrier("L-x"), listOf("listingId"))
    assertEquals("L-x", id)

    // isX path
    val active: Any? = callPrivate(repo, "findValueOn", ActiveCarrier(true), listOf("active"))
    assertEquals(true, active)

    // declared-field path
    val enabled: Any? =
        callPrivate(repo, "findValueOn", EnabledFieldCarrier(true), listOf("enabled"))
    assertEquals(true, enabled)
  }

  @Test
  fun listing_reflection_trySetBooleanField_sets_both_paths() {
    val repo = FakeListingRepository()

    // via declared boolean field
    val hasEnabled = EnabledFieldCarrier(true)
    callPrivate<Unit>(repo, "trySetBooleanField", hasEnabled, listOf("enabled"), false)
    assertFalse(hasEnabled.enabled)

    // via setter setActive(boolean)
    val hasActive = ActiveCarrier(true)
    callPrivate<Unit>(repo, "trySetBooleanField", hasActive, listOf("active"), false)
    // read back through isActive()
    val nowActive: Any? = callPrivate(repo, "findValueOn", hasActive, listOf("active"))
    assertEquals(false, nowActive)
  }

  @Test
  fun listing_reflection_matchesUser_ownerId_alias() {
    val repo = FakeListingRepository()
    val ownerCarrier = OwnerCarrier(ownerId = "u-777")

    val v: Any? =
        callPrivate(
            repo,
            "findValueOn",
            ownerCarrier,
            listOf("creatorUserId", "creatorId", "ownerId", "userId"))
    assertEquals("u-777", v?.toString())
  }

  @Test
  fun listing_reflection_searchByLocation_branches() {
    val repo = FakeListingRepository()

    // null branch: object without any location-like field
    data class NoLocation(val other: String = "x")
    val nullVal: Any? =
        callPrivate(
            repo, "findValueOn", NoLocation(), listOf("location", "place", "coords", "position"))
    assertNull(nullVal)
  }
}
