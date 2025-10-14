package com.android.sample.screen

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.MyBookingsViewModel
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MyBookingsViewModelLogicTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun booking(
      id: String = "b1",
      creatorId: String = "t1",
      bookerId: String = "s1",
      listingId: String = "L1",
      start: Date = Date(),
      end: Date = Date(start.time + 90 * 60 * 1000), // 1h30
      price: Double = 30.0
  ) =
      Booking(
          bookingId = id,
          associatedListingId = listingId,
          listingCreatorId = creatorId,
          bookerId = bookerId,
          sessionStart = start,
          sessionEnd = end,
          status = BookingStatus.CONFIRMED,
          price = price)

  /** Simple in-memory fakes */
  private class FakeBookingRepo(private val list: List<Booking>) : BookingRepository {
    override fun getNewUid() = "X"

    override suspend fun getAllBookings() = list

    override suspend fun getBooking(bookingId: String) = list.first { it.bookingId == bookingId }

    override suspend fun getBookingsByTutor(tutorId: String) =
        list.filter { it.listingCreatorId == tutorId }

    override suspend fun getBookingsByUserId(userId: String) = list.filter { it.bookerId == userId }

    override suspend fun getBookingsByStudent(studentId: String) =
        list.filter { it.bookerId == studentId }

    override suspend fun getBookingsByListing(listingId: String) =
        list.filter { it.associatedListingId == listingId }

    override suspend fun addBooking(booking: Booking) {}

    override suspend fun updateBooking(bookingId: String, booking: Booking) {}

    override suspend fun deleteBooking(bookingId: String) {}

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

    override suspend fun confirmBooking(bookingId: String) {}

    override suspend fun completeBooking(bookingId: String) {}

    override suspend fun cancelBooking(bookingId: String) {}
  }

  private class FakeRatingRepo(
      private val map: Map<String, List<Rating>> // key: listingId -> ratings
  ) : RatingRepository {
    override fun getNewUid() = "R"

    override suspend fun getAllRatings(): List<Rating> = map.values.flatten()

    override suspend fun getRating(ratingId: String) = error("not used in these tests")

    override suspend fun getRatingsByFromUser(fromUserId: String) = emptyList<Rating>()

    override suspend fun getRatingsByToUser(toUserId: String) = emptyList<Rating>()

    override suspend fun getRatingsOfListing(listingId: String): List<Rating> =
        map[listingId] ?: emptyList()

    override suspend fun addRating(rating: Rating) {}

    override suspend fun updateRating(ratingId: String, rating: Rating) {}

    override suspend fun deleteRating(ratingId: String) {}

    override suspend fun getTutorRatingsOfUser(userId: String) = emptyList<Rating>()

    override suspend fun getStudentRatingsOfUser(userId: String) = emptyList<Rating>()
  }

  private class FakeProfileRepo(private val map: Map<String, Profile>) : ProfileRepository {
    override fun getNewUid() = "P"

    override suspend fun getProfile(userId: String) = map.getValue(userId)

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles() = map.values.toList()

    override suspend fun searchProfilesByLocation(
        location: com.android.sample.model.map.Location,
        radiusKm: Double
    ) = emptyList<Profile>()
  }

  private class FakeListingRepo(private val map: Map<String, Listing>) : ListingRepository {
    override fun getNewUid() = "L"

    override suspend fun getAllListings() = map.values.toList()

    override suspend fun getProposals() = map.values.filterIsInstance<Proposal>()

    override suspend fun getRequests() = map.values.filterIsInstance<Request>()

    override suspend fun getListing(listingId: String) = map.getValue(listingId)

    override suspend fun getListingsByUser(userId: String) =
        map.values.filter { it.creatorUserId == userId }

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = map.values.filter { it.skill == skill }

    override suspend fun searchByLocation(
        location: com.android.sample.model.map.Location,
        radiusKm: Double
    ) = emptyList<Listing>()
  }

  @Test
  fun load_success_populates_cards_and_formats_labels() = runTest {
    val start = Date(0L) // 01/01/1970 00:00 UTC
    val end = Date(0L + 90 * 60 * 1000) // +1h30

    val listing = Proposal("L1", "t1", description = "", location = Location(), hourlyRate = 30.0)
    val prof = Profile("t1", "Alice Martin", "a@a.com")
    val rating = Rating("r1", "s1", "t1", StarRating.FOUR, "", RatingType.Listing("L1"))

    val vm =
        MyBookingsViewModel(
            bookingRepo = FakeBookingRepo(listOf(booking(start = start, end = end))),
            userId = "s1",
            listingRepo = FakeListingRepo(mapOf("L1" to listing)),
            profileRepo = FakeProfileRepo(mapOf("t1" to prof)),
            ratingRepo = FakeRatingRepo(mapOf("L1" to listOf(rating))),
            locale = Locale.UK,
            demo = false)

    this.testScheduler.advanceUntilIdle()

    val c = vm.uiState.value.single()
    assertEquals("01/01/1970", c.dateLabel) // now deterministic
    assertEquals("1h 30m", c.durationLabel)
  }

  @Test
  fun when_rating_absent_stars_and_count_are_zero_and_pluralization_for_exact_hours() = runTest {
    val twoHours =
        booking(
            id = "b2", start = Date(0L), end = Date(0L + 2 * 60 * 60 * 1000) // 2 hours exact
            )

    val vm =
        MyBookingsViewModel(
            bookingRepo = FakeBookingRepo(listOf(twoHours)),
            userId = "s1",
            listingRepo =
                FakeListingRepo(
                    mapOf(
                        "L1" to
                            Proposal(
                                "L1",
                                "t1",
                                description = "",
                                location = Location(),
                                hourlyRate = 10.0))),
            profileRepo = FakeProfileRepo(mapOf("t1" to Profile("t1", "T", "t@t.com"))),
            ratingRepo = FakeRatingRepo(mapOf("L1" to emptyList())), // no rating
            locale = Locale.US,
            demo = false)

    this.testScheduler.advanceUntilIdle()
    val c = vm.uiState.value.single()
    assertEquals(0, c.ratingStars)
    assertEquals(0, c.ratingCount)
    assertEquals("2hrs", c.durationLabel) // pluralization branch
  }

  @Test
  fun listing_fetch_failure_skips_booking() = runTest {
    val failingListingRepo =
        object : ListingRepository {
          override fun getNewUid() = "L"

          override suspend fun getAllListings() = emptyList<Listing>()

          override suspend fun getProposals() = emptyList<Proposal>()

          override suspend fun getRequests() = emptyList<Request>()

          override suspend fun getListing(listingId: String) = throw RuntimeException("no listing")

          override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

          override suspend fun addProposal(proposal: Proposal) {}

          override suspend fun addRequest(request: Request) {}

          override suspend fun updateListing(listingId: String, listing: Listing) {}

          override suspend fun deleteListing(listingId: String) {}

          override suspend fun deactivateListing(listingId: String) {}

          override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

          override suspend fun searchByLocation(
              location: com.android.sample.model.map.Location,
              radiusKm: Double
          ) = emptyList<Listing>()
        }

    val vm =
        MyBookingsViewModel(
            bookingRepo = FakeBookingRepo(listOf(booking())),
            userId = "s1",
            listingRepo = failingListingRepo,
            profileRepo = FakeProfileRepo(emptyMap()),
            ratingRepo = FakeRatingRepo(emptyMap()),
            demo = false)

    this.testScheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value.isEmpty()) // buildCardSafely returned null → skipped
  }

  @Test
  fun profile_fetch_failure_skips_booking() = runTest {
    val listing = Proposal("L1", "t1", description = "", location = Location(), hourlyRate = 10.0)
    val failingProfiles =
        object : ProfileRepository {
          override fun getNewUid() = "P"

          override suspend fun getProfile(userId: String) = throw RuntimeException("no profile")

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles() = emptyList<Profile>()

          override suspend fun searchProfilesByLocation(
              location: com.android.sample.model.map.Location,
              radiusKm: Double
          ) = emptyList<Profile>()
        }

    val vm =
        MyBookingsViewModel(
            bookingRepo = FakeBookingRepo(listOf(booking())),
            userId = "s1",
            listingRepo = FakeListingRepo(mapOf("L1" to listing)),
            profileRepo = failingProfiles,
            ratingRepo = FakeRatingRepo(emptyMap()),
            demo = false)

    this.testScheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value.isEmpty())
  }

  @Test
  fun load_empty_results_in_empty_list() = runTest {
    val vm =
        MyBookingsViewModel(
            bookingRepo = FakeBookingRepo(emptyList()),
            userId = "s1",
            listingRepo = FakeListingRepo(emptyMap()),
            profileRepo = FakeProfileRepo(emptyMap()),
            ratingRepo = FakeRatingRepo(emptyMap()),
            demo = false)
    this.testScheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value.isEmpty())
  }

  @Test
  fun load_demo_populates_demo_cards() = runTest {
    val vm =
        MyBookingsViewModel(
            bookingRepo = FakeBookingRepo(emptyList()),
            userId = "s1",
            listingRepo = FakeListingRepo(emptyMap()),
            profileRepo = FakeProfileRepo(emptyMap()),
            ratingRepo = FakeRatingRepo(emptyMap()),
            demo = true)
    this.testScheduler.advanceUntilIdle()
    val cards = vm.uiState.value
    assertEquals(2, cards.size)
    assertEquals("Alice Martin", cards[0].tutorName)
    assertEquals("Lucas Dupont", cards[1].tutorName)
  }
}
