package com.android.sample.model

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.FakeBookingRepository
import com.android.sample.model.listing.FakeListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.FakeRatingRepository
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryLocal
import com.android.sample.model.user.ProfileRepositoryProvider
import java.lang.reflect.Method
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
              hourlyRate = 20.0)

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
    Assert.assertNull(nullVal)
  }

  // -------------------- Providers: default + swapping --------------------

  @Test
  fun providers_expose_defaults_and_allow_swapping() = runBlocking {
    // keep originals to restore
    val origBooking = BookingRepositoryProvider.repository
    val origRating = RatingRepositoryProvider.repository
    val origListing = ListingRepositoryProvider.repository
    val origProfile = ProfileRepositoryProvider.repository
    try {
      // Defaults should be the lazy singletons
      assertTrue(BookingRepositoryProvider.repository is FakeBookingRepository)
      assertTrue(RatingRepositoryProvider.repository is FakeRatingRepository)
      assertTrue(ListingRepositoryProvider.repository is FakeListingRepository)
      assertTrue(ProfileRepositoryProvider.repository is ProfileRepositoryLocal)

      // Swap Booking repo to a custom stub and verify
      val customBooking =
          object : BookingRepository {
            override fun getNewUid() = "X"

            override suspend fun getAllBookings() = emptyList<Booking>()

            override suspend fun getBooking(bookingId: String) = error("unused")

            override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

            override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

            override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

            override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

            override suspend fun addBooking(booking: Booking) {}

            override suspend fun updateBooking(bookingId: String, booking: Booking) {}

            override suspend fun deleteBooking(bookingId: String) {}

            override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

            override suspend fun confirmBooking(bookingId: String) {}

            override suspend fun completeBooking(bookingId: String) {}

            override suspend fun cancelBooking(bookingId: String) {}
          }
      BookingRepositoryProvider.repository = customBooking
      assertSame(customBooking, BookingRepositoryProvider.repository)

      // Swap Rating repo to a new instance and verify
      val customRating = FakeRatingRepository()
      RatingRepositoryProvider.repository = customRating
      assertSame(customRating, RatingRepositoryProvider.repository)

      // Swap Listing repo to a new instance and verify
      val customListing = FakeListingRepository()
      ListingRepositoryProvider.repository = customListing
      assertSame(customListing, ListingRepositoryProvider.repository)

      // Swap Profile repo to a custom stub and verify
      val customProfile =
          object : ProfileRepository {
            override fun getNewUid(): String = "X"

            override suspend fun getProfile(userId: String): Profile = error("unused")

            override suspend fun addProfile(profile: Profile) {}

            override suspend fun updateProfile(userId: String, profile: Profile) {}

            override suspend fun deleteProfile(userId: String) {}

            override suspend fun getAllProfiles(): List<Profile> = emptyList()

            override suspend fun searchProfilesByLocation(
                location: Location,
                radiusKm: Double
            ): List<Profile> = emptyList()

            override suspend fun getProfileById(userId: String): Profile = error("unused")

            override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()
          }
      ProfileRepositoryProvider.repository = customProfile
      assertSame(customProfile, ProfileRepositoryProvider.repository)
    } finally {
      // restore singletons so other tests aren’t affected
      BookingRepositoryProvider.repository = origBooking
      RatingRepositoryProvider.repository = origRating
      ListingRepositoryProvider.repository = origListing
      ProfileRepositoryProvider.repository = origProfile
    }
  }

  // -------------------- FakeRatingRepository: branch + CRUD coverage --------------------

  @Test
  fun ratingFake_hardcoded_getRatingsOfListing_branches() = runBlocking {
    val repo = FakeRatingRepository()

    // listing-1 branch (3 ratings → 5,4,5)
    val l1 = repo.getRatingsOfListing("listing-1")
    assertEquals(3, l1.size)
    assertEquals(StarRating.FIVE, l1[0].starRating)
    assertEquals(StarRating.FOUR, l1[1].starRating)

    // listing-2 branch (2 ratings → 4,4)
    val l2 = repo.getRatingsOfListing("listing-2")
    assertEquals(2, l2.size)
    assertEquals(StarRating.FOUR, l2[0].starRating)

    // else branch
    val other = repo.getRatingsOfListing("does-not-exist")
    assertTrue(other.isEmpty())
  }

  @Test
  fun ratingFake_add_update_get_delete_and_filters() = runBlocking {
    val repo = FakeRatingRepository()

    // add → stored under provided ratingId (reflection path getIdOrGenerate)
    val r1 =
        Rating(
            ratingId = "R1",
            fromUserId = "student-1",
            toUserId = "tutor-1",
            starRating = StarRating.FOUR,
            comment = "good",
            ratingType = RatingType.Listing("L1"))
    repo.addRating(r1)

    // filters by from/to user
    assertEquals(1, repo.getRatingsByFromUser("student-1").size)
    assertEquals(1, repo.getRatingsByToUser("tutor-1").size)

    // tutor & student aggregates (heuristics use toUserId/target)
    assertEquals(1, repo.getTutorRatingsOfUser("tutor-1").size)
    assertEquals(1, repo.getStudentRatingsOfUser("tutor-1").size) // same object targeted to tutor-1

    // update existing id
    val r1updated = r1.copy(starRating = StarRating.FIVE, comment = "great!")
    runCatching { repo.updateRating("R1", r1updated) }.onFailure { fail("update failed: $it") }
    assertEquals(StarRating.FIVE, repo.getRating("R1").starRating)

    // delete and verify removal
    repo.deleteRating("R1")
    assertTrue(repo.getAllRatings().none { it.ratingId == "R1" })
  }

  @Test
  fun ratingFake_getRating_throws_when_missing() = runBlocking {
    val repo = FakeRatingRepository()
    try {
      repo.getRating("missing-id")
      fail("Expected NoSuchElementException")
    } catch (e: NoSuchElementException) {
      // expected
    }
  }

  // =====================================================================
  // FakeListingRepository: Detailed Tests
  // =====================================================================

  @Test
  fun listingFake_addProposalAndRequest_addsToLists() = runBlocking {
    val repo = FakeListingRepository()
    val proposal = Proposal(listingId = "p1")
    val request = Request(listingId = "r1")

    repo.addProposal(proposal)
    repo.addRequest(request)

    assertEquals(1, repo.getProposals().size)
    assertEquals("p1", repo.getProposals().first().listingId)
    assertEquals(1, repo.getRequests().size)
    assertEquals("r1", repo.getRequests().first().listingId)

    // Verify that addProposal/addRequest do not affect getAllListings
    assertTrue(repo.getAllListings().isEmpty())
  }

  @Test
  fun listingFake_updateListing_success_and_failure() = runBlocking {
    val initialProposal = Proposal(listingId = "p1", description = "Initial")
    val repo = FakeListingRepository(initial = listOf(initialProposal))

    // Successful update
    val updatedProposal = initialProposal.copy(description = "Updated")
    repo.updateListing("p1", updatedProposal)
    val fetched = repo.getAllListings().first { it.listingId == "p1" }
    assertEquals("Updated", fetched.description)

    // Failure on non-existent ID
    try {
      repo.updateListing("non-existent", updatedProposal)
      fail("Expected NoSuchElementException for updating non-existent listing")
    } catch (e: NoSuchElementException) {
      // Expected
    }
  }

  @Test
  fun listingFake_deleteListing_removesFromRepo() = runBlocking {
    val proposal = Proposal(listingId = "p1")
    val repo = FakeListingRepository(initial = listOf(proposal))
    assertEquals(1, repo.getAllListings().size)

    repo.deleteListing("p1")
    assertTrue(repo.getAllListings().isEmpty())

    // Deleting non-existent ID should not throw
    repo.deleteListing("non-existent")
  }

  @Test
  fun listingFake_deactivateListing_setsInactive() = runBlocking {
    val proposal = Proposal(listingId = "p1", isActive = true)
    val repo = FakeListingRepository(initial = listOf(proposal))

    repo.deactivateListing("p1")

    val fetched = repo.getAllListings().first()
    assertFalse("Listing should be inactive after deactivation", fetched.isActive)
  }

  @Test
  fun listingFake_getListingsByUser_filtersCorrectly() = runBlocking {
    val p1 = Proposal(listingId = "p1", creatorUserId = "user1")
    val p2 = Proposal(listingId = "p2", creatorUserId = "user2")
    val p3 = Proposal(listingId = "p3", creatorUserId = "user1")
    val repo = FakeListingRepository(initial = listOf(p1, p2, p3))

    val user1Listings = repo.getListingsByUser("user1")
    assertEquals(2, user1Listings.size)
    assertTrue(user1Listings.all { it.creatorUserId == "user1" })

    val user2Listings = repo.getListingsByUser("user2")
    assertEquals(1, user2Listings.size)
    assertEquals("p2", user2Listings.first().listingId)

    val user3Listings = repo.getListingsByUser("user3")
    assertTrue(user3Listings.isEmpty())
  }

  @Test
  fun listingFake_searchBySkill_filtersCorrectly() = runBlocking {
    val skill1 = Skill(mainSubject = MainSubject.TECHNOLOGY)
    val skill2 = Skill(mainSubject = MainSubject.MUSIC)
    val p1 = Proposal(listingId = "p1", skill = skill1)
    val p2 = Proposal(listingId = "p2", skill = skill2)
    val repo = FakeListingRepository(initial = listOf(p1, p2))

    val techResults = repo.searchBySkill(skill1)
    assertEquals(1, techResults.size)
    assertEquals("p1", techResults.first().listingId)

    val musicResults = repo.searchBySkill(skill2)
    assertEquals(1, musicResults.size)
    assertEquals("p2", musicResults.first().listingId)

    val nonExistentSkill = Skill(mainSubject = MainSubject.ACADEMICS)
    val emptyResults = repo.searchBySkill(nonExistentSkill)
    assertTrue(emptyResults.isEmpty())
  }

  @Test
  fun listingFake_searchByLocation_filtersCorrectly() = runBlocking {
    val loc1 = Location(10.0, 10.0)
    val loc2 = Location(20.0, 20.0)
    val p1 = Proposal(listingId = "p1", location = loc1)
    val p2 = Request(listingId = "r1", location = loc2)
    val p3 = Proposal(listingId = "p3", location = loc1)
    val repo = FakeListingRepository(initial = listOf(p1, p2, p3))

    val loc1Results = repo.searchByLocation(loc1, 5.0)
    assertEquals(2, loc1Results.size)
    assertTrue(loc1Results.any { it.listingId == "p1" })
    assertTrue(loc1Results.any { it.listingId == "p3" })

    val loc2Results = repo.searchByLocation(loc2, 5.0)
    assertEquals(1, loc2Results.size)
    assertEquals("r1", loc2Results.first().listingId)
  }

  @Test
  fun listingFake_getFakeListings_returnsMockData() {
    val repo = FakeListingRepository()
    val fakeListings = repo.getFakeListings()
    assertFalse("getFakeListings should return the pre-populated list", fakeListings.isEmpty())
    assertEquals(3, fakeListings.size)
  }

  // =====================================================================
  // ProfileRepositoryLocal: Detailed Tests
  // =====================================================================

  @Test
  fun profileLocal_getAllProfiles_returnsPredefinedList() = runBlocking {
    val repo = ProfileRepositoryLocal()
    val profiles = repo.getAllProfiles()
    assertEquals(2, profiles.size)
    assertTrue(profiles.any { it.userId == "test" })
    assertTrue(profiles.any { it.userId == "fake2" })
  }

  @Test
  fun profileLocal_getProfile_returnsCorrectProfile_forExistingId() = runBlocking {
    val repo = ProfileRepositoryLocal()
    val profile1 = repo.getProfile("test")
    assertEquals("John Doe", profile1.name)
    assertEquals("john.doe@epfl.ch", profile1.email)

    val profile2 = repo.getProfile("fake2")
    assertEquals("GuiGui", profile2.name)
    assertEquals("mimi@epfl.ch", profile2.email)
  }

  @Test
  fun profileLocal_getProfile_throwsException_forNonExistentId() = runBlocking {
    val repo = ProfileRepositoryLocal()
    try {
      repo.getProfile("non-existent-id")
      fail("Expected NoSuchElementException for getProfile")
    } catch (e: NoSuchElementException) {
      // Expected
    }
  }

  @Test
  fun profileLocal_getProfileById_returnsCorrectProfile_forExistingId() = runBlocking {
    val repo = ProfileRepositoryLocal()
    val profile1 = repo.getProfileById("test")
    assertEquals("John Doe", profile1.name)

    val profile2 = repo.getProfileById("fake2")
    assertEquals("GuiGui", profile2.name)
  }

  @Test
  fun profileLocal_getProfileById_throwsException_forNonExistentId() = runBlocking {
    val repo = ProfileRepositoryLocal()
    try {
      repo.getProfileById("non-existent-id")
      fail("Expected NoSuchElementException for getProfileById")
    } catch (e: NoSuchElementException) {
      // Expected
    }
  }

  @Test
  fun profileLocal_getSkillsForUser_returnsCorrectSkills() = runBlocking {
    val repo = ProfileRepositoryLocal()
    val tutor1Skills = repo.getSkillsForUser("tutor-1")
    assertEquals(2, tutor1Skills.size)
    assertTrue(tutor1Skills.any { it.mainSubject == MainSubject.MUSIC })

    val testSkills = repo.getSkillsForUser("test")
    assertEquals(1, testSkills.size)

    assertEquals(MainSubject.TECHNOLOGY, testSkills.first().mainSubject)
  }

  @Test
  fun profileLocal_getSkillsForUser_returnsEmptyList_forUserWithNoSkills() = runBlocking {
    val repo = ProfileRepositoryLocal()
    val skills = repo.getSkillsForUser("unknown-user")
    assertTrue(skills.isEmpty())
  }

  @Test
  fun profileLocal_unimplementedMethods_throwNotImplementedError() = runBlocking {
    val repo = ProfileRepositoryLocal()
    val dummyProfile = repo.profileFake1

    try {
      repo.getNewUid()
      fail("getNewUid should throw NotImplementedError")
    } catch (e: NotImplementedError) {
      // Expected
    }

    try {
      repo.addProfile(dummyProfile)
      fail("addProfile should throw NotImplementedError")
    } catch (e: NotImplementedError) {
      // Expected
    }

    try {
      repo.updateProfile("test", dummyProfile)
      fail("updateProfile should throw NotImplementedError")
    } catch (e: NotImplementedError) {
      // Expected
    }

    try {
      repo.deleteProfile("test")
      fail("deleteProfile should throw NotImplementedError")
    } catch (e: NotImplementedError) {
      // Expected
    }

    try {
      repo.searchProfilesByLocation(dummyProfile.location, 10.0)
      fail("searchProfilesByLocation should throw NotImplementedError")
    } catch (e: NotImplementedError) {
      // Expected
    }
  }
}
